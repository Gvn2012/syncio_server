import html
import io
import logging
import os
import time
from collections import Counter
from datetime import datetime, timezone
from typing import Iterable

import matplotlib
import pandas as pd
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
SCALABLE_DEPLOYMENTS = {
    name.strip()
    for name in os.getenv("SCALABLE_DEPLOYMENTS", "").split(",")
    if name.strip()
}
GITHUB_REPO = os.getenv("GITHUB_REPO", "Gvn2012/syncio_server")
GITHUB_BRANCH = os.getenv("GITHUB_BRANCH", "main")
GITHUB_TOKEN = os.getenv("GITHUB_TOKEN", "")
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


def truncate_text(value: str, limit: int = 3500) -> str:
    if len(value) <= limit:
        return value
    return f"{value[:limit]}\n...(truncated)"


def github_api(path: str, params: dict | None = None) -> dict:
    headers = {"Accept": "application/vnd.github+json"}
    if GITHUB_TOKEN:
        headers["Authorization"] = f"Bearer {GITHUB_TOKEN}"
    response = requests.get(
        f"https://api.github.com{path}",
        headers=headers,
        params=params or {},
        timeout=60,
    )
    response.raise_for_status()
    return response.json()


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


def get_apps_api() -> client.AppsV1Api:
    return client.AppsV1Api()


def current_bot_image(core_api: client.CoreV1Api) -> tuple[str | None, str | None]:
    pod_name = os.getenv("HOSTNAME")
    if not pod_name:
        return None, "HOSTNAME is not set"
    try:
        pod = core_api.read_namespaced_pod(pod_name, TARGET_NAMESPACE)
    except ApiException as exc:
        LOGGER.exception("Failed to get bot pod")
        return None, str(exc)

    for container_def in pod.spec.containers or []:
        if container_def.name == "telegram-bot":
            return container_def.image, None
    return None, "telegram-bot container image not found"


def get_version(core_api: client.CoreV1Api) -> str:
    image, error = current_bot_image(core_api)
    if error:
        return "<b>Bot version unavailable</b>\n" f"<code>{escape(error)}</code>"

    image_tag = image.split(":")[-1] if ":" in image else image
    return (
        "<b>Telegram bot version</b>\n\n"
        f"<b>Image:</b> <code>{escape(image)}</code>\n"
        f"<b>Version:</b> <code>{escape(image_tag)}</code>\n"
        f"<b>Namespace:</b> <code>{TARGET_NAMESPACE}</code>\n"
        f"<b>Repo:</b> <code>{escape(GITHUB_REPO)}</code>\n"
        f"<b>Branch:</b> <code>{escape(GITHUB_BRANCH)}</code>"
    )


def shorten_label(value: str, length: int = 32) -> str:
    if len(value) <= length:
        return value
    return f"{value[: length - 3]}..."


def make_dataframe(items: list[dict], columns: list[str]) -> pd.DataFrame:
    frame = pd.DataFrame(items)
    if frame.empty:
        return pd.DataFrame(columns=columns)
    return frame


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


def build_dataframe_bar_chart(
    frame: pd.DataFrame,
    label_column: str,
    value_column: str,
    title: str,
    x_label: str,
    color: str,
    limit: int = 10,
) -> io.BytesIO:
    plot_frame = frame.sort_values(by=value_column, ascending=False).head(limit).copy()
    plot_frame[label_column] = plot_frame[label_column].apply(shorten_label)
    return build_bar_chart(
        plot_frame[label_column].tolist(),
        plot_frame[value_column].tolist(),
        title,
        x_label,
        color,
    )


def build_grouped_bar_chart(
    frame: pd.DataFrame,
    label_column: str,
    first_column: str,
    second_column: str,
    title: str,
    y_label: str,
    first_label: str,
    second_label: str,
    first_color: str,
    second_color: str,
    limit: int = 10,
) -> io.BytesIO:
    plot_frame = frame.sort_values(by=second_column, ascending=False).head(limit).copy()
    plot_frame[label_column] = plot_frame[label_column].apply(shorten_label)

    fig, ax = plt.subplots(figsize=(12, max(4, len(plot_frame) * 0.6)))
    positions = range(len(plot_frame))
    width = 0.4
    ax.bar(
        [position - width / 2 for position in positions],
        plot_frame[first_column],
        width=width,
        color=first_color,
        label=first_label,
    )
    ax.bar(
        [position + width / 2 for position in positions],
        plot_frame[second_column],
        width=width,
        color=second_color,
        label=second_label,
    )
    ax.set_xticks(list(positions))
    ax.set_xticklabels(plot_frame[label_column], rotation=20, ha="right")
    ax.set_title(title)
    ax.set_ylabel(y_label)
    ax.legend()
    ax.grid(axis="y", linestyle="--", alpha=0.3)
    fig.tight_layout()

    image = io.BytesIO()
    fig.savefig(image, format="png", dpi=180, bbox_inches="tight")
    plt.close(fig)
    image.seek(0)
    return image


