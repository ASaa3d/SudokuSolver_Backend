#!/bin/sh
echo "$PRIVATE_PEM_B64" | base64 -d > /etc/secrets/private.pem
chmod 600 /etc/secrets/private.pem
exec java -XX:MaxRAMPercentage=75.0 -jar /app/app.jar