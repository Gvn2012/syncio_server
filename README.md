# SyncIO Server 🚀

SyncIO Server is a high-performance, resilient microservices backend designed for real-time social and organizational communication. Built with **Java 21** and **Spring Boot 3.4**, it orchestrates a distributed system of services using **gRPC** for low-latency internal communication and **Apache Kafka** for event-driven reliability.

---

## 🏛️ Architecture Overview

SyncIO follows a modern microservices architecture, emphasizing separation of concerns, high availability, and horizontal scalability.

### Core Ecosystem
- **API Gateway**: The secure entry point leveraging **Spring Cloud Gateway**.
- **Service Discovery**: **Netflix Eureka** for dynamic service registration and lookup.
- **Internal Communication**: Type-safe, high-performance **gRPC** interfaces.
- **Event Mesh**: **Apache Kafka** for asynchronous processing and cross-service event synchronization.
- **Observability**: Distributed tracing with **Zipkin**, metrics via **Prometheus/Grafana**, and a dedicated **Logging Service** for centralized log aggregation.

---

## 🛠️ Service Catalog

| Service | Responsibility | Storage / Tech |
| :--- | :--- | :--- |
| **Auth Service** | Identity management, OAuth2, and JWT issuance. | MySQL |
| **User Service** | User profiles, account settings, and identity data. | MySQL |
| **Messaging Service** | Real-time chat, group management, and message persistence. | MySQL |
| **Websocket Service** | Stateful STOMP/Websocket bridge for real-time client updates. | Kafka |
| **Presence Service** | Real-time user status (Online/Offline) tracking. | Redis / Kafka |
| **Call Service** | WebRTC signaling and multi-party call orchestration. | gRPC |
| **Org Service** | Organizational hierarchy and workforce management. | MySQL |
| **Permission Service** | Fine-grained RBAC and organizational permissions. | MySQL |
| **Relationship Service** | Social graph, friendships, and blocked users. | MySQL / Kafka |
| **Post Service** | Social media feeds, likes, and comments. | MySQL / Kafka |
| **Ranking Service** | Algorithmic content ranking and user scoring. | - |
| **Search Service** | Full-text search for users and content. | Kafka / Search |
| **Uploading Service** | Media processing and cloud storage integration. | Kafka |
| **Logging Service** | Centralized aggregation of system-wide logs. | MongoDB / Kafka |
| **Telegram Bot** | Integration bridge for Telegram interactions. | - |

---

## 🚀 Tech Stack

- **Framework**: Spring Boot 3.4.1, Spring Cloud 2024.0.0
- **Language**: Java 21
- **Communication**: gRPC, Protobuf, REST
- **Message Broker**: Apache Kafka
- **Databases**: MySQL, MongoDB
- **Tracing & Monitoring**: OpenZipkin, Prometheus, Grafana
- **Infrastructure**: Docker Compose, Kubernetes (GKE), ArgoCD

---

## 📦 Project Structure

```text
├── api_gateway         # entry: api_gateway/
├── auth_service        # entry: auth_service/
├── call_service        # entry: call_service/
├── discovery_service   # entry: discovery_service/
├── logging_service     # entry: logging_service/
├── messaging_service   # entry: messaging_service/
├── notification_service# entry: notification_service/
├── org_service         # entry: org_service/
├── permission_service  # entry: permission_service/
├── post_service        # entry: post_service/
├── presence_service    # entry: presence_service/
├── ranking_service     # entry: ranking_service/
├── relationship_service# entry: relationship_service/
├── search_service      # entry: search_service/
├── uploading_service   # entry: uploading_service/
├── user_service        # entry: user_service/
├── websocket_service   # entry: websocket_service/
├── telegram_bot        # entry: telegram_bot/
├── shared              # entry: shared/
├── grpc_common         # entry: grpc_common/
├── k8s                 # entry: k8s/
├── argocd              # entry: argocd/
└── prometheus          # entry: prometheus/
```

---

## 🧠 Featured Algorithms & Logic

SyncIO implements several sophisticated algorithms to ensure a premium user experience and system reliability.

### 1. Content Ranking (XGBoost)
The **Ranking Service** utilizes Gradient Boosted Decision Trees (XGBoost) to rank content. It processes features such as:
- **Author Affinity**: Historical interaction strength between user and author.
- **Interaction Velocity**: Real-time engagement rate (likes/comments per hour).
- **Recency Decay**: Time-based relevance degradation.
- **Content Freshness**: Boost for newly published media-rich posts.

### 2. Social Graph Analytics
- **Mutual Friends**: Efficient set-intersection algorithms in the **Relationship Service** to identify common connections.
- **Interaction Velocity Tracking**: Uses **Redis Sorted Sets (ZSet)** to maintain real-time trending scores based on weighted user actions.

### 3. Content Intelligence
- **Cosine Similarity**: Used in the **Post Service** for near-duplicate detection and "similar posts" recommendations by calculating the angular distance between content vectors.
- **Fuzzy Search**: Leverages **Levenshtein Distance** via Elasticsearch in the **Search Service** to provide resilient search results against typos and partial matches.

### 4. Real-time Management
- **Heartbeat-based Presence**: TTL-backed status management in Redis to track user availability with high precision and low overhead.
- **WebRTC Signaling**: Stateful orchestration of ICE candidates and SDP offers in the **Call Service** for peer-to-peer communication.

---

## 🛠️ Getting Started

### Prerequisites
- JDK 21
- Docker & Docker Compose
- Maven 3.9+

### Local Development
1. **Clone the repository**:
   ```bash
   git clone https://github.com/Gvn2012/syncio_server.git
   cd syncio_server
   ```

2. **Start Infrastructure**:
   ```bash
   docker-compose up -d
   ```

3. **Build & Run**:
   ```bash
   ./mvnw clean install
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
Copyright © 2024 Gvn2012. All rights reserved.
