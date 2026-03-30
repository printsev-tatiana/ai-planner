# GitHub Actions Setup Guide

## Required Secrets

Set these secrets in your GitHub repository settings (`Settings > Secrets and variables > Actions`):

### 1. Azure Credentials
**Secret Name:** `AZURE_CREDENTIALS`

Create a service principal:
```bash
az ad sp create-for-rbac \
  --name github-actions-ai-planner \
  --role contributor \
  --scopes /subscriptions/{SUBSCRIPTION_ID} \
  --json-auth
```

**Value:** Paste the entire JSON output as the secret value.

### 2. Azure Database Password
**Secret Name:** `AZURE_DB_PASSWORD`

**Value:** The SQL Server `sa` user password (e.g., `YourAzurePasswordHere123!`)

### 3. Azure Database URL
**Secret Name:** `AZURE_DB_URL`

**Value:** Full JDBC connection string
```
jdbc:sqlserver://ai-planner-sqlserver.database.windows.net:1433;database=aiplanner;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;
```

### 4. Azure Database Username
**Secret Name:** `AZURE_DB_USERNAME`

**Value:** `sqladmin@ai-planner-sqlserver`

### 5. Azure App Name
**Secret Name:** `AZURE_APP_NAME`

**Value:** The Azure App Service name (e.g., `ai-planner-app`)

---

## Setup Steps

### Step 1: Create Azure Service Principal
```bash
# Login to Azure
az login

# Set your subscription ID
SUBSCRIPTION_ID=$(az account show --query id -o tsv)

# Create service principal
az ad sp create-for-rbac \
  --name github-actions-ai-planner \
  --role contributor \
  --scopes /subscriptions/$SUBSCRIPTION_ID \
  --json-auth
```

### Step 2: Copy the JSON Output
The command will output something like:
```json
{
  "clientId": "xxx",
  "clientSecret": "xxx",
  "subscriptionId": "xxx",
  "tenantId": "xxx",
  "activeDirectoryEndpointUrl": "https://login.microsoftonline.com",
  "resourceManagerEndpointUrl": "https://management.azure.com/",
  "activeDirectoryGraphResourceId": "https://graph.microsoft.com/",
  "sqlManagementEndpointUrl": "https://management.core.windows.net:8443/",
  "galleryEndpointUrl": "https://gallery.azure.com/",
  "managementEndpointUrl": "https://management.core.windows.net/"
}
```

### Step 3: Add Secrets to GitHub
1. Go to your GitHub repository
2. Click `Settings` > `Secrets and variables` > `Actions`
3. Click `New repository secret`
4. Add each secret with appropriate values

### Step 4: Verify Workflow
1. Push a change to `develop` branch to trigger the test workflow
2. Push a change to `main` branch to trigger both test and deploy workflows
3. Check Actions tab to monitor workflow execution

---

## Workflow Files

- `.github/workflows/migration.yml` - Main migration and deployment workflow

### Workflow Triggers
- **Test migrations**: Push/PR to `develop` or `main` branch
- **Deploy to Azure**: Push to `main` branch only

### Workflow Steps
1. **Migrate** - Tests migrations against local SQL Server in Docker
2. **Deploy to Azure** - Runs migrations and deploys to Azure App Service (main only)

---

## Manual Deployment (without GitHub Actions)

### Deploy to Azure Manually
```bash
# Login to Azure
az login

# Build the application
cd api/aiplanner
mvn clean package

# Run migrations
mvn liquibase:update \
  -Dspring.datasource.url="YOUR_AZURE_DB_URL" \
  -Dspring.datasource.username="YOUR_USERNAME" \
  -Dspring.datasource.password="YOUR_PASSWORD"

# Deploy to App Service
az webapp deployment source config-zip \
  --resource-group ai-planner-rg \
  --name ai-planner-app \
  --src target/aiplanner-0.0.1-SNAPSHOT.jar
```

---

## Troubleshooting

### Workflow Fails at Migration Step
1. Check `AZURE_DB_*` secrets are correctly set
2. Verify Azure SQL Database exists and is accessible
3. Check firewall rules allow GitHub runners
4. Review workflow logs for specific error messages

### Deployment Fails
1. Verify `AZURE_APP_NAME` secret is correct
2. Check `AZURE_CREDENTIALS` is valid and up-to-date
3. Ensure Azure App Service is in the same region
4. Check App Service logs: `az webapp log tail --resource-group {rg} --name {app}`

### Reset Service Principal
If credentials expire or need to be reset:
```bash
az ad sp delete --id {client-id}
# Then create a new one using steps above
```

---

## Security Best Practices

✅ **DO:**
- Rotate credentials regularly (quarterly)
- Use environment-specific passwords
- Limit service principal scope to specific resource groups
- Monitor secret access in GitHub audit logs
- Use Azure Key Vault for additional security

❌ **DON'T:**
- Commit secrets in code or configuration files
- Share secret values outside GitHub
- Use weak passwords for SQL Server
- Store plaintext passwords in documentation

