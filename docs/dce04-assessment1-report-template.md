# DCE04.2 Assessment 1 Report Template

## Cover Page

**Programme:** 4241 - DCE L7 Diploma in Cloud Engineering Level 7  
**Course:** DCE04.2 Multi-Cloud Automation and Emerging Technologies  
**Assessment:** Assessment 1 - Implementation and Technical Documentation  
**Assessment Topic:** AWS Containerized DevOps Pipeline: Microservices Deployment  
**Student Name:** `<your name>`  
**Student ID:** `<your student ID>`  
**Submission Date:** `<date>`  

### Cloud Account Details for Verification

| Platform | Account / Tenant | Region | Notes |
| --- | --- | --- | --- |
| AWS | `<AWS account ID or masked account>` | `<region>` | Used for ECS, ECR, ALB, CI/CD, IAM, S3, DynamoDB, CloudWatch, SNS |
| Azure | `<tenant name or tenant ID>` | N/A | Used for Microsoft Entra ID SAML federation |

> Do not include passwords, secret access keys, private keys, or full sensitive credentials in the report.

---

## Table of Contents

1. Introduction
2. Requirements Analysis
3. Solution Overview
4. Architecture Diagram
5. Infrastructure as Code Design
6. Terraform State Management
7. Networking and Security Design
8. Container Platform Design
9. ECR and Docker Image Management
10. Application Load Balancer Design
11. Data, Assets, and Notifications
12. Autoscaling and Monitoring
13. CI/CD Pipeline Design
14. Blue/Green Deployment Design
15. Azure Entra ID SAML Federation
16. Implementation Evidence
17. Validation and Testing
18. Cost Control and Cleanup
19. Challenges and Lessons Learned
20. Conclusion
21. References
22. Appendix

---

## 1. Introduction

This report documents the design and implementation of an Infrastructure as Code based AWS containerized DevOps pipeline. The solution uses Terraform to provision AWS infrastructure and Microsoft Entra ID to provide SAML-based single sign-on into AWS.

The implemented platform supports two independently containerized services:

- `frontend-service`: React + Nginx frontend container
- `backend-service`: Spring Boot backend API container

Each service is deployed to Amazon ECS Fargate, exposed through its own public Application Load Balancer, and released through an automated CI/CD pipeline.

This report uses the current project structure as the application source. The `frontend/` and `backend/` folders are built and deployed independently, each with its own ECR repository, ECS task definition, ECS service, ALB, CodeBuild project, and CodeDeploy blue/green deployment group.

### Screenshot Placeholder

**Figure 1: Final deployed application overview**

`[Insert screenshot: browser showing the deployed frontend or service endpoint]`

---

## 2. Requirements Analysis

The assessment requires a complete AWS containerized deployment platform managed through Infrastructure as Code. The major requirements are:

- A dedicated high-availability VPC with public subnets across at least two Availability Zones
- Separate security groups for public ALBs and ECS tasks
- Two public-facing ALBs, one for each service
- Two ECR repositories, one for each service image
- One ECS Fargate cluster hosting two ECS services
- Two task definitions with CPU, memory, image, and logging configuration
- Autoscaling policies and CloudWatch alarms for both services
- S3 buckets for pipeline artifacts and application assets
- DynamoDB table for application data
- SNS topic for critical notifications
- IAM roles and policies for ECS, CodeBuild, CodeDeploy, and CodePipeline
- Two CodeBuild projects
- Two CodeDeploy applications or deployment groups for ECS blue/green deployment
- One CodePipeline using Source, parallel Build, and parallel Deploy stages
- Terraform local and remote state management
- Azure Entra ID SAML federation with AWS IAM roles

### Screenshot Placeholder

**Figure 2: Assessment requirements checklist**

`[Insert screenshot: your checklist or highlighted requirements from the assignment brief]`

---

## 3. Solution Overview

The solution uses Terraform as the main Infrastructure as Code tool. AWS hosts the application runtime, container registry, networking, storage, monitoring, and CI/CD resources. Microsoft Entra ID is used as the external identity provider for AWS Console access through SAML 2.0.

The application is deployed as two services:

| Service | Runtime | Container Port | AWS Components |
| --- | --- | ---: | --- |
| `frontend-service` | React + Nginx | 80 | ECR, ECS Fargate, ALB, CodeBuild, CodeDeploy |
| `backend-service` | Spring Boot | 8080 | ECR, ECS Fargate, ALB, CodeBuild, CodeDeploy |

