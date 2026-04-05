import html
import io
import logging
import os
import time
from collections import Counter
from typing import Iterable

import matplotlib
import requests
from kubernetes import client, config
from kubernetes.client import ApiException

matplotlib.use("Agg")
from matplotlib import pyplot as plt


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
ARGOCD_APP_NAMESPACE = os.getenv("ARGOCD_APP_NAMESPACE", "argocd")
ARGOCD_APP_NAME = os.getenv("ARGOCD_APP_NAME", "syncio-app")
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


def send_photo(chat_id: str, image: io.BytesIO, caption: str) -> None:
    image.seek(0)
    response = requests.post(
        f"{API_BASE}/sendPhoto",
        data={
            "chat_id": chat_id,
            "caption": caption,
            "parse_mode": "HTML",
        },
        files={"photo": ("metrics.png", image.getvalue(), "image/png")},
        timeout=60,
    )
    response.raise_for_status()
    data = response.json()
    if not data.get("ok"):
        raise RuntimeError(f"Telegram API error: {data}")


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


def parse_cpu_millicores(value: str | None) -> int:
    if not value:
        return 0
    if value.endswith("m"):
        return int(value[:-1])
    return int(float(value) * 1000)


def parse_memory_bytes(value: str | None) -> int:
    if not value:
        return 0
    suffixes = {
        "Ki": 1024,
        "Mi": 1024**2,
        "Gi": 1024**3,
        "Ti": 1024**4,
        "K": 1000,
        "M": 1000**2,
        "G": 1000**3,
        "T": 1000**4,
    }
    for suffix, multiplier in suffixes.items():
        if value.endswith(suffix):
            return int(float(value[: -len(suffix)]) * multiplier)
    return int(value)


def format_bytes(num_bytes: int) -> str:
    value = float(num_bytes)
    for unit in ["B", "Ki", "Mi", "Gi", "Ti"]:
        if value < 1024 or unit == "Ti":
            return f"{value:.0f}{unit}" if unit == "B" else f"{value:.1f}{unit}"
        value /= 1024
    return f"{value:.1f}Ti"


def get_custom_objects_api() -> client.CustomObjectsApi:
    return client.CustomObjectsApi()


def collect_top_pod_metrics() -> tuple[list[tuple[str, int, int]] | None, str | None]:
    custom_api = get_custom_objects_api()
    try:
        data = custom_api.list_namespaced_custom_object(
            group="metrics.k8s.io",
            version="v1beta1",
            namespace=TARGET_NAMESPACE,
            plural="pods",
        )
    except ApiException as exc:
        LOGGER.exception("Failed to get pod metrics")
        return None, str(exc)

    items = []
    for item in data.get("items", []):
        total_cpu = 0
        total_memory = 0
        for container in item.get("containers", []):
            usage = container.get("usage", {})
            total_cpu += parse_cpu_millicores(usage.get("cpu"))
            total_memory += parse_memory_bytes(usage.get("memory"))
        items.append((item["metadata"]["name"], total_cpu, total_memory))
    items.sort(key=lambda row: (row[2], row[1]), reverse=True)
    return items, None


def collect_top_node_metrics() -> tuple[list[tuple[str, int, int]] | None, str | None]:
    custom_api = get_custom_objects_api()
    try:
        data = custom_api.list_cluster_custom_object(
            group="metrics.k8s.io",
            version="v1beta1",
            plural="nodes",
        )
    except ApiException as exc:
        LOGGER.exception("Failed to get node metrics")
        return None, str(exc)

    items = []
    for item in data.get("items", []):
        usage = item.get("usage", {})
        items.append(
            (
                item["metadata"]["name"],
                parse_cpu_millicores(usage.get("cpu")),
                parse_memory_bytes(usage.get("memory")),
            )
        )
    items.sort(key=lambda row: (row[2], row[1]), reverse=True)
    return items, None


def collect_storage_metrics(
    core_api: client.CoreV1Api,
) -> tuple[list[tuple[str, str | None, int]] | None, str | None]:
    try:
        pvcs = core_api.list_namespaced_persistent_volume_claim(TARGET_NAMESPACE).items
    except ApiException as exc:
        LOGGER.exception("Failed to list PVCs")
        return None, str(exc)

    items = []
    for pvc in pvcs:
        requested = (pvc.spec.resources.requests or {}).get("storage", "0")
        items.append(
            (
                pvc.metadata.name,
                pvc.spec.storage_class_name,
                parse_memory_bytes(requested) if requested else 0,
            )
        )
    items.sort(key=lambda row: row[2], reverse=True)
    return items, None


