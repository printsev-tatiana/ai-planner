# 🧠 AI Planner App — Solo Founder MVP Technical Plan

> **Stack:** Spring Boot 3 (Java 21) · Swift (iOS) · Java/Jetpack Compose (Android) · Azure · PostgreSQL · Docker · GitHub Actions

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Repository Structure & GitHub Setup](#2-repository-structure--github-setup)
3. [GitHub Issues — Templates & Project Board](#3-github-issues--templates--project-board)
4. [Architecture Overview](#4-architecture-overview)
5. [Database Design — SQL Schemas](#5-database-design--sql-schemas)
6. [Backend — Java Modular Monolith API](#6-backend--java-modular-monolith-api)
7. [Background Messaging — Email & Notifications](#7-background-messaging--email--notifications)
8. [Stripe Integration — Premium Subscriptions](#8-stripe-integration--premium-subscriptions)
9. [Docker — Build & Local Dev](#9-docker--build--local-dev)
10. [Azure Infrastructure Setup](#10-azure-infrastructure-setup)
11. [CI/CD — GitHub Actions Pipeline](#11-cicd--github-actions-pipeline)
12. [iOS App — Swift](#12-ios-app--swift)
13. [Android App — Java / Jetpack Compose](#13-android-app--java--jetpack-compose)
14. [Documentation — Docs as Code](#14-documentation--docs-as-code)
15. [Milestones & Solo Execution Order](#15-milestones--solo-execution-order)

---

## 1. Project Overview

### What We're Building

A minimalistic AI-powered planner app where the user:
- Opens **one persistent chat** backed by **Claude Haiku**
- Sends text messages or **voice memos** (transcribed on-device)
- The AI extracts **events and to-do items** and schedules them
- The AI sends **proactive in-chat notifications** when events are due
- Users can upgrade to **Premium** via Stripe for higher AI usage

### Technology Choices (2026)

| Layer | Technology | Reason |
|---|---|---|
| API | Spring Boot 3.3 + Java 21 Virtual Threads | Lightweight, production-proven, excellent Azure support |
| Architecture | Modular Monolith (DDD modules) | Solo-friendly; extract to microservices later |
| Database | Azure Database for PostgreSQL Flexible Server | Managed, cost-effective, JSON column support |
| Messaging | Azure Service Bus + Spring Boot Scheduler | Reliable async email/notification dispatch |
| Email | Azure Communication Services (ACS) | Native Azure, no third-party dependency |
| Payments | Stripe Billing + Webhooks | Industry standard |
| AI | Anthropic Claude Haiku via REST | Fast, cheap, great for structured extraction |
| iOS | Swift 6 + SwiftUI + AVFoundation | Native, on-device voice, latest concurrency |
| Android | Java 17 + Jetpack Compose + Room | Modern Android with Java (as requested) |
| Container | Docker + Azure Container Apps | Serverless containers, autoscale to zero |
| IaC | Azure Bicep | Native Azure IaC, simpler than Terraform for solo |
| CI/CD | GitHub Actions | Native, free for public repos |
| Docs | MkDocs Material + GitHub Pages | Docs-as-code, zero cost |

---

## 2. Repository Structure & GitHub Setup

### Monorepo Layout

```
ai-planner/
├── .github/
│   ├── ISSUE_TEMPLATE/
│   │   ├── bug_report.yml
│   │   ├── feature_request.yml
│   │   └── task.yml
│   ├── workflows/
│   │   ├── api-ci.yml
│   │   ├── api-cd.yml
│   │   ├── ios-ci.yml
│   │   ├── android-ci.yml
│   │   └── docs.yml
│   ├── PULL_REQUEST_TEMPLATE.md
│   └── dependabot.yml
│
├── api/                          # Spring Boot Modular Monolith
│   ├── src/
│   │   └── main/
│   │       ├── java/com/aiplanner/
│   │       │   ├── AiPlannerApplication.java
│   │       │   ├── config/           # Global config beans
│   │       │   ├── shared/           # Shared kernel (DTOs, exceptions, utils)
│   │       │   ├── module/
│   │       │   │   ├── auth/         # Registration, JWT, OAuth2
│   │       │   │   ├── user/         # User profile, preferences
│   │       │   │   ├── chat/         # AI chat, message persistence
│   │       │   │   ├── planner/      # Events, todos, scheduling
│   │       │   │   ├── notification/ # In-app + email notifications
│   │       │   │   └── billing/      # Stripe subscription
│   │       │   └── infrastructure/
│   │       │       ├── messaging/    # Azure Service Bus
│   │       │       ├── ai/           # Claude Haiku client
│   │       │       └── storage/      # Blob storage (voice memos)
│   │       └── resources/
│   │           ├── application.yml
│   │           ├── application-prod.yml
│   │           └── db/migration/     # Flyway migrations
│   ├── Dockerfile
│   ├── pom.xml
│   └── README.md
│
├── ios/                          # Swift iOS App
│   ├── AiPlanner/
│   │   ├── App/
│   │   ├── Features/
│   │   │   ├── Chat/
│   │   │   ├── VoiceMemo/
│   │   │   └── Subscription/
│   │   ├── Core/
│   │   │   ├── Network/
│   │   │   ├── Storage/
│   │   │   └── Auth/
│   │   └── Resources/
│   ├── AiPlannerTests/
│   └── AiPlanner.xcodeproj
│
├── android/                      # Android App
│   ├── app/
│   │   └── src/main/
│   │       ├── java/com/aiplanner/
│   │       │   ├── ui/           # Jetpack Compose screens
│   │       │   ├── data/         # Repository, Room, Retrofit
│   │       │   ├── domain/       # Use cases
│   │       │   └── service/      # FCM service
│   │       └── res/
│   ├── build.gradle
│   └── README.md
│
├── infra/                        # Azure Bicep IaC
│   ├── main.bicep
│   ├── modules/
│   │   ├── containerApp.bicep
│   │   ├── postgres.bicep
│   │   ├── serviceBus.bicep
│   │   ├── communication.bicep
│   │   └── keyVault.bicep
│   └── parameters/
│       ├── dev.bicepparam
│       └── prod.bicepparam
│
├── docs/                         # MkDocs documentation
│   ├── mkdocs.yml
│   ├── docs/
│   │   ├── index.md
│   │   ├── architecture/
│   │   ├── api/
│   │   ├── ios/
│   │   └── android/
│   └── overrides/
│
└── README.md
```

### Initial Git Setup

```bash
# Create repo (GitHub CLI)
gh repo create ai-planner --private --clone
cd ai-planner

# Branch protection rules (apply via GitHub settings or CLI)
gh api repos/:owner/ai-planner/branches/main/protection \
  --method PUT \
  --field required_status_checks='{"strict":true,"contexts":["api-ci","ios-ci","android-ci"]}' \
  --field enforce_admins=false \
  --field required_pull_request_reviews='{"required_approving_review_count":0}'

# Recommended branch strategy for solo dev:
# main        → production (protected, deploy on merge)
# develop     → integration branch
# feature/*   → feature branches
# fix/*        → bug fix branches
```

---

## 3. GitHub Issues — Templates & Project Board

### `.github/ISSUE_TEMPLATE/bug_report.yml`

```yaml
name: 🐛 Bug Report
description: Something is broken
labels: ["bug", "needs-triage"]
body:
  - type: dropdown
    id: component
    attributes:
      label: Component
      options: [API, iOS, Android, Infrastructure, Docs]
    validations:
      required: true
  - type: textarea
    id: description
    attributes:
      label: Description
      placeholder: What happened? What did you expect?
    validations:
      required: true
  - type: textarea
    id: repro
    attributes:
      label: Steps to Reproduce
      placeholder: "1. Go to...\n2. Click...\n3. See error"
  - type: textarea
    id: logs
    attributes:
      label: Logs / Screenshots
      render: shell
  - type: dropdown
    id: severity
    attributes:
      label: Severity
      options: [Critical, High, Medium, Low]
```

### `.github/ISSUE_TEMPLATE/feature_request.yml`

```yaml
name: ✨ Feature Request
description: Suggest a new feature
labels: ["enhancement"]
body:
  - type: dropdown
    id: component
    attributes:
      label: Component
      options: [API, iOS, Android, Infrastructure, Docs]
    validations:
      required: true
  - type: textarea
    id: problem
    attributes:
      label: Problem / Motivation
      placeholder: What problem does this solve?
    validations:
      required: true
  - type: textarea
    id: solution
    attributes:
      label: Proposed Solution
  - type: textarea
    id: acceptance
    attributes:
      label: Acceptance Criteria
      placeholder: "- [ ] User can...\n- [ ] API returns..."
```

### `.github/ISSUE_TEMPLATE/task.yml`

```yaml
name: ✅ Task
description: Development task or chore
labels: ["task"]
body:
  - type: dropdown
    id: component
    attributes:
      label: Component
      options: [API, iOS, Android, Infrastructure, Docs]
    validations:
      required: true
  - type: textarea
    id: description
    attributes:
      label: What needs to be done?
    validations:
      required: true
  - type: textarea
    id: checklist
    attributes:
      label: Checklist
      placeholder: "- [ ] Step 1\n- [ ] Step 2"
  - type: dropdown
    id: milestone
    attributes:
      label: Milestone
      options: [M1-API, M2-iOS, M3-Android, M4-Launch]
```

### `.github/PULL_REQUEST_TEMPLATE.md`

```markdown
## Summary
<!-- What does this PR do? Link the issue: Closes #123 -->

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Refactor / cleanup
- [ ] Docs / config

## Testing
- [ ] Unit tests added / updated
- [ ] Tested locally

## Checklist
- [ ] Code follows project conventions
- [ ] No hardcoded secrets
- [ ] Migrations are backward-compatible
- [ ] OpenAPI spec updated (if API changed)
```

### GitHub Project Board Setup

```bash
# Create project board via GitHub CLI
gh project create --title "AI Planner MVP" --owner @me

# Add labels
gh label create "M1-API" --color 0075ca
gh label create "M2-iOS" --color e4e669
gh label create "M3-Android" --color d93f0b
gh label create "M4-Launch" --color 0e8a16
gh label create "bug" --color d73a4a
gh label create "enhancement" --color a2eeef
gh label create "task" --color ffffff
gh label create "needs-triage" --color fbca04
```

---

## 4. Architecture Overview

### Modular Monolith — Module Boundaries

```
┌─────────────────────────────────────────────────────────────┐
│                     API Gateway Layer                        │
│              (Spring Security, Rate Limiting)                 │
└────────┬───────────────────────────────────────┬────────────┘
         │                                         │
┌────────▼────────┐                    ┌───────────▼──────────┐
│   AUTH MODULE   │                    │    BILLING MODULE     │
│  - JWT / OAuth2 │                    │  - Stripe Webhooks    │
│  - Refresh flow │                    │  - Subscription state │
└────────┬────────┘                    └───────────┬──────────┘
         │ uses                                    │ gates
┌────────▼────────────────────────────────────────▼──────────┐
│                       CHAT MODULE                            │
│  - Message persistence (append-only log)                     │
│  - Claude Haiku orchestration                                │
│  - Intent extraction (events/todos from messages)            │
│  - Notification trigger evaluation                           │
└────────┬──────────────────────────────────────┬────────────┘
         │ writes to                             │ schedules via
┌────────▼────────┐                    ┌─────────▼────────────┐
│ PLANNER MODULE  │                    │ NOTIFICATION MODULE   │
│  - Events       │                    │  - In-chat messages   │
│  - To-do items  │                    │  - Email (ACS)        │
│  - Recurrence   │                    │  - Push (FCM/APNs)    │
└─────────────────┘                    └──────────────────────┘
         │                                        │
┌────────▼────────────────────────────────────────▼──────────┐
│              INFRASTRUCTURE LAYER (Shared)                   │
│  - Azure Service Bus producer/consumer                       │
│  - PostgreSQL (Spring Data JPA)                              │
│  - Azure Blob (voice memo storage)                           │
│  - Claude Haiku REST client                                  │
└─────────────────────────────────────────────────────────────┘
```

### Key Architectural Decisions

**1. Virtual Threads (Java 21)**
Use `spring.threads.virtual.enabled=true`. This eliminates the thread-per-request bottleneck for I/O-heavy AI calls without reactive programming complexity.

**2. Module Isolation via packages**
Each module exposes only a public API interface. Cross-module calls go through interfaces, never direct repository calls. This is enforced by ArchUnit tests.

**3. Append-only chat log**
Chat messages are never updated or deleted — only appended. Notifications sent by the system are stored as messages with `sender_type = SYSTEM`.

**4. Idempotent notification dispatch**
Notifications carry a `deduplication_key`. Azure Service Bus + database unique constraint prevent double-sending.

---

## 5. Database Design — SQL Schemas

### Flyway Migration: `V1__init.sql`

```sql
-- ============================================================
-- USERS
-- ============================================================
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(320) NOT NULL UNIQUE,
    display_name    VARCHAR(100),
    avatar_url      VARCHAR(500),
    auth_provider   VARCHAR(20) NOT NULL DEFAULT 'EMAIL', -- EMAIL | GOOGLE | APPLE
    password_hash   VARCHAR(255),                         -- NULL for social login
    email_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    tier            VARCHAR(20) NOT NULL DEFAULT 'FREE',  -- FREE | PREMIUM
    timezone        VARCHAR(60) NOT NULL DEFAULT 'UTC',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);

-- ============================================================
-- SUBSCRIPTIONS (Stripe)
-- ============================================================
CREATE TABLE subscriptions (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id               UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    stripe_customer_id    VARCHAR(100) NOT NULL UNIQUE,
    stripe_subscription_id VARCHAR(100) UNIQUE,
    status                VARCHAR(30) NOT NULL, -- active | canceled | past_due | trialing
    plan                  VARCHAR(30) NOT NULL DEFAULT 'PREMIUM_MONTHLY',
    current_period_start  TIMESTAMPTZ,
    current_period_end    TIMESTAMPTZ,
    cancel_at_period_end  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_subscriptions_user ON subscriptions(user_id);

-- ============================================================
-- CHAT CONVERSATIONS (one per user — the single chat)
-- ============================================================
CREATE TABLE conversations (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id)   -- enforces one conversation per user
);

-- ============================================================
-- CHAT MESSAGES (append-only log)
-- ============================================================
CREATE TABLE messages (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id     UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    sender_type         VARCHAR(20) NOT NULL,     -- USER | AI | SYSTEM
    content_type        VARCHAR(20) NOT NULL DEFAULT 'TEXT',  -- TEXT | VOICE_MEMO | NOTIFICATION
    text_content        TEXT,
    voice_memo_url      VARCHAR(500),             -- Azure Blob URL (voice memos)
    voice_transcript    TEXT,                     -- transcribed on-device, stored here
    ai_model            VARCHAR(50),              -- e.g. claude-haiku-4-5 (for AI messages)
    metadata            JSONB,                    -- { "event_ids": [...], "todo_ids": [...] }
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_messages_conversation ON messages(conversation_id, created_at DESC);
CREATE INDEX idx_messages_sender ON messages(conversation_id, sender_type);

-- ============================================================
-- PLANNER EVENTS
-- ============================================================
CREATE TABLE events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    start_at        TIMESTAMPTZ NOT NULL,
    end_at          TIMESTAMPTZ,
    all_day         BOOLEAN NOT NULL DEFAULT FALSE,
    recurrence_rule VARCHAR(200),                 -- RRULE string (RFC 5545)
    source_message_id UUID REFERENCES messages(id), -- which message created this
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE | CANCELLED | DONE
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_events_user_start ON events(user_id, start_at);
CREATE INDEX idx_events_user_status ON events(user_id, status);

-- ============================================================
-- TO-DO ITEMS
-- ============================================================
CREATE TABLE todos (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title             VARCHAR(500) NOT NULL,
    notes             TEXT,
    due_at            TIMESTAMPTZ,
    priority          SMALLINT NOT NULL DEFAULT 2,  -- 1=high 2=medium 3=low
    completed         BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at      TIMESTAMPTZ,
    source_message_id UUID REFERENCES messages(id),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_todos_user_due ON todos(user_id, due_at);
CREATE INDEX idx_todos_user_completed ON todos(user_id, completed);

-- ============================================================
-- SCHEDULED NOTIFICATIONS
-- ============================================================
CREATE TABLE scheduled_notifications (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reference_type      VARCHAR(20) NOT NULL,     -- EVENT | TODO | SYSTEM
    reference_id        UUID,                     -- FK to event or todo
    channel             VARCHAR(20) NOT NULL,     -- IN_CHAT | EMAIL | PUSH
    deduplication_key   VARCHAR(200) NOT NULL UNIQUE,  -- prevents double-send
    scheduled_for       TIMESTAMPTZ NOT NULL,
    sent_at             TIMESTAMPTZ,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING | SENT | FAILED | CANCELLED
    payload             JSONB NOT NULL,           -- { "title": "...", "body": "..." }
    retry_count         SMALLINT NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notif_scheduled_for ON scheduled_notifications(scheduled_for)
    WHERE status = 'PENDING';
CREATE INDEX idx_notif_user ON scheduled_notifications(user_id, status);

-- ============================================================
-- PUSH DEVICE TOKENS
-- ============================================================
CREATE TABLE device_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    platform    VARCHAR(10) NOT NULL,   -- IOS | ANDROID
    token       VARCHAR(512) NOT NULL,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, token)
);

-- ============================================================
-- AI USAGE TRACKING (rate limiting free tier)
-- ============================================================
CREATE TABLE ai_usage (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    month_year      CHAR(7) NOT NULL,             -- format: 2026-03
    message_count   INTEGER NOT NULL DEFAULT 0,
    token_count     INTEGER NOT NULL DEFAULT 0,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, month_year)
);

-- ============================================================
-- TRIGGER: auto-update updated_at
-- ============================================================
CREATE OR REPLACE FUNCTION trigger_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();
CREATE TRIGGER set_updated_at BEFORE UPDATE ON subscriptions
    FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();
CREATE TRIGGER set_updated_at BEFORE UPDATE ON events
    FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();
CREATE TRIGGER set_updated_at BEFORE UPDATE ON todos
    FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();
```

### Flyway Migration: `V2__indexes.sql`

```sql
-- GIN index for JSONB metadata search on messages
CREATE INDEX idx_messages_metadata ON messages USING GIN(metadata);

-- Partial index for pending notifications due in the next 24h (hot query)
CREATE INDEX idx_notif_upcoming ON scheduled_notifications(scheduled_for)
    WHERE status = 'PENDING' AND scheduled_for <= NOW() + INTERVAL '24 hours';
```

---

## 6. Backend — Java Modular Monolith API

### `pom.xml` — Key Dependencies

```xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>3.3.5</version>
</parent>

<properties>
  <java.version>21</java.version>
  <testcontainers.version>1.20.0</testcontainers.version>
</properties>

<dependencies>
  <!-- Web -->
  <dependency><groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId></dependency>

  <!-- Security -->
  <dependency><groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId></dependency>

  <!-- Data -->
  <dependency><groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
  <dependency><groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId></dependency>
  <dependency><groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId></dependency>
  <dependency><groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId></dependency>

  <!-- Validation -->
  <dependency><groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId></dependency>

  <!-- Messaging -->
  <dependency><groupId>com.azure.spring</groupId>
    <artifactId>spring-cloud-azure-starter-servicebus</artifactId>
    <version>5.17.0</version></dependency>

  <!-- HTTP Client (for Claude & Stripe) -->
  <dependency><groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId></dependency>

  <!-- Observability -->
  <dependency><groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId></dependency>
  <dependency><groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-azure-monitor</artifactId>
    <version>1.13.0</version></dependency>

  <!-- OpenAPI Docs -->
  <dependency><groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.6.0</version></dependency>

  <!-- Lombok -->
  <dependency><groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId><optional>true</optional></dependency>

  <!-- MapStruct -->
  <dependency><groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId><version>1.6.0</version></dependency>

  <!-- Test -->
  <dependency><groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
  <dependency><groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId><scope>test</scope></dependency>
  <dependency><groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit-junit5</artifactId>
    <version>1.3.0</version><scope>test</scope></dependency>
</dependencies>
```

### `application.yml`

```yaml
spring:
  application:
    name: ai-planner-api
  threads:
    virtual:
      enabled: true          # Java 21 virtual threads — enable globally
  datasource:
    url: ${DB_URL}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
  jpa:
    hibernate:
      ddl-auto: validate     # Flyway owns schema — JPA only validates
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        jdbc:
          time_zone: UTC
  flyway:
    locations: classpath:db/migration
    baseline-on-migrate: true

  # Azure Service Bus
  cloud:
    azure:
      servicebus:
        connection-string: ${AZURE_SERVICEBUS_CONNECTION_STRING}
        namespace: ${AZURE_SERVICEBUS_NAMESPACE}

server:
  port: 8080
  compression:
    enabled: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized

# Claude Haiku
claude:
  api-key: ${ANTHROPIC_API_KEY}
  model: claude-haiku-4-5-20251001
  max-tokens: 1024
  base-url: https://api.anthropic.com

# Stripe
stripe:
  secret-key: ${STRIPE_SECRET_KEY}
  webhook-secret: ${STRIPE_WEBHOOK_SECRET}
  premium-price-id: ${STRIPE_PREMIUM_PRICE_ID}

# Free tier limit
app:
  free-tier:
    messages-per-month: 50
  notification:
    poller-interval-seconds: 60
```

### Module Structure Pattern

Each module follows this internal pattern (example: `chat` module):

```
module/chat/
├── api/                       # Public contract (interfaces + DTOs)
│   ├── ChatFacade.java        # Single entry point for other modules
│   └── dto/
│       ├── SendMessageRequest.java
│       └── MessageResponse.java
├── domain/                    # Business logic — no Spring annotations
│   ├── Message.java           # JPA Entity
│   ├── Conversation.java
│   └── MessageSender.java     # Enum: USER, AI, SYSTEM
├── application/               # Use cases (Spring @Service)
│   ├── SendMessageUseCase.java
│   ├── GetConversationUseCase.java
│   └── AiResponseOrchestrator.java
├── infrastructure/            # Adapters (JPA, external HTTP)
│   ├── MessageRepository.java
│   ├── ConversationRepository.java
│   └── ClaudeHaikuClient.java
└── web/                       # REST controllers
    └── ChatController.java
```

### Chat Module — Core Use Case

```java
// application/SendMessageUseCase.java
@Service
@RequiredArgsConstructor
public class SendMessageUseCase {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ClaudeHaikuClient claudeClient;
    private final PlannerFacade plannerFacade;      // cross-module via interface
    private final NotificationFacade notificationFacade;
    private final AiUsageService usageService;

    @Transactional
    public MessageResponse send(UUID userId, SendMessageRequest request) {
        usageService.assertWithinFreeTierLimit(userId);

        Conversation conversation = conversationRepository
            .findByUserId(userId)
            .orElseGet(() -> conversationRepository.save(new Conversation(userId)));

        // Persist user message
        Message userMessage = messageRepository.save(Message.userText(
            conversation.getId(), request.getContent()
        ));

        // Build context window (last 20 messages)
        List<Message> history = messageRepository
            .findTop20ByConversationIdOrderByCreatedAtDesc(conversation.getId());

        // Call Claude Haiku
        ClaudeResponse aiResponse = claudeClient.chat(buildSystemPrompt(), history, request.getContent());

        // Persist AI response
        Message aiMessage = messageRepository.save(
            Message.aiResponse(conversation.getId(), aiResponse.getText(), aiResponse.getModel())
        );

        // Extract and schedule planner items (async)
        if (aiResponse.hasExtractedItems()) {
            plannerFacade.scheduleExtractedItems(userId, aiResponse.getExtractedItems(), userMessage.getId());
        }

        usageService.increment(userId);
        return MessageResponse.from(aiMessage);
    }

    private String buildSystemPrompt() {
        return """
            You are a helpful AI planner assistant. When the user mentions events or tasks,
            extract them in JSON format under a key "extracted_items" in your response.
            Always respond naturally first, then include extracted items.
            For notifications, create reminders 15 minutes before events.
            Today is %s UTC.
            """.formatted(LocalDate.now(ZoneOffset.UTC));
    }
}
```

### ArchUnit Test — Enforce Module Boundaries

```java
// test/ArchitectureTest.java
@AnalyzeClasses(packages = "com.aiplanner")
public class ArchitectureTest {

    @ArchTest
    static final ArchRule modules_should_not_access_internals =
        noClasses()
            .that().resideInAPackage("..module.chat..")
            .should().accessClassesThat()
            .resideInAPackage("..module.billing.infrastructure..");

    @ArchTest
    static final ArchRule domain_should_not_depend_on_spring =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework..");

    @ArchTest
    static final ArchRule repositories_only_in_infrastructure =
        classes()
            .that().haveSimpleNameEndingWith("Repository")
            .should().resideInAPackage("..infrastructure..");
}
```

---

## 7. Background Messaging — Email & Notifications

### Notification Dispatch Flow

```
Scheduler (every 60s)
    └─► Query: SELECT * FROM scheduled_notifications
              WHERE status='PENDING' AND scheduled_for <= NOW()
              FOR UPDATE SKIP LOCKED    ← prevents double-processing
        └─► For each notification:
              ├─► channel = IN_CHAT  →  insert Message(sender_type=SYSTEM)
              ├─► channel = EMAIL    →  publish to Service Bus queue "email-dispatch"
              └─► channel = PUSH     →  publish to Service Bus queue "push-dispatch"
```

### Notification Scheduler

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationPoller {

    private final ScheduledNotificationRepository notifRepo;
    private final MessageRepository messageRepository;
    private final ServiceBusTemplate serviceBusTemplate;

    @Scheduled(fixedDelayString = "${app.notification.poller-interval-seconds}000")
    @Transactional
    public void dispatchDueNotifications() {
        List<ScheduledNotification> due = notifRepo.findDueForDispatch(LocalDateTime.now(ZoneOffset.UTC));

        for (ScheduledNotification notif : due) {
            try {
                switch (notif.getChannel()) {
                    case IN_CHAT -> dispatchInChat(notif);
                    case EMAIL   -> serviceBusTemplate.sendAsync("email-dispatch",
                                        ServiceBusMessage.of(notif.toJson()));
                    case PUSH    -> serviceBusTemplate.sendAsync("push-dispatch",
                                        ServiceBusMessage.of(notif.toJson()));
                }
                notif.markSent();
            } catch (Exception e) {
                log.error("Failed to dispatch notification {}", notif.getId(), e);
                notif.incrementRetry();
            }
            notifRepo.save(notif);
        }
    }

    private void dispatchInChat(ScheduledNotification notif) {
        Conversation conv = conversationRepo.findByUserId(notif.getUserId()).orElseThrow();
        messageRepository.save(Message.systemNotification(
            conv.getId(),
            notif.getPayload().getBody(),
            notif.getId()
        ));
    }
}
```

### Email Consumer (Azure Service Bus)

```java
@Component
@RequiredArgsConstructor
public class EmailDispatchConsumer {

    private final EmailService emailService;

    @ServiceBusListener(destination = "email-dispatch")
    public void onEmailMessage(NotificationPayload payload) {
        emailService.send(
            payload.getUserEmail(),
            payload.getTitle(),
            payload.getBody()
        );
    }
}

// Azure Communication Services email sender
@Service
public class AzureEmailService implements EmailService {

    private final EmailClient emailClient;     // injected from ACS SDK
    private final String senderAddress;

    public void send(String to, String subject, String body) {
        EmailMessage message = new EmailMessage()
            .setSenderAddress(senderAddress)
            .setToRecipients(new EmailAddress(to))
            .setSubject(subject)
            .setBodyHtml(renderHtmlTemplate(subject, body));

        emailClient.beginSend(message).getFinalResult();
    }
}
```

---

## 8. Stripe Integration — Premium Subscriptions

### Subscription Flow

```
App                     API                      Stripe
 │                       │                          │
 ├─ POST /billing/checkout ─►                       │
 │                       ├─ Create customer ────────►
 │                       ◄─ customer_id ────────────┤
 │                       ├─ Create checkout session ─►
 │◄── redirect_url ──────┤                          │
 │                       │                          │
 ├─ [User completes payment on Stripe hosted page]  │
 │                       │◄── webhook: checkout.session.completed
 │                       │    → activate subscription
 │                       │    → update user.tier = PREMIUM
```

### Billing Controller

```java
@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
public class BillingController {

    private final StripeService stripeService;

    @PostMapping("/checkout")
    public CheckoutResponse createCheckout(@AuthenticationPrincipal JwtUser user) {
        String sessionUrl = stripeService.createCheckoutSession(user.getId(), user.getEmail());
        return new CheckoutResponse(sessionUrl);
    }

    @PostMapping("/portal")
    public PortalResponse customerPortal(@AuthenticationPrincipal JwtUser user) {
        String portalUrl = stripeService.createPortalSession(user.getId());
        return new PortalResponse(portalUrl);
    }

    // Stripe calls this — no auth, verified by webhook signature
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        stripeService.handleWebhook(payload, sigHeader);
        return ResponseEntity.ok().build();
    }
}
```

### Stripe Service

```java
@Service
@RequiredArgsConstructor
public class StripeService {

    @Value("${stripe.secret-key}") private String secretKey;
    @Value("${stripe.webhook-secret}") private String webhookSecret;
    @Value("${stripe.premium-price-id}") private String premiumPriceId;

    private final SubscriptionRepository subscriptionRepo;
    private final UserRepository userRepository;

    public String createCheckoutSession(UUID userId, String email) {
        Stripe.apiKey = secretKey;
        // Create or retrieve customer
        String customerId = subscriptionRepo.findByUserId(userId)
            .map(Subscription::getStripeCustomerId)
            .orElseGet(() -> createStripeCustomer(email));

        SessionCreateParams params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
            .setCustomer(customerId)
            .addLineItem(SessionCreateParams.LineItem.builder()
                .setPrice(premiumPriceId)
                .setQuantity(1L)
                .build())
            .setSuccessUrl("https://app.aiplanner.io/premium/success")
            .setCancelUrl("https://app.aiplanner.io/premium/cancel")
            .build();

        return Session.create(params).getUrl();
    }

    @Transactional
    public void handleWebhook(String payload, String sigHeader) {
        Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

        switch (event.getType()) {
            case "checkout.session.completed" -> activateSubscription(event);
            case "customer.subscription.deleted" -> cancelSubscription(event);
            case "invoice.payment_failed" -> markPaymentFailed(event);
        }
    }
}
```

---

## 9. Docker — Build & Local Dev

### `api/Dockerfile` (multi-stage, optimized)

```dockerfile
# ─── Stage 1: Build ───────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build
COPY pom.xml .
COPY src ./src

# Download deps separately for layer caching
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw dependency:go-offline -q

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw package -DskipTests -q

# Extract layers for optimal caching
RUN java -Djarmode=layertools -jar target/ai-planner-api.jar extract --destination extracted

# ─── Stage 2: Runtime ─────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Security: non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Copy layers in order of least-to-most-likely-to-change
COPY --from=builder /build/extracted/dependencies/ ./
COPY --from=builder /build/extracted/spring-boot-loader/ ./
COPY --from=builder /build/extracted/snapshot-dependencies/ ./
COPY --from=builder /build/extracted/application/ ./

EXPOSE 8080

# Tuned for container: small heap, G1GC, enable virtual threads
ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:prod}", \
  "org.springframework.boot.loader.launch.JarLauncher"]
```

### `docker-compose.yml` — Local Dev

```yaml
version: "3.9"
services:

  api:
    build:
      context: ./api
      target: runtime
    ports: ["8080:8080"]
    environment:
      SPRING_PROFILES_ACTIVE: local
      DB_URL: jdbc:postgresql://postgres:5432/aiplanner
      DB_USER: aiplanner
      DB_PASSWORD: localpassword
      ANTHROPIC_API_KEY: ${ANTHROPIC_API_KEY}
      STRIPE_SECRET_KEY: ${STRIPE_SECRET_KEY}
      STRIPE_WEBHOOK_SECRET: ${STRIPE_WEBHOOK_SECRET}
      STRIPE_PREMIUM_PRICE_ID: ${STRIPE_PREMIUM_PRICE_ID}
      AZURE_SERVICEBUS_CONNECTION_STRING: ${AZURE_SERVICEBUS_CONNECTION_STRING}
    depends_on:
      postgres:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 5s
      retries: 3

  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: aiplanner
      POSTGRES_USER: aiplanner
      POSTGRES_PASSWORD: localpassword
    ports: ["5432:5432"]
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U aiplanner"]
      interval: 5s
      retries: 5

  # Local Stripe webhook forwarding
  stripe-cli:
    image: stripe/stripe-cli:latest
    command: listen --forward-to api:8080/api/v1/billing/webhook
    environment:
      STRIPE_API_KEY: ${STRIPE_SECRET_KEY}

volumes:
  postgres_data:
```

### `.env.example`

```dotenv
ANTHROPIC_API_KEY=sk-ant-...
STRIPE_SECRET_KEY=sk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...
STRIPE_PREMIUM_PRICE_ID=price_...
AZURE_SERVICEBUS_CONNECTION_STRING=Endpoint=sb://...
```

---

## 10. Azure Infrastructure Setup

### Prerequisites

```bash
# Install tools
brew install azure-cli bicep
az login
az account set --subscription "<YOUR_SUBSCRIPTION_ID>"

# Register providers
az provider register --namespace Microsoft.App
az provider register --namespace Microsoft.Communication
az provider register --namespace Microsoft.ServiceBus
az provider register --namespace Microsoft.DBforPostgreSQL
```

### `infra/main.bicep`

```bicep
targetScope = 'subscription'

param environment string = 'prod'
param location string = 'westeurope'
param appName string = 'aiplanner'

var resourceGroupName = '${appName}-${environment}-rg'

resource rg 'Microsoft.Resources/resourceGroups@2024-03-01' = {
  name: resourceGroupName
  location: location
}

// Key Vault
module keyVault 'modules/keyVault.bicep' = {
  scope: rg
  name: 'keyVault'
  params: { appName: appName, environment: environment, location: location }
}

// PostgreSQL Flexible Server
module postgres 'modules/postgres.bicep' = {
  scope: rg
  name: 'postgres'
  params: {
    appName: appName
    environment: environment
    location: location
    keyVaultName: keyVault.outputs.keyVaultName
  }
}

// Azure Service Bus
module serviceBus 'modules/serviceBus.bicep' = {
  scope: rg
  name: 'serviceBus'
  params: { appName: appName, environment: environment, location: location }
}

// Azure Communication Services
module communication 'modules/communication.bicep' = {
  scope: rg
  name: 'communication'
  params: { appName: appName, environment: environment, location: location }
}

// Container App (API)
module containerApp 'modules/containerApp.bicep' = {
  scope: rg
  name: 'containerApp'
  params: {
    appName: appName
    environment: environment
    location: location
    dbUrl: postgres.outputs.connectionString
    serviceBusConnectionString: serviceBus.outputs.connectionString
    keyVaultName: keyVault.outputs.keyVaultName
  }
}
```

### `infra/modules/postgres.bicep`

```bicep
param appName string
param environment string
param location string
param keyVaultName string

var serverName = '${appName}-${environment}-pg'
var adminUser = 'pgadmin'

resource pgServer 'Microsoft.DBforPostgreSQL/flexibleServers@2023-12-01-preview' = {
  name: serverName
  location: location
  sku: {
    name: 'Standard_B1ms'   // 1 vCore, 2GiB RAM — cost-effective MVP
    tier: 'Burstable'
  }
  properties: {
    version: '16'
    administratorLogin: adminUser
    administratorLoginPassword: adminPassword   // from Key Vault reference
    storage: { storageSizeGB: 32 }
    backup: {
      backupRetentionDays: 7
      geoRedundantBackup: 'Disabled'
    }
    highAvailability: { mode: 'Disabled' }     // enable for production scale
  }
}

resource database 'Microsoft.DBforPostgreSQL/flexibleServers/databases@2023-12-01-preview' = {
  parent: pgServer
  name: 'aiplanner'
}

// Allow Azure services
resource firewallRule 'Microsoft.DBforPostgreSQL/flexibleServers/firewallRules@2023-12-01-preview' = {
  parent: pgServer
  name: 'AllowAzureServices'
  properties: {
    startIpAddress: '0.0.0.0'
    endIpAddress: '0.0.0.0'
  }
}

output connectionString string = 'jdbc:postgresql://${pgServer.properties.fullyQualifiedDomainName}:5432/aiplanner?sslmode=require'
```

### `infra/modules/containerApp.bicep`

```bicep
param appName string
param environment string
param location string
param dbUrl string
param serviceBusConnectionString string
param keyVaultName string

// Container Apps Environment
resource caEnv 'Microsoft.App/managedEnvironments@2024-03-01' = {
  name: '${appName}-${environment}-cae'
  location: location
  properties: {
    appLogsConfiguration: {
      destination: 'log-analytics'
    }
  }
}

// Container App
resource containerApp 'Microsoft.App/containerApps@2024-03-01' = {
  name: '${appName}-${environment}-api'
  location: location
  properties: {
    managedEnvironmentId: caEnv.id
    configuration: {
      ingress: {
        external: true
        targetPort: 8080
        transport: 'auto'
      }
      secrets: [
        { name: 'db-url', value: dbUrl }
        { name: 'servicebus-conn', value: serviceBusConnectionString }
        // Sensitive secrets fetched from Key Vault at deploy time
      ]
    }
    template: {
      scale: {
        minReplicas: 0          // scale to zero when idle — cost savings
        maxReplicas: 3
        rules: [{
          name: 'http-scale'
          http: { metadata: { concurrentRequests: '30' } }
        }]
      }
      containers: [{
        name: 'api'
        image: 'ghcr.io/OWNER/ai-planner-api:latest'
        resources: { cpu: json('0.5'), memory: '1Gi' }
        env: [
          { name: 'SPRING_PROFILES_ACTIVE', value: 'prod' }
          { name: 'DB_URL', secretRef: 'db-url' }
          { name: 'AZURE_SERVICEBUS_CONNECTION_STRING', secretRef: 'servicebus-conn' }
        ]
      }]
    }
  }
}

output fqdn string = containerApp.properties.configuration.ingress.fqdn
```

### Deploy Commands

```bash
# First-time setup
az deployment sub create \
  --location westeurope \
  --template-file infra/main.bicep \
  --parameters infra/parameters/prod.bicepparam

# Update only container app (after image push)
az deployment group create \
  --resource-group aiplanner-prod-rg \
  --template-file infra/modules/containerApp.bicep \
  --parameters environment=prod
```

---

## 11. CI/CD — GitHub Actions Pipeline

### `.github/workflows/api-ci.yml`

```yaml
name: API CI

on:
  pull_request:
    paths: ["api/**"]
  push:
    branches: [develop]
    paths: ["api/**"]

jobs:
  test:
    name: Test & Analyse
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_DB: aiplanner_test
          POSTGRES_USER: test
          POSTGRES_PASSWORD: test
        ports: ["5432:5432"]
        options: --health-cmd pg_isready --health-interval 5s --health-retries 5

    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          java-version: 21
          distribution: temurin
          cache: maven

      - name: Run Tests
        working-directory: api
        env:
          DB_URL: jdbc:postgresql://localhost:5432/aiplanner_test
          DB_USER: test
          DB_PASSWORD: test
        run: ./mvnw verify -Pci

      - name: Upload Coverage
        uses: codecov/codecov-action@v4
        with:
          files: api/target/site/jacoco/jacoco.xml

      - name: SonarCloud Analysis
        working-directory: api
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
        run: ./mvnw sonar:sonar -Dsonar.projectKey=aiplanner-api

  build-image:
    name: Build Docker Image
    needs: test
    runs-on: ubuntu-latest
    outputs:
      image-tag: ${{ steps.meta.outputs.tags }}

    steps:
      - uses: actions/checkout@v4

      - name: Docker meta
        id: meta
        uses: docker/metadata-action@v5
        with:
          images: ghcr.io/${{ github.repository_owner }}/ai-planner-api
          tags: |
            type=sha,prefix=sha-
            type=ref,event=branch

      - uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - uses: docker/setup-buildx-action@v3

      - name: Build & Push
        uses: docker/build-push-action@v6
        with:
          context: api
          push: true
          tags: ${{ steps.meta.outputs.tags }}
          cache-from: type=gha
          cache-to: type=gha,mode=max
          platforms: linux/amd64,linux/arm64
```

### `.github/workflows/api-cd.yml`

```yaml
name: API CD — Deploy to Azure

on:
  push:
    branches: [main]
    paths: ["api/**", "infra/**"]

permissions:
  id-token: write     # OIDC → no long-lived secrets
  contents: read
  packages: write

jobs:
  deploy:
    name: Deploy to Azure Container Apps
    runs-on: ubuntu-latest
    environment: production

    steps:
      - uses: actions/checkout@v4

      - name: Azure Login (OIDC)
        uses: azure/login@v2
        with:
          client-id: ${{ secrets.AZURE_CLIENT_ID }}
          tenant-id: ${{ secrets.AZURE_TENANT_ID }}
          subscription-id: ${{ secrets.AZURE_SUBSCRIPTION_ID }}

      - name: Build & Push Image
        uses: docker/build-push-action@v6
        with:
          context: api
          push: true
          tags: ghcr.io/${{ github.repository_owner }}/ai-planner-api:${{ github.sha }}

      - name: Deploy Bicep Infrastructure
        uses: azure/arm-deploy@v2
        with:
          scope: subscription
          subscriptionId: ${{ secrets.AZURE_SUBSCRIPTION_ID }}
          region: westeurope
          template: infra/main.bicep
          parameters: infra/parameters/prod.bicepparam

      - name: Update Container App Image
        run: |
          az containerapp update \
            --name aiplanner-prod-api \
            --resource-group aiplanner-prod-rg \
            --image ghcr.io/${{ github.repository_owner }}/ai-planner-api:${{ github.sha }}

      - name: Health Check
        run: |
          sleep 30
          curl -f https://aiplanner-prod-api.azurecontainerapps.io/actuator/health
```

### `.github/workflows/ios-ci.yml`

```yaml
name: iOS CI

on:
  pull_request:
    paths: ["ios/**"]

jobs:
  build-test:
    runs-on: macos-15
    steps:
      - uses: actions/checkout@v4

      - name: Select Xcode
        run: sudo xcode-select -s /Applications/Xcode_16.2.app

      - name: Build & Test
        run: |
          xcodebuild test \
            -project ios/AiPlanner.xcodeproj \
            -scheme AiPlanner \
            -sdk iphonesimulator \
            -destination 'platform=iOS Simulator,name=iPhone 16' \
            -resultBundlePath TestResults.xcresult \
            | xcpretty

      - name: Upload Test Results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: ios-test-results
          path: TestResults.xcresult
```

### `.github/dependabot.yml`

```yaml
version: 2
updates:
  - package-ecosystem: maven
    directory: /api
    schedule: { interval: weekly }
    open-pull-requests-limit: 5

  - package-ecosystem: swift
    directory: /ios
    schedule: { interval: weekly }

  - package-ecosystem: gradle
    directory: /android
    schedule: { interval: weekly }

  - package-ecosystem: github-actions
    directory: /
    schedule: { interval: weekly }
```

---

## 12. iOS App — Swift

### Architecture: MVVM + Clean Architecture

```
ios/AiPlanner/
├── App/
│   └── AiPlannerApp.swift          # @main, DI container setup
├── Core/
│   ├── Network/
│   │   ├── APIClient.swift         # URLSession + async/await
│   │   ├── AuthInterceptor.swift
│   │   └── Endpoints.swift
│   ├── Auth/
│   │   ├── AuthService.swift
│   │   └── KeychainStore.swift
│   └── Persistence/
│       └── LocalCache.swift        # SwiftData for offline messages
└── Features/
    ├── Chat/
    │   ├── ChatView.swift           # SwiftUI
    │   ├── ChatViewModel.swift      # @Observable
    │   ├── MessageBubble.swift
    │   └── VoiceMemoButton.swift
    └── Settings/
        ├── SettingsView.swift
        └── PremiumUpgradeView.swift
```

### `ChatViewModel.swift`

```swift
import Foundation
import AVFoundation
import SwiftUI

@Observable
final class ChatViewModel {
    var messages: [MessageResponse] = []
    var inputText: String = ""
    var isRecording = false
    var isLoading = false
    var error: String?

    private let apiClient: APIClient
    private var audioRecorder: AVAudioRecorder?
    private var recordingURL: URL?

    init(apiClient: APIClient = .shared) {
        self.apiClient = apiClient
    }

    // MARK: — Load history

    func loadMessages() async {
        do {
            messages = try await apiClient.get("/api/v1/chat/messages")
        } catch {
            self.error = error.localizedDescription
        }
    }

    // MARK: — Send text message

    func sendMessage() async {
        guard !inputText.trimmingCharacters(in: .whitespaces).isEmpty else { return }
        let text = inputText
        inputText = ""
        isLoading = true
        defer { isLoading = false }

        // Optimistic UI — add user message immediately
        messages.append(.userOptimistic(text))

        do {
            let response: MessageResponse = try await apiClient.post(
                "/api/v1/chat/messages",
                body: SendMessageRequest(content: text)
            )
            // Replace optimistic with real AI response
            messages.append(response)
        } catch {
            self.error = "Failed to send: \(error.localizedDescription)"
        }
    }

    // MARK: — Voice memo

    func startRecording() {
        let session = AVAudioSession.sharedInstance()
        try? session.setCategory(.record, mode: .default)
        try? session.setActive(true)

        recordingURL = FileManager.default.temporaryDirectory
            .appendingPathComponent(UUID().uuidString + ".m4a")

        let settings: [String: Any] = [
            AVFormatIDKey: kAudioFormatMPEG4AAC,
            AVSampleRateKey: 44100,
            AVNumberOfChannelsKey: 1
        ]

        audioRecorder = try? AVAudioRecorder(url: recordingURL!, settings: settings)
        audioRecorder?.record()
        isRecording = true
    }

    func stopRecordingAndSend() async {
        audioRecorder?.stop()
        isRecording = false

        guard let url = recordingURL else { return }
        isLoading = true
        defer { isLoading = false }

        do {
            // Transcribe on-device using Speech framework
            let transcript = try await SpeechTranscriber().transcribe(url: url)

            // Upload voice memo + transcript
            let response: MessageResponse = try await apiClient.postMultipart(
                "/api/v1/chat/voice-memo",
                fileURL: url,
                fields: ["transcript": transcript]
            )
            messages.append(response)
        } catch {
            self.error = "Voice memo failed: \(error.localizedDescription)"
        }
    }
}
```

### `ChatView.swift`

```swift
import SwiftUI

struct ChatView: View {
    @State private var viewModel = ChatViewModel()
    @State private var scrollProxy: ScrollViewProxy?

    var body: some View {
        VStack(spacing: 0) {
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(viewModel.messages) { message in
                            MessageBubble(message: message)
                                .id(message.id)
                        }
                    }
                    .padding()
                }
                .onChange(of: viewModel.messages.count) {
                    withAnimation {
                        proxy.scrollTo(viewModel.messages.last?.id)
                    }
                }
            }

            Divider()

            // Input bar
            HStack(spacing: 12) {
                TextField("Ask your AI planner...", text: $viewModel.inputText, axis: .vertical)
                    .textFieldStyle(.roundedBorder)
                    .lineLimit(1...5)

                // Voice memo button
                Button {
                    Task {
                        if viewModel.isRecording {
                            await viewModel.stopRecordingAndSend()
                        } else {
                            viewModel.startRecording()
                        }
                    }
                } label: {
                    Image(systemName: viewModel.isRecording ? "stop.circle.fill" : "mic.circle.fill")
                        .font(.title2)
                        .foregroundStyle(viewModel.isRecording ? .red : .accentColor)
                }

                Button {
                    Task { await viewModel.sendMessage() }
                } label: {
                    Image(systemName: "arrow.up.circle.fill")
                        .font(.title2)
                }
                .disabled(viewModel.inputText.trimmingCharacters(in: .whitespaces).isEmpty
                          || viewModel.isLoading)
            }
            .padding()
        }
        .task { await viewModel.loadMessages() }
        .alert("Error", isPresented: .constant(viewModel.error != nil)) {
            Button("OK") { viewModel.error = nil }
        } message: {
            Text(viewModel.error ?? "")
        }
    }
}
```

### Push Notification Setup (iOS)

```swift
// AppDelegate.swift
import UserNotifications
import UIKit

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {

    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        return true
    }

    func application(_ application: UIApplication,
                     didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        let token = deviceToken.map { String(format: "%02.2hhx", $0) }.joined()
        Task { try? await APIClient.shared.registerDeviceToken(token, platform: "IOS") }
    }
}
```

### App Store Distribution

```bash
# 1. Create App ID in Apple Developer portal
# 2. Create provisioning profiles (Distribution)
# 3. Configure in Xcode: Signing & Capabilities
# 4. Archive:
xcodebuild archive \
  -project ios/AiPlanner.xcodeproj \
  -scheme AiPlanner \
  -archivePath AiPlanner.xcarchive

# 5. Export IPA:
xcodebuild -exportArchive \
  -archivePath AiPlanner.xcarchive \
  -exportPath AiPlannerIPA \
  -exportOptionsPlist ExportOptions.plist

# 6. Upload to App Store Connect:
xcrun altool --upload-app -f AiPlannerIPA/AiPlanner.ipa \
  -u "$APPLE_ID" -p "$APP_SPECIFIC_PASSWORD"
```

---

## 13. Android App — Java / Jetpack Compose

### Architecture: MVVM + Repository + Room

```
android/app/src/main/java/com/aiplanner/
├── ui/
│   ├── MainActivity.java
│   ├── theme/
│   │   ├── Theme.java
│   │   └── Color.java
│   └── screens/
│       ├── chat/
│       │   ├── ChatScreen.java      # Composable
│       │   └── ChatViewModel.java   # extends ViewModel
│       └── settings/
│           └── SettingsScreen.java
├── data/
│   ├── repository/
│   │   └── ChatRepository.java
│   ├── remote/
│   │   ├── ApiService.java          # Retrofit interface
│   │   └── ApiClient.java           # Retrofit builder
│   ├── local/
│   │   ├── AppDatabase.java         # Room
│   │   └── MessageDao.java
│   └── model/
│       ├── Message.java
│       └── SendMessageRequest.java
└── service/
    └── PlannerFirebaseService.java   # FCM
```

### `ChatViewModel.java`

```java
public class ChatViewModel extends ViewModel {

    private final ChatRepository repository;
    private final MutableStateFlow<List<Message>> _messages = new MutableStateFlow<>(List.of());
    public final StateFlow<List<Message>> messages = _messages;

    private final MutableStateFlow<Boolean> _isLoading = new MutableStateFlow<>(false);
    public final StateFlow<Boolean> isLoading = _isLoading;

    public ChatViewModel(ChatRepository repository) {
        this.repository = repository;
        loadMessages();
    }

    public void loadMessages() {
        ViewModelKt.viewModelScope(this).launch(Dispatchers.getIO(), CoroutineStart.DEFAULT,
            (scope, continuation) -> {
                _isLoading.setValue(true);
                try {
                    List<Message> msgs = repository.getMessages();
                    _messages.setValue(msgs);
                } catch (Exception e) {
                    // handle error
                } finally {
                    _isLoading.setValue(false);
                }
                return Unit.INSTANCE;
            }
        );
    }

    public void sendMessage(String text) {
        // Optimistic update
        List<Message> current = new ArrayList<>(_messages.getValue());
        current.add(Message.userOptimistic(text));
        _messages.setValue(current);

        repository.sendMessage(text)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                response -> {
                    List<Message> updated = new ArrayList<>(_messages.getValue());
                    updated.add(response);
                    _messages.setValue(updated);
                },
                error -> { /* handle */ }
            );
    }
}
```

### `ChatScreen.java` (Jetpack Compose)

```java
@Composable
public static void ChatScreen(ChatViewModel viewModel) {
    List<Message> messages = viewModel.messages.collectAsStateWithLifecycle().getValue();
    boolean isLoading = viewModel.isLoading.collectAsStateWithLifecycle().getValue();
    MutableState<String> inputText = remember(mutableStateOf(""));

    LazyListState listState = rememberLazyListState();

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(horizontal(16.dp)),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, keyExtractor = m -> m.getId()) { message ->
                MessageBubble(message);
            };
        };

        HorizontalDivider();

        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText.getValue(),
                onValueChange = v -> inputText.setValue(v),
                modifier = Modifier.weight(1f),
                placeholder = () -> Text("Ask your AI planner..."),
                maxLines = 5
            );
            Spacer(Modifier.width(8.dp));
            IconButton(
                onClick = () -> {
                    viewModel.sendMessage(inputText.getValue());
                    inputText.setValue("");
                },
                enabled = !inputText.getValue().isBlank() && !isLoading
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, "Send");
            };
        };
    };
}
```

### `build.gradle` (app level)

```groovy
android {
    compileSdk 35
    defaultConfig {
        minSdk 26
        targetSdk 35
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
    buildFeatures { compose true }
    composeOptions { kotlinCompilerExtensionVersion '1.5.14' }
}

dependencies {
    // Compose
    implementation platform('androidx.compose:compose-bom:2024.12.01')
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.material3:material3'
    implementation 'androidx.activity:activity-compose:1.9.3'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7'

    // Network
    implementation 'com.squareup.retrofit2:retrofit:2.11.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.11.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'

    // Local DB
    implementation 'androidx.room:room-runtime:2.6.1'
    annotationProcessor 'androidx.room:room-compiler:2.6.1'

    // Push
    implementation 'com.google.firebase:firebase-messaging:24.1.0'

    // Auth token storage
    implementation 'androidx.security:security-crypto:1.1.0-alpha06'
}
```

### Google Play Distribution

```bash
# 1. Create keystore
keytool -genkey -v -keystore release.jks -alias aiplanner \
  -keyalg RSA -keysize 2048 -validity 10000

# 2. Build release APK / AAB
./gradlew bundleRelease

# 3. Sign
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore release.jks app/build/outputs/bundle/release/app-release.aab aiplanner

# 4. Upload via Google Play Console or fastlane
# Recommended: use fastlane supply for CI/CD
```

---

## 14. Documentation — Docs as Code

### Setup MkDocs Material

```bash
pip install mkdocs-material mkdocstrings
```

### `docs/mkdocs.yml`

```yaml
site_name: AI Planner — Developer Docs
site_url: https://OWNER.github.io/ai-planner
repo_url: https://github.com/OWNER/ai-planner
repo_name: OWNER/ai-planner

theme:
  name: material
  palette:
    - scheme: default
      primary: deep purple
      accent: purple
      toggle: { icon: material/brightness-7, name: Switch to dark mode }
    - scheme: slate
      primary: deep purple
      toggle: { icon: material/brightness-4, name: Switch to light mode }
  features:
    - navigation.tabs
    - navigation.sections
    - navigation.top
    - search.suggest
    - content.code.copy

nav:
  - Home: index.md
  - Architecture:
      - Overview: architecture/overview.md
      - Database: architecture/database.md
      - Modules: architecture/modules.md
  - API:
      - Authentication: api/auth.md
      - Chat: api/chat.md
      - Planner: api/planner.md
      - Billing: api/billing.md
  - iOS: ios/setup.md
  - Android: android/setup.md
  - Infrastructure: infra/azure.md
  - Contributing: contributing.md

plugins:
  - search
  - tags

markdown_extensions:
  - pymdownx.highlight: { anchor_linenums: true }
  - pymdownx.superfences:
      custom_fences:
        - name: mermaid
          class: mermaid
          format: !!python/name:pymdownx.superfences.fence_code_format
  - admonition
  - pymdownx.details
  - pymdownx.tabbed: { alternate_style: true }
```

### `.github/workflows/docs.yml`

```yaml
name: Deploy Docs

on:
  push:
    branches: [main]
    paths: ["docs/**"]

permissions:
  contents: write

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - uses: actions/setup-python@v5
        with: { python-version: "3.12" }

      - run: pip install mkdocs-material

      - run: mkdocs gh-deploy --force --config-file docs/mkdocs.yml
```

### OpenAPI → Docs Automation

Add to `api-cd.yml` after deploy:

```yaml
- name: Generate & Commit OpenAPI Spec
  run: |
    sleep 10   # wait for container to be healthy
    curl -s https://aiplanner-prod-api.azurecontainerapps.io/v3/api-docs \
      -o docs/docs/api/openapi.json
    git config user.name "github-actions[bot]"
    git config user.email "github-actions[bot]@users.noreply.github.com"
    git add docs/docs/api/openapi.json
    git diff --staged --quiet || git commit -m "chore: update OpenAPI spec [skip ci]"
    git push
```

---

## 15. Milestones & Solo Execution Order

```
MILESTONE 1 — API Foundation (Weeks 1–3)
├── [ ] Set up monorepo + GitHub repo + branch protection
├── [ ] GitHub issue templates + project board
├── [ ] Spring Boot project skeleton (modular structure)
├── [ ] Flyway migrations (V1 + V2)
├── [ ] Auth module (JWT, register, login)
├── [ ] User module (profile)
├── [ ] Docker + docker-compose local dev
├── [ ] Unit tests + ArchUnit boundary tests
└── [ ] API CI pipeline (GitHub Actions)

MILESTONE 2 — Azure Infrastructure (Week 4)
├── [ ] Azure account + resource group setup
├── [ ] Bicep IaC (postgres, service bus, container apps)
├── [ ] Azure Communication Services (email)
├── [ ] Key Vault secrets + OIDC GitHub federation
├── [ ] CD pipeline (deploy on main merge)
└── [ ] Health check + basic monitoring

MILESTONE 3 — Core Features (Weeks 5–7)
├── [ ] Chat module (message persistence + context window)
├── [ ] Claude Haiku integration + intent extraction
├── [ ] Planner module (events + todos)
├── [ ] Notification scheduler + Service Bus consumers
├── [ ] Push notification registration endpoint
├── [ ] Stripe billing (checkout + webhooks)
└── [ ] AI usage tracking (free tier limit)

MILESTONE 4 — iOS App (Weeks 8–10)
├── [ ] Xcode project setup + SwiftUI navigation
├── [ ] API client (URLSession + async/await)
├── [ ] Auth screens (register + login)
├── [ ] Chat view + ChatViewModel
├── [ ] Voice memo recording + on-device transcription
├── [ ] Push notification registration (APNs)
├── [ ] Premium upgrade screen (WebView → Stripe)
├── [ ] iOS CI pipeline
└── [ ] TestFlight beta → App Store submission

MILESTONE 5 — Android App (Weeks 11–13)
├── [ ] Android project setup + Jetpack Compose
├── [ ] Retrofit + Room setup
├── [ ] Auth screens
├── [ ] Chat screen + ViewModel
├── [ ] Voice recording (MediaRecorder)
├── [ ] FCM push notifications
├── [ ] Premium upgrade (in-app WebView)
├── [ ] Android CI pipeline
└── [ ] Internal testing → Google Play submission

MILESTONE 6 — Launch Prep (Week 14)
├── [ ] MkDocs documentation
├── [ ] Load test API (k6 or Gatling)
├── [ ] Security audit (OWASP checklist)
├── [ ] Privacy policy + terms of service
├── [ ] App Store / Play Store listings
└── [ ] Public launch 🚀
```

---

> **💡 Solo Founder Tips**
>
> - Use **GitHub Copilot** or Claude Code CLI inside your IDE to accelerate boilerplate
> - Keep the free tier generous enough to acquire users, then convert with Stripe
> - Azure Container Apps scale-to-zero means $0 cost when idle — perfect for MVP
> - Start iOS first if you are primarily an iPhone user — dogfood your own product
> - Keep the notification poller simple before adding Service Bus — a scheduled task is enough for MVP scale
> - Commit to trunk-based development (short-lived branches) to avoid merge hell when working solo
