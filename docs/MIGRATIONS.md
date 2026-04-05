# Database Migrations Guide

Both **Auth Server** and **API Server** use **Liquibase** for automatic database schema management. Migrations run automatically when containers start—no manual steps required.

## 🔄 How Migrations Work

### Automatic Execution on Container Restart

When a container starts:

1. **Liquibase** connects to PostgreSQL and checks the `DATABASECHANGELOG` table
2. **Compares** executed migrations vs. changeset files in the classpath
3. **Runs** only new/unapplied migrations (idempotent—safe to restart)
4. **Tracks** all executed migrations to prevent re-running

**Result**: Each container restart applies any new migrations automatically. No downtime needed.

---

## 📂 Migration Files

### Auth Server

Location: `auth/src/main/resources/db/changelog/`

- **db.changelog-master.xml** — Entry point that references all changesets
- **changesets/01-initial-schema.sql** — Users table (email, password, roles)
- **changesets/02-oauth2-schema.sql** — Spring OAuth2 tables (clients, tokens, consent)

### API Server

Location: `api/src/main/resources/db/changelog/`

- **db.changelog-master.yaml** — Existing API schema changesets

---

## ⚙️ Configuration

Both apps are configured to run Liquibase automatically:

### Auth Server (`auth/src/main/resources/application.yml`)

```yaml
spring:
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.xml
    enabled: true  # Automatic on startup
```

### API Server (`api/src/main/resources/application.yml`)

```yaml
spring:
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.yaml
    enabled: true  # Automatic on startup
```

---

## 🚀 Running Containers

### Prerequisites

PostgreSQL running with two databases:

```bash
psql -U postgres
CREATE DATABASE aiplannerauth;
CREATE DATABASE aiplanner;
```

### Start Containers

```bash
# Build auth image
docker build -t ai-planner-auth:latest ./auth

# Build api image  
docker build -t ai-planner-api:latest ./api

# Run auth server (container automatically runs migrations)
docker run -d --name auth-server \
  -e DB_HOST=postgres-host \
  -e DB_NAME=aiplannerauth \
  -e DB_USER=postgres \
  -e DB_PASS=secret \
  -p 9000:9000 \
  ai-planner-auth:latest

# Run api server (container automatically runs migrations)
docker run -d --name api-server \
  -e DB_HOST=postgres-host \
  -e DB_NAME=aiplanner \
  -e DB_USER=postgres \
  -e DB_PASS=secret \
  -e AUTH_SERVER_URI=http://auth-server:9000 \
  -p 8080:8080 \
  ai-planner-api:latest
```

**Migrations run automatically on startup.**

### Redeployment (Fetch Latest Image & Restart)

```bash
# Stop existing containers
docker stop auth-server api-server

# Remove existing containers
docker rm auth-server api-server

# Pull latest images
docker pull registry/ai-planner-auth:latest
docker pull registry/ai-planner-api:latest

# Start containers again
docker run -d --name auth-server ...
docker run -d --name api-server ...
```

**New migrations execute automatically on container restart.**

---

## 🌐 Azure Container Apps Deployment

For production on Azure, set environment variables in the Container App:

### Auth Server Container App

```
Name: auth-server
Image: your-registry.azurecr.io/ai-planner-auth:latest

Environment Variables:
  DB_HOST: your-postgres.postgres.database.azure.com
  DB_PORT: 5432
  DB_NAME: aiplannerauth
  DB_USER: adminuser
  DB_PASS: ${DB_PASSWORD_SECRET}
  SPRING_LIQUIBASE_ENABLED: true
  SPRING_JPA_HIBERNATE_DDL_AUTO: validate
```

### API Server Container App

```
Name: api-server
Image: your-registry.azurecr.io/ai-planner-api:latest

Environment Variables:
  DB_HOST: your-postgres.postgres.database.azure.com
  DB_PORT: 5432
  DB_NAME: aiplanner
  DB_USER: adminuser
  DB_PASS: ${DB_PASSWORD_SECRET}
  AUTH_SERVER_URI: https://auth-server.your-domain.com
  SPRING_LIQUIBASE_ENABLED: true
  SPRING_JPA_HIBERNATE_DDL_AUTO: validate
```

### Important: Create Databases First

Before deploying containers, create the databases:

```bash
# Using Azure PostgreSQL Server
psql -h your-postgres.postgres.database.azure.com -U adminuser@your-server

# Then in psql:
CREATE DATABASE aiplannerauth;
CREATE DATABASE aiplanner;
```

---

## 🛠️ Manual Migration Management

### Check Migration Status

```bash
# Connect to auth database
psql -U postgres -d aiplannerauth

# View applied migrations
SELECT * FROM databasechangelog ORDER BY orderexecuted DESC;

# View migrations pending
-- Compare with files in auth/src/main/resources/db/changelog/changesets/
```

### Revert a Migration (Advanced)

Liquibase doesn't support rolling back SQL migrations by default. If you need to revert:

1. **Option 1**: Create a new changeset that undoes the changes
   ```sql
   -- changesets/03-revert-something.sql
   DROP TABLE IF EXISTS some_table;
   ```