The frontend service handles the browser user interface. The backend service provides API functionality and demonstrates access to the Terraform-created DynamoDB application table through the ECS task role.

### Screenshot Placeholder

**Figure 3: Local repository structure**

`[Insert screenshot: project folder showing frontend, backend, terraform, deploy files]`

---

## 4. Architecture Diagram

The architecture contains four main flows:

1. Infrastructure provisioning flow: Developer -> Terraform -> AWS resources
2. Application access flow: Users -> ALB -> ECS Fargate services
3. CI/CD flow: Source -> CodePipeline -> CodeBuild -> ECR -> CodeDeploy -> ECS
4. Identity flow: Azure Entra ID -> SAML provider -> AWS IAM roles

### Architecture Diagram Placeholder

**Figure 4: Full AWS ECS Fargate DevOps architecture**

`[Insert architecture diagram here]`

The diagram should include:

- Developer / source repository
- Terraform
- Terraform remote state S3 bucket and DynamoDB lock table
- AWS VPC
- Two Availability Zones
- Public subnets
- Two public ALBs
- ECS Fargate cluster
- Two ECS services
- Two ECR repositories
- CodePipeline
- Two CodeBuild projects
- Two CodeDeploy deployment groups
- Blue and green target groups
- S3 artifacts bucket
- S3 assets bucket
- DynamoDB application table
- CloudWatch logs and alarms
- SNS topic
- Microsoft Entra ID
- AWS IAM SAML provider
- DevOpsEngineer and ReadOnlyAuditor IAM roles

---

## 5. Infrastructure as Code Design

Terraform was selected because it supports reusable, modular Infrastructure as Code and can manage resources consistently. The project is structured into modules to separate networking, security, ECS, ECR, ALB, CI/CD, monitoring, data resources, and identity federation.

Implemented Terraform structure:

```text
terraform/
  providers.tf
  backend.tf
  main.tf
  variables.tf
  outputs.tf
  environments/
    dev/
      terraform.tfvars
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

At this stage, the Terraform modules are prepared as a validated skeleton before real AWS resources are added. This allows the structure, variable names, provider setup, and module boundaries to be checked without creating cloud resources.

Validation commands:

```bash
cd terraform
terraform fmt -recursive
terraform init
terraform validate
terraform plan -var-file=environments/dev/terraform.tfvars
```

Remote backend resources:

```text
S3 state bucket: dce042-terraform-state-345594568549-ap-southeast-2
State key: dce042/dev/terraform.tfstate
DynamoDB lock table: dce042-terraform-locks
Region: ap-southeast-2
```

### Screenshot Placeholders

**Figure 5: Terraform project folder structure**

`[Insert screenshot: terraform folder and modules]`

**Figure 6: Terraform format and validation result**

`[Insert screenshot: terraform fmt and terraform validate output]`

**Figure 7: Terraform plan output**

`[Insert screenshot: terraform plan summary]`

---

## 6. Terraform State Management

The solution uses Terraform state to track deployed infrastructure. During development, local state can demonstrate how Terraform records resource state. For team or production usage, a remote backend should be configured.

Remote backend design:

- S3 bucket stores `terraform.tfstate`
- DynamoDB table provides state locking
- Encryption is enabled for the state bucket
- State resources are created before the main infrastructure

### Screenshot Placeholders

**Figure 8: Local Terraform state file created**

`[Insert screenshot: local terraform.tfstate file or terraform show output]`

**Figure 9: S3 remote backend bucket**

`[Insert screenshot: AWS S3 bucket used for Terraform state]`

**Figure 10: DynamoDB lock table**

`[Insert screenshot: DynamoDB table used for Terraform locking]`

**Figure 11: Terraform init using remote backend**

`[Insert screenshot: terraform init success output]`

---

## 7. Networking and Security Design

The AWS network is deployed inside a dedicated VPC. Public subnets are created across at least two Availability Zones to support high availability for the Application Load Balancers and ECS services.

Network components:

- Dedicated VPC: `dce042-dev-vpc`, `vpc-0cf7603ca02199d54`, CIDR `10.42.0.0/16`
- Public subnet 1: `dce042-dev-public-1`, `subnet-0bf82506506e9f5af`, `10.42.1.0/24`, `ap-southeast-2a`
- Public subnet 2: `dce042-dev-public-2`, `subnet-08fd5fc4ae92dee09`, `10.42.2.0/24`, `ap-southeast-2b`
- Internet Gateway: `dce042-dev-igw`, `igw-019a80d8c586cf5cd`
- Public route table: `dce042-dev-public-rt`, `rtb-0d3b9d15ab10779fc`
- Route table associations: both public subnets associated with the public route table
- NAT Gateway: not created for the assessment demo to avoid unnecessary hourly cost

Security design:

- Frontend ALB security group `dce042-dev-frontend-alb-sg` allows inbound TCP 80 from `0.0.0.0/0`
- Backend ALB security group `dce042-dev-backend-alb-sg` allows inbound TCP 80 from `0.0.0.0/0`
- Frontend ECS security group `dce042-dev-frontend-ecs-sg` allows inbound TCP 80 only from the frontend ALB security group
- Backend ECS security group `dce042-dev-backend-ecs-sg` allows inbound TCP 8080 only from the backend ALB security group
- ECS task execution role uses `AmazonECSTaskExecutionRolePolicy`
- Backend ECS task role grants access only to the `dce042-users-posts` DynamoDB table and its indexes
- IAM roles follow least-privilege principles where practical

Created security resources:

```text
Frontend ALB SG: sg-0c9187b2d9bd81479
Frontend ECS SG: sg-014acabc9439e4619
Backend ALB SG: sg-091d6f99b77c96945
Backend ECS SG: sg-094e6867126ea148a