def build_bar_chart(
    labels: list[str],
    values: list[float],
    title: str,
    x_label: str,
    color: str,
) -> io.BytesIO:
    figure_height = max(4, len(labels) * 0.45)
    fig, ax = plt.subplots(figsize=(11, figure_height))
    positions = list(range(len(labels)))
    ax.barh(positions, values, color=color)
    ax.set_yticks(positions)
    ax.set_yticklabels(labels, fontsize=9)
    ax.invert_yaxis()
    ax.set_title(title)
    ax.set_xlabel(x_label)
    ax.grid(axis="x", linestyle="--", alpha=0.3)
    fig.tight_layout()

    image = io.BytesIO()
    fig.savefig(image, format="png", dpi=180, bbox_inches="tight")
    plt.close(fig)
    image.seek(0)
    return image


def format_timestamp(value: object) -> str:
    if value is None:
        return "Unknown"
    timestamp = getattr(value, "strftime", None)
    if timestamp is not None:
        return value.strftime("%Y-%m-%d %H:%M:%S UTC")
    return escape(str(value))


def get_events(core_api: client.CoreV1Api) -> str:
    try:
        events = core_api.list_namespaced_event(TARGET_NAMESPACE).items
    except ApiException as exc:
        LOGGER.exception("Failed to list events")
        return "<b>Failed to list events</b>\n" f"<code>{escape(str(exc))}</code>"

    def event_sort_key(event: client.CoreV1Event) -> str:
        return str(
            event.last_timestamp
            or event.event_time
            or event.first_timestamp
            or event.metadata.creation_timestamp
            or ""
        )

    warnings = [event for event in events if (event.type or "") == "Warning"]
    selected = sorted(warnings or events, key=event_sort_key, reverse=True)[:10]

    lines = [f"<b>Recent events in namespace:</b> <code>{TARGET_NAMESPACE}</code>", ""]
    if warnings:
        lines.append("<b>Showing warning events first</b>")
        lines.append("")

    for event in selected:
        involved = event.involved_object
        object_ref = f"{involved.kind}/{involved.name}" if involved else "Unknown"
        lines.append(
            f"<b>{escape(event.type or 'Normal')}</b> <code>{escape(event.reason or 'Unknown')}</code>"
        )
        lines.append(f"<code>{escape(object_ref)}</code>")
        lines.append(escape(event.message or "No message"))
        lines.append(f"<i>{format_timestamp(event.last_timestamp or event.event_time or event.first_timestamp or event.metadata.creation_timestamp)}</i>")
        lines.append("")

    if len(lines) == 2:
        lines.append("No events found.")
    return "\n".join(lines).rstrip()


def get_restarts(core_api: client.CoreV1Api) -> str:
    pods, error = list_pods(core_api)
    if pods is None:
        return "<b>Failed to inspect restarts</b>\n" f"<code>{escape(error)}</code>"

    items = []
    for pod in pods:
        statuses = pod.status.container_statuses or []
        restart_count = sum(status.restart_count or 0 for status in statuses)
        waiting_reason = next(
            (
                status.state.waiting.reason
                for status in statuses
                if status.state and status.state.waiting and status.state.waiting.reason
            ),
            "",
        )
        items.append((pod.metadata.name, restart_count, pod.status.phase or "Unknown", waiting_reason))

    items = [item for item in items if item[1] > 0]
    items.sort(key=lambda row: row[1], reverse=True)

    lines = [f"<b>Pod restarts in namespace:</b> <code>{TARGET_NAMESPACE}</code>", ""]
    if not items:
        lines.append("No pod restarts detected.")
        return "\n".join(lines)

    for name, restart_count, phase, waiting_reason in items[:15]:
        suffix = f", reason <b>{escape(waiting_reason)}</b>" if waiting_reason else ""
        lines.append(
            f"<code>{escape(name)}</code> - restarts <b>{restart_count}</b>, phase <b>{escape(phase)}</b>{suffix}"
        )
    return "\n".join(lines)


