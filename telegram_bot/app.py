import html
import logging
import os
import time
from collections import Counter
from typing import Iterable

import requests
from kubernetes import client, config
from kubernetes.client import ApiException


logging.basicConfig(
    level=os.getenv("LOG_LEVEL", "INFO").upper(),
    format="%(asctime)s %(levelname)s %(message)s",
)
LOGGER = logging.getLogger("telegram-bot")

BOT_TOKEN = os.environ["TELEGRAM_BOT_TOKEN"]
ALLOWED_CHAT_IDS = {
    chat_id.strip()
    for chat_id in os.getenv("ALLOWED_CHAT_IDS", "").split(",")
    if chat_id.strip()
}
TARGET_NAMESPACE = os.getenv("TARGET_NAMESPACE", "syncio")
POLL_TIMEOUT_SECONDS = int(os.getenv("POLL_TIMEOUT_SECONDS", "30"))
API_BASE = f"https://api.telegram.org/bot{BOT_TOKEN}"


def load_kubernetes() -> client.CoreV1Api:
    config.load_incluster_config()
    return client.CoreV1Api()


def telegram_api(method: str, payload: dict | None = None) -> dict:
    response = requests.post(
        f"{API_BASE}/{method}",
        json=payload or {},
        timeout=60,
    )
    response.raise_for_status()
    data = response.json()
    if not data.get("ok"):
        raise RuntimeError(f"Telegram API error: {data}")
    return data


def send_message(chat_id: str, text: str) -> None:
    telegram_api(
        "sendMessage",
        {
            "chat_id": chat_id,
            "text": text,
            "parse_mode": "HTML",
            "disable_web_page_preview": True,
        },
    )


def escape(value: str | None) -> str:
    return html.escape(value or "Unknown")


def format_pods(pods: Iterable[client.V1Pod]) -> str:
    lines = [f"<b>Pods in namespace:</b> <code>{TARGET_NAMESPACE}</code>", ""]
    for pod in pods:
        status = escape(pod.status.phase)
        name = escape(pod.metadata.name)
        lines.append(f"<code>{name}</code> - <b>{status}</b>")
    if len(lines) == 2:
        lines.append("No pods found.")
    return "\n".join(lines)


def list_pods(core_api: client.CoreV1Api) -> tuple[list[client.V1Pod] | None, str | None]:
    try:
        return core_api.list_namespaced_pod(TARGET_NAMESPACE).items, None
    except ApiException as exc:
        LOGGER.exception("Failed to list pods")
        return None, str(exc)


def get_pods(core_api: client.CoreV1Api, pending_only: bool = False) -> str:
    pods, error = list_pods(core_api)
    if pods is None:
        return (
            "<b>Failed to list pods</b>\n"
            f"<code>{escape(error)}</code>"
        )
    if pending_only:
        pending_statuses = {"Pending", "ContainerCreating", "Init:0/1"}
        filtered = [
            pod
            for pod in pods
            if (pod.status.phase or "") == "Pending"
            or any(
                (status.state.waiting and status.state.waiting.reason in pending_statuses)
                for status in (pod.status.container_statuses or [])
            )
        ]
        if not filtered:
            return (
                f"<b>Pending pods in namespace:</b> <code>{TARGET_NAMESPACE}</code>\n\n"
                "No pending pods."
            )
        return format_pods(filtered)
    return format_pods(pods)


def get_deployments() -> str:
    apps_api = client.AppsV1Api()
    try:
        deployments = apps_api.list_namespaced_deployment(TARGET_NAMESPACE).items
    except ApiException as exc:
        LOGGER.exception("Failed to list deployments")
        return "<b>Failed to list deployments</b>\n" f"<code>{escape(str(exc))}</code>"

    lines = [f"<b>Deployments in namespace:</b> <code>{TARGET_NAMESPACE}</code>", ""]
    for deployment in deployments:
        name = escape(deployment.metadata.name)
        ready = deployment.status.ready_replicas or 0
        desired = deployment.spec.replicas or 0
        updated = deployment.status.updated_replicas or 0
        lines.append(
            f"<code>{name}</code> - ready <b>{ready}/{desired}</b>, updated <b>{updated}</b>"
        )
    if len(lines) == 2:
        lines.append("No deployments found.")
    return "\n".join(lines)