ECS task execution role:
  arn:aws:iam::345594568549:role/dce042-dev-ecs-task-execution-role

Frontend task role:
  arn:aws:iam::345594568549:role/dce042-dev-frontend-task-role

Backend task role:
  arn:aws:iam::345594568549:role/dce042-dev-backend-task-role
```

### Screenshot Placeholders

**Figure 12: VPC resource map**

`[Insert screenshot: AWS VPC resource map]`

**Figure 13: Public subnets across two Availability Zones**

`[Insert screenshot: subnet list showing two AZs]`

**Figure 14: Route table with Internet Gateway route**

`[Insert screenshot: route table showing 0.0.0.0/0 to IGW]`

**Figure 15: Terraform network apply output**

`[Insert screenshot: terraform apply output showing 7 added]`

**Figure 16: ALB security group inbound rules**

`[Insert screenshot: ALB SG allowing HTTP/HTTPS]`

**Figure 17: ECS task security group inbound rules**

`[Insert screenshot: ECS SG allowing traffic only from ALB SG]`

**Figure 18: ECS task execution IAM role**

`[Insert screenshot: IAM role dce042-dev-ecs-task-execution-role with AmazonECSTaskExecutionRolePolicy]`

**Figure 19: Backend task role DynamoDB policy**

`[Insert screenshot: IAM inline policy dce042-dev-backend-dynamodb-access]`

---

## 8. Container Platform Design

Amazon ECS with Fargate is used to run containerized services without managing servers. One ECS cluster hosts both services.

ECS resources:

- One ECS cluster: `dce042-dev-ecs-cluster`
- Frontend task definition: `dce042-frontend-task:1`
- Backend task definition: `dce042-backend-task:1`
- Frontend ECS service: `frontend-service`
- Backend ECS service: `backend-service`
- CloudWatch log groups: `/ecs/dce042-frontend`, `/ecs/dce042-backend`
- ECS task execution role: `dce042-dev-ecs-task-execution-role`
- Frontend task role: `dce042-dev-frontend-task-role`
- Backend task role: `dce042-dev-backend-task-role`

Task sizing:

| Service | CPU | Memory | Desired Count |
| --- | ---: | ---: | ---: |
| `frontend-service` | 256 | 512 MB | 1 |
| `backend-service` | 256 | 512 MB | 1 |

Service mapping:

```text
frontend-service:
  source: frontend/
  Dockerfile: frontend/Dockerfile
  container name: frontend
  container port: 80
  health check path: /health
  image: 345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/dce042-frontend:latest
  environment: BACKEND_UPSTREAM=dce042-dev-backend-alb-1291356867.ap-southeast-2.elb.amazonaws.com
  ALB: frontend-alb
  target groups: frontend-blue-tg / frontend-green-tg