def build_pie_chart(
    frame: pd.DataFrame,
    label_column: str,
    value_column: str,
    title: str,
    colors: list[str] | None = None,
) -> io.BytesIO:
    fig, ax = plt.subplots(figsize=(8, 8))
    ax.pie(
        frame[value_column],
        labels=frame[label_column],
        autopct="%1.0f%%",
        startangle=90,
        colors=colors,
    )
    ax.set_title(title)
    fig.tight_layout()
    image = io.BytesIO()
    fig.savefig(image, format="png", dpi=180, bbox_inches="tight")
    plt.close(fig)
    image.seek(0)
    return image


def pod_metrics_frame(items: list[tuple[str, int, int]]) -> pd.DataFrame:
    rows = [
        {
            "name": name,
            "cpu_m": cpu_m,
            "memory_mib": round(mem_b / (1024**2), 2),
        }
        for name, cpu_m, mem_b in items
    ]
    return make_dataframe(rows, ["name", "cpu_m", "memory_mib"])


def node_metrics_frame(items: list[tuple[str, int, int]]) -> pd.DataFrame:
    rows = [
        {
            "name": name,
            "cpu_m": cpu_m,
            "memory_gib": round(mem_b / (1024**3), 2),
        }
        for name, cpu_m, mem_b in items
    ]
    return make_dataframe(rows, ["name", "cpu_m", "memory_gib"])


def storage_metrics_frame(items: list[tuple[str, str | None, int]]) -> pd.DataFrame:
    rows = [
        {
            "name": name,
            "storage_class": storage_class or "Unknown",
            "requested_gib": round(requested_bytes / (1024**3), 2),
        }
        for name, storage_class, requested_bytes in items
    ]
    return make_dataframe(rows, ["name", "storage_class", "requested_gib"])


def restart_metrics_frame(core_api: client.CoreV1Api) -> tuple[pd.DataFrame | None, str | None]:
    pods, error = list_pods(core_api)
    if pods is None:
        return None, error

    rows = []
    for pod in pods:
        statuses = pod.status.container_statuses or []
        rows.append(
            {
                "name": pod.metadata.name,
                "restarts": sum(status.restart_count or 0 for status in statuses),
            }
        )
    frame = make_dataframe(rows, ["name", "restarts"])
    if not frame.empty:
        frame = frame[frame["restarts"] > 0]
    return frame, None


def pod_phase_frame(core_api: client.CoreV1Api) -> tuple[pd.DataFrame | None, str | None]:
    pods, error = list_pods(core_api)
    if pods is None:
        return None, error

    counts = Counter((pod.status.phase or "Unknown") for pod in pods)
    rows = [{"phase": phase, "count": count} for phase, count in sorted(counts.items())]
    return make_dataframe(rows, ["phase", "count"]), None


def deployment_frame() -> tuple[pd.DataFrame | None, str | None]:
    try:
        deployments = get_apps_api().list_namespaced_deployment(TARGET_NAMESPACE).items
    except ApiException as exc:
        LOGGER.exception("Failed to list deployments")
        return None, str(exc)

    rows = []
    for deployment in deployments:
        desired = deployment.spec.replicas or 0
        ready = deployment.status.ready_replicas or 0
        rows.append(
            {
                "name": deployment.metadata.name,
                "desired": desired,
                "ready": ready,
                "availability_pct": round((ready / desired) * 100, 1) if desired else 0,
            }
        )
    return make_dataframe(rows, ["name", "desired", "ready", "availability_pct"]), None


def resource_frame(core_api: client.CoreV1Api) -> tuple[pd.DataFrame | None, str | None]:
    pods, error = list_pods(core_api)
    if pods is None:
        return None, error

    grouped: dict[str, dict[str, float]] = {}
    for pod in pods:
        owner_name = pod.metadata.name.rsplit("-", 2)[0] if pod.metadata.name else "unknown"
        bucket = grouped.setdefault(
            owner_name,
            {
                "name": owner_name,
                "cpu_requests_m": 0,
                "cpu_limits_m": 0,
                "memory_requests_mib": 0,
                "memory_limits_mib": 0,
            },
        )
        for container_def in pod.spec.containers or []:
            requests_map = container_def.resources.requests or {}
            limits_map = container_def.resources.limits or {}
            bucket["cpu_requests_m"] += parse_cpu_millicores(requests_map.get("cpu"))
            bucket["cpu_limits_m"] += parse_cpu_millicores(limits_map.get("cpu"))
            bucket["memory_requests_mib"] += round(
                parse_memory_bytes(requests_map.get("memory")) / (1024**2), 2
            )
            bucket["memory_limits_mib"] += round(
                parse_memory_bytes(limits_map.get("memory")) / (1024**2), 2
            )
    return make_dataframe(
        list(grouped.values()),
        [
            "name",
            "cpu_requests_m",
            "cpu_limits_m",
            "memory_requests_mib",
            "memory_limits_mib",
        ],
    ), None


def send_frame_chart(
    chat_id: str,
    frame: pd.DataFrame,
    label_column: str,
    value_column: str,
    title: str,
    x_label: str,
    color: str,
    caption: str,
    limit: int = 10,
) -> None:
    if frame.empty:
        send_message(chat_id, "No data available for this chart.")
        return
    image = build_dataframe_bar_chart(frame, label_column, value_column, title, x_label, color, limit)
    send_photo(chat_id, image, caption)


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


