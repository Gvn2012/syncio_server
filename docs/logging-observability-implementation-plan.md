# Logging and Observability Implementation Plan

## Objective

Design and implement a logging and observability approach for Syncio that can:

- capture request logs and detailed request metadata at the edge and in each service
- correlate client-to-server, server-to-server, gRPC, Kafka, and WebSocket activity
- support debugging, incident response, and business/security audit needs
- fit the current Kubernetes deployment model under `k8s/`

This document is intentionally written as a pre-implementation plan. No application or manifest changes are included here yet.

## Current State Assessment

### What already exists

The project already has partial observability foundations:

- Most Spring Boot services include Micrometer tracing with Brave and Zipkin.
- Most services expose Prometheus metrics through `/actuator/prometheus`.
- The Kubernetes deployment already runs a Zipkin instance.
- The API gateway is the single public entrypoint behind GKE ingress.

Concrete examples from the current repository:

- `api_gateway` already enables tracing and Prometheus in [api_gateway/src/main/resources/application.properties](/Users/cps/IdeaProjects/syncio_server/api_gateway/src/main/resources/application.properties:9).
- The Kubernetes gateway config mirrors that same setup in [k8s/gateway-config.yaml](/Users/cps/IdeaProjects/syncio_server/k8s/gateway-config.yaml:16).
- Zipkin is deployed in [k8s/infra.yaml](/Users/cps/IdeaProjects/syncio_server/k8s/infra.yaml:198).
- Service deployments inject `MANAGEMENT_ZIPKIN_TRACING_ENDPOINT` in [k8s/services.yaml](/Users/cps/IdeaProjects/syncio_server/k8s/services.yaml:49).
- There is already a request metadata extraction utility in [user_service/src/main/java/io/github/gvn2012/user_service/utils/RequestMetadataUtils.java](/Users/cps/IdeaProjects/syncio_server/user_service/src/main/java/io/github/gvn2012/user_service/utils/RequestMetadataUtils.java:10).
- There is already a persistent audit entity pattern in [user_service/src/main/java/io/github/gvn2012/user_service/entities/UserAuditLog.java](/Users/cps/IdeaProjects/syncio_server/user_service/src/main/java/io/github/gvn2012/user_service/entities/UserAuditLog.java:15).

### What is missing

The current setup is not yet sufficient for the goal you described.

#### 1. No centralized log collection stack in Kubernetes

There is no log aggregation backend or collector defined under `k8s/` for:

- application stdout/stderr logs
- Kubernetes metadata enrichment
- queryable retention
- dashboards and search

There is no visible deployment for:

- Loki
- Elasticsearch/OpenSearch
- Fluent Bit
- Vector
- Grafana Alloy
- OpenTelemetry Collector for logs

#### 2. No standardized structured logging format across services

The services use SLF4J logging, but there is no shared JSON log format, no common MDC fields, and no common schema. That means:

- logs will be inconsistent across services
- searching by trace ID, user ID, request ID, route, action, or conversation ID will be unreliable
- parsing at the collector/backend layer will be harder

#### 3. No explicit request correlation strategy beyond existing tracing

Tracing is present, but the codebase does not yet show a project-wide rule for:

- request ID propagation
- user ID propagation
- client session/device metadata propagation
- Kafka correlation propagation
- gRPC metadata propagation
- WebSocket session correlation

Micrometer tracing gives you trace/span context, but you still need application-level fields for operational and audit use cases.

#### 4. No separation of diagnostic logs vs audit logs

Your stated goal mixes two different concerns:

- diagnostic/operational logging
- durable business/security audit trail

These should not be treated as the same thing.

Operational logs are for debugging and observability:

- request received
- downstream call latency
- retry/failure
- Kafka consumer processing

Audit logs are immutable records of user actions:

- user changed profile
- user sent message
- user deleted message
- admin changed role
- token revoked

The existing `UserAuditLog` entity proves this distinction already exists in one service, but it is not standardized platform-wide.

#### 5. Sensitive data handling is not defined

Because you want “detailed metadata from request”, the project needs explicit redaction rules before implementation. Otherwise the system will eventually log:

- `Authorization` headers
- cookies or tokens
- PII in request bodies
- passwords
- email verification codes
- internal secrets from headers or env-backed config

This is especially important because the current manifests also contain plain-text secrets and credentials in `k8s/` and compose files. That is a separate security issue, but it raises the risk of accidental log leakage.

## Recommendation

## Recommended High-Level Approach

Use a three-layer observability design instead of a “single log service” design.

### Layer 1: Distributed tracing for request flow

