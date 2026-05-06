# SyncIO Server 🚀

SyncIO Server is a high-performance, resilient microservices backend designed for real-time social and organizational communication. Built with **Java 21** and **Spring Boot 3.4**, it orchestrates a distributed system of services using **gRPC** for low-latency internal communication and **Apache Kafka** for event-driven reliability.

---

## 🏛️ Architecture Overview

SyncIO follows a modern microservices architecture, leveraging **Google Cloud Platform (GCP)** and **Google Kubernetes Engine (GKE)** for production deployments.

### Core Ecosystem

- **API Gateway**: Secure entry point leveraging **Spring Cloud Gateway**.
- **Ingress**: GKE **Ingress (GCE)** for global load balancing and SSL termination (via Google-managed certificates) at `syncio.site`.
- **Service Discovery**: **Netflix Eureka** for dynamic service registration.
- **Internal Communication**: High-performance **gRPC** interfaces.
- **Event Mesh**: **Apache Kafka** for asynchronous event-driven architecture (EDA).
- **Observability**: Distributed tracing with **Zipkin**, metrics via **Prometheus/Grafana**, and a dedicated **Logging Service**.
- **CI/CD & GitOps**: **GitHub Actions** for automated builds and **ArgoCD** for continuous deployment to GKE.

---

## 🛠️ Service Catalog

| Service                  | Responsibility                                                | Storage / Tech   |
| :----------------------- | :------------------------------------------------------------ | :--------------- |
| **Auth Service**         | Identity management, OAuth2, and JWT issuance.                | MySQL            |
| **User Service**         | User profiles, account settings, and identity data.           | MySQL            |
| **Messaging Service**    | Real-time chat, group management, and message persistence.    | MySQL            |
| **Websocket Service**    | Stateful STOMP/Websocket bridge for real-time client updates. | Kafka            |
| **Presence Service**     | Real-time user status (Online/Offline) tracking.              | Redis / Kafka    |
| **Call Service**         | WebRTC signaling and multi-party call orchestration.          | gRPC             |
| **Org Service**          | Organizational hierarchy and workforce management.            | MySQL            |
| **Permission Service**   | Fine-grained RBAC and organizational permissions.             | MySQL            |
| **Relationship Service** | Social graph, friendships, and blocked users.                 | MySQL / Kafka    |
| **Post Service**         | Social media feeds, likes, and comments.                      | MySQL / Kafka    |
| **Ranking Service**      | Algorithmic content ranking and user scoring.                 | Python (XGBoost) |
| **Search Service**       | Full-text search for users and content.                       | Kafka / Search   |
| **Uploading Service**    | Media processing and cloud storage integration.               | Kafka            |
| **Logging Service**      | Centralized aggregation of system-wide logs.                  | MongoDB / Kafka  |
| **Telegram Bot**         | Integration bridge for Telegram interactions.                 | Python           |

---

## 🚀 Tech Stack

- **Framework**: Spring Boot 3.4.1, Spring Cloud 2024.0.0, FastAPI (Python)
- **Language**: Java 21, Python 3.12
- **Communication**: gRPC, Protobuf, REST
- **Message Broker**: Apache Kafka
- **Databases**: MySQL, MongoDB, Redis
- **Tracing & Monitoring**: OpenZipkin, Prometheus, Grafana
- **Infrastructure**: GKE (Google Kubernetes Engine), ArgoCD, GitHub Actions

---

## 📦 Project Structure

```text
├── api_gateway          # entry: api_gateway/
├── auth_service         # entry: auth_service/
├── call_service         # entry: call_service/
├── discovery_service    # entry: discovery_service/
├── logging_service      # entry: logging_service/
├── messaging_service    # entry: messaging_service/
├── notification_service # entry: notification_service/
├── org_service          # entry: org_service/
├── permission_service   # entry: permission_service/
├── post_service         # entry: post_service/
├── presence_service     # entry: presence_service/
├── ranking_service      # entry: ranking_service/
├── relationship_service # entry: relationship_service/
├── search_service       # entry: search_service/
├── uploading_service    # entry: uploading_service/
├── user_service         # entry: user_service/
├── websocket_service    # entry: websocket_service/
├── telegram_bot         # entry: telegram_bot/
├── shared               # entry: shared/
├── grpc_common          # entry: grpc_common/
├── k8s                  # entry: k8s/
├── argocd               # entry: argocd/
└── prometheus           # entry: prometheus/
```

---

## 🧠 Featured Algorithms & Logic

SyncIO implements several sophisticated algorithms to ensure a premium user experience and system reliability.