backend-service:
  source: backend/
  Dockerfile: backend/Dockerfile
  container name: backend
  container port: 8080
  health check path: /actuator/health
  image: 345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/dce042-backend:latest
  environment: APP_DYNAMODB_ENABLED=true, DYNAMODB_TABLE_NAME=dce042-users-posts
  ALB: backend-alb
  target groups: backend-blue-tg / backend-green-tg
```

Runtime validation:

```text
frontend-service: desired 1, running 1, steady state
backend-service: desired 1, running 1, steady state
frontend blue target group: healthy
backend blue target group: healthy
frontend /health: HTTP 200 OK
backend /actuator/health: HTTP 200 OK, status UP
frontend /api/users: HTTP 200 OK, returns users through Nginx proxy
```

### Screenshot Placeholders

**Figure 17: ECS cluster**

`[Insert screenshot: ECS cluster overview]`

**Figure 18: Frontend ECS service**

`[Insert screenshot: frontend service running]`

**Figure 19: Backend ECS service**

`[Insert screenshot: backend service running]`

**Figure 20: ECS task definitions**

`[Insert screenshot: two task definitions and latest revisions]`

**Figure 21: CloudWatch container logs**

`[Insert screenshot: log groups or log stream output]`

**Figure 22: ALB target groups with healthy ECS targets**

`[Insert screenshot: frontend/backend blue target groups showing healthy registered targets]`

---

## 9. ECR and Docker Image Management

Amazon ECR stores Docker images for each service. The CI/CD pipeline builds images and pushes them into ECR before deployment.

ECR repositories:

- Frontend: `dce042-frontend`
  `345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/dce042-frontend`
- Backend: `dce042-backend`
  `345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/dce042-backend`

Image management includes:

- Versioned image tags
- Image scanning on push
- AES256 encryption
- Lifecycle policy to retain the latest 10 images
- `force_delete = true` for assessment cleanup

### Screenshot Placeholders

**Figure 22: Two ECR repositories**

`[Insert screenshot: ECR repository list]`

**Figure 23: ECR repository security settings**

`[Insert screenshot: scan on push and AES256 encryption settings]`

**Figure 24: ECR lifecycle policy**

`[Insert screenshot: lifecycle policy keeping recent images]`

**Figure 25: Frontend image pushed to ECR**

`[Insert screenshot: frontend ECR image tag]`

**Figure 26: Backend image pushed to ECR**

`[Insert screenshot: backend ECR image tag]`

---

## 10. Application Load Balancer Design

Two public Application Load Balancers are used, one for each service. Each ALB forwards traffic to its corresponding ECS service target group.

ALB resources:

- Frontend ALB: `dce042-dev-frontend-alb`
  `dce042-dev-frontend-alb-1508953672.ap-southeast-2.elb.amazonaws.com`
- Backend ALB: `dce042-dev-backend-alb`
  `dce042-dev-backend-alb-1291356867.ap-southeast-2.elb.amazonaws.com`
- Frontend blue/green target groups:
  `dce042-dev-frontend-blue-tg`, `dce042-dev-frontend-green-tg`
- Backend blue/green target groups:
  `dce042-dev-backend-blue-tg`, `dce042-dev-backend-green-tg`
- HTTP listener 80 for each ALB
- Frontend health check path: `/health`
- Backend health check path: `/actuator/health`

The listeners initially forward traffic to the blue target groups. The green target groups are reserved for CodeDeploy blue/green deployments. The target group type is `ip`, which is required for ECS Fargate tasks.

### Screenshot Placeholders

**Figure 27: Frontend ALB**

`[Insert screenshot: frontend ALB overview]`

**Figure 28: Backend ALB**

`[Insert screenshot: backend ALB overview]`

**Figure 29: ALB listeners**

`[Insert screenshot: listeners for frontend/backend ALBs]`

**Figure 30: Blue and green target groups**

`[Insert screenshot: four target groups with ports and health check paths]`

**Figure 31: Healthy target groups**

`[Insert screenshot after ECS services are created: target groups showing healthy ECS targets]`

---

## 11. Data, Assets, and Notifications

The solution includes storage and notification resources required by the assessment.

Resources:

- S3 bucket for CodePipeline artifacts: `dce042-dev-pipeline-artifacts-345594568549-ap-southeast-2`
- S3 bucket for application assets: `dce042-dev-app-assets-345594568549-ap-southeast-2`
- DynamoDB table for users, posts, raw source records, and demo transaction data: `dce042-users-posts`
- SNS topic for deployment or scaling notifications: `dce042-dev-critical-notifications`

In this implementation, DynamoDB is used as the AWS-managed application data resource required by the assessment. The backend stores imported users, posts, raw source traceability records, and demo transaction records in the `dce042-users-posts` table. The backend ECS task role grants table access without storing AWS access keys inside the container.

DynamoDB table design:

```text
Table name: dce042-users-posts
Partition key: pk string
Sort key: sk string
Billing mode: PAY_PER_REQUEST
Point-in-time recovery: ENABLED

