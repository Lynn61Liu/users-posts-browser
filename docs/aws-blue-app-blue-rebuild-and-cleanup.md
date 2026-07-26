# AWS Blue/Green Rebuild And Cleanup Notes

This document records the AWS setup needed to redeploy the full `users-posts-browser` project for Task 1a.

The goal is:

- Blue EC2 runs the current project with `Version 1.0` and `Blue environment` visible in the app header.
- Green EC2 runs the current project with `Version 2.0 - New Feature Deployed` and `Green environment` visible in the app header.
- The Application Load Balancer initially points to Blue, then the listener is switched to Green for the final screenshot.

## Known Values

- AWS account: `345594568549`
- AWS region: Asia Pacific Sydney, `ap-southeast-2`
- Application name: `users-posts-browser`
- Current ALB DNS, if reused: `users-posts-browser-alb-745144766.ap-southeast-2.elb.amazonaws.com`
- Current ALB name: `users-posts-browser-alb`
- Current ALB VPC: `vpc-0d6fed3e3b262d32e`
- Blue EC2 name: `users-posts-browser-blue`
- Green EC2 name: `users-posts-browser-green`
- ALB security group name: `users-posts-browser-alb-sg`
- App EC2 security group name: `users-posts-browser-app-sg`
- Blue target group name: `users-posts-blue-tg`
- Green target group name: `users-posts-green-tg`
- Frontend image repository: `users-posts-browser-frontend`
- Backend image repository: `users-posts-browser-backend`
- Blue image tag: `v1`
- Green image tag: `v2`

If the marking rubric expects the shorter names, use these instead:

- ALB security group: `alb-sg`
- App EC2 security group: `web-sg`
- Blue target group: `webapp-blue`
- Green target group: `webapp-green`

Do not mix naming styles in screenshots. Pick one style and use it consistently.

## Architecture

```text
Internet
  |
Application Load Balancer, HTTP :80
  |
Blue or Green Target Group, HTTP :3000
  |
Blue or Green EC2
  |
Docker containers
  |-- React static frontend via Nginx, host port :3000 -> container port :80
  |-- Spring Boot backend, Docker network port :8080
  |-- PostgreSQL, Docker network port :5432
```

The EC2 host exposes only the frontend service to the ALB:

- Browser traffic reaches the ALB on port `80`.
- ALB forwards traffic to the selected EC2 target on port `3000`.
- Docker maps EC2 host port `3000` to the frontend container port `80`.
- Nginx serves the React build.
- Nginx forwards `/api/*` to the backend container on Docker network port `8080`.
- The backend connects to the PostgreSQL container on Docker network port `5432`.

## Prep Before Creating Paid Resources

Do these first so the AWS Console work is fast.

### 1. Select The Correct AWS Region

In AWS Console, switch the region to:

```text
Asia Pacific (Sydney) ap-southeast-2
```

All resources must be created in this region. If the region changes, the ALB, EC2, security groups, target groups, and ECR repositories will not appear together.

### 2. Confirm Or Recreate ECR Repositories

Open:

```text
AWS Console -> ECR -> Private repositories
```

Confirm these private repositories exist:

```text
users-posts-browser-backend
users-posts-browser-frontend
```

If they were deleted during cleanup, recreate them before pushing images.

### 3. Build And Push The Two Versions

From the project folder, log in to ECR:

```text
aws ecr get-login-password --region ap-southeast-2 \
  | docker login --username AWS --password-stdin 345594568549.dkr.ecr.ap-southeast-2.amazonaws.com
```

Build and push the Blue frontend image:

```text
docker build \
  --platform linux/arm64 \
  --build-arg VITE_APP_VERSION="Version 1.0" \
  --build-arg VITE_DEPLOY_ENV="Blue environment" \
  -t 345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/users-posts-browser-frontend:v1 \
  frontend

docker push 345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/users-posts-browser-frontend:v1
```

Build and push the Green frontend image:

```text
docker build \
  --platform linux/arm64 \
  --build-arg VITE_APP_VERSION="Version 2.0 - New Feature Deployed" \
  --build-arg VITE_DEPLOY_ENV="Green environment" \
  -t 345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/users-posts-browser-frontend:v2 \
  frontend

docker push 345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/users-posts-browser-frontend:v2
```

Build and push the backend image:

```text
docker build \
  --platform linux/arm64 \
  -t 345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/users-posts-browser-backend:v1 \
  backend

docker tag \
  345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/users-posts-browser-backend:v1 \
  345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/users-posts-browser-backend:v2

docker push 345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/users-posts-browser-backend:v1
docker push 345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/users-posts-browser-backend:v2
```

