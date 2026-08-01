# Terraform Structure

This directory contains the Infrastructure as Code structure for the DCE04.2 assessment.

Current route:

- Two ECS Fargate services: `frontend-service` and `backend-service`
- Two ECR repositories: `dce042-frontend` and `dce042-backend`
- Two public ALBs: one for frontend and one for backend
- One DynamoDB application table: `dce042-users-posts`
- Terraform remote state will be prepared in the next step with S3 and DynamoDB locking

## Layout

```text
terraform/
  providers.tf
  backend.tf
  main.tf
  variables.tf
  outputs.tf
  environments/dev/terraform.tfvars
  modules/
    network/
    security/
    ecr/
    alb/
    ecs/
    autoscaling/
    data/
    monitoring/
    cicd/
    entra-federation/
```

## Local Validation

The remote backend is enabled and the network module has been applied for the dev environment.

```bash
cd terraform
terraform fmt -recursive
terraform init
terraform validate
terraform plan -var-file=environments/dev/terraform.tfvars
```

The S3 remote backend is enabled in `backend.tf` after completing step 6.

Remote backend resources:

```text
S3 state bucket: dce042-terraform-state-345594568549-ap-southeast-2
State key: dce042/dev/terraform.tfstate
DynamoDB lock table: dce042-terraform-locks
Region: ap-southeast-2
```

## Network Resources

Step 7.1 has created the low-cost public networking layer:

```text
VPC: dce042-dev-vpc / vpc-0cf7603ca02199d54 / 10.42.0.0/16
Public subnet 1: dce042-dev-public-1 / subnet-0bf82506506e9f5af / 10.42.1.0/24 / ap-southeast-2a
Public subnet 2: dce042-dev-public-2 / subnet-08fd5fc4ae92dee09 / 10.42.2.0/24 / ap-southeast-2b
Internet Gateway: dce042-dev-igw / igw-019a80d8c586cf5cd
Public route table: dce042-dev-public-rt / rtb-0d3b9d15ab10779fc
NAT Gateway: not created
```

The NAT Gateway is intentionally disabled in `environments/dev/terraform.tfvars` to avoid ongoing NAT hourly charges for this assessment.

## Security Resources

Step 8.1 has created the security groups and ECS IAM roles:

```text
Frontend ALB SG: dce042-dev-frontend-alb-sg / sg-0c9187b2d9bd81479
Frontend ECS SG: dce042-dev-frontend-ecs-sg / sg-014acabc9439e4619
Backend ALB SG: dce042-dev-backend-alb-sg / sg-091d6f99b77c96945
Backend ECS SG: dce042-dev-backend-ecs-sg / sg-094e6867126ea148a

ECS task execution role: arn:aws:iam::345594568549:role/dce042-dev-ecs-task-execution-role
Frontend task role: arn:aws:iam::345594568549:role/dce042-dev-frontend-task-role
Backend task role: arn:aws:iam::345594568549:role/dce042-dev-backend-task-role
```

The backend task role grants read/write permissions only to the `dce042-users-posts` DynamoDB table and its indexes.

## ECR Resources

Step 9 has created the container image repositories:

```text
Frontend repository:
  dce042-frontend
  345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/dce042-frontend

Backend repository:
  dce042-backend
  345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/dce042-backend
```

Both repositories have scan on push enabled, AES256 encryption, and a lifecycle policy that keeps the most recent 10 images.

## ALB Resources

Step 10 has created the public load balancing layer:

```text
Frontend ALB:
  dce042-dev-frontend-alb
  dce042-dev-frontend-alb-1508953672.ap-southeast-2.elb.amazonaws.com
  Listener: HTTP 80 -> dce042-dev-frontend-blue-tg

Backend ALB:
  dce042-dev-backend-alb
  dce042-dev-backend-alb-1291356867.ap-southeast-2.elb.amazonaws.com
  Listener: HTTP 80 -> dce042-dev-backend-blue-tg

Target groups:
  dce042-dev-frontend-blue-tg / port 80 / health check /health
  dce042-dev-frontend-green-tg / port 80 / health check /health
  dce042-dev-backend-blue-tg / port 8080 / health check /actuator/health
  dce042-dev-backend-green-tg / port 8080 / health check /actuator/health
```

The target groups intentionally use `target_type = "ip"` for ECS Fargate compatibility.

## ECS Resources

Step 11 has created the ECS Fargate runtime layer:

```text
Cluster: dce042-dev-ecs-cluster

Frontend:
  Service: frontend-service
  Task definition: dce042-frontend-task:1
  Image: 345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/dce042-frontend:latest
  Log group: /ecs/dce042-frontend
  Target group: dce042-dev-frontend-blue-tg

Backend:
  Service: backend-service
  Task definition: dce042-backend-task:1
  Image: 345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/dce042-backend:latest
  Log group: /ecs/dce042-backend
  Target group: dce042-dev-backend-blue-tg
```

Both services run one Fargate task with `256` CPU and `512` MB memory. The frontend proxies `/api` requests to the backend ALB through `BACKEND_UPSTREAM`.

## Data And Notification Resources

Step 12 has created the storage and notification layer:

```text
S3 pipeline artifact bucket:
  dce042-dev-pipeline-artifacts-345594568549-ap-southeast-2

S3 application assets bucket:
  dce042-dev-app-assets-345594568549-ap-southeast-2

DynamoDB application table:
  dce042-users-posts
  billing mode: PAY_PER_REQUEST
  point-in-time recovery: ENABLED

SNS topic:
  dce042-dev-critical-notifications
```

The existing DynamoDB table was imported into Terraform state with:

```bash
terraform import -var-file=environments/dev/terraform.tfvars module.data.aws_dynamodb_table.application dce042-users-posts
```

Apply result:

```text
10 added, 1 changed, 0 destroyed
```

## Autoscaling And Monitoring

Step 13 has created ECS service autoscaling and CloudWatch CPU alarms:

```text
Scalable targets:
  service/dce042-dev-ecs-cluster/frontend-service
  service/dce042-dev-ecs-cluster/backend-service

Capacity:
  min: 1
  max: 2

Scaling policies:
  dce042-dev-frontend-up
  dce042-dev-frontend-down
  dce042-dev-backend-up
  dce042-dev-backend-down

CloudWatch alarms:
  dce042-dev-frontend-cpu-high
  dce042-dev-frontend-cpu-low
  dce042-dev-backend-cpu-high
  dce042-dev-backend-cpu-low

Notification target:
  arn:aws:sns:ap-southeast-2:345594568549:dce042-dev-critical-notifications
```

Apply result:

```text
10 added, 0 changed, 0 destroyed
```
