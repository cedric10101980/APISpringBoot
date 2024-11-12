#!/bin/bash

docker compose down
# Remove the old Docker image
docker rmi -f outboundacrcicd.azurecr.io/outbound/ai-info-api-app:master

# Run the Docker container
docker compose up -d