def get_app_status() -> str:
    custom_api = get_custom_objects_api()
    try:
        app = custom_api.get_namespaced_custom_object(
            group="argoproj.io",
            version="v1alpha1",
            namespace=ARGOCD_APP_NAMESPACE,
            plural="applications",
            name=ARGOCD_APP_NAME,
        )
    except ApiException as exc:
        LOGGER.exception("Failed to get Argo CD application")
        return "<b>Failed to get Argo CD application</b>\n" f"<code>{escape(str(exc))}</code>"

    spec = app.get("spec", {})
    status = app.get("status", {})
    source = spec.get("source") or (spec.get("sources") or [{}])[0]
    sync = status.get("sync", {})
    health = status.get("health", {})
    operation = status.get("operationState", {})
    summary = status.get("summary", {})
    resources = summary.get("externalURLs", [])

    lines = [
        "<b>Argo CD application</b>",
        "",
        f"<b>Name:</b> <code>{escape(app.get('metadata', {}).get('name'))}</code>",
        f"<b>Namespace:</b> <code>{escape(ARGOCD_APP_NAMESPACE)}</code>",
        f"<b>Project:</b> <code>{escape(spec.get('project'))}</code>",
        f"<b>Sync:</b> <b>{escape(sync.get('status'))}</b>",
        f"<b>Health:</b> <b>{escape(health.get('status'))}</b>",
        f"<b>Revision:</b> <code>{escape(sync.get('revision'))}</code>",
        f"<b>Target revision:</b> <code>{escape(source.get('targetRevision'))}</code>",
        f"<b>Operation phase:</b> <b>{escape(operation.get('phase'))}</b>",
        f"<b>Repo:</b> <code>{escape(source.get('repoURL'))}</code>",
        f"<b>Path:</b> <code>{escape(source.get('path'))}</code>",
        f"<b>Cluster:</b> <code>{escape(spec.get('destination', {}).get('server'))}</code>",
        f"<b>Destination namespace:</b> <code>{escape(spec.get('destination', {}).get('namespace'))}</code>",
    ]

    if resources:
        lines.extend(["", "<b>External URLs:</b>"])
        for url in resources[:5]:
            lines.append(f"<code>{escape(url)}</code>")

    conditions = status.get("conditions") or []
    if conditions:
        lines.extend(["", "<b>Conditions:</b>"])
        for condition in conditions[:5]:
            lines.append(
                f"<code>{escape(condition.get('type'))}</code> - {escape(condition.get('message'))}"
            )

    return "\n".join(lines)


def get_top_pods() -> str:
    items, error = collect_top_pod_metrics()
    if items is None:
        return "<b>Failed to get pod metrics</b>\n" f"<code>{escape(error)}</code>"
    lines = [f"<b>Top pods in namespace:</b> <code>{TARGET_NAMESPACE}</code>", ""]
    for name, cpu_m, mem_b in items[:15]:
        lines.append(
            f"<code>{escape(name)}</code> - CPU <b>{cpu_m}m</b>, RAM <b>{format_bytes(mem_b)}</b>"
        )
    if len(lines) == 2:
        lines.append("No pod metrics found.")
    return "\n".join(lines)


def get_top_nodes() -> str:
    items, error = collect_top_node_metrics()
    if items is None:
        return "<b>Failed to get node metrics</b>\n" f"<code>{escape(error)}</code>"
    lines = ["<b>Top nodes</b>", ""]
    for name, cpu_m, mem_b in items:
        lines.append(
            f"<code>{escape(name)}</code> - CPU <b>{cpu_m}m</b>, RAM <b>{format_bytes(mem_b)}</b>"
        )
    if len(lines) == 2:
        lines.append("No node metrics found.")
    return "\n".join(lines)


def get_storage(core_api: client.CoreV1Api) -> str:
    items, error = collect_storage_metrics(core_api)
    if items is None:
        return "<b>Failed to list PVCs</b>\n" f"<code>{escape(error)}</code>"

    lines = [f"<b>Storage in namespace:</b> <code>{TARGET_NAMESPACE}</code>", ""]
    total_requested = 0
    for name, storage_class, requested_bytes in items:
        total_requested += requested_bytes
        lines.append(
            f"<code>{escape(name)}</code> - class <b>{escape(storage_class)}</b>, requested <b>{format_bytes(requested_bytes)}</b>"
        )
    if items:
        lines.extend(["", f"<b>Total requested storage:</b> <b>{format_bytes(total_requested)}</b>"])
    else:
        lines.append("No persistent volume claims found.")
    return "\n".join(lines)


def send_pod_metrics_chart(chat_id: str) -> None:
    items, error = collect_top_pod_metrics()
    if items is None:
        send_message(chat_id, "<b>Failed to get pod metrics</b>\n" f"<code>{escape(error)}</code>")
        return
    if not items:
        send_message(chat_id, "No pod metrics found.")
        return

    top_items = items[:10]
    labels = [name[:36] for name, _, _ in top_items]
    memory_values = [round(mem_b / (1024**2), 1) for _, _, mem_b in top_items]
    image = build_bar_chart(
        labels,
        memory_values,
        f"Pod RAM Usage - {TARGET_NAMESPACE}",
        "MiB",
        "#2D6A4F",
    )
    send_photo(chat_id, image, f"<b>Pod RAM usage snapshot</b>\n<code>{TARGET_NAMESPACE}</code>")