USER item:        pk = USER#<externalUserId>, sk = PROFILE
POST item:        pk = USER#<externalUserId>, sk = POST#<externalPostId>
RAW_SOURCE item:  pk = RAW#<sourceType>#<externalId>, sk = METADATA
TRANSACTION item: pk = TRANSACTION#<transactionId>, sk = METADATA
```

Terraform managed the existing DynamoDB table by importing it into state:

```text
terraform import module.data.aws_dynamodb_table.application dce042-users-posts
terraform apply result: 10 added, 1 changed, 0 destroyed
```

### Screenshot Placeholders

**Figure 29: CodePipeline artifact S3 bucket**

`[Insert screenshot: S3 artifact bucket dce042-dev-pipeline-artifacts-345594568549-ap-southeast-2]`

**Figure 30: Application assets S3 bucket**

`[Insert screenshot: S3 assets bucket dce042-dev-app-assets-345594568549-ap-southeast-2]`

**Figure 31: DynamoDB application table**

`[Insert screenshot: DynamoDB table]`

**Figure 31a: DynamoDB table items**

`[Insert screenshot: USER, POST, RAW_SOURCE, and TRANSACTION items in DynamoDB]`

**Figure 32: SNS topic and subscription**

`[Insert screenshot: SNS topic dce042-dev-critical-notifications. If email subscription is configured later, include the confirmed subscription screenshot.]`

---

## 12. Autoscaling and Monitoring

Each ECS service is configured with autoscaling and CloudWatch alarms. The goal is to automatically increase capacity when CPU usage is high and reduce capacity when CPU usage is low.

Required total:

- Four scaling policies:
  `dce042-dev-frontend-up`, `dce042-dev-frontend-down`, `dce042-dev-backend-up`, `dce042-dev-backend-down`
- Four CloudWatch alarms:
  `dce042-dev-frontend-cpu-high`, `dce042-dev-frontend-cpu-low`, `dce042-dev-backend-cpu-high`, `dce042-dev-backend-cpu-low`
- CloudWatch logs for containers
- SNS notification target:
  `arn:aws:sns:ap-southeast-2:345594568549:dce042-dev-critical-notifications`

Autoscaling limits:

```text
frontend-service: min 1, max 2
backend-service:  min 1, max 2
```

The CPU high alarms trigger scale-up policies when average CPU is above 60%. The CPU low alarms trigger scale-down policies when average CPU is below 20%. At idle, a low CPU alarm may enter `ALARM` state; this is expected and does not reduce the service below the configured minimum capacity of one task.

### Screenshot Placeholders

**Figure 33: Frontend service autoscaling configuration**

`[Insert screenshot: frontend scaling policy]`

**Figure 34: Backend service autoscaling configuration**

`[Insert screenshot: backend scaling policy]`

**Figure 35: Four CloudWatch CPU alarms**

`[Insert screenshot: CloudWatch alarms list]`

**Figure 36: CloudWatch alarm action to SNS**

`[Insert screenshot: alarm action or SNS integration]`

---

## 13. CI/CD Pipeline Design

The CI/CD pipeline automates source retrieval, image build, image push, and ECS blue/green deployment for both services.

Pipeline flow:

```text
Source
  |
  v
Parallel Build
  +--> dce042-frontend-build -> dce042-frontend ECR
  +--> dce042-backend-build -> dce042-backend ECR
  |
  v
Parallel Deploy
  +--> dce042-frontend-deploy -> frontend-service
  +--> dce042-backend-deploy -> backend-service