def get_services(core_api: client.CoreV1Api) -> str:
    try:
        services = core_api.list_namespaced_service(TARGET_NAMESPACE).items
    except ApiException as exc:
        LOGGER.exception("Failed to list services")
        return "<b>Failed to list services</b>\n" f"<code>{escape(str(exc))}</code>"

    lines = [f"<b>Services in namespace:</b> <code>{TARGET_NAMESPACE}</code>", ""]
    for service in services:
        name = escape(service.metadata.name)
        service_type = escape(service.spec.type)
        ports = ", ".join(str(port.port) for port in service.spec.ports or [])
        lines.append(f"<code>{name}</code> - <b>{service_type}</b> - ports: <code>{ports or '-'}</code>")
    if len(lines) == 2:
        lines.append("No services found.")
    return "\n".join(lines)


def get_cluster_stats(core_api: client.CoreV1Api) -> str:
    try:
        pods = core_api.list_namespaced_pod(TARGET_NAMESPACE).items
        services = core_api.list_namespaced_service(TARGET_NAMESPACE).items
        nodes = core_api.list_node().items
        deployments = client.AppsV1Api().list_namespaced_deployment(TARGET_NAMESPACE).items
    except ApiException as exc:
        LOGGER.exception("Failed to collect cluster stats")
        return "<b>Failed to collect cluster stats</b>\n" f"<code>{escape(str(exc))}</code>"

    pod_statuses = Counter((pod.status.phase or "Unknown") for pod in pods)
    ready_nodes = 0
    for node in nodes:
        conditions = {cond.type: cond.status for cond in node.status.conditions or []}
        if conditions.get("Ready") == "True":
            ready_nodes += 1

    ready_deployments = sum(
        1
        for deployment in deployments
        if (deployment.status.ready_replicas or 0) == (deployment.spec.replicas or 0)
    )

    lines = [
        "<b>Syncio cluster statistics</b>",
        "",
        f"<b>Namespace:</b> <code>{TARGET_NAMESPACE}</code>",
        f"<b>Nodes Ready:</b> <b>{ready_nodes}/{len(nodes)}</b>",
        f"<b>Deployments Ready:</b> <b>{ready_deployments}/{len(deployments)}</b>",
        f"<b>Services:</b> <b>{len(services)}</b>",
        f"<b>Pods:</b> <b>{len(pods)}</b>",
        "",
        "<b>Pod phase summary:</b>",
    ]
    for status, count in sorted(pod_statuses.items()):
        lines.append(f"• <b>{escape(status)}</b>: {count}")
    return "\n".join(lines)


def help_text() -> str:
    return (
        "<b>Syncio bot commands</b>\n\n"
        "<code>/pods</code> - list pods in the syncio namespace\n"
        "<code>/pods pending</code> - list only pending pods\n"
        "<code>/deployments</code> - list deployments and ready counts\n"
        "<code>/services</code> - list services in the syncio namespace\n"
        "<code>/cluster</code> - show cluster statistics for syncio\n"
        "<code>/help</code> - show this help"
    )


def handle_message(core_api: client.CoreV1Api, message: dict) -> None:
    chat_id = str(message["chat"]["id"])
    if ALLOWED_CHAT_IDS and chat_id not in ALLOWED_CHAT_IDS:
        LOGGER.warning("Ignoring unauthorized chat_id=%s", chat_id)
        return

    text = (message.get("text") or "").strip()
    if not text:
        return

    LOGGER.info("Received command '%s' from chat_id=%s", text, chat_id)

    if text == "/pods":
        send_message(chat_id, get_pods(core_api))
        return
    if text == "/pods pending":
        send_message(chat_id, get_pods(core_api, pending_only=True))
        return
    if text == "/deployments":
        send_message(chat_id, get_deployments())
        return
    if text == "/services":
        send_message(chat_id, get_services(core_api))
        return
    if text == "/cluster":
        send_message(chat_id, get_cluster_stats(core_api))
        return
    if text == "/help" or text == "/start":
        send_message(chat_id, help_text())
        return

    send_message(
        chat_id,
        "Unknown command.\nUse <code>/help</code> to see supported commands.",
    )


def main() -> None:
    core_api = load_kubernetes()
    offset = None

    while True:
        try:
            payload = {
                "timeout": POLL_TIMEOUT_SECONDS,
                "allowed_updates": ["message"],
            }
            if offset is not None:
                payload["offset"] = offset

            updates = telegram_api("getUpdates", payload)["result"]
            for update in updates:
                offset = update["update_id"] + 1
                message = update.get("message")
                if message:
                    handle_message(core_api, message)
        except requests.RequestException:
            LOGGER.exception("Telegram polling failed")
            time.sleep(5)
        except Exception:
            LOGGER.exception("Unexpected bot error")
            time.sleep(5)


if __name__ == "__main__":
    main()