def describe_pod(core_api: client.CoreV1Api, pod_name: str) -> str:
    try:
        pod = core_api.read_namespaced_pod(pod_name, TARGET_NAMESPACE)
    except ApiException as exc:
        LOGGER.exception("Failed to describe pod")
        return "<b>Failed to describe pod</b>\n" f"<code>{escape(str(exc))}</code>"

    statuses = pod.status.container_statuses or []
    restarts = sum(status.restart_count or 0 for status in statuses)
    images = ", ".join(container_def.image for container_def in pod.spec.containers or [])
    lines = [
        "<b>Pod details</b>",
        "",
        f"<b>Name:</b> <code>{escape(pod.metadata.name)}</code>",
        f"<b>Phase:</b> <b>{escape(pod.status.phase)}</b>",
        f"<b>Node:</b> <code>{escape(pod.spec.node_name)}</code>",
        f"<b>Pod IP:</b> <code>{escape(pod.status.pod_ip)}</code>",
        f"<b>Restarts:</b> <b>{restarts}</b>",
        f"<b>Service account:</b> <code>{escape(pod.spec.service_account_name)}</code>",
        f"<b>Images:</b> <code>{escape(images)}</code>",
    ]
    waiting_reasons = [
        status.state.waiting.reason
        for status in statuses
        if status.state and status.state.waiting and status.state.waiting.reason
    ]
    if waiting_reasons:
        lines.append(f"<b>Waiting reasons:</b> <code>{escape(', '.join(waiting_reasons))}</code>")
    return "\n".join(lines)


def describe_deployment(deployment_name: str) -> str:
    try:
        deployment = get_apps_api().read_namespaced_deployment(deployment_name, TARGET_NAMESPACE)
    except ApiException as exc:
        LOGGER.exception("Failed to describe deployment")
        return "<b>Failed to describe deployment</b>\n" f"<code>{escape(str(exc))}</code>"

    images = ", ".join(container_def.image for container_def in deployment.spec.template.spec.containers or [])
    conditions = deployment.status.conditions or []
    lines = [
        "<b>Deployment details</b>",
        "",
        f"<b>Name:</b> <code>{escape(deployment.metadata.name)}</code>",
        f"<b>Replicas:</b> <b>{deployment.status.ready_replicas or 0}/{deployment.spec.replicas or 0}</b>",
        f"<b>Updated:</b> <b>{deployment.status.updated_replicas or 0}</b>",
        f"<b>Available:</b> <b>{deployment.status.available_replicas or 0}</b>",
        f"<b>Strategy:</b> <code>{escape(deployment.spec.strategy.type)}</code>",
        f"<b>Images:</b> <code>{escape(images)}</code>",
    ]
    for condition in conditions[:3]:
        lines.append(
            f"<b>{escape(condition.type)}:</b> <code>{escape(condition.reason)}</code> - {escape(condition.message)}"
        )
    return "\n".join(lines)


def describe_service(core_api: client.CoreV1Api, service_name: str) -> str:
    try:
        service = core_api.read_namespaced_service(service_name, TARGET_NAMESPACE)
    except ApiException as exc:
        LOGGER.exception("Failed to describe service")
        return "<b>Failed to describe service</b>\n" f"<code>{escape(str(exc))}</code>"

    selector = ", ".join(f"{key}={value}" for key, value in (service.spec.selector or {}).items()) or "-"
    ports = ", ".join(
        f"{port.port}->{port.target_port}/{port.protocol}"
        for port in service.spec.ports or []
    ) or "-"
    return (
        "<b>Service details</b>\n\n"
        f"<b>Name:</b> <code>{escape(service.metadata.name)}</code>\n"
        f"<b>Type:</b> <b>{escape(service.spec.type)}</b>\n"
        f"<b>Cluster IP:</b> <code>{escape(service.spec.cluster_ip)}</code>\n"
        f"<b>Selector:</b> <code>{escape(selector)}</code>\n"
        f"<b>Ports:</b> <code>{escape(ports)}</code>"
    )


def get_pod_logs(core_api: client.CoreV1Api, pod_name: str) -> str:
    try:
        logs = core_api.read_namespaced_pod_log(
            pod_name,
            TARGET_NAMESPACE,
            tail_lines=120,
            timestamps=True,
        )
    except ApiException as exc:
        LOGGER.exception("Failed to get pod logs")
        return "<b>Failed to get pod logs</b>\n" f"<code>{escape(str(exc))}</code>"

    return (
        f"<b>Logs for pod</b> <code>{escape(pod_name)}</code>\n\n"
        f"<pre>{escape(truncate_text(logs or 'No logs found.'))}</pre>"
    )


def rollout_status(deployment_name: str) -> str:
    try:
        deployment = get_apps_api().read_namespaced_deployment(deployment_name, TARGET_NAMESPACE)
    except ApiException as exc:
        LOGGER.exception("Failed to get rollout status")
        return "<b>Failed to get rollout status</b>\n" f"<code>{escape(str(exc))}</code>"

    observed = deployment.status.observed_generation or 0
    generation = deployment.metadata.generation or 0
    progressing = "Yes" if observed >= generation else "No"
    return (
        "<b>Rollout status</b>\n\n"
        f"<b>Deployment:</b> <code>{escape(deployment.metadata.name)}</code>\n"
        f"<b>Ready:</b> <b>{deployment.status.ready_replicas or 0}/{deployment.spec.replicas or 0}</b>\n"
        f"<b>Updated:</b> <b>{deployment.status.updated_replicas or 0}</b>\n"
        f"<b>Available:</b> <b>{deployment.status.available_replicas or 0}</b>\n"
        f"<b>Observed generation:</b> <code>{observed}</code>\n"
        f"<b>Target generation:</b> <code>{generation}</code>\n"
        f"<b>Controller caught up:</b> <b>{progressing}</b>"
    )