```

CI/CD resources:

- One CodePipeline:
  - `dce042-dev-pipeline`
- Two CodeBuild projects:
  - `dce042-dev-frontend-build` using `buildspec-frontend.yml`
  - `dce042-dev-backend-build` using `buildspec-backend.yml`
- Two CodeDeploy applications or deployment groups:
  - `dce042-dev-frontend-deploy` / `dce042-dev-frontend-dg`
  - `dce042-dev-backend-deploy` / `dce042-dev-backend-dg`
- One S3 artifact bucket:
  - `dce042-dev-pipeline-artifacts-345594568549-ap-southeast-2`
- IAM roles for CodeBuild, CodeDeploy, and CodePipeline

Successful execution evidence:

```text
Pipeline execution: d7c43ff6-e7d1-4270-9be1-d968570da089
Source commit: f61b35361a24fb70af8eb329fd990651f5435161
Source stage: Succeeded
Parallel build stage: Succeeded
Parallel blue/green deploy stage: Succeeded

Frontend deployment: d-LFMSP6QVJ
Backend deployment:  d-V5NDI8QVJ

Frontend final task definition: dce042-frontend-task:3
Backend final task definition:  dce042-backend-task:3
```

Build outputs:

```text
Frontend ECR image tag: frontend-f61b35361a24
Backend ECR image tag:  backend-f61b35361a24
```

Both CodeBuild projects run in privileged mode so Docker images can be built and pushed to Amazon ECR. Each build outputs `imageDetail.json` for ECS blue/green deployment and `imagedefinitions.json` as a standard ECS deployment fallback.

Deployment artifact files:

| Service | AppSpec | Task Definition Template | Image Artifact |
| --- | --- | --- | --- |
| Frontend | `deploy/frontend/appspec.yml` | `deploy/frontend/taskdef.json` | `imageDetail.json` |
| Backend | `deploy/backend/appspec.yml` | `deploy/backend/taskdef.json` | `imageDetail.json` |

The task definition templates use `<IMAGE1_NAME>` as the container image placeholder. During deployment, CodeDeploy replaces this placeholder with the ECR image URI produced by the matching CodeBuild project.

During implementation, the first backend build failed because the GitHub source branch still pointed to an older Dockerfile. After pushing commit `f61b353`, backend CodeBuild succeeded. The first deploy attempt then failed because the CodePipeline role needed `ecs:RegisterTaskDefinition`; the permission was added and the failed deploy stage was retried successfully.

### Screenshot Placeholders

**Figure 37: CodePipeline overview**

`[Insert screenshot: pipeline stages]`

**Figure 38: Source stage succeeded**

`[Insert screenshot: Source stage success]`

**Figure 39: Parallel CodeBuild stages succeeded**

`[Insert screenshot: both build actions succeeded]`

**Figure 40: Parallel CodeDeploy stages succeeded**

`[Insert screenshot: both deploy actions succeeded]`

**Figure 40a: CodeDeploy lifecycle events**

`[Insert screenshot: BeforeInstall, Install, AllowTraffic lifecycle events succeeded]`

**Figure 40b: ECS blue/green task sets**

`[Insert screenshot: blue and green task sets during deployment or final primary task set after deployment]`

**Figure 41: CodeBuild logs for frontend image build**

`[Insert screenshot: frontend CodeBuild logs]`

**Figure 42: CodeBuild logs for backend image build**

`[Insert screenshot: backend CodeBuild logs]`

---

## 14. Blue/Green Deployment Design

Blue/green deployment reduces downtime and deployment risk. CodeDeploy creates a new version of the ECS task set, validates it through the green target group, and then shifts traffic from the old version to the new version.

Blue/green components:

- Blue target group
- Green target group
- Production listener
- CodeDeploy deployment group
- ECS service integration
- Rollback strategy

### Screenshot Placeholders

**Figure 43: Frontend CodeDeploy deployment group**

`[Insert screenshot: frontend deployment group]`

**Figure 44: Backend CodeDeploy deployment group**

`[Insert screenshot: backend deployment group]`

**Figure 45: Successful blue/green deployment**

`[Insert screenshot: CodeDeploy successful deployment]`

**Figure 46: ECS service updated after deployment**

`[Insert screenshot: ECS service showing new task definition revision]`

---

## 15. Azure Entra ID SAML Federation

Microsoft Entra ID is configured as the identity provider. AWS is configured as the service provider through an IAM SAML provider. Entra groups are mapped to AWS IAM roles.

Identity design:

| Entra Group | AWS IAM Role | Purpose |
| --- | --- | --- |
| `DevOpsEngineer` | `DevOpsEngineer` | Deployment and operational access |
| `ReadOnlyAuditor` | `ReadOnlyAuditor` | Read-only audit access |

SAML configuration:

```text
Identifier / Entity ID:
urn:amazon:webservices