Keep and improve the existing tracing layer.

Purpose:

- answer “which services were involved?”
- measure latency and failure points
- visualize the full path across gateway, services, gRPC, Kafka consumers, and WebSocket handlers

Recommendation:

- keep Micrometer tracing in services for now
- continue using Zipkin in the short term because it already exists
- plan a later migration to OpenTelemetry Collector plus Tempo or another OTLP-native backend if the platform grows

### Layer 2: Structured centralized logs for debugging and search

Add structured JSON logs from all services and ship them from Kubernetes to a centralized log backend.

Purpose:

- answer “what exactly happened in this request or action?”
- search by trace ID, request ID, user ID, route, conversation ID, message ID, org ID, or error code
- investigate issues faster than raw `kubectl logs`

Recommendation:

- standardize on JSON logs written to stdout
- collect logs with a Kubernetes node-level collector
- enrich logs with pod, namespace, container, node, and workload labels
- store logs in a searchable backend

Best-fit backend for this repo:

- `Fluent Bit` or `Grafana Alloy` as collector
- `Loki` as log backend
- `Grafana` for search and dashboards

Why this is the best fit here:

- simpler than Elasticsearch/OpenSearch to operate
- cheaper and lighter for a small to medium microservice deployment
- works well with Kubernetes metadata enrichment
- works well together with Prometheus and tracing-style workflows

### Layer 3: Durable audit/event records for business actions

Do not rely on centralized logs alone for important client actions.

Purpose:

- answer “who did what, when, against which entity, from where?”
- preserve compliance/security/business history
- survive log rotation, retention pruning, or backend outages

Recommendation:

- define a shared audit event model
- persist critical audit events in service-owned databases or an audit store
- optionally publish audit events to Kafka for asynchronous fan-out and analytics

This is the right place for:

- authentication actions
- account changes
- role/permission changes
- message deletion or moderation
- organization membership changes

## Why Not Use Only a Log Service

Using only a centralized logging service is not enough for this system.

Reasons:

- traces are better than logs for cross-service latency and topology
- logs are better than traces for rich payload-adjacent debugging context
- audit records are better than logs for immutable user/business action history

If you force all three use cases into logs only, the result is noisy, expensive, harder to query, and weaker from a security standpoint.

## Target Architecture

### Ingress and gateway edge

At the API gateway:

- generate or honor `X-Request-Id`
- ensure trace context is propagated
- record normalized request/response logs
- capture user ID after authentication succeeds
- capture route ID and downstream target
- do not log request/response bodies by default

Fields to capture:

- `timestamp`
- `level`
- `service.name`
- `environment`
- `traceId`
- `spanId`
- `requestId`
- `http.method`
- `http.path`
- `http.route`
- `http.status_code`
- `duration_ms`
- `client.ip`
- `user.id`
- `user.agent`
- `device.platform`
- `origin`
- `referer`
- `k8s.namespace`
- `k8s.pod`

### Internal HTTP/gRPC services

For every service:

- add a request logging interceptor/filter
- populate MDC from trace context and request headers
- add outbound client interceptors for HTTP and gRPC
- propagate correlation and user context safely

Fields to add where available:

- `downstream.service`
- `grpc.method`
- `grpc.status`
- `peer.service`
- `retry.count`
- `error.code`
- `exception.type`

### Kafka producers and consumers

Because the system already runs Kafka, you should treat asynchronous flows as first-class observability paths.

For Kafka producers:

- attach trace context in headers
- attach `requestId`, `userId`, `eventType`, `aggregateId`
- log publish success/failure and topic/partition/offset when available

For Kafka consumers:

- restore correlation context from headers
- start/continue trace span
- log processing start/end/failure
- include consumer group, topic, partition, and offset

### WebSocket flows

For `websocket_service` and messaging-related flows:

- create a correlation record when the socket connects
- map `sessionId -> userId -> requestId/traceId seed`
- log connect, subscribe, send, receive, disconnect, and error events
- include destination/topic/conversation identifiers

WebSocket logging should be event-level, not payload-dump-level.

## Recommended Technology Choices

### Short-term choice

- Tracing backend: existing Zipkin
- Metrics backend: existing Prometheus-compatible setup
- Logs collector: Fluent Bit
- Logs backend: Loki
- Visualization: Grafana
- Log format: JSON to stdout
- Correlation: trace ID + request ID + user ID + action ID

This is the recommended first implementation because it matches the current repo maturity and existing infrastructure.

### Medium-term upgrade path

Move to:

- OpenTelemetry Collector
- OTLP export from applications
- Tempo for traces
- Loki for logs
- Prometheus for metrics

This gives you one collector plane for metrics, traces, and logs, but it is more moving parts than you need for the first implementation.

## Detailed Implementation Plan

## Phase 0: Design and governance

### Goals

- define schema before writing logs
- define redaction rules before capturing metadata
- define what is diagnostic logging vs audit logging

### Tasks

1. Define the canonical log schema.

Required baseline fields:

- `timestamp`
- `severity`
- `message`
- `service`
- `environment`
- `version`
- `traceId`
- `spanId`
- `requestId`
- `userId`
- `sessionId`
- `action`
- `outcome`

Protocol-specific optional fields:

- HTTP: method, path, route, status, latency, client IP
- gRPC: service, method, status, deadline, peer
- Kafka: topic, key, partition, offset, consumer group
- WebSocket: session, destination, event type

2. Define metadata classification.

Allow-list metadata:

- request path
- method
- client IP
- user agent
- origin
- referer
- platform
- timezone
- locale
- authenticated user ID
- tenant/org ID where applicable

Never log:

- passwords
- tokens
- cookies
- auth headers
- refresh tokens
- full request bodies by default
- full response bodies by default
- secret env vars

3. Define audit event taxonomy.

Create a platform-wide action list such as:

- `AUTH_LOGIN_SUCCEEDED`
- `AUTH_LOGIN_FAILED`
- `AUTH_LOGOUT`
- `USER_PROFILE_UPDATED`
- `USER_EMAIL_ADDED`
- `ORG_MEMBER_INVITED`
- `MESSAGE_SENT`
- `MESSAGE_DELETED`
- `PERMISSION_ROLE_UPDATED`

4. Define retention and cost policy.

Suggested starting point:

- application logs: 7 to 14 days
- security/audit logs in DB: 90 to 365 days depending on business need
- traces: 3 to 7 days

### Deliverables

- logging schema spec
- redaction spec
- audit action catalog
- retention policy

## Phase 1: Shared application instrumentation library

### Goals

- avoid re-implementing logging separately in every service
- ensure schema consistency across all Spring services

### Tasks

1. Create a shared observability module.

Preferred location:

- new module such as `observability_shared` or extend `shared`

Contents:

- MDC key constants
- request ID utilities
- redaction helpers
- JSON log helpers
- HTTP servlet filter
- WebFlux filter
- gRPC server interceptor
- gRPC client interceptor
- Kafka producer interceptor/header helper
- Kafka consumer interceptor/context restorer

2. Standardize log output.

For Spring Boot services:

- add Logback JSON encoder configuration
- send logs to stdout only
- do not write application log files inside containers

3. Populate MDC consistently.

At minimum:

- `traceId`
- `spanId`
- `requestId`
- `service`
- `userId`
- `orgId`
- `sessionId`
- `clientIp`

4. Define common log event methods.

Examples:

- request received
- request completed
- downstream call completed
- Kafka publish completed
- Kafka consume completed
- WebSocket event handled
- business audit event emitted

### Deliverables

- shared observability module
- common logback config pattern
- reusable interceptors and helpers

## Phase 2: HTTP request logging and correlation

### Goals

- capture client-to-server request metadata consistently
- make every log line queryable by request and user

### Tasks

1. API gateway instrumentation.

Implement a gateway filter that:

- reads `X-Request-Id` if present, otherwise generates one
- writes the request ID back to the response header
- logs request start and completion
- records selected headers and route metadata
- stores authenticated `userId` after auth succeeds

2. Service-level request interceptors.

For Spring MVC services:

- add servlet filter or interceptor

For Spring WebFlux services:

- add reactive `WebFilter`

3. Normalize request metadata extraction.

Use the existing `RequestMetadataUtils` concept as a starting point, but promote it into a shared implementation. Extend it to include:

- request ID
- trace ID
- route template
- authenticated principal/user ID
- organization/tenant identifier when present

4. Add response completion logging.

Each request should log one completion event with:

- outcome
- status code
- total duration
- error summary if failed

### Deliverables

- gateway request logging filter
- per-service request interceptor/filter
- shared metadata extraction

## Phase 3: Internal service-to-service observability

### Goals

- follow request context across HTTP and gRPC hops

### Tasks

1. Outbound HTTP instrumentation.

For every `RestTemplate` or `WebClient`:

- add interceptor/filter to propagate `X-Request-Id`
- propagate trace context automatically
- propagate safe user context headers only if required
- log downstream latency and result

2. Inbound gRPC instrumentation.

Add a server interceptor that:

- extracts correlation metadata
- restores MDC
- creates/continues span context
- logs method, status, and duration

3. Outbound gRPC instrumentation.

Add a client interceptor that:

- injects request ID and trace headers
- logs downstream method calls

4. Define header/metadata naming conventions.

Recommended:

- `X-Request-Id`
- `X-User-Id`
- `X-Org-Id`

For gRPC metadata, use lowercase ASCII-safe names such as:

- `x-request-id`
- `x-user-id`
- `x-org-id`

### Deliverables

- HTTP client interceptors
- gRPC client/server interceptors
- correlation propagation standard

## Phase 4: Kafka event correlation

### Goals

- make asynchronous event chains debuggable

### Tasks

1. Standardize Kafka headers.

Add headers:

- `traceparent` or equivalent tracing headers
- `x-request-id`
- `x-user-id`
- `x-org-id`
- `event-type`
- `event-id`

2. Producer instrumentation.

Wrap producer send operations to log:

- topic
- key
- event type
- request ID
- trace ID
- publish result

3. Consumer instrumentation.

On consume:

- extract headers
- restore context
- create/continue trace
- log topic, partition, offset, event type, and handling result

4. Dead-letter strategy.

For critical consumers:

- define retry policy
- define dead-letter topic policy
- log failure with correlation fields

### Deliverables

- Kafka header standard
- producer/consumer logging helpers
- failure handling guidelines

## Phase 5: WebSocket observability

### Goals

- correlate socket events with user and request context

### Tasks

1. Connection lifecycle logging.

Log:

- connect
- authenticate
- subscribe
- unsubscribe
- disconnect
- error

2. Session context store.

Store per active session:

- session ID
- user ID
- connection time
- device/platform metadata
- source IP if available

3. Message event logging.

Log event metadata only:

- destination
- conversation ID
- message type
- action
- outcome

Do not log message content by default.

### Deliverables

- WebSocket connection/event logger
- session context correlation plan

## Phase 6: Audit logging for client actions

### Goals

- create durable business/security history distinct from operational logs

### Tasks

1. Define which actions require persistence.

Examples:

- auth events
- profile changes
- membership/role changes
- message delete/moderation
- file upload completion/failure

2. Standardize audit event model.

Suggested fields:

- `id`
- `occurredAt`
- `actorUserId`
- `actorType`
- `action`
- `targetType`
- `targetId`
- `requestId`
- `traceId`
- `ipAddress`
- `userAgent`
- `origin`
- `outcome`
- `oldValue`
- `newValue`

3. Choose persistence strategy.

Recommended first version:

- each service persists its own audit events for the entities it owns

Alternative:

- publish to Kafka and persist centrally in a dedicated audit service

Recommendation for this repo:

- start with per-service persistence
- use the existing `UserAuditLog` pattern as the baseline
- move to a central audit stream later only if operationally justified

4. Add audit emission at service layer boundaries.

Emit audit events after business success/failure is known, not only at controller entry.

### Deliverables

- shared audit schema
- service-by-service audit scope map
- persistence implementation plan

## Phase 7: Kubernetes log collection and storage

### Goals

- collect all container logs from the cluster
- enrich them with Kubernetes metadata
- make them searchable centrally

### Tasks

1. Deploy Loki.

Deploy into the cluster with:

- namespace placement strategy
- PVC-backed storage
- retention config
- auth/network policy if needed

2. Deploy Grafana.

Configure datasources:

- Loki
- Prometheus
- Zipkin or tracing datasource equivalent

3. Deploy Fluent Bit or Grafana Alloy as a DaemonSet.

Responsibilities:

- tail container logs from node filesystem
- parse JSON logs
- enrich with Kubernetes metadata
- drop noisy/unwanted fields
- ship to Loki

4. Label workloads for searchability.

Add useful labels/annotations to deployments:

- `app.kubernetes.io/name`
- `app.kubernetes.io/component`
- `app.kubernetes.io/part-of`
- `app.kubernetes.io/version`

5. Add optional namespace/workload routing.

If needed:

- different retention for infra logs vs app logs
- separate streams for gateway, services, Kafka, and system components

### Suggested Kubernetes additions

Under `k8s/`, plan to add manifests such as:

- `k8s/observability/loki.yaml`
- `k8s/observability/grafana.yaml`
- `k8s/observability/fluent-bit.yaml`
- `k8s/observability/service-monitors-or-scrape-configs.yaml`

### Deliverables

- log backend deployment
- cluster-wide log collector
- dashboards and saved queries