### 1. Content Ranking (XGBoost)

The **Ranking Service** utilizes a Gradient Boosted Decision Tree (XGBoost) model to calculate personalized relevance scores. The model operates as a regressor (`reg:squarederror`) processing a multi-dimensional feature vector:

- **Author Affinity**: A coefficient representing the historical engagement strength between the requester and the content author.
- **Interaction Velocity**: Real-time engagement rate tracking (likes, comments, and shares per hour).
- **Recency Decay**: A logarithmic time-based penalty that prioritizes newer content.
- **Feature Engineering**:
  - `freshness_boost`: $1.0 / (1.0 + \text{recency\_hours})$
  - `interaction_density`: $\text{velocity\_score} / (1.0 + \text{recency\_hours})$
  - `category_id`: Categorical encoding for Post Types (Normal, Poll, Event, Task, Announcement).

### 2. Event-Driven Architecture (Kafka Flow)

SyncIO leverages Apache Kafka to decouple services and ensure eventual consistency across the distributed system.

- **Real-time Synchronization**: `WsOutboundEvent` allows services to push real-time updates (notifications, presence, chat) to the **Websocket Service** for client delivery.
- **Data Enrichment**: `ImageUploadedEvent` triggers background tasks in `user_service` and `post_service` to update profile/content media references.
- **Search & Analytics**: `PostActivityEvent` and `PostSearchEvent` flow from the **Post Service** to the **Ranking** and **Search** services to update indices and scores asynchronously.
- **Logging Pipeline**: A centralized `CentralizedLogEvent` is emitted by all services and consumed by the **Logging Service** for persistence in MongoDB.

### 3. Caching Strategy (Redis)

Distributed caching is implemented using **Redis** to minimize database load and reduce latency for frequent operations:

- **Relationship Caching**: The **Post Service** caches user social graphs (friends, following, blocks) to quickly filter feed visibility.
- **Content Moderation**: Global "Banned Words" lists are cached in-memory across the **Post Service** cluster.
- **Presence Management**: Real-time user statuses are stored with a 5-minute sliding TTL, providing a lightweight heartbeat mechanism.
- **Trending Metrics**: Interaction scores are maintained in **Redis Sorted Sets (ZSets)** for high-concurrency velocity tracking.
- **Social Graph Analytics**: Set-intersection algorithms for **Mutual Friends** discovery and pre-calculated social affinity scores.

### 4. Content Intelligence

- **Cosine Similarity**: Used in the **Post Service** for near-duplicate detection and "similar posts" recommendations by calculating the angular distance between high-dimensional content vectors.
- **Fuzzy Search**: Leverages **Levenshtein Distance** via Elasticsearch in the **Search Service** to provide resilient search results against typos and partial matches.

### 5. Real-time Communication & Presence

- **Heartbeat-based Presence**: TTL-backed status management in Redis to track user availability with high precision and minimal overhead.
- **WebRTC Signaling**: Stateful orchestration of ICE candidates and SDP offers in the **Call Service** for peer-to-peer communication.
- **Reactive WebSocket Bridge**: Stateless scaling of real-time updates using Kafka as a backplane for the **Websocket Service**.

---

## 🛠️ Getting Started

### Prerequisites

- JDK 21 & Python 3.12
- `kubectl` configured for GCP/GKE
- Maven 3.9+

### Deployment

SyncIO is designed to run on **Kubernetes**. Local execution via Docker Compose is deprecated in favor of K8s-native deployment.

1. **Clone the repository**:

   ```bash
   git clone https://github.com/Gvn2012/syncio_server.git
   cd syncio_server
   ```

2. **Kubernetes Configuration**:
   Explore the `k8s/` directory for manifests. The project uses a GitOps approach:
   - **GitHub Actions** builds images and updates `k8s/services.yaml`.
   - **ArgoCD** automatically synchronizes these changes to the GKE cluster.

3. **Manual Deployment (Optional)**:
   ```bash
   kubectl apply -f k8s/namespace.yaml
   kubectl apply -f k8s/infra.yaml
   kubectl apply -f k8s/services.yaml
   ```

---

## 🛡️ Best Practices

The project adheres to strict coding standards:

- **Clean Architecture**: Clear separation of Web, Service, and Data layers.
- **gRPC Abstraction**: Dedicated client layers to prevent transport leakage.
- **Resiliency**: Fallback mechanisms and circuit breakers for inter-service calls.
- **DTO Pattern**: Mandatory DTOs for all API boundaries.

---

## 📄 License

Copyright © 2026 Gvn2012. All rights reserved.
