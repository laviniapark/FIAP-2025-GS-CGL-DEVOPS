LOCATION="eastus2"
RG="rg-gs2025-cgl"
ACR_NAME="lyrags2025acr"
SERVER_NAME="sqlserver-gs2025-lyra"
USERNAME="lyra"
PASSWORD="GlobalSolution2025!"
DBNAME="lyradb"
APP_SERVICE_PLAN="plan-gs2025-lyra"
WEBAPP_NAME="lyrags2025webapp"
IMAGE_NAME="lyra-java"

# Resource Group nao criado pois ja existe (criado na materia de .NET)

az acr create --resource-group $RG --name $ACR_NAME --sku Basic --admin-enabled

ACR_USERNAME=$(az acr credential show --name "$ACR_NAME" --query "username" -o tsv)
ACR_PASSWORD=$(az acr credential show --name "$ACR_NAME" --query "passwords[0].value" -o tsv)

echo $ACR_USERNAME
echo $ACR_PASSWORD

az sql server create -l $LOCATION -g $RG -n $SERVER_NAME -u $USERNAME -p $PASSWORD --enable-public-network true

az sql db create -g $RG -s $SERVER_NAME -n $DBNAME --service-objective Basic --backup-storage-redundancy Local --zone-redundant false

az sql server firewall-rule create -g $RG -s $SERVER_NAME -n AllowAll --start-ip-address 0.0.0.0 --end-ip-address 255.255.255.255

az appservice plan create \
  --name $APP_SERVICE_PLAN \
  --resource-group $RG \
  --location $LOCATION \
  --sku F1 \
  --is-linux

az webapp create \
  --name $WEBAPP_NAME \
  --resource-group $RG \
  --plan $APP_SERVICE_PLAN \
  --runtime "JAVA:17-java17"

az webapp config container set \
    --name $WEBAPP_NAME \
    --resource-group $RG \
    --container-image-name ${ACR_NAME}.azurecr.io/${IMAGE_NAME}:latest \
    --container-registry-url https://${ACR_NAME}.azurecr.io \
    --container-registry-user $ACR_USERNAME \
    --container-registry-password $ACR_PASSWORD

az webapp config appsettings set \
  --name $WEBAPP_NAME \
  --resource-group $RG \
  --settings \
    WEBSITES_PORT=8080 \
    ACTIVE_PROFILE=prod \
    DB_URL="jdbc:sqlserver://${SERVER_NAME}.database.windows.net:1433;database=${DBNAME};encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;" \
    DB_USERNAME=$USERNAME \
    DB_PASSWORD=$PASSWORD