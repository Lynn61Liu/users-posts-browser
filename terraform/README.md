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