def get_deployment_images(deployment_name: str) -> str:
    try:
        deployment = get_apps_api().read_namespaced_deployment(deployment_name, TARGET_NAMESPACE)
    except ApiException as exc:
        LOGGER.exception("Failed to get deployment images")
        return "<b>Failed to get deployment images</b>\n" f"<code>{escape(str(exc))}</code>"

    lines = [f"<b>Images for deployment</b> <code>{escape(deployment_name)}</code>", ""]
    for container_def in deployment.spec.template.spec.containers or []:
        lines.append(f"<code>{escape(container_def.name)}</code>: <code>{escape(container_def.image)}</code>")
    return "\n".join(lines)


def restart_deployment(chat_id: str, deployment_name: str) -> None:
    if not deployment_name:
        send_message(chat_id, "Usage: <code>/restart &lt;deployment&gt;</code>")
        return
    if SCALABLE_DEPLOYMENTS and deployment_name not in SCALABLE_DEPLOYMENTS:
        send_message(chat_id, f"<b>Restart not allowed</b>\n<code>{escape(deployment_name)}</code> is not in the allowlist.")
        return
    try:
        get_apps_api().patch_namespaced_deployment(
            deployment_name,
            TARGET_NAMESPACE,
            {
                "spec": {
                    "template": {
                        "metadata": {
                            "annotations": {
                                "kubectl.kubernetes.io/restartedAt": datetime.now(timezone.utc).isoformat()
                            }
                        }
                    }
                }
            },
        )
    except ApiException as exc:
        LOGGER.exception("Failed to restart deployment")
        send_message(chat_id, "<b>Failed to restart deployment</b>\n" f"<code>{escape(str(exc))}</code>")
        return
    send_message(chat_id, f"<b>Restart triggered</b>\n<code>{escape(deployment_name)}</code>")


def delete_pod(chat_id: str, core_api: client.CoreV1Api, pod_name: str) -> None:
    if not pod_name:
        send_message(chat_id, "Usage: <code>/delete pod &lt;name&gt;</code>")
        return
    if not pod_name.startswith("syncio-"):
        send_message(chat_id, "Only `syncio-*` pods can be deleted by the bot.")
        return
    try:
        core_api.delete_namespaced_pod(pod_name, TARGET_NAMESPACE)
    except ApiException as exc:
        LOGGER.exception("Failed to delete pod")
        send_message(chat_id, "<b>Failed to delete pod</b>\n" f"<code>{escape(str(exc))}</code>")
        return
    send_message(chat_id, f"<b>Pod deleted</b>\n<code>{escape(pod_name)}</code>")


def get_service_endpoints(core_api: client.CoreV1Api, service_name: str) -> str:
    try:
        endpoints = core_api.read_namespaced_endpoints(service_name, TARGET_NAMESPACE)
    except ApiException as exc:
        LOGGER.exception("Failed to get service endpoints")
        return "<b>Failed to get service endpoints</b>\n" f"<code>{escape(str(exc))}</code>"

    addresses = []
    for subset in endpoints.subsets or []:
        for address in subset.addresses or []:
            target = address.target_ref.name if address.target_ref else "-"
            addresses.append(f"{address.ip} ({target})")
    if not addresses:
        return f"<b>Service endpoints</b>\n\n<code>{escape(service_name)}</code>\nNo ready endpoints."
    return (
        f"<b>Service endpoints</b>\n\n<code>{escape(service_name)}</code>\n"
        + "\n".join(f"<code>{escape(address)}</code>" for address in addresses[:20])
    )


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


def send_pod_cpu_chart(chat_id: str) -> None:
    items, error = collect_top_pod_metrics()
    if items is None:
        send_message(chat_id, "<b>Failed to get pod metrics</b>\n" f"<code>{escape(error)}</code>")
        return
    frame = pod_metrics_frame(items)
    send_frame_chart(
        chat_id,
        frame,
        "name",
        "cpu_m",
        f"Pod CPU Usage - {TARGET_NAMESPACE}",
        "Millicores",
        "#1D3557",
        f"<b>Pod CPU usage snapshot</b>\n<code>{TARGET_NAMESPACE}</code>",
    )


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


def send_node_cpu_chart(chat_id: str) -> None:
    items, error = collect_top_node_metrics()
    if items is None:
        send_message(chat_id, "<b>Failed to get node metrics</b>\n" f"<code>{escape(error)}</code>")
        return
    frame = node_metrics_frame(items)
    send_frame_chart(
        chat_id,
        frame,
        "name",
        "cpu_m",
        "Node CPU Usage",
        "Millicores",
        "#A44A3F",
        "<b>Node CPU usage snapshot</b>",
    )


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


