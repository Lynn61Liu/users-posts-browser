#!/bin/bash
set -euxo pipefail

AWS_REGION="ap-southeast-2"
AWS_ACCOUNT_ID="345594568549"
IMAGE_TAG="v2"
APP_NAME="users-posts-browser-green"
ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
DYNAMODB_TABLE_NAME="dce042-users-posts"

dnf install -y awscli docker
systemctl enable --now docker

aws ecr get-login-password --region "$AWS_REGION" \
  | docker login --username AWS --password-stdin "$ECR_REGISTRY"

docker network create users-posts-browser || true

docker rm -f "$APP_NAME-backend" "$APP_NAME-frontend" || true

docker run -d \
  --name "$APP_NAME-backend" \
  --network users-posts-browser \
  --network-alias backend \
  --restart unless-stopped \
  -e SERVER_PORT=8080 \
  -e AWS_REGION="$AWS_REGION" \
  -e DYNAMODB_TABLE_NAME="$DYNAMODB_TABLE_NAME" \
  -e APP_DYNAMODB_ENABLED=true \
  -e APP_DEV_RESET_ENABLED=false \
  "${ECR_REGISTRY}/users-posts-browser-backend:${IMAGE_TAG}"

docker run -d \
  --name "$APP_NAME-frontend" \
  --network users-posts-browser \
  --restart unless-stopped \
  -p 3000:80 \
  "${ECR_REGISTRY}/users-posts-browser-frontend:${IMAGE_TAG}"