2. **Option 2**: Reset the database
   ```bash
   docker-compose down -v
   docker-compose up -d
   ```

### Force Migration Execution

⚠️ **Only in development**:

```bash
# Update changelog tracking (dangerous—use only if migrations failed to record)
psql -U postgres -d aiplannerauth -c \
  "DELETE FROM databasechangelog WHERE id = '02-oauth2-schema';"

# Restart app to re-run migration
docker-compose restart auth-server
```

---

## ⚙️ Configuration Options

### Environment Variables

Both apps accept these Liquibase settings:

```properties
# Enable/disable Liquibase (default: true)
SPRING_LIQUIBASE_ENABLED=true

# Fail if migrations are pending (recommended for production)
SPRING_LIQUIBASE_FAIL_ON_UPDATED_SQL=true

# Contexts (run only specific labeled migrations)
SPRING_LIQUIBASE_CONTEXTS=production

# Default schema
SPRING_LIQUIBASE_DEFAULT_SCHEMA=public
```

### application.yml / application.properties

**Auth Server** (`auth/src/main/resources/application.yml`):
```yaml
spring:
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.xml
    enabled: true
```

**API Server** (`api/src/main/resources/application.properties`):
```properties
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yaml
spring.liquibase.enabled=true
```

---

## 📊 Adding New Migrations

### For Auth Server

1. Create a new SQL file in `auth/src/main/resources/db/changelog/changesets/`:
   ```
   03-add-user-profile.sql
   ```

2. Add it to the master changelog (`db.changelog-master.xml`):
   ```xml
   <include file="db/changelog/changesets/03-add-user-profile.sql"/>
   ```

3. Restart the auth server:
   ```bash
   docker-compose restart auth-server
   ```

### For API Server

1. Create a new YAML changeset in `api/src/main/resources/db/changelog/`:
   ```yaml
   - changeSet:
       id: add-messages-table
       author: dev
       changes:
         - sql:
             sql: CREATE TABLE messages (...);
   ```

2. Restart the API server:
   ```bash
   docker-compose restart api-server
   ```

---

## ✅ Health Check & Verification

### Check if Migrations Ran Successfully

```bash
# View auth DB tables
docker-compose exec postgres psql -U postgres -d aiplannerauth -c "\dt"

# Output should show:
# databasechangelog
# databasechangeloglock
# users
# oauth2_registered_client
# oauth2_authorization
# oauth2_authorization_consent
```

### Monitor Migration Logs

```bash
# Real-time auth logs
docker-compose logs -f auth-server | grep -i liquibase

# Real-time API logs
docker-compose logs -f api-server | grep -i liquibase
```

### Handle Migration Failures

If migrations fail during startup:

```bash
# Check the error
docker-compose logs auth-server | tail -50

# Potential issues:
# 1. Database doesn't exist → Create it manually
# 2. Connection refused → Ensure PostgreSQL is healthy
# 3. Permission denied → Check DB user privileges
# 4. Table already exists → Check DATABASECHANGELOG for duplicates

# Reset and retry
docker-compose down -v
docker-compose up -d
```

---

## 🔒 Production Best Practices

1. **Set `SPRING_JPA_HIBERNATE_DDL_AUTO: validate`**
   - Prevents accidental schema changes
   - Apps fail if DB doesn't match code

2. **Set `SPRING_LIQUIBASE_FAIL_ON_UPDATED_SQL: true`**
   - Fails if migration SQL differs from recorded version
   - Detects tampering or misconfiguration

3. **Use Managed PostgreSQL** (Azure Database for PostgreSQL)
   - Automatic backups
   - Automated failover
   - Better security

4. **Run migrations before deploying**
   ```bash
   # In CI/CD pipeline
   docker run --rm \
     -e DB_HOST=postgres.azure.com \
     -e DB_NAME=aiplannerauth \
     your-registry/ai-planner-auth:latest
   ```

5. **Monitor DATABASECHANGELOG**
   ```sql
   SELECT * FROM databasechangelog 
   WHERE dateexecuted > NOW() - INTERVAL 1 DAY;
   ```

---

## 📝 Troubleshooting

| Issue | Cause | Solution |
|-------|-------|----------|
| "DATABASECHANGELOG doesn't exist" | First app startup | Normal—Liquibase creates it automatically |
| "Liquibase lock timeout" | Concurrent migration attempts | Only one app should run migrations at a time |
| "Migration not found" | Wrong file path or XML reference | Check `db.changelog-master.xml` includes |
| "Table already exists" | Migration ran twice | Check DATABASECHANGELOG for duplicates |
| Migrate but doesn't start | SQL syntax error | Check logs, verify SQL with `psql` |

---

## 🎯 Summary

- **Automatic**: Migrations run on app startup
- **Idempotent**: Safe to restart containers
- **Tracked**: `DATABASECHANGELOG` table prevents re-runs
- **Declarative**: Add new changesets, Liquibase handles execution
- **Production-ready**: Supports managed databases and container orchestration