def send_restart_chart(core_api: client.CoreV1Api, chat_id: str) -> None:
    frame, error = restart_metrics_frame(core_api)
    if frame is None:
        send_message(chat_id, "<b>Failed to inspect restarts</b>\n" f"<code>{escape(error)}</code>")
        return
    send_frame_chart(
        chat_id,
        frame,
        "name",
        "restarts",
        f"Pod Restarts - {TARGET_NAMESPACE}",
        "Restart count",
        "#C1121F",
        f"<b>Pod restart snapshot</b>\n<code>{TARGET_NAMESPACE}</code>",
    )


def send_pod_phase_chart(core_api: client.CoreV1Api, chat_id: str) -> None:
    frame, error = pod_phase_frame(core_api)
    if frame is None:
        send_message(chat_id, "<b>Failed to get pod phases</b>\n" f"<code>{escape(error)}</code>")
        return
    if frame.empty:
        send_message(chat_id, "No pod phase data found.")
        return
    image = build_pie_chart(
        frame,
        "phase",
        "count",
        f"Pod Phases - {TARGET_NAMESPACE}",
        ["#2A9D8F", "#E9C46A", "#E76F51", "#264653", "#8D99AE"],
    )
    send_photo(chat_id, image, f"<b>Pod phase distribution</b>\n<code>{TARGET_NAMESPACE}</code>")


def send_deployment_chart(chat_id: str) -> None:
    frame, error = deployment_frame()
    if frame is None:
        send_message(chat_id, "<b>Failed to list deployments</b>\n" f"<code>{escape(error)}</code>")
        return
    if frame.empty:
        send_message(chat_id, "No deployments found.")
        return
    image = build_grouped_bar_chart(
        frame,
        "name",
        "ready",
        "desired",
        f"Deployment Readiness - {TARGET_NAMESPACE}",
        "Replicas",
        "Ready",
        "Desired",
        "#2A9D8F",
        "#ADB5BD",
    )
    send_photo(chat_id, image, f"<b>Deployment readiness snapshot</b>\n<code>{TARGET_NAMESPACE}</code>")


def send_cpu_request_chart(core_api: client.CoreV1Api, chat_id: str) -> None:
    frame, error = resource_frame(core_api)
    if frame is None:
        send_message(chat_id, "<b>Failed to inspect resource requests</b>\n" f"<code>{escape(error)}</code>")
        return
    if frame.empty:
        send_message(chat_id, "No resource request data found.")
        return
    image = build_grouped_bar_chart(
        frame,
        "name",
        "cpu_requests_m",
        "cpu_limits_m",
        f"CPU Requests vs Limits - {TARGET_NAMESPACE}",
        "Millicores",
        "Requests",
        "Limits",
        "#457B9D",
        "#E63946",
    )
    send_photo(chat_id, image, f"<b>CPU requests vs limits</b>\n<code>{TARGET_NAMESPACE}</code>")


def send_ram_request_chart(core_api: client.CoreV1Api, chat_id: str) -> None:
    frame, error = resource_frame(core_api)
    if frame is None:
        send_message(chat_id, "<b>Failed to inspect resource requests</b>\n" f"<code>{escape(error)}</code>")
        return
    if frame.empty:
        send_message(chat_id, "No resource request data found.")
        return
    image = build_grouped_bar_chart(
        frame,
        "name",
        "memory_requests_mib",
        "memory_limits_mib",
        f"RAM Requests vs Limits - {TARGET_NAMESPACE}",
        "MiB",
        "Requests",
        "Limits",
        "#588157",
        "#BC4749",
    )
    send_photo(chat_id, image, f"<b>RAM requests vs limits</b>\n<code>{TARGET_NAMESPACE}</code>")


def send_storage_class_chart(core_api: client.CoreV1Api, chat_id: str) -> None:
    items, error = collect_storage_metrics(core_api)
    if items is None:
        send_message(chat_id, "<b>Failed to list PVCs</b>\n" f"<code>{escape(error)}</code>")
        return
    frame = storage_metrics_frame(items)
    if frame.empty:
        send_message(chat_id, "No persistent volume claims found.")
        return
    grouped = (
        frame.groupby("storage_class", as_index=False)["requested_gib"]
        .sum()
        .sort_values(by="requested_gib", ascending=False)
    )
    image = build_pie_chart(
        grouped,
        "storage_class",
        "requested_gib",
        f"Storage Class Share - {TARGET_NAMESPACE}",
        ["#355070", "#6D597A", "#B56576", "#E56B6F", "#EAAC8B"],
    )
    send_photo(chat_id, image, f"<b>Storage by class</b>\n<code>{TARGET_NAMESPACE}</code>")


def send_cpu_vs_ram_scatter(chat_id: str) -> None:
    items, error = collect_top_pod_metrics()
    if items is None:
        send_message(chat_id, "<b>Failed to get pod metrics</b>\n" f"<code>{escape(error)}</code>")
        return
    frame = pod_metrics_frame(items)
    if frame.empty:
        send_message(chat_id, "No pod metrics found.")
        return

    plot_frame = frame.head(12).copy()
    fig, ax = plt.subplots(figsize=(10, 7))
    ax.scatter(plot_frame["cpu_m"], plot_frame["memory_mib"], c="#3A86FF", s=90, alpha=0.8)
    for _, row in plot_frame.iterrows():
        ax.annotate(shorten_label(row["name"], 18), (row["cpu_m"], row["memory_mib"]), fontsize=8)
    ax.set_title(f"Pod CPU vs RAM - {TARGET_NAMESPACE}")
    ax.set_xlabel("CPU (millicores)")
    ax.set_ylabel("RAM (MiB)")
    ax.grid(True, linestyle="--", alpha=0.3)
    fig.tight_layout()
    image = io.BytesIO()
    fig.savefig(image, format="png", dpi=180, bbox_inches="tight")
    plt.close(fig)
    image.seek(0)
    send_photo(chat_id, image, f"<b>Pod CPU vs RAM scatter</b>\n<code>{TARGET_NAMESPACE}</code>")


