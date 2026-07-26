#!/bin/bash
set -euxo pipefail

AWS_REGION="ap-southeast-2"
AWS_ACCOUNT_ID="345594568549"
IMAGE_TAG="v2"
APP_NAME="users-posts-browser-green"
ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

dnf install -y awscli docker
systemctl enable --now docker

aws ecr get-login-password --region "$AWS_REGION" \
  | docker login --username AWS --password-stdin "$ECR_REGISTRY"

docker network create users-posts-browser || true
docker volume create users-posts-browser-green-postgres

docker rm -f "$APP_NAME-postgres" "$APP_NAME-backend" "$APP_NAME-frontend" || true

docker run -d \
  --name "$APP_NAME-postgres" \
  --network users-posts-browser \
  --restart unless-stopped \
  -e POSTGRES_DB=users_posts_browser \
  -e POSTGRES_USER=users_posts_browser \
  -e POSTGRES_PASSWORD=users_posts_browser \
  -v users-posts-browser-green-postgres:/var/lib/postgresql/data \
  postgres:16-alpine

until docker exec "$APP_NAME-postgres" pg_isready -U users_posts_browser -d users_posts_browser; do
  sleep 5
done

docker run -d \
  --name "$APP_NAME-backend" \
  --network users-posts-browser \
  --network-alias backend \
  --restart unless-stopped \
  -e SERVER_PORT=8080 \
  -e DB_HOST="$APP_NAME-postgres" \
  -e DB_PORT=5432 \
  -e DB_NAME=users_posts_browser \
  -e DB_USERNAME=users_posts_browser \
  -e DB_PASSWORD=users_posts_browser \
  -e APP_DEV_RESET_ENABLED=false \
  "${ECR_REGISTRY}/users-posts-browser-backend:${IMAGE_TAG}"

docker run -d \
  --name "$APP_NAME-frontend" \
  --network users-posts-browser \
  --restart unless-stopped \
  -p 3000:80 \
  "${ECR_REGISTRY}/users-posts-browser-frontend:${IMAGE_TAG}"
