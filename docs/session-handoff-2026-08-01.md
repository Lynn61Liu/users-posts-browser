---
status: in-progress
branch: main
timestamp: 2026-08-01T17:02:00+12:00
---

# DCE04.2 AWS Handoff - 2026-08-01

## Summary

Today we progressed the DCE04.2 assessment infrastructure from Terraform backend setup through a working ECS Fargate deployment. The frontend and backend containers were built, pushed to ECR, deployed to ECS, attached to ALB target groups, and verified through public ALB endpoints.

At the end of the session, the main ongoing-cost runtime resources were destroyed to avoid overnight charges. The Terraform code, remote state, VPC, security groups, ECR repositories/images, DynamoDB application table, Terraform backend S3 bucket, and Terraform lock table remain available for tomorrow.

## Completed Today

- Step 6: Terraform remote backend
  - S3 state bucket: `dce042-terraform-state-345594568549-ap-southeast-2`
  - State key: `dce042/dev/terraform.tfstate`
  - DynamoDB lock table: `dce042-terraform-locks`

- Step 7.1: Network
  - VPC: `dce042-dev-vpc`, `vpc-0cf7603ca02199d54`
  - Public subnets:
    - `subnet-0bf82506506e9f5af`, `ap-southeast-2a`, `10.42.1.0/24`
    - `subnet-08fd5fc4ae92dee09`, `ap-southeast-2b`, `10.42.2.0/24`
  - Internet Gateway: `igw-019a80d8c586cf5cd`
  - Public route table: `rtb-0d3b9d15ab10779fc`
  - NAT Gateway: not created

- Step 8.1: Security groups and IAM
  - Frontend ALB SG: `sg-0c9187b2d9bd81479`
  - Frontend ECS SG: `sg-014acabc9439e4619`
  - Backend ALB SG: `sg-091d6f99b77c96945`
  - Backend ECS SG: `sg-094e6867126ea148a`
  - ECS task execution role: `dce042-dev-ecs-task-execution-role`
  - Frontend task role: `dce042-dev-frontend-task-role`
  - Backend task role: `dce042-dev-backend-task-role`

- Step 9: ECR
  - Frontend repository: `dce042-frontend`
  - Backend repository: `dce042-backend`
  - Both repositories have `latest` images pushed.

- Step 10: ALB
  - Frontend ALB was created and verified:
    - `dce042-dev-frontend-alb-1508953672.ap-southeast-2.elb.amazonaws.com`
  - Backend ALB was created and verified:
    - `dce042-dev-backend-alb-1291356867.ap-southeast-2.elb.amazonaws.com`
  - Four blue/green target groups were created.

- Step 11: ECS Fargate
  - ECS cluster: `dce042-dev-ecs-cluster`
  - Frontend service: `frontend-service`
  - Backend service: `backend-service`
  - Task definitions:
    - `dce042-frontend-task:1`
    - `dce042-backend-task:1`
  - CloudWatch log groups:
    - `/ecs/dce042-frontend`
    - `/ecs/dce042-backend`

## Working Deployment Evidence Captured Before Shutdown

- Frontend `/health` returned `HTTP 200 OK` with body `ok`.
- Backend `/actuator/health` returned `HTTP 200 OK` with `"status":"UP"`.
- Frontend `/api/users` returned users through the frontend Nginx proxy to the backend ALB.
- Backend `/api/users` returned users from DynamoDB.
- Frontend blue target group was healthy.
- Backend blue target group was healthy.
- ECS services had `desiredCount = 1`, `runningCount = 1`, `pendingCount = 0`.

## Shutdown Done To Reduce Cost

The following command was run from `terraform/`:

```bash
terraform destroy -auto-approve \
  -var-file=environments/dev/terraform.tfvars \
  -target=module.ecs \
  -target=module.alb
```

Result:

```text
Destroy complete! Resources: 15 destroyed.
```

Destroyed:

- ECS services
- ECS cluster
- ECS task definitions from Terraform state
- CloudWatch log groups created by the ECS module
- Frontend/backend ALBs
- HTTP listeners
- Blue/green target groups

Post-shutdown verification:

```text
ECS cluster dce042-dev-ecs-cluster:
  Not listed

ALBs dce042-dev-frontend-alb and dce042-dev-backend-alb:
  LoadBalancerNotFound

ECR latest images:
  Still present

DynamoDB application table:
  dce042-users-posts is ACTIVE
```

## Resources Intentionally Kept

These are kept so tomorrow can continue quickly:

- Terraform backend S3 bucket
- Terraform DynamoDB lock table
- VPC, public subnets, IGW, route table
- Security groups
- IAM roles
- ECR repositories and images
- DynamoDB application table `dce042-users-posts`

These remaining resources are low or minimal cost compared with ALB and ECS Fargate tasks.

## How To Continue Tomorrow

From the repo root:

```bash
cd /Users/yinliu/Desktop/users-posts-browser
git status --short
```

Then restore the runtime resources:

```bash
cd terraform
terraform init -reconfigure
terraform plan -var-file=environments/dev/terraform.tfvars
terraform apply -auto-approve -var-file=environments/dev/terraform.tfvars
```

Expected result:

```text
Terraform will recreate module.alb and module.ecs resources.
```

After apply, wait for services and validate:

```bash
terraform output alb
terraform output ecs

aws ecs describe-services \
  --cluster dce042-dev-ecs-cluster \
  --services frontend-service backend-service \
  --region ap-southeast-2
```

Then test the new ALB DNS names from `terraform output alb`:

```bash
curl -i http://<frontend-alb-dns>/health
curl -i http://<backend-alb-dns>/actuator/health
curl http://<frontend-alb-dns>/api/users
```

Important: ALB DNS names may change after recreation. Use the fresh `terraform output alb` values.

## Next Assessment Steps

1. Recreate ECS/ALB tomorrow if more screenshots are needed.
2. Continue with Step 12: Data, assets, and notifications.
3. Then continue with monitoring, autoscaling, CI/CD, and CodeDeploy blue/green.
4. Before final submission, run a full Terraform apply, capture screenshots, then destroy costly resources again.

## Notes

- Terraform still prints a warning that `dynamodb_table` in the S3 backend is deprecated. This is intentionally kept because the assessment asks for DynamoDB locking evidence.
- Backend Dockerfile was updated to use `maven:3.9.11-eclipse-temurin-21` for the build stage because Apple Silicon cross-platform build failed when Maven Wrapper tried to untar Maven inside an amd64 build.
- Buildspec default IAM role names were corrected to `dce042-dev-*` names.