def send_top_summary_table(core_api: client.CoreV1Api, chat_id: str) -> None:
    items, error = collect_top_pod_metrics()
    if items is None:
        send_message(chat_id, "<b>Failed to get pod metrics</b>\n" f"<code>{escape(error)}</code>")
        return
    frame = pod_metrics_frame(items).head(8)
    if frame.empty:
        send_message(chat_id, "No pod metrics found.")
        return

    frame["name"] = frame["name"].apply(lambda value: shorten_label(value, 24))
    table_text = frame.to_string(index=False)
    send_message(
        chat_id,
        f"<b>Top pod metrics table</b>\n<code>{TARGET_NAMESPACE}</code>\n\n<pre>{escape(table_text)}</pre>",
    )


def scale_deployment(chat_id: str, deployment_name: str, replicas_value: str) -> None:
    if not deployment_name or not replicas_value:
        send_message(chat_id, "Usage: <code>/scale &lt;deployment&gt; &lt;replicas&gt;</code>")
        return
    if SCALABLE_DEPLOYMENTS and deployment_name not in SCALABLE_DEPLOYMENTS:
        send_message(
            chat_id,
            f"<b>Scaling not allowed</b>\n<code>{escape(deployment_name)}</code> is not in the allowlist.",
        )
        return
    try:
        replicas = int(replicas_value)
    except ValueError:
        send_message(chat_id, "Replica count must be an integer.")
        return
    if replicas < 0 or replicas > 10:
        send_message(chat_id, "Replica count must be between <b>0</b> and <b>10</b>.")
        return

    try:
        scale = get_apps_api().read_namespaced_deployment_scale(deployment_name, TARGET_NAMESPACE)
        current = scale.spec.replicas or 0
        get_apps_api().patch_namespaced_deployment_scale(
            deployment_name,
            TARGET_NAMESPACE,
            {"spec": {"replicas": replicas}},
        )
    except ApiException as exc:
        LOGGER.exception("Failed to scale deployment")
        send_message(chat_id, "<b>Failed to scale deployment</b>\n" f"<code>{escape(str(exc))}</code>")
        return

    send_message(
        chat_id,
        (
            f"<b>Scaled deployment</b>\n"
            f"<code>{escape(deployment_name)}</code>: <b>{current}</b> -> <b>{replicas}</b>\n"
            f"<i>Note: Argo CD self-heal may revert this if Git still declares a different replica count.</i>"
        ),
    )


def get_app_resources() -> str:
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
        LOGGER.exception("Failed to get Argo CD resources")
        return "<b>Failed to get Argo CD resources</b>\n" f"<code>{escape(str(exc))}</code>"

    resources = app.get("status", {}).get("resources") or []
    lines = [f"<b>Argo CD resources</b> <code>{escape(ARGOCD_APP_NAME)}</code>", ""]
    for resource in resources[:20]:
        lines.append(
            f"<code>{escape(resource.get('kind'))}/{escape(resource.get('name'))}</code> - "
            f"sync <b>{escape(resource.get('status'))}</b>, health <b>{escape(resource.get('health', {}).get('status'))}</b>"
        )
    if len(lines) == 2:
        lines.append("No resources found.")
    return "\n".join(lines)


def get_app_history() -> str:
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
        LOGGER.exception("Failed to get Argo CD history")
        return "<b>Failed to get Argo CD history</b>\n" f"<code>{escape(str(exc))}</code>"

    history = app.get("status", {}).get("history") or []
    lines = [f"<b>Argo CD history</b> <code>{escape(ARGOCD_APP_NAME)}</code>", ""]
    for item in history[:5]:
        lines.append(
            f"<b>ID {item.get('id')}</b> - <code>{escape(item.get('revision'))}</code> - "
            f"<i>{escape(item.get('deployedAt'))}</i>"
        )
    if len(lines) == 2:
        lines.append("No history found.")
    return "\n".join(lines)


def sync_argocd_app(chat_id: str) -> None:
    custom_api = get_custom_objects_api()
    try:
        app = custom_api.get_namespaced_custom_object(
            group="argoproj.io",
            version="v1alpha1",
            namespace=ARGOCD_APP_NAMESPACE,
            plural="applications",
            name=ARGOCD_APP_NAME,
        )
        operation = app.get("status", {}).get("operationState", {})
        if operation.get("phase") in {"Running", "Terminating"}:
            send_message(chat_id, f"<b>Sync already in progress</b>\n<code>{escape(ARGOCD_APP_NAME)}</code>")
            return

        revision = app.get("spec", {}).get("source", {}).get("targetRevision") or GITHUB_BRANCH
        custom_api.patch_namespaced_custom_object(
            group="argoproj.io",
            version="v1alpha1",
            namespace=ARGOCD_APP_NAMESPACE,
            plural="applications",
            name=ARGOCD_APP_NAME,
            body={
                "operation": {
                    "sync": {
                        "revision": revision,
                        "prune": False,
                    }
                }
            },
        )
    except ApiException as exc:
        LOGGER.exception("Failed to sync Argo CD app")
        send_message(chat_id, "<b>Failed to start Argo CD sync</b>\n" f"<code>{escape(str(exc))}</code>")
        return
    send_message(chat_id, f"<b>Argo CD sync triggered</b>\n<code>{escape(ARGOCD_APP_NAME)}</code>")