The backend code is the same for both versions. It is tagged as both `v1` and `v2` only so the Blue and Green EC2 user data can stay simple.

These commands build `linux/arm64` images because the EC2 settings below use `t4g.micro`. If you choose an x86 instance such as `t3.micro`, build `linux/amd64` images instead.

### 4. Confirm The EC2 IAM Role

Open:

```text
IAM -> Roles
```

Confirm this role exists:

```text
users-posts-browser-ec2-role
```

It should be trusted by EC2 and include:

```text
AmazonEC2ContainerRegistryReadOnly
AmazonSSMManagedInstanceCore
```

The first permission lets EC2 pull Docker images from ECR. The second permission lets you connect through Session Manager without opening SSH broadly.

### 5. Create Or Confirm Security Groups

Open:

```text
EC2 -> Security Groups
```

Create or confirm the ALB security group:

```text
Name: users-posts-browser-alb-sg
Inbound: HTTP 80 from 0.0.0.0/0
Outbound: All traffic
```

Create or confirm the app EC2 security group:

```text
Name: users-posts-browser-app-sg
Inbound: Custom TCP 3000 from users-posts-browser-alb-sg
Inbound: SSH 22 from your IP, optional
Outbound: All traffic
```

Port `3000` is the production frontend entry point on the EC2 host. PostgreSQL and the backend do not need public inbound rules because they run inside Docker.

## Paid Resource Steps To Do Together

Do this section in one session to reduce running time and cost.

### 1. Launch The Blue EC2 Instance

Open:

```text
EC2 -> Instances -> Launch instances
```

Use:

```text
Name: users-posts-browser-blue
AMI: Amazon Linux 2023
Architecture: ARM 64-bit
Instance type: t4g.micro
VPC: same VPC as the future ALB
Subnet: public subnet
Auto-assign public IP: enabled
Security group: users-posts-browser-app-sg
IAM instance profile: users-posts-browser-ec2-role
```

In Advanced details, paste the full contents of:

```text
aws/user-data/blue.sh
```

Blue starts these containers:

```text
postgres:16-alpine
users-posts-browser-backend:v1
users-posts-browser-frontend:v1
```

### 2. Launch The Green EC2 Instance

Use the same EC2 settings, except:

```text
Name: users-posts-browser-green
```

In Advanced details, paste the full contents of:

```text
aws/user-data/green.sh
```

Green starts these containers:

```text
postgres:16-alpine
users-posts-browser-backend:v2
users-posts-browser-frontend:v2
```

### 3. Verify Each EC2 From Session Manager

Open:

```text
EC2 -> Instances -> selected instance -> Connect -> Session Manager
```

Check containers:

```text
docker ps
```

Check the frontend locally on each EC2:

```text
curl -I http://localhost:3000
curl http://localhost:3000 | head
```

Expected:

- The frontend container is running.
- The backend container is running.
- The PostgreSQL container is running.
- `localhost:3000` returns the React/Nginx frontend.
- Blue page contains `Version 1.0`.
- Green page contains `Version 2.0 - New Feature Deployed`.
- If the user list is empty, click `Sync Data` in the browser after the ALB is working.

Important: `localhost:8080` on the EC2 host is expected to fail unless the backend port is explicitly published. The backend is intentionally internal to Docker.

### 4. Create The Blue Target Group

Open:

```text
EC2 -> Target Groups -> Create target group
```

Use:

```text
Target type: Instances
Name: users-posts-blue-tg
Protocol: HTTP
Port: 3000
VPC: same VPC as the Blue EC2 and ALB
Health check path: /
```

Register:

```text
users-posts-browser-blue
Port: 3000
```

Wait until the target health becomes:

```text
Healthy
```

### 5. Create The Green Target Group

Use the same target group settings, except:

```text
Name: users-posts-green-tg
```

Register:

```text
users-posts-browser-green
Port: 3000
```

Wait until the target health becomes:

```text
Healthy
```

If either target is unhealthy, check:

- EC2 security group allows TCP `3000` from the ALB security group.
- The frontend container maps host port `3000` to container port `80`.
- The target group health check path is `/`.
- The EC2 instance and target group are in the same VPC.
- The EC2 user data finished successfully.
- ECR images exist with the required `v1` and `v2` tags.

### 6. Create The ALB

Open:

```text
EC2 -> Load Balancers -> Create load balancer -> Application Load Balancer
```

Use:

```text
Name: users-posts-browser-alb
Scheme: Internet-facing
IP address type: IPv4
VPC: same VPC as the EC2 instances
Mappings: at least two public subnets in different Availability Zones
Security group: users-posts-browser-alb-sg
Listener: HTTP 80
Default action: Forward to users-posts-blue-tg
```

After the ALB status is active, open its DNS name in the browser:

```text
http://<alb-dns-name>
```

Expected Blue result:

```text
Users & Posts
Version 1.0
Blue environment
```

### 7. Screenshot The Target Groups Page

Open:

```text
EC2 -> Target Groups
```

Take a screenshot showing:

- `users-posts-blue-tg` target is `Healthy`.
- `users-posts-green-tg` target is `Healthy`.
- The registered Blue and Green EC2 instances are visible.

This satisfies the rubric item that checks the target groups and healthy targets.

### 8. Switch The Listener To Green

Open:

```text
EC2 -> Load Balancers -> users-posts-browser-alb -> Listeners and rules
```

Edit the HTTP `:80` listener default action:

```text
From: Forward to users-posts-blue-tg
To:   Forward to users-posts-green-tg
```

Wait about 30 seconds, then refresh:

```text
http://<alb-dns-name>
```

Expected Green result:

```text
Users & Posts
Version 2.0 - New Feature Deployed
Green environment
```

Take the browser screenshot for the next rubric item.

## Notes About Direct Access

If the app EC2 security group only allows TCP `3000` from the ALB security group, then this may not work from your laptop:

```text
http://<ec2-public-ip>:3000
```

That is expected. The intended public entry point is the ALB DNS name.

For temporary debugging only, add:

```text
Custom TCP 3000 from My IP
```

Remove that rule after debugging.

## Cleanup Checklist To Save Money

Delete paid resources after screenshots are saved.

### 1. Terminate EC2 Instances

Open:

```text
EC2 -> Instances
```

Terminate:

```text
users-posts-browser-blue
users-posts-browser-green
```

Do not only stop the instances if the goal is minimum cost. Stopped instances no longer bill for compute, but their EBS volumes can still bill. Termination usually deletes the root EBS volume unless the volume was configured to persist.

After termination, open:

```text
EC2 -> Volumes
```

Delete any unattached volumes left from these instances.

### 2. Delete The ALB

Open:

```text
EC2 -> Load Balancers
```

Delete:

```text
users-posts-browser-alb
```

Application Load Balancers are billed while running, so this is the most important cleanup item after EC2.

### 3. Delete Target Groups

Open:

```text
EC2 -> Target Groups
```

Delete:

```text
users-posts-blue-tg
users-posts-green-tg
```

Target groups are not usually the main cost source, but deleting them avoids confusion later.

### 4. Delete ECR Images Or Repositories

Open:

```text
ECR -> Private repositories
```

Either delete only the images:

```text
users-posts-browser-backend:v1
users-posts-browser-backend:v2
users-posts-browser-frontend:v1
users-posts-browser-frontend:v2
```

or delete the full repositories:

```text
users-posts-browser-backend
users-posts-browser-frontend
```

ECR storage is billed by storage size. It is usually small for this experiment, but deleting it reduces cost to near zero.

If you delete the repositories, recreate them before pushing images again.

### 5. Check Elastic IPs

Open:

```text
EC2 -> Elastic IPs
```

Release any Elastic IP created for this experiment.

Unattached Elastic IPs can cost money. If you did not create one manually, there may be nothing to delete.

### 6. Check NAT Gateways

Open:

```text
VPC -> NAT Gateways
```

Delete any NAT Gateway created for this experiment.

NAT Gateways are billed hourly and can become expensive. The default simple setup should not need one.

### 7. Optional Cleanup: Security Groups And IAM Role

Security groups and IAM roles do not normally create direct hourly charges.

You can keep these for later:

```text
users-posts-browser-alb-sg
users-posts-browser-app-sg
users-posts-browser-ec2-role
```

Keeping them makes rebuilds faster.

Delete them only if you want a completely clean AWS account.

### 8. Optional Cleanup: CloudWatch Logs

Open:

```text
CloudWatch -> Log groups
```

Delete log groups created only for this experiment, if any exist.

The current Docker setup does not intentionally create CloudWatch log groups, but EC2 or SSM may create small logs depending on settings.

## What Can Safely Stay

These usually do not bill by themselves:

- Default VPC
- Default subnets
- Route tables
- Internet gateway
- Security groups
- IAM role
- Key pair

Keep them if you want to rebuild with less setup.