Reply URL / ACS URL:
https://signin.aws.amazon.com/saml

Role claim:
https://aws.amazon.com/SAML/Attributes/Role

RoleSessionName claim:
https://aws.amazon.com/SAML/Attributes/RoleSessionName
```

### Screenshot Placeholders

**Figure 47: Entra Enterprise Application overview**

`[Insert screenshot: AWS Single-Account Access or AWS SAML Federation app]`

**Figure 48: SAML Basic Configuration**

`[Insert screenshot: Identifier and Reply URL]`

**Figure 49: SAML Attributes and Claims**

`[Insert screenshot: Role, RoleSessionName, SessionDuration claims]`

**Figure 50: Entra groups**

`[Insert screenshot: DevOpsEngineer and ReadOnlyAuditor groups]`

**Figure 51: Users and groups assignment**

`[Insert screenshot: assigned users/groups and app roles]`

**Figure 52: Federation Metadata XML download**

`[Insert screenshot: SAML certificate / metadata download area]`

**Figure 53: AWS IAM SAML provider**

`[Insert screenshot: AWS IAM identity provider]`

**Figure 54: AWS IAM roles for SAML federation**

`[Insert screenshot: DevOpsEngineer and ReadOnlyAuditor roles]`

**Figure 55: Conditional Access menu not available**

`[Insert screenshot: search result or menu showing Conditional Access is not available in the current tenant]`

**Figure 55a: Tenant properties page**

`[Insert screenshot: Microsoft Entra tenant Properties page]`

**Figure 55b: Security defaults status**

`[Insert screenshot: Manage security defaults showing the current status]`

Explanation:

```text
Conditional Access was reviewed during implementation. The current Microsoft Entra tenant did not expose the Conditional Access feature, so the implementation records the tenant limitation and Security defaults status as alternative evidence. In a production environment, Conditional Access would be configured to require MFA for the AWS SAML Enterprise Application.
```

**Figure 56: Successful AWS Console login through Entra ID**

`[Insert screenshot: AWS Console showing federated role session]`

---

## 16. Implementation Evidence

This section records the implementation process in sequence.

### Screenshot Placeholders

**Figure 57: AWS Budget configured before deployment**

`[Insert screenshot: AWS Budget / Billing Alert]`

**Figure 58: Terraform init**

`[Insert screenshot: terraform init command output]`

**Figure 59: Terraform validate**

`[Insert screenshot: terraform validate command output]`

**Figure 60: Terraform apply completed**

`[Insert screenshot: terraform apply summary]`

**Figure 61: Terraform outputs**

`[Insert screenshot: terraform output showing ALB DNS names and role ARNs]`

---

## 17. Validation and Testing

Validation confirms that the infrastructure was created correctly and the application is reachable through the load balancers.

Validation checklist:

- Terraform commands completed successfully
- Both ECR repositories contain images
- Both ECS services are running
- ALB target groups are healthy
- Frontend URL is reachable
- Backend URL is reachable
- CodePipeline completed successfully
- CodeDeploy blue/green deployment completed successfully
- CloudWatch logs are being written
- CloudWatch alarms exist
- SNS topic exists
- Entra SAML login to AWS works

### Screenshot Placeholders

**Figure 62: Frontend ALB endpoint working**

`[Insert screenshot: browser showing frontend ALB DNS]`

**Figure 63: Backend ALB endpoint working**

`[Insert screenshot: browser or curl output for backend ALB DNS]`

**Figure 64: ECS target health check passed**

`[Insert screenshot: healthy targets]`

**Figure 65: Pipeline validation result**

`[Insert screenshot: successful CodePipeline execution]`

---

## 18. Cost Control and Cleanup

Cost control was considered before deployment. The solution uses small Fargate task sizes, short CloudWatch log retention, DynamoDB on-demand billing, and avoids unnecessary resources where possible.

Cleanup steps:

```bash
terraform destroy -var-file=environments/dev/terraform.tfvars
```

Resources checked after cleanup:

- ALBs deleted
- ECS services and tasks deleted
- ECR repositories deleted or emptied
- CodePipeline deleted
- CodeBuild projects deleted
- CodeDeploy applications deleted
- S3 buckets deleted or emptied
- DynamoDB tables deleted
- CloudWatch alarms deleted
- SNS topics deleted
- IAM roles deleted
- VPC deleted

### Screenshot Placeholders

**Figure 66: Terraform destroy completed**

`[Insert screenshot: terraform destroy complete output]`

**Figure 67: AWS resource cleanup verification**

`[Insert screenshot: relevant AWS Console pages showing resources removed]`

**Figure 68: Billing dashboard after cleanup**

`[Insert screenshot: AWS Billing dashboard or cost explorer check]`

---

## 19. Challenges and Lessons Learned

### Challenges

- `<Describe challenge 1, such as configuring ECS blue/green deployment>`
- `<Describe challenge 2, such as SAML role claim mapping>`
- `<Describe challenge 3, such as controlling AWS costs during testing>`

### Lessons Learned

- Infrastructure as Code improves repeatability and reduces manual configuration errors.
- Security groups and IAM roles should be designed with least privilege.
- Remote Terraform state is important for collaboration and state locking.
- Blue/green deployment reduces downtime during service updates.
- SAML federation centralizes identity management and supports MFA enforcement.

---

## 20. Conclusion

This project demonstrates the use of Terraform to automate AWS infrastructure deployment for a containerized DevOps platform. The solution provisions networking, security, ECS Fargate services, ECR repositories, ALBs, autoscaling, monitoring, CI/CD pipeline resources, data storage, notifications, and Azure Entra ID SAML federation.

The implementation meets the assessment requirements by showing a repeatable IaC deployment, automated container delivery pipeline, high-availability service design, monitoring and alerting, and federated identity access.

---

## 21. References

- Terraform AWS Provider Documentation: https://registry.terraform.io/providers/hashicorp/aws/latest/docs
- AWS ECS Documentation: https://docs.aws.amazon.com/ecs/
- AWS Fargate Documentation: https://docs.aws.amazon.com/AmazonECS/latest/developerguide/AWS_Fargate.html
- AWS ECR Documentation: https://docs.aws.amazon.com/ecr/
- AWS CodePipeline Documentation: https://docs.aws.amazon.com/codepipeline/
- AWS CodeBuild Documentation: https://docs.aws.amazon.com/codebuild/
- AWS CodeDeploy ECS Blue/Green Documentation: https://docs.aws.amazon.com/codedeploy/latest/userguide/deployment-steps-ecs.html
- AWS IAM SAML Federation Documentation: https://docs.aws.amazon.com/IAM/latest/UserGuide/id_roles_providers_saml.html
- Microsoft Entra AWS Single-Account Access Tutorial: https://learn.microsoft.com/en-us/entra/identity/saas-apps/amazon-web-service-tutorial
- AWS Pricing Calculator: https://calculator.aws/

---

## 22. Appendix

### Appendix A: Terraform Commands

```bash
terraform fmt
terraform init
terraform validate
terraform plan -var-file=environments/dev/terraform.tfvars
terraform apply -var-file=environments/dev/terraform.tfvars
terraform output
terraform destroy -var-file=environments/dev/terraform.tfvars
```

### Appendix B: Local Application Commands

```bash
docker compose up --build
```

### Appendix C: Screenshot Naming Convention

Recommended screenshot naming:

```text
figure-01-final-application.png
figure-02-requirements-checklist.png
figure-03-repository-structure.png
figure-04-architecture-diagram.png
...
figure-68-billing-dashboard-after-cleanup.png
```

### Appendix D: Final Submission Checklist

- [ ] Report exported as PDF
- [ ] Presentation file prepared
- [ ] Architecture diagram included
- [ ] Screenshots numbered and captioned
- [ ] Terraform code included or linked
- [ ] AWS resources validated
- [ ] Azure Entra ID SSO validated
- [ ] Cleanup completed
- [ ] No secrets included in report