def get_github_repo_summary() -> str:
    try:
        repo = github_api(f"/repos/{GITHUB_REPO}")
    except requests.RequestException as exc:
        LOGGER.exception("Failed to get GitHub repo summary")
        return "<b>Failed to get GitHub repo info</b>\n" f"<code>{escape(str(exc))}</code>"

    return (
        "<b>GitHub repo</b>\n\n"
        f"<b>Name:</b> <code>{escape(repo.get('full_name'))}</code>\n"
        f"<b>Default branch:</b> <code>{escape(repo.get('default_branch'))}</code>\n"
        f"<b>Stars:</b> <b>{repo.get('stargazers_count', 0)}</b>\n"
        f"<b>Open issues:</b> <b>{repo.get('open_issues_count', 0)}</b>\n"
        f"<b>Last push:</b> <code>{escape(repo.get('pushed_at'))}</code>\n"
        f"<b>URL:</b> <code>{escape(repo.get('html_url'))}</code>"
    )


def get_github_latest_commit() -> str:
    try:
        commit = github_api(f"/repos/{GITHUB_REPO}/commits/{GITHUB_BRANCH}")
    except requests.RequestException as exc:
        LOGGER.exception("Failed to get GitHub commit")
        return "<b>Failed to get latest commit</b>\n" f"<code>{escape(str(exc))}</code>"

    author = ((commit.get("commit") or {}).get("author") or {}).get("name")
    message = ((commit.get("commit") or {}).get("message") or "").splitlines()[0]
    return (
        "<b>Latest GitHub commit</b>\n\n"
        f"<b>Branch:</b> <code>{escape(GITHUB_BRANCH)}</code>\n"
        f"<b>SHA:</b> <code>{escape(commit.get('sha'))}</code>\n"
        f"<b>Author:</b> <code>{escape(author)}</code>\n"
        f"<b>Message:</b> {escape(message)}"
    )