## Phase 8: Dashboarding, alerts, and operational readiness

### Goals

- make the data usable, not just collected

### Tasks

1. Create Grafana dashboards.

Minimum dashboards:

- gateway request overview
- per-service error rate and latency
- Kafka consumer failures
- WebSocket connection counts and errors
- top exception types

2. Create saved log queries.

Examples:

- by `traceId`
- by `requestId`
- by `userId`
- by `conversationId`
- by `action`
- by `error.code`

3. Define alerts.

Examples:

- gateway 5xx spike
- auth failures anomaly
- Kafka consumer error spike
- WebSocket disconnect anomaly
- Loki ingestion failure

### Deliverables

- Grafana dashboards
- alert definitions
- runbook queries

## Implementation Order Recommendation

Implement in this order:

1. Define schema, redaction, and audit rules.
2. Standardize JSON logging and MDC in application code.
3. Instrument gateway and HTTP services.
4. Add gRPC and Kafka correlation.
5. Add WebSocket event logging.
6. Deploy Loki + collector + Grafana in Kubernetes.
7. Add durable audit logging for critical business actions.
8. Add dashboards, alerts, and runbooks.

This order keeps the rollout controlled. If you deploy a log backend first without standardizing logs, you will ingest inconsistent noise.

## Service-by-Service Rollout Recommendation

Start with the services that give the highest visibility payoff:

### Wave 1

- `api_gateway`
- `auth_service`
- `user_service`
- `messaging_service`
- `websocket_service`

Reason:

- these cover the public edge
- authentication context
- primary user actions
- message flow
- real-time activity

### Wave 2

- `org_service`
- `permission_service`
- `relationship_service`
- `post_service`
- `search_service`

### Wave 3

- `notification_service`
- `uploading_service`
- `presence_service`
- `call_service`
- supporting Python services if they remain in active use

## Acceptance Criteria

The implementation should be considered successful only when all of the following are true:

1. A single external request can be traced from ingress through gateway and downstream services using `traceId` and `requestId`.
2. Logs from all pods are searchable centrally without using `kubectl logs`.
3. A query by `userId` shows relevant request and action history without exposing secrets.
4. Kafka-triggered flows can be correlated back to the original initiating request.
5. WebSocket events can be linked to user and session context.
6. Critical user/business actions are persisted as audit events, not only emitted as logs.
7. Authorization headers, tokens, cookies, passwords, and sensitive request bodies are not logged.
8. Dashboards exist for gateway, service errors, and Kafka/WebSocket failures.

## Risks and Controls

### Risk: logging too much data

Control:

- allow-list metadata
- disable payload logging by default
- redact sensitive headers and fields

### Risk: high log cost and noisy storage

Control:

- structured concise events
- retention policy
- sampling only for debug-only categories if needed

### Risk: trace/log correlation gaps

Control:

- shared interceptors
- standard header propagation
- mandatory `requestId` generation at gateway

### Risk: audit inconsistency across services

Control:

- shared audit schema
- rollout checklist per service

## Specific Recommendations for This Repository

Based on the current repo and `k8s/` layout, the best practical approach is:

1. Keep the existing Zipkin tracing for the first implementation rather than replacing it immediately.
2. Add structured JSON logging to all services and standard MDC correlation fields.
3. Deploy `Fluent Bit + Loki + Grafana` in Kubernetes for centralized logs.
4. Implement request correlation first at `api_gateway`, then propagate through HTTP, gRPC, Kafka, and WebSocket layers.
5. Treat critical client actions as persistent audit events in service databases, not just log lines.
6. Refactor the existing request metadata extraction and `UserAuditLog` patterns into a shared platform standard.

That gives you the fastest path to a workable production observability stack without overengineering the first rollout.

## Proposed Files and Workstreams for the Next Step

When proceeding with implementation, the likely workstreams are:

- shared observability module under the Maven monorepo
- Spring logging configuration updates in each service
- gateway filter updates in `api_gateway`
- HTTP/gRPC/Kafka/WebSocket interceptor additions across services
- new Kubernetes manifests under `k8s/observability`
- optional audit table/entity/service expansion in selected services

## Final Decision

Recommended first implementation:

- tracing: keep current Micrometer -> Zipkin
- logs: structured JSON to stdout
- collection: Fluent Bit DaemonSet
- backend: Loki
- UI: Grafana
- correlation keys: `traceId`, `spanId`, `requestId`, `userId`, `sessionId`, `action`
- durable business trail: per-service audit logging for critical actions

This is the best balance of implementation speed, operational simplicity, and debugging value for the current Syncio project.
