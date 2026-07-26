# Task 1a Blue/Green Project Deployment

These scripts deploy the full Users & Posts project on EC2:

- PostgreSQL container.
- Spring Boot backend container.
- React/Nginx frontend container.

Use the matching script in the EC2 launch wizard under:

```text
Advanced details -> User data
```

The EC2 instance needs an IAM role with:

```text
AmazonEC2ContainerRegistryReadOnly
```

## Blue EC2

Copy the full contents of:

```text
aws/user-data/blue.sh
```

Expected browser result:

```text
Users & Posts
Version 1.0
Blue environment
```

## Green EC2

Copy the full contents of:

```text
aws/user-data/green.sh
```

Expected browser result after switching the ALB listener to the green target group:

```text
Users & Posts
Version 2.0 - New Feature Deployed
Green environment
```

## Build And Push Images First

Build and push two frontend image versions. The backend can use the same image tag for both, or you can tag it as both `v1` and `v2` for a simpler EC2 script.

Blue frontend:

```text
docker build \
  --platform linux/arm64 \
  --build-arg VITE_APP_VERSION="Version 1.0" \
  --build-arg VITE_DEPLOY_ENV="Blue environment" \
  -t 345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/users-posts-browser-frontend:v1 \
  frontend
```

Green frontend:

```text
docker build \
  --platform linux/arm64 \
  --build-arg VITE_APP_VERSION="Version 2.0 - New Feature Deployed" \
  --build-arg VITE_DEPLOY_ENV="Green environment" \
  -t 345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/users-posts-browser-frontend:v2 \
  frontend
```

Backend:

```text
docker build \
  --platform linux/arm64 \
  -t 345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/users-posts-browser-backend:v1 \
  backend
docker tag \
  345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/users-posts-browser-backend:v1 \
  345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/users-posts-browser-backend:v2
```

Push all image tags:

```text
docker push 345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/users-posts-browser-frontend:v1
docker push 345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/users-posts-browser-frontend:v2
docker push 345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/users-posts-browser-backend:v1
docker push 345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/users-posts-browser-backend:v2
```

## AWS Settings To Match These Scripts

- AMI: Amazon Linux 2023.
- EC2 security group: allow custom TCP `3000` from the ALB security group.
- Target group protocol: HTTP.
- Target group port: `3000`.
- Health check path: `/`.

The frontend container listens on EC2 host port `3000`, and Nginx proxies `/api/*` to the backend container.

The build commands use `linux/arm64` because the recommended EC2 instance is `t4g.micro`. If you choose `t3.micro` or another x86 instance, build `linux/amd64` images instead.
