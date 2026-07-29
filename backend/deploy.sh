echo -n "Enter your server password: "
read -s SERVER_PASS
echo

SERVER_USER="eoso"
SERVER_IP="10.91.91.55"
APP_DIR="/home/enterprise/app/nps-onboarding"

JAR_NAME="nps_onboarding_test-0.0.1-SNAPSHOT.jar"
REMOTE_JAR_PATH="$APP_DIR/$JAR_NAME"

PROPERTIES_SOURCE="src/main/resources/application.properties"
PROPERTIES_DEST="$APP_DIR/config/application.properties"

PUBLIC_KEY_SOURCE="src/main/java/org/example/signer/keys/NIBSS-999999.public.pem"
PUBLIC_KEY_DEST="$APP_DIR/config/NIBSS-999999.public.pem"

PRIVATE_KEY_SOURCE="src/main/java/org/example/signer/keys/999999.private.pem"
PRIVATE_KEY_DEST="$APP_DIR/config/999999.private.pem"

XML_OUTPUT_DIR="$APP_DIR/logs"

set -e

echo "Building the application..."
./mvnw clean package -DskipTests

echo "Preparing server properties..."
# Create a temporary properties file for deployment with server-specific paths
cp src/main/resources/application.properties target/application.properties.deploy
# Use '|' as delimiter in sed to handle slashes in paths
sed "s|app.keys.private-path=.*|app.keys.private-path=$PRIVATE_KEY_DEST|" target/application.properties.deploy > target/application.properties.deploy.tmp && mv target/application.properties.deploy.tmp target/application.properties.deploy
sed "s|app.keys.public-path=.*|app.keys.public-path=$PUBLIC_KEY_DEST|" target/application.properties.deploy > target/application.properties.deploy.tmp && mv target/application.properties.deploy.tmp target/application.properties.deploy
sed "s|app.xml.output-dir=.*|app.xml.output-dir=$XML_OUTPUT_DIR|" target/application.properties.deploy > target/application.properties.deploy.tmp && mv target/application.properties.deploy.tmp target/application.properties.deploy

PROPERTIES_SOURCE="target/application.properties.deploy"

echo "--- PREPARING SERVER DIRECTORIES ---"
sshpass -p "$SERVER_PASS" ssh -o StrictHostKeyChecking=no $SERVER_USER@$SERVER_IP "
  echo \"$SERVER_PASS\" | sudo -S mkdir -p \"$APP_DIR/config\"
  echo \"$SERVER_PASS\" | sudo -S mkdir -p \"$APP_DIR/logs\"
  echo \"$SERVER_PASS\" | sudo -S chown -R $SERVER_USER:$SERVER_USER \"$APP_DIR\"
"

echo "--- COPYING FILES ---"
sshpass -p "$SERVER_PASS" scp -o StrictHostKeyChecking=no "target/$JAR_NAME" "$SERVER_USER@$SERVER_IP:$REMOTE_JAR_PATH"
sshpass -p "$SERVER_PASS" scp -o StrictHostKeyChecking=no "$PROPERTIES_SOURCE" "$SERVER_USER@$SERVER_IP:$PROPERTIES_DEST"
sshpass -p "$SERVER_PASS" scp -o StrictHostKeyChecking=no "$PUBLIC_KEY_SOURCE" "$SERVER_USER@$SERVER_IP:$PUBLIC_KEY_DEST"
sshpass -p "$SERVER_PASS" scp -o StrictHostKeyChecking=no "$PRIVATE_KEY_SOURCE" "$SERVER_USER@$SERVER_IP:$PRIVATE_KEY_DEST"

echo "--- RESTARTING APPLICATION ---"
sshpass -p "$SERVER_PASS" ssh -o StrictHostKeyChecking=no $SERVER_USER@$SERVER_IP "
  echo \"$SERVER_PASS\" | sudo -S systemctl daemon-reload
  echo \"$SERVER_PASS\" | sudo -S systemctl restart nps-onboarding
  echo \"$SERVER_PASS\" | sudo -S systemctl status nps-onboarding --no-pager -l
"

echo "Deployment completed."