def send_node_metrics_chart(chat_id: str) -> None:
    items, error = collect_top_node_metrics()
    if items is None:
        send_message(chat_id, "<b>Failed to get node metrics</b>\n" f"<code>{escape(error)}</code>")
        return
    if not items:
        send_message(chat_id, "No node metrics found.")
        return

    labels = [name for name, _, _ in items]
    memory_values = [round(mem_b / (1024**3), 2) for _, _, mem_b in items]
    image = build_bar_chart(labels, memory_values, "Node RAM Usage", "GiB", "#BC6C25")
    send_photo(chat_id, image, "<b>Node RAM usage snapshot</b>")


def send_storage_chart(core_api: client.CoreV1Api, chat_id: str) -> None:
    items, error = collect_storage_metrics(core_api)
    if items is None:
        send_message(chat_id, "<b>Failed to list PVCs</b>\n" f"<code>{escape(error)}</code>")
        return
    if not items:
        send_message(chat_id, "No persistent volume claims found.")
        return

    labels = [name for name, _, _ in items[:10]]
    values = [round(size / (1024**3), 2) for _, _, size in items[:10]]
    image = build_bar_chart(
        labels,
        values,
        f"PVC Requested Storage - {TARGET_NAMESPACE}",
        "GiB",
        "#6C757D",
    )
    send_photo(chat_id, image, f"<b>PVC storage snapshot</b>\n<code>{TARGET_NAMESPACE}</code>")


def get_resource_requests(core_api: client.CoreV1Api) -> str:
    pods, error = list_pods(core_api)
    if pods is None:
        return "<b>Failed to inspect resource requests</b>\n" f"<code>{escape(error)}</code>"

    total_cpu_requests = total_cpu_limits = 0
    total_mem_requests = total_mem_limits = 0

    for pod in pods:
        for container in pod.spec.containers or []:
            requests_map = container.resources.requests or {}
            limits_map = container.resources.limits or {}
            total_cpu_requests += parse_cpu_millicores(requests_map.get("cpu"))
            total_cpu_limits += parse_cpu_millicores(limits_map.get("cpu"))
            total_mem_requests += parse_memory_bytes(requests_map.get("memory"))
            total_mem_limits += parse_memory_bytes(limits_map.get("memory"))

    return (
        f"<b>Resource requests in namespace:</b> <code>{TARGET_NAMESPACE}</code>\n\n"
        f"<b>CPU requests:</b> <b>{total_cpu_requests}m</b>\n"
        f"<b>CPU limits:</b> <b>{total_cpu_limits}m</b>\n"
        f"<b>RAM requests:</b> <b>{format_bytes(total_mem_requests)}</b>\n"
        f"<b>RAM limits:</b> <b>{format_bytes(total_mem_limits)}</b>"
    )


def help_text() -> str:
    return (
        "<b>Syncio bot commands</b>\n\n"
        "<code>/pods</code> - list pods in the syncio namespace\n"
        "<code>/pods pending</code> - list only pending pods\n"
        "<code>/events</code> - show recent namespace events\n"
        "<code>/restarts</code> - show pods with restart counts\n"
        "<code>/app</code> - show Argo CD application status\n"
        "<code>/deployments</code> - list deployments and ready counts\n"
        "<code>/services</code> - list services in the syncio namespace\n"
        "<code>/cluster</code> - show cluster statistics for syncio\n"
        "<code>/top pods</code> - live CPU/RAM usage for pods\n"
        "<code>/top nodes</code> - live CPU/RAM usage for nodes\n"
        "<code>/graph pods</code> - RAM usage chart for pods\n"
        "<code>/graph nodes</code> - RAM usage chart for nodes\n"
        "<code>/graph storage</code> - PVC storage chart\n"
        "<code>/storage</code> - PVC storage requests in syncio\n"
        "<code>/resources</code> - CPU/RAM requests and limits summary\n"
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
    if text == "/events":
        send_message(chat_id, get_events(core_api))
        return
    if text == "/restarts":
        send_message(chat_id, get_restarts(core_api))
        return
    if text == "/app":
        send_message(chat_id, get_app_status())
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
    if text == "/top pods":
        send_message(chat_id, get_top_pods())
        return
    if text == "/top nodes":
        send_message(chat_id, get_top_nodes())
        return
    if text == "/graph pods":
        send_pod_metrics_chart(chat_id)
        return
    if text == "/graph nodes":
        send_node_metrics_chart(chat_id)
        return
    if text == "/graph storage":
        send_storage_chart(core_api, chat_id)
        return
    if text == "/storage":
        send_message(chat_id, get_storage(core_api))
        return
    if text == "/resources":
        send_message(chat_id, get_resource_requests(core_api))
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