def get_github_workflows() -> str:
    try:
        runs = github_api(f"/repos/{GITHUB_REPO}/actions/runs", params={"per_page": 5, "branch": GITHUB_BRANCH})
    except requests.RequestException as exc:
        LOGGER.exception("Failed to get GitHub workflows")
        return "<b>Failed to get workflow runs</b>\n" f"<code>{escape(str(exc))}</code>"

    items = runs.get("workflow_runs") or []
    lines = [f"<b>GitHub workflow runs</b> <code>{escape(GITHUB_REPO)}</code>", ""]
    for item in items[:5]:
        lines.append(
            f"<code>{escape(item.get('name'))}</code> - <b>{escape(item.get('status'))}/{escape(item.get('conclusion'))}</b>"
        )
        lines.append(f"{escape(item.get('head_branch'))} @ <code>{escape(item.get('head_sha'))}</code>")
        lines.append("")
    if len(lines) == 2:
        lines.append("No workflow runs found.")
    return "\n".join(lines).rstrip()


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
        "<code>/version</code> - show current bot version\n"
        "<code>/pods</code> - list pods in the syncio namespace\n"
        "<code>/pods pending</code> - list only pending pods\n"
        "<code>/describe pod &lt;name&gt;</code> - show pod details\n"
        "<code>/describe deploy &lt;name&gt;</code> - show deployment details\n"
        "<code>/describe service &lt;name&gt;</code> - show service details\n"
        "<code>/logs &lt;pod&gt;</code> - tail pod logs\n"
        "<code>/rollout &lt;deployment&gt;</code> - rollout status\n"
        "<code>/image &lt;deployment&gt;</code> - show deployment images\n"
        "<code>/endpoints &lt;service&gt;</code> - show service endpoints\n"
        "<code>/restart &lt;deployment&gt;</code> - rollout restart an allowed deployment\n"
        "<code>/delete pod &lt;name&gt;</code> - delete a syncio pod\n"
        "<code>/events</code> - show recent namespace events\n"
        "<code>/restarts</code> - show pods with restart counts\n"
        "<code>/app</code> - show Argo CD application status\n"
        "<code>/argocd resources</code> - list Argo CD resource states\n"
        "<code>/argocd history</code> - recent Argo CD deploy history\n"
        "<code>/argocd sync</code> - trigger Argo CD sync\n"
        "<code>/deployments</code> - list deployments and ready counts\n"
        "<code>/services</code> - list services in the syncio namespace\n"
        "<code>/cluster</code> - show cluster statistics for syncio\n"
        "<code>/top pods</code> - live CPU/RAM usage for pods\n"
        "<code>/top nodes</code> - live CPU/RAM usage for nodes\n"
        "<code>/graph pods</code> - RAM usage chart for pods\n"
        "<code>/graph pods cpu</code> - CPU usage chart for pods\n"
        "<code>/graph pods ram</code> - RAM usage chart for pods\n"
        "<code>/graph nodes</code> - RAM usage chart for nodes\n"
        "<code>/graph nodes cpu</code> - CPU usage chart for nodes\n"
        "<code>/graph nodes ram</code> - RAM usage chart for nodes\n"
        "<code>/graph storage</code> - PVC storage chart\n"
        "<code>/graph storage classes</code> - storage split by class\n"
        "<code>/graph restarts</code> - restart count chart\n"
        "<code>/graph pod-phases</code> - pod phase distribution\n"
        "<code>/graph deployments</code> - ready vs desired replicas\n"
        "<code>/graph cpu requests</code> - CPU requests vs limits\n"
        "<code>/graph ram requests</code> - RAM requests vs limits\n"
        "<code>/graph pods scatter</code> - CPU vs RAM scatter plot\n"
        "<code>/summary pods</code> - top pod metrics table\n"
        "<code>/scale &lt;deployment&gt; &lt;replicas&gt;</code> - scale an allowed deployment\n"
        "<code>/github repo</code> - GitHub repository summary\n"
        "<code>/github latest</code> - latest commit on configured branch\n"
        "<code>/github workflows</code> - recent GitHub workflow runs\n"
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

    if text == "/scale" or text.startswith("/scale "):
        parts = text.split()
        scale_deployment(chat_id, parts[1] if len(parts) > 1 else "", parts[2] if len(parts) > 2 else "")
        return
    if text == "/restart" or text.startswith("/restart "):
        parts = text.split()
        restart_deployment(chat_id, parts[1] if len(parts) > 1 else "")
        return
    if text == "/delete pod" or text.startswith("/delete pod "):
        parts = text.split(maxsplit=2)
        delete_pod(chat_id, core_api, parts[2] if len(parts) > 2 else "")
        return
    if text == "/describe pod" or text.startswith("/describe pod "):
        parts = text.split(maxsplit=2)
        send_message(chat_id, describe_pod(core_api, parts[2] if len(parts) > 2 else ""))
        return
    if text == "/describe deploy" or text.startswith("/describe deploy "):
        parts = text.split(maxsplit=2)
        send_message(chat_id, describe_deployment(parts[2] if len(parts) > 2 else ""))
        return
    if text == "/describe service" or text.startswith("/describe service "):
        parts = text.split(maxsplit=2)
        send_message(chat_id, describe_service(core_api, parts[2] if len(parts) > 2 else ""))
        return
    if text == "/logs" or text.startswith("/logs "):
        parts = text.split(maxsplit=1)
        send_message(chat_id, get_pod_logs(core_api, parts[1] if len(parts) > 1 else ""))
        return
    if text == "/rollout" or text.startswith("/rollout "):
        parts = text.split(maxsplit=1)
        send_message(chat_id, rollout_status(parts[1] if len(parts) > 1 else ""))
        return
    if text == "/image" or text.startswith("/image "):
        parts = text.split(maxsplit=1)
        send_message(chat_id, get_deployment_images(parts[1] if len(parts) > 1 else ""))
        return
    if text == "/endpoints" or text.startswith("/endpoints "):
        parts = text.split(maxsplit=1)
        send_message(chat_id, get_service_endpoints(core_api, parts[1] if len(parts) > 1 else ""))
        return

    if text == "/version":
        send_message(chat_id, get_version(core_api))
        return
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
    if text == "/argocd resources":
        send_message(chat_id, get_app_resources())
        return
    if text == "/argocd history":
        send_message(chat_id, get_app_history())
        return
    if text == "/argocd sync":
        sync_argocd_app(chat_id)
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
    if text == "/graph pods ram":
        send_pod_metrics_chart(chat_id)
        return
    if text == "/graph pods cpu":
        send_pod_cpu_chart(chat_id)
        return
    if text == "/graph nodes":
        send_node_metrics_chart(chat_id)
        return
    if text == "/graph nodes ram":
        send_node_metrics_chart(chat_id)
        return
    if text == "/graph nodes cpu":
        send_node_cpu_chart(chat_id)
        return
    if text == "/graph storage":
        send_storage_chart(core_api, chat_id)
        return
    if text == "/graph storage classes":
        send_storage_class_chart(core_api, chat_id)
        return
    if text == "/graph restarts":
        send_restart_chart(core_api, chat_id)
        return
    if text == "/graph pod-phases":
        send_pod_phase_chart(core_api, chat_id)
        return
    if text == "/graph deployments":
        send_deployment_chart(chat_id)
        return
    if text == "/graph cpu requests":
        send_cpu_request_chart(core_api, chat_id)
        return
    if text == "/graph ram requests":
        send_ram_request_chart(core_api, chat_id)
        return
    if text == "/graph pods scatter":
        send_cpu_vs_ram_scatter(chat_id)
        return
    if text == "/summary pods":
        send_top_summary_table(core_api, chat_id)
        return
    if text == "/github repo":
        send_message(chat_id, get_github_repo_summary())
        return
    if text == "/github latest":
        send_message(chat_id, get_github_latest_commit())
        return
    if text == "/github workflows":
        send_message(chat_id, get_github_workflows())
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
