# DCE04.2 Assessment 1 Step-by-Step Guide

本文档用于指导完成 DCE04.2 Assessment 1：AWS Containerized DevOps Pipeline: Microservices Deployment。目标是在真正创建云资源之前先完成规划、本地验证、费用控制和代码准备，避免因为反复试错产生不必要费用。

> 重要：不要一开始就运行 `terraform apply`。先完成本文档第 1 到第 5 部分，再用 `terraform plan` 检查资源清单，最后才短时间创建资源、截图验证、销毁资源。

> 云端配置：无，仅作业分析和报告规划

## 1. 作业目标和交付物

> 云端配置：无，仅作业需求分析

### 1.1 作业核心目标

本作业要求使用 Terraform 创建一套 AWS 容器化 DevOps 平台，包含：

- AWS VPC、public subnets、route tables、Internet Gateway
- 两个 Application Load Balancers，每个服务一个
- 两个 ECR repositories，每个服务一个
- 一个 ECS Fargate cluster
- 两个 ECS task definitions
- 两个 ECS services
- 四个 autoscaling policies
- 四个 CloudWatch CPU alarms
- CloudWatch logs
- 两个 S3 buckets：pipeline artifacts bucket 和 application assets bucket
- 一个 DynamoDB table
- 一个 SNS topic
- IAM roles and policies for ECS, CodeBuild, CodeDeploy, CodePipeline
- 两个 CodeBuild projects
- 两个 CodeDeploy applications / deployment groups
- 一个 CodePipeline：Source -> Parallel Builds -> Parallel Blue/Green Deploys
- Terraform local state 证据
- Terraform remote backend：S3 backend + DynamoDB locking，或 Terraform Cloud
- Azure Entra ID SAML SSO 到 AWS
- README、架构图、截图和技术说明报告

> 云端配置：无，仅报告和演示文件准备

### 1.2 最终需要提交

建议最终提交一个 PDF report 和一个 presentation 文件。报告中应包含：

- 封面：姓名、学号、课程、Assessment title
- 目录
- 作业需求分析
- 架构图
- Terraform 项目结构说明
- AWS 网络设计说明
- ECS / ECR / ALB 设计说明
- CI/CD pipeline 设计说明
- Blue/Green deployment 说明
- IAM 和 Security Group 安全说明
- Azure Entra ID federation 说明
- Monitoring / autoscaling / SNS 说明
- Terraform state management 说明
- 每一步截图证据
- 测试和验证结果
- Cleanup 资源删除证明

> 云端配置：AWS Billing Console + Microsoft Entra admin center (Azure) + 本地工具

## 2. 费用控制和准备工作

> 云端配置：AWS Pricing Calculator / AWS Billing Console

### 2.1 先确认哪些资源可能收费

以下资源即使流量很小，也可能产生费用：

- Application Load Balancer：按小时和 LCU 计费
- NAT Gateway：按小时和数据处理量计费，常见误收费来源
- ECS Fargate tasks：按 vCPU、memory、storage 运行时间计费
- CodeBuild：按 build 执行时间和 compute type 计费
- CodePipeline：active pipeline 可能按月计费
- CloudWatch logs / metrics / alarms：存储和告警可能收费
- S3：存储、请求和数据传输可能收费
- DynamoDB：如果选择 provisioned capacity，可能持续收费
- Public IPv4 地址、数据传输、Route 53、ACM 等也可能产生费用


> 云端配置：AWS Console，主要影响 VPC、ALB、ECS、CodePipeline、CloudWatch 的创建方式

### 2.2 建议费用控制策略

为了完成作业但控制费用，建议：

- 使用一个低成本 region，例如 `ap-southeast-2` 或老师指定 region。
- 不使用 NAT Gateway，除非老师明确要求。可以把 ECS tasks 放在 public subnets 并启用 `assign_public_ip = true`，但 security group 只允许 ALB security group 入站访问。
- ECS service desired count 先设置为 `1`。需要展示 HA 时，报告中解释 target subnets 跨两个 AZ，截图展示配置；验证后立即销毁。
- Fargate task 使用小规格，例如 `cpu = 256`、`memory = 512`。
- DynamoDB 使用 on-demand billing。
- CloudWatch log retention 设置为 1 到 3 天。
- 不购买域名，不配置 Route 53，除非课程明确要求。
- HTTPS 可以使用 ACM，但如果没有域名，报告中说明生产环境应使用 HTTPS，本次 demo 使用 HTTP listener。评分标准提到 HTTPS，若要争取高分，建议使用已有域名和 ACM 证书。
- 所有资源加统一 tag，例如 `Project = dce042-assessment1`，方便查找和删除。
- 完成截图后立即运行 `terraform destroy`。

> 云端配置：AWS Console + 本地 AWS CLI

### 2.3 AWS 账号准备

在创建资源前完成：

1. 登录 AWS Console。
2. 开启 Billing Alert / Budget。
3. 创建预算，例如日预算或总预算。
4. 确认当前用户有创建 VPC、ECS、ECR、IAM、S3、DynamoDB、CodePipeline、CodeBuild、CodeDeploy、CloudWatch、SNS 的权限。
5. 安装并配置 AWS CLI：

```bash
aws configure
aws sts get-caller-identity
```

6. 安装 Terraform：

```bash
terraform version
Terraform v1.15.8
on darwin_arm64
```

7. 本地安装 Docker，并确认可以 build 镜像：

```bash
docker version
docker compose up --build
```

> 云端配置：Microsoft Entra admin center (Azure) + AWS IAM Console / Terraform

### 2.4 Azure Entra ID 准备

Azure 部分主要用于身份联合登录 AWS：

本作业明确要求 `aws_iam_saml_provider`，因此这里按 Microsoft Entra ID Enterprise Application + AWS IAM SAML provider 的方式准备。AWS IAM Identity Center 也是常见生产方案，但本作业报告中优先展示 IAM SAML provider。

> 云端配置：Microsoft Entra admin center (Azure)

#### 2.4.1 确认 Azure 权限

1. 打开 Microsoft Entra admin center:
   - https://entra.microsoft.com
2. 确认可以进入：
   - `Identity`
   - `Applications`
   - `Enterprise applications`
   - `Groups`
   - `Protection`
   - `Conditional Access`
3. 确认当前账号至少具备以下其中一种能力：
   - 可以创建 Enterprise Application
   - 可以创建 Groups
   - 可以配置 Single sign-on
   - 可以查看或创建 Conditional Access policy
4. 截图保存：
   - Entra tenant overview
   - 当前登录用户或 tenant name

如果学校或个人 Azure tenant 不允许创建 Conditional Access policy，可以在报告中说明权限限制，并提供 MFA / Security defaults / per-user MFA 的可见配置截图作为替代证据。

> 云端配置：Microsoft Entra admin center (Azure)

#### 2.4.2 创建两个 Entra groups

创建两个 group，用于后续映射到 AWS IAM roles。

1. 在 Entra admin center 打开：
   - `Identity` -> `Groups` -> `All groups`
2. 点击 `New group`。
3. 创建第一个 group：
   - Group type: `Security`
   - Group name: ` myDevOpsEngineer`
   - Membership type: `Assigned`
4. 创建第二个 group：
   - Group type: `Security`
   - Group name: `myReadOnlyAuditor`
   - Membership type: `Assigned`
5. 把测试用户加入 group：
   - 自己的账号加入 `myDevOpsEngineer`
   - 可选：另一个测试账号加入 `myReadOnlyAuditor`
6. 截图保存：
   - 两个 groups 列表
   - 每个 group 的 members 页面



报告说明建议：

```text
DevOpsEngineer maps to an AWS IAM role with deployment-related permissions.
ReadOnlyAuditor maps to an AWS IAM role with read-only permissions.
```

> 云端配置：Microsoft Entra admin center (Azure)

#### 2.4.3 创建 AWS Enterprise Application

1. 打开：
   - `Identity` -> `Applications` -> `Enterprise applications`
2. 点击 `New application`。
3. 搜索 AWS 相关应用。
4. 如果可用，优先选择：
   - `AWS Single-Account Access`
6. 创建后进入该 Enterprise Application。
7. 截图保存：
   - Enterprise application overview
   - Application name

> 云端配置：AWS IAM Console / Terraform outputs + Microsoft Entra admin center (Azure)

#### 2.4.4 先准备 AWS 侧信息

配置 Entra SAML 前，需要知道 AWS SAML 登录使用的固定值，以及后续 Terraform 会创建的 AWS role ARN。

AWS IAM SAML provider 方式常用值：

```text
Identifier / Entity ID:
urn:amazon:webservices

Reply URL / Assertion Consumer Service URL:
https://signin.aws.amazon.com/saml

Sign on URL:
https://signin.aws.amazon.com/saml
```

后续 Terraform 创建完成后，还需要把这些 ARN 用于 SAML claims：

```text
SAML provider ARN:
arn:aws:iam::<account-id>:saml-provider/<provider-name>

DevOpsEngineer role ARN:
arn:aws:iam::<account-id>:role/DevOpsEngineer

ReadOnlyAuditor role ARN:
arn:aws:iam::<account-id>:role/ReadOnlyAuditor
```

注意：如果 Terraform 还没有创建 IAM role，可以先完成 Entra 应用创建和 Basic SAML Configuration，role mapping 可等 AWS IAM role 创建后再补。

> 云端配置：Microsoft Entra admin center (Azure)

#### 2.4.5 配置 SAML Single Sign-On

1. 进入刚创建的 Enterprise Application。
2. 打开：
   - `Single sign-on`
3. 选择：
   - `SAML`
4. 在 `Basic SAML Configuration` 中填写：
   - Identifier / Entity ID: `urn:amazon:webservices`
   - Reply URL / ACS URL: `https://signin.aws.amazon.com/saml`
   - Sign on URL: `https://signin.aws.amazon.com/saml`
5. 保存配置。
6. 截图保存：
   - Basic SAML Configuration

> 云端配置：Microsoft Entra admin center (Azure)，需要 AWS IAM role ARN 和 SAML provider ARN

#### 2.4.6 配置 SAML attributes and claims

AWS 需要 SAML assertion 中包含 role 信息。常见 claim：

```text
https://aws.amazon.com/SAML/Attributes/Role
https://aws.amazon.com/SAML/Attributes/RoleSessionName
https://aws.amazon.com/SAML/Attributes/SessionDuration
```

建议准备：

```text
Role:
arn:aws:iam::<account-id>:role/DevOpsEngineer,arn:aws:iam::<account-id>:saml-provider/<provider-name>

RoleSessionName:
user.userprincipalname

SessionDuration:
3600
```

如果需要同时支持 `DevOpsEngineer` 和 `ReadOnlyAuditor`，可以使用 group-based claims 或为不同用户/group 配置不同 role claim。最简单的 assessment demo 可以先验证一个 role，例如 `DevOpsEngineer`，然后在报告中说明第二个 group 对应第二个 IAM role。

截图保存：

- Attributes and Claims 页面
- Role claim 配置
- RoleSessionName claim 配置

> 云端配置：Microsoft Entra admin center (Azure)

#### 2.4.7 分配用户或 groups 到 Enterprise Application

1. 进入 Enterprise Application。
2. 打开：
   - `Users and groups`
3. 点击：
   - `Add user/group`
4. 添加：
   - `DevOpsEngineer`
   - `ReadOnlyAuditor`
5. 如果 gallery app 支持 application roles，可以把 group 分配到对应 app role。
6. 截图保存：
   - Users and groups assignment 页面

> 云端配置：Microsoft Entra admin center (Azure)，文件后续给 AWS IAM / Terraform 使用

#### 2.4.8 下载 Federation Metadata XML

1. 进入 Enterprise Application。
2. 打开：
   - `Single sign-on` -> `SAML`
3. 找到：
   - `SAML Certificates`
4. 下载：
   - `Federation Metadata XML`
5. 保存到本地安全位置，例如：

```text
terraform/secrets/entra-federation-metadata.xml
```

注意：

- 不建议把 metadata XML 提交到公开 GitHub。
- 如果课程要求提交 Terraform 代码，可以使用变量传入 metadata document，或在报告中截图说明 metadata 来源。
- Terraform 中 `aws_iam_saml_provider` 需要使用这个 metadata document。

> 云端配置：Microsoft Entra admin center (Azure)

#### 2.4.9 准备 MFA / Conditional Access 证据

推荐使用 Conditional Access policy 强制 AWS Enterprise Application 登录时需要 MFA。但是 Conditional Access 通常需要 Microsoft Entra ID P1 或包含 P1 的 Microsoft 365 plan。如果当前 tenant 左侧菜单没有 `Protection` 或 `Conditional Access`，可以使用本节的替代证据方案。

#### Option A: 有 Conditional Access 时

1. 在 Entra admin center 顶部搜索栏搜索：
   - `Conditional Access`
2. 如果能找到，打开：
   - `Protection` -> `Conditional Access`
3. 点击：
   - `Create new policy`
4. Policy name:
   - `Require MFA for AWS SAML Federation`
5. Users:
   - Include: `DevOpsEngineer` 和 `ReadOnlyAuditor`
   - 如果当前 plan 不支持 group assignment，则选择测试用户个人账号
6. Target resources:
   - Cloud apps: 选择 `AWS SAML Federation - DCE042` 或对应 AWS Enterprise Application
7. Conditions:
   - 可以保持默认，或按需要限制 location/device
8. Access controls:
   - Grant
   - 勾选 `Require multifactor authentication`
9. Enable policy:
   - 如果担心锁定账号，先选择 `Report-only`
   - 验证无误后再选择 `On`
10. 截图保存：
   - Conditional Access policy overview
   - Users/groups targeting
   - Target application
   - Grant control requiring MFA

#### Option B: 没有 Protection / Conditional Access 菜单时

如果看不到 `Protection` 或 `Conditional Access`，按下面方式处理：

1. 在 Entra admin center 顶部搜索：
   - `Conditional Access`
2. 如果搜索不到，打开：
   - `Identity` -> `Overview` -> `Properties`
3. 找到：
   - `Manage security defaults`
4. 如果可以开启，设置：
   - Security defaults: `Enabled`
5. 截图保存：
   - 找不到 Conditional Access 的菜单或搜索结果
   - Tenant properties 页面
   - Security defaults 状态

当前项目采用此 Option B 替代证据路径。已完成的证据包括：

- [x] 找不到 Conditional Access 的菜单或搜索结果截图
- [x] Tenant properties 页面截图
- [x] Security defaults 状态截图

如果 `Manage security defaults` 也不可用，使用下面任意一种替代证据：

- 截图当前 tenant plan 或错误提示，证明 Conditional Access 不可用
- 截图 Microsoft Entra plan limitation 信息
- 截图用户账号的 MFA / authentication methods 页面
- 截图登录 AWS SAML application 时出现 MFA challenge
- 在报告中说明生产环境将使用 Conditional Access 强制 AWS SSO MFA

报告说明建议：

```text
Conditional Access was not available in the current Microsoft Entra tenant because it requires a higher Entra plan such as Entra ID P1. As an alternative, the project records the tenant limitation and uses available MFA/security defaults evidence. In a production environment, Conditional Access would be configured to require MFA for the AWS SAML Enterprise Application.
```

安全提醒：

- 不要把所有管理员账号都锁进新 policy，除非确认有 break-glass account。
- 如果没有 Conditional Access 权限，至少截图证明测试用户已启用 MFA，或 tenant security defaults 已启用。

> 云端配置：Microsoft Entra admin center (Azure) + AWS IAM Console / Terraform

#### 2.4.10 Azure 准备阶段检查清单

- [ ] 可以登录 Microsoft Entra admin center
- [ ] 已创建 `DevOpsEngineer` group
- [ ] 已创建 `ReadOnlyAuditor` group
- [ ] 测试用户已加入 group
- [ ] 已创建 AWS Enterprise Application
- [ ] 已配置 SAML Basic Configuration
- [ ] 已准备 Role / RoleSessionName claims
- [ ] 已分配 groups 到 Enterprise Application
- [ ] 已下载 Federation Metadata XML
- [ ] 已准备 MFA / Conditional Access 截图证据

> 云端配置：无，主要检查本地代码是否能部署成两个 AWS ECS services

## 3. 当前代码是否满足两个程序要求

> 云端配置：无，本地仓库结构检查

### 3.1 当前项目现状

当前仓库已有两个可容器化程序：

- `backend/`：Spring Boot API，已有 `backend/Dockerfile`
- `frontend/`：React + Vite + Nginx，已有 `frontend/Dockerfile`

也就是说，当前项目可以被部署成两个 ECS Fargate services：

- Service 1: `frontend-service`，来自 `frontend/`
- Service 2: `backend-service`，来自 `backend/`

每个服务都可以有：

- 独立 ECR repository
- 独立 CodeBuild project
- 独立 task definition
- 独立 ECS service
- 独立 ALB
- 独立 CodeDeploy blue/green deployment group

> 云端配置：无，作业方案判断

### 3.2 是否完全满足“两个 microservices”

结论：本项目选择使用当前 `frontend` + `backend` 作为两个独立容器服务。这是最低风险、最贴合作业 DevOps/IaC 评分重点的方案。

原因：

- 作业写的是 two microservices，通常指两个后端业务服务。
- 当前项目是一个 frontend 加一个 backend。Frontend 是一个独立容器服务，但严格来说更像 web UI，不是业务 microservice。
- 本作业主要评分点是 AWS IaC、ECS、ALB、CI/CD、Blue/Green、Monitoring 和 Entra SSO，因此两个独立 containerized services 可以支撑作业实现。
- 报告中需要明确说明：本实现部署的是两个 independently containerized services，其中 frontend service 负责 UI，backend service 负责 API。
- 如果老师严格要求两个后端 microservices，可在后续扩展为 `users-service` 和 `posts-service`，但本次不采用。

> 云端配置：无，部署方案选择

### 3.3 推荐方案

最终选择：**使用当前 `frontend` + `backend` 作为两个 ECS Fargate 服务，并采用完全 DynamoDB 数据路线**。

后续所有 Terraform、ECR、ECS、ALB、CodeBuild、CodeDeploy 和 CodePipeline 步骤都按以下两个服务设计：

```text
frontend-service:
  source folder: frontend/
  Dockerfile: frontend/Dockerfile
  container port: 80
  ECR: dce042-frontend
  ALB: frontend-alb
  ECS task definition: frontend-task
  ECS service: frontend-service
  CodeBuild project: dce042-frontend-build
  CodeDeploy app/deployment group: dce042-frontend-deploy

backend-service:
  source folder: backend/
  Dockerfile: backend/Dockerfile
  container port: 8080
  ECR: dce042-backend
  ALB: backend-alb
  ECS task definition: backend-task
  ECS service: backend-service
  CodeBuild project: dce042-backend-build
  CodeDeploy app/deployment group: dce042-backend-deploy
```

> 云端配置：AWS ECS / ECR / ALB / CodeBuild / CodeDeploy，后续由 Terraform 创建

#### 服务部署方案：使用当前 frontend + backend 作为两个 ECS 服务

本项目采用此方案。

适合目标：

- 快速完成作业
- 不拆分现有前后端项目
- 重点展示 AWS DevOps 和 IaC

服务设计：

```text
frontend service:
  Dockerfile: frontend/Dockerfile
  container port: 80
  ECR: dce042-frontend
  ALB: frontend-alb
  ECS service: frontend-service

backend service:
  Dockerfile: backend/Dockerfile
  container port: 8080
  ECR: dce042-backend
  ALB: backend-alb
  ECS service: backend-service
```

需要补充：

- 两个 buildspec 文件，分别 build frontend 和 backend 镜像
- 两个 ECS task definition 模板
- 两个 CodeDeploy appspec 文件
- Frontend Nginx/API 配置需要适配 ECS 环境，不能继续只使用 Docker Compose 内部主机名 `backend`
- Backend 使用 DynamoDB 作为唯一应用数据存储
- users、posts、raw source 和 transaction demo 都写入 Terraform 创建的同一张 DynamoDB table

> 云端配置：AWS ECS / ECR / ALB / CodeBuild / CodeDeploy，后续由 Terraform 创建

#### 备选服务方案：新增第二个后端 microservice

本项目不采用此方案，仅作为备选。

适合目标：

- 更严格满足 two microservices
- 报告更容易解释成真正微服务架构

设计例子：

```text
users-service:
  GET /api/users
  GET /api/users/{id}

posts-service:
  GET /api/users/{id}/posts
  GET /api/posts
```

缺点：

- 需要拆分 Spring Boot 项目或新增第二个后端项目
- 前端 API 调用要改
- 测试、Dockerfile、pipeline 都要多维护一套

> 云端配置：无，方案决策

#### 建议选择

本作业的评分重点是 AWS IaC 和 CI/CD，不是复杂业务微服务拆分。因此本项目选择当前两个容器服务，并在报告里明确说明：

> The solution deploys two independently containerized services: a React/Nginx frontend service and a Spring Boot backend API service. Each service has its own ECR repository, ECS task definition, ECS service, ALB, CodeBuild project, and CodeDeploy blue/green deployment group.

后续文档、Terraform 命名、截图和报告都统一使用：

- `frontend-service`
- `backend-service`

如果老师坚持 two backend microservices，再升级到 `users-service` / `posts-service` 拆分；当前 assessment implementation 不按此备选路线执行。

> 云端配置：无，先在本地 Docker / application code 验证

## 4. 本地代码准备

> 云端配置：无，本地 Docker Compose 验证

### 4.1 本地运行验证

先确保当前程序本地可以跑。完全 DynamoDB 路线不需要额外数据库容器，但需要 AWS 凭证和 DynamoDB table：

```bash
aws dynamodb create-table \
  --table-name dce042-users-posts \
  --attribute-definitions AttributeName=pk,AttributeType=S AttributeName=sk,AttributeType=S \
  --key-schema AttributeName=pk,KeyType=HASH AttributeName=sk,KeyType=RANGE \
  --billing-mode PAY_PER_REQUEST \
  --region ap-southeast-2
```

如果 table 已经存在，跳过创建即可。然后启动本地容器：

```bash
docker compose up --build
```

验证：

- Frontend: http://localhost:3000
- Backend: http://localhost:8080
- DynamoDB table: `dce042-users-posts`

截图：

- Docker Compose containers running
- Frontend 页面
- Backend health 或 root endpoint
- DynamoDB table items after sync

> 云端配置：AWS ALB / ECS 会使用健康检查，本节先改本地应用代码

### 4.2 补健康检查 endpoint

状态：已完成。

ECS / ALB 需要健康检查。本项目当前 health check 路径如下：

```text
frontend-service: GET /health
backend-service:  GET /actuator/health
```

Backend 已有 Spring Boot Actuator health endpoint：

```text
GET /actuator/health
```

Backend root endpoint 也可用于人工检查：

```text
GET /
```

Frontend Nginx 已新增显式健康检查 endpoint：

```text
GET /health
```

必要检查：

- `frontend/` 当前使用 Nginx 提供静态页面，健康检查路径使用 `/health`。
- `backend/` 当前已有 Spring Boot actuator health，可优先使用 `/actuator/health`。
- ECS ALB target group 的 health check path 建议：
  - frontend target group: `/health`
  - backend target group: `/actuator/health`

验证命令：

```bash
cd backend
./mvnw -q -Dtest=BackendRootEndpointTests test

cd ../frontend
npm run build

cd ..
docker compose up -d --build --wait
curl http://localhost:3000/health
curl http://localhost:8080/actuator/health
```

> 云端配置：AWS ECS / DynamoDB / S3，先准备本地应用配置调整

### 4.2.1 完全 DynamoDB 的云端运行调整

当前 `frontend` + `backend` 可以作为两个 ECS 服务，但云端运行前必须处理两个配置差异：

1. Frontend 在 Docker Compose 中通过 Nginx 代理到 `http://backend:8080`。这个主机名只在本地 Docker Compose 网络里存在，在 ECS 中不能直接使用。
2. Backend 直接访问 DynamoDB。云端 ECS 不需要额外数据库服务或数据库容器。

本项目后续按以下方式处理：

```text
frontend-service:
  继续使用 React + Nginx 容器
  Nginx /api proxy 在 ECS 环境中指向 backend-alb DNS name

backend-service:
  继续使用 Spring Boot 容器
  通过 AWS SDK 访问 DynamoDB
  users/posts/raw source/transaction demo 存入同一张 DynamoDB table
  使用 ECS task role 授权 DynamoDB，不在容器中写 AWS secret key
```

报告中建议写：

```text
The backend uses DynamoDB as the application data store. JSONPlaceholder users and posts, raw source records, and demo transaction records are stored in one Terraform-managed DynamoDB table using a pk/sk single-table design. The ECS task role grants the backend the required DynamoDB permissions without storing AWS access keys in the container.
```

不部署额外关系型数据库，因为：

- 作业明确要求 DynamoDB table
- 额外数据库服务不在资源清单中
- 额外数据库服务会增加费用和安全配置复杂度
- 当前代码已经可以使用 DynamoDB 满足 users/posts 持久化和 demo transaction

> 云端配置：AWS CodeBuild / ECR，先准备本地 pipeline 配置文件

### 4.3 准备两个 buildspec 文件

状态：已完成。根目录现在保留两个独立 buildspec：

```text
buildspec-frontend.yml
buildspec-backend.yml
```

CodeBuild project 需要分别指定 buildspec path：

```text
dce042-frontend-build -> buildspec-frontend.yml
dce042-backend-build  -> buildspec-backend.yml
```

重要：两个 CodeBuild project 都需要开启 **Privileged mode**，因为 buildspec 会执行 `docker build` 和 `docker push`。

Frontend buildspec 负责：

1. 登录 ECR
2. 执行 `npm ci`
3. 执行 `npm test -- --run`
4. build image: `dce042-frontend`
5. tag image: `frontend-<commit-sha>`
5. push 到 `dce042-frontend` ECR
6. 输出 deploy artifact：
   - `imageDetail.json`，给 ECS Blue/Green CodeDeploy 使用
   - `imagedefinitions.json`，给普通 ECS deploy 备用

Backend buildspec 负责：

1. 登录 ECR
2. 执行 Maven backend tests，但跳过本地 Docker Compose smoke test
3. build image: `dce042-backend`
4. tag image: `backend-<commit-sha>`
5. push 到 `dce042-backend` ECR
6. 输出 deploy artifact：
   - `imageDetail.json`，给 ECS Blue/Green CodeDeploy 使用
   - `imagedefinitions.json`，给普通 ECS deploy 备用

注意：`deploy/frontend/appspec.yml`、`deploy/frontend/taskdef.json`、`deploy/backend/appspec.yml`、`deploy/backend/taskdef.json` 已在 4.4 准备。buildspec 会把它们复制到对应的 CodeBuild output artifact 中。

> 云端配置：AWS CodeDeploy / ECS Blue/Green，先准备本地 deployment artifacts

### 4.4 准备 CodeDeploy 文件

状态：已完成。当前新增了：

```text
deploy/
  frontend/
    appspec.yml
    taskdef.json
  backend/
    appspec.yml
    taskdef.json
```

这些文件用于 ECS blue/green deployment。

每个服务的 buildspec 会把对应文件复制到 CodeBuild output artifact：

```text
frontend build output:
  appspec.yml
  taskdef.json
  imageDetail.json
  imagedefinitions.json

backend build output:
  appspec.yml
  taskdef.json
  imageDetail.json
  imagedefinitions.json
```

CodeDeploy ECS blue/green 主要使用：

```text
appspec.yml
taskdef.json
imageDetail.json
```

`imagedefinitions.json` 是普通 ECS deploy 的备用 artifact。

Frontend `appspec.yml` 绑定：

```text
ECS service: frontend-service
container name: frontend
container port: 80
target groups: frontend-blue-tg / frontend-green-tg
```

Backend `appspec.yml` 绑定：

```text
ECS service: backend-service
container name: backend
container port: 8080
target groups: backend-blue-tg / backend-green-tg
```

Frontend `taskdef.json` 重点：

```text
family: dce042-frontend-task
container: frontend
image: <IMAGE1_NAME>
port: 80
environment:
  BACKEND_UPSTREAM=<backend ALB DNS name>
log group: /ecs/dce042-frontend
```

Backend `taskdef.json` 重点：

```text
family: dce042-backend-task
container: backend
image: <IMAGE1_NAME>
port: 8080
environment:
  APP_DYNAMODB_ENABLED=true
  DYNAMODB_TABLE_NAME=dce042-users-posts
  AWS_REGION=<region>
log group: /ecs/dce042-backend
```

`<IMAGE1_NAME>` 由 CodeDeploy ECS deploy action 使用 `imageDetail.json` 替换成本次 build 推送到 ECR 的 image URI。

CodeBuild project 需要配置的环境变量：

```text
frontend build:
  BACKEND_ALB_DNS_NAME=<backend ALB DNS name>
  FRONTEND_EXECUTION_ROLE_ARN=<optional; default uses arn:aws:iam::<account>:role/dce042-ecs-task-execution-role>
  FRONTEND_TASK_ROLE_ARN=<optional; default uses arn:aws:iam::<account>:role/dce042-frontend-task-role>

backend build:
  DYNAMODB_TABLE_NAME=dce042-users-posts
  BACKEND_EXECUTION_ROLE_ARN=<optional; default uses arn:aws:iam::<account>:role/dce042-ecs-task-execution-role>
  BACKEND_TASK_ROLE_ARN=<optional; default uses arn:aws:iam::<account>:role/dce042-backend-task-role>
```

注意：如果 Terraform 中 IAM role 名称不是上面的默认名称，就必须在 CodeBuild project 里显式设置对应 ARN 环境变量。

> 云端配置：无，本地 Terraform 文件结构准备

## 5. Terraform 项目结构

状态：已完成。当前 Terraform 项目结构如下：

```text
terraform/
  README.md
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
      main.tf
      variables.tf
      outputs.tf

    security/
      main.tf
      variables.tf
      outputs.tf

    ecr/
      main.tf
      variables.tf
      outputs.tf

    alb/
      main.tf
      variables.tf
      outputs.tf

    ecs/
      main.tf
      variables.tf
      outputs.tf

    autoscaling/
      main.tf
      variables.tf
      outputs.tf

    data/
      main.tf
      variables.tf
      outputs.tf

    monitoring/
      main.tf
      variables.tf
      outputs.tf

    cicd/
      main.tf
      variables.tf
      outputs.tf

    entra-federation/
      main.tf
      variables.tf
      outputs.tf
```

当前第 5 步只创建 Terraform structure 和 module skeleton，不创建 AWS resources。这样可以先验证项目组织、变量命名和模块边界，避免提前产生费用。

已完成本地验证：

```bash
cd terraform
terraform fmt -recursive
terraform init
terraform validate
terraform plan -var-file=environments/dev/terraform.tfvars
```

验证结果：

```text
terraform validate: Success
terraform plan: only output values, no AWS resources to create
```

注意：`terraform init` 已生成 `.terraform.lock.hcl`，建议提交到版本库，用于锁定 provider 版本。`.terraform/`、state、plan、secrets 已加入 `.gitignore`。

> 云端配置：AWS S3 + AWS DynamoDB，本地执行 Terraform bootstrap

## 6. Terraform Backend 准备

Terraform remote backend 本身通常要先创建一次。建议用单独 bootstrap：

```text
terraform-bootstrap/
  main.tf
```

创建：

- S3 bucket for Terraform state
- DynamoDB table for Terraform locking

步骤：

```bash
cd terraform-bootstrap
terraform init
terraform plan
terraform apply
```

状态：已完成。

已创建：

```text
S3 state bucket:
  dce042-terraform-state-345594568549-ap-southeast-2

DynamoDB lock table:
  dce042-terraform-locks

Terraform state key:
  dce042/dev/terraform.tfstate

Region:
  ap-southeast-2
```

实际执行结果：

```text
terraform-bootstrap apply:
  6 added, 0 changed, 0 destroyed

main terraform init:
  Successfully configured the backend "s3"

main terraform apply:
  0 added, 0 changed, 0 destroyed
  remote state object written to S3
```

AWS 验证结果：

```text
S3 state object:
  dce042/dev/terraform.tfstate

S3 versioning:
  Enabled

S3 public access block:
  all public access blocked

DynamoDB lock table:
  ACTIVE
  billing mode: PAY_PER_REQUEST
  key: LockID string
```

注意：当前 Terraform 版本会提示 `dynamodb_table` backend 参数 deprecated，建议使用 `use_lockfile`。本作业要求展示 S3 backend + DynamoDB locking，因此本项目保留 DynamoDB lock table 配置作为作业证据。

截图：

- `terraform init`
- `terraform apply`
- S3 backend bucket
- DynamoDB lock table

然后在主 `terraform/backend.tf` 配置：

```hcl
terraform {
  backend "s3" {
    bucket         = "your-terraform-state-bucket"
    key            = "dce042/dev/terraform.tfstate"
    region         = "ap-southeast-2"
    dynamodb_table = "your-terraform-lock-table"
    encrypt        = true
  }
}
```

主项目初始化：

```bash
cd terraform
terraform init
terraform plan -var-file=environments/dev/terraform.tfvars
```

> 云端配置：AWS VPC Console，由 Terraform 创建网络资源

## 7. AWS 网络模块

> 云端配置：AWS VPC / Subnets / Route Tables / Internet Gateway

### 7.1 要创建的资源

- VPC：已创建 `dce042-dev-vpc`
- Two public subnets across two AZs：已创建 `dce042-dev-public-1` 和 `dce042-dev-public-2`
- Internet Gateway：已创建 `dce042-dev-igw`
- Route table：已创建 `dce042-dev-public-rt`
- Route table associations：两个 public subnets 已关联 public route table
- Optional NAT Gateway：本项目默认不创建，避免 NAT Gateway 持续小时费用

> 云端配置：AWS VPC / ECS networking，费用控制重点是避免 NAT Gateway

### 7.2 推荐低成本设计

为了控制费用：

- ALB 放 public subnets
- ECS tasks 也先放 public subnets
- ECS tasks 开启 public IP
- ECS task security group 只允许 ALB security group 访问
- 不创建 NAT Gateway

报告中说明：生产环境建议 ECS tasks 放 private subnets，并通过 NAT Gateway 或 VPC endpoints 访问外部服务。本次 assessment demo 为控制费用使用 public subnet + strict SG。

### 7.2.1 Terraform 执行结果

执行目录：

```bash
cd terraform
terraform plan -var-file=environments/dev/terraform.tfvars -out=tfplan
terraform apply -auto-approve tfplan
```

实际结果：

```text
Apply complete! Resources: 7 added, 0 changed, 0 destroyed.
```

已创建资源：

```text
VPC:
  Name: dce042-dev-vpc
  ID: vpc-0cf7603ca02199d54
  CIDR: 10.42.0.0/16
  State: available

Public subnet 1:
  Name: dce042-dev-public-1
  ID: subnet-0bf82506506e9f5af
  CIDR: 10.42.1.0/24
  AZ: ap-southeast-2a
  Auto-assign public IPv4: enabled

Public subnet 2:
  Name: dce042-dev-public-2
  ID: subnet-08fd5fc4ae92dee09
  CIDR: 10.42.2.0/24
  AZ: ap-southeast-2b
  Auto-assign public IPv4: enabled

Internet Gateway:
  Name: dce042-dev-igw
  ID: igw-019a80d8c586cf5cd
  Attached VPC: vpc-0cf7603ca02199d54

Public route table:
  Name: dce042-dev-public-rt
  ID: rtb-0d3b9d15ab10779fc
  Route: 0.0.0.0/0 -> igw-019a80d8c586cf5cd

NAT Gateway:
  Not created
```

Terraform output:

```bash
terraform output network
```

> 云端配置：AWS VPC Console

### 7.3 截图证据

- VPC resource map
- Subnets across two AZs
- Route table with `0.0.0.0/0 -> Internet Gateway`
- Security groups
- Terraform apply output showing `7 added`
- Terraform output `network`

> 云端配置：AWS VPC Security Groups + AWS IAM

## 8. Security Groups 和 IAM

> 云端配置：AWS VPC Security Groups

### 8.1 Security Groups

需要两个层级：

```text
alb_sg:
  inbound 80 from 0.0.0.0/0
  inbound 443 from 0.0.0.0/0 if HTTPS enabled
  outbound to ECS task ports

ecs_task_sg:
  inbound frontend/backend app port from alb_sg only
  outbound HTTPS / AWS services
```

实际已创建四个 security groups：

```text
Frontend ALB security group:
  Name: dce042-dev-frontend-alb-sg
  ID: sg-0c9187b2d9bd81479
  Inbound: TCP 80 from 0.0.0.0/0
  Outbound: TCP 80 to dce042-dev-frontend-ecs-sg

Frontend ECS security group:
  Name: dce042-dev-frontend-ecs-sg
  ID: sg-014acabc9439e4619
  Inbound: TCP 80 only from dce042-dev-frontend-alb-sg
  Outbound: all traffic to 0.0.0.0/0

Backend ALB security group:
  Name: dce042-dev-backend-alb-sg
  ID: sg-091d6f99b77c96945
  Inbound: TCP 80 from 0.0.0.0/0
  Outbound: TCP 8080 to dce042-dev-backend-ecs-sg

Backend ECS security group:
  Name: dce042-dev-backend-ecs-sg
  ID: sg-094e6867126ea148a
  Inbound: TCP 8080 only from dce042-dev-backend-alb-sg
  Outbound: all traffic to 0.0.0.0/0
```

说明：

- ALB security group 对公网开放 HTTP 80，让 browser 或 health checker 可以访问 load balancer。
- ECS task security group 不对公网开放，只接受来自对应 ALB security group 的流量。
- Backend ECS 端口是 `8080`，因为 Spring Boot backend 运行在 8080。
- Frontend ECS 端口是 `80`，因为 frontend container 使用 Nginx。
- HTTPS/443 暂未开启，因为还没有配置 domain 和 ACM certificate。

> 云端配置：AWS IAM

### 8.2 IAM Roles

至少需要：

- ECS task execution role：已创建 `dce042-dev-ecs-task-execution-role`
- Frontend ECS task role：已创建 `dce042-dev-frontend-task-role`
- Backend ECS task role：已创建 `dce042-dev-backend-task-role`
- CodeBuild service role
- CodeDeploy service role
- CodePipeline service role

权限原则：

- ECR push/pull 只给对应 repository
- S3 artifacts 只给 pipeline bucket
- CloudWatch logs 只给对应 log groups
- ECS update/deploy 只给对应 cluster 和 service
- DynamoDB 只给需要访问的 table

实际已创建的 IAM roles：

```text
ECS task execution role:
  Name: dce042-dev-ecs-task-execution-role
  ARN: arn:aws:iam::345594568549:role/dce042-dev-ecs-task-execution-role
  Attached policy: AmazonECSTaskExecutionRolePolicy
  Purpose: ECS agent 用它拉取 ECR image，并把 container logs 写入 CloudWatch Logs。

Frontend task role:
  Name: dce042-dev-frontend-task-role
  ARN: arn:aws:iam::345594568549:role/dce042-dev-frontend-task-role
  Purpose: frontend container 的业务身份。目前 frontend 不直接访问 AWS 服务，所以暂时没有额外业务权限。

Backend task role:
  Name: dce042-dev-backend-task-role
  ARN: arn:aws:iam::345594568549:role/dce042-dev-backend-task-role
  Inline policy: dce042-dev-backend-dynamodb-access
  Purpose: backend container 的业务身份，用于读写 DynamoDB application table。
```

Backend DynamoDB 权限范围：

```text
Allowed table:
  arn:aws:dynamodb:ap-southeast-2:345594568549:table/dce042-users-posts

Allowed index pattern:
  arn:aws:dynamodb:ap-southeast-2:345594568549:table/dce042-users-posts/index/*

Allowed actions:
  BatchGetItem, BatchWriteItem, DeleteItem, DescribeTable,
  GetItem, PutItem, Query, Scan, UpdateItem
```

Terraform 执行结果：

```text
Apply complete! Resources: 17 added, 0 changed, 0 destroyed.
```

查看输出：

```bash
cd terraform
terraform output security
```

> 云端配置：AWS ECR

## 9. ECR 模块

创建两个 repositories：

```text
Frontend repository:
  Name: dce042-frontend
  URI: 345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/dce042-frontend
  Scan on push: enabled
  Encryption: AES256
  Image tag mutability: MUTABLE

Backend repository:
  Name: dce042-backend
  URI: 345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/dce042-backend
  Scan on push: enabled
  Encryption: AES256
  Image tag mutability: MUTABLE
```

已配置：

- image scanning on push
- AES256 encryption
- lifecycle policy：只保留最近 10 个 images
- `force_delete = true`：方便作业结束后 Terraform destroy 清理仓库

Terraform 执行结果：

```text
Apply complete! Resources: 4 added, 0 changed, 0 destroyed.
```

查看输出：

```bash
cd terraform
terraform output ecr
```

截图证据：

- 两个 ECR repositories
- repository settings showing scan on push enabled
- repository settings showing AES256 encryption
- lifecycle policy showing keep most recent 10 images
- 镜像 push 后的 image tags

> 云端配置：AWS Elastic Load Balancing / ECS target groups

## 10. ALB 模块

作业要求两个 public-facing ALBs：

```text
Frontend ALB:
  Name: dce042-dev-frontend-alb
  DNS: dce042-dev-frontend-alb-1508953672.ap-southeast-2.elb.amazonaws.com
  Scheme: internet-facing
  State: active
  Listener: HTTP 80 -> dce042-dev-frontend-blue-tg

Backend ALB:
  Name: dce042-dev-backend-alb
  DNS: dce042-dev-backend-alb-1291356867.ap-southeast-2.elb.amazonaws.com
  Scheme: internet-facing
  State: active
  Listener: HTTP 80 -> dce042-dev-backend-blue-tg
```

每个 ALB 需要：

- listener 80：已创建
- optional listener 443
- target group blue：已创建
- target group green：已创建
- health check path：已配置

Blue/Green 建议每个服务两个 target groups：

```text
frontend-blue-tg:
  Name: dce042-dev-frontend-blue-tg
  Port: 80
  Target type: ip
  Health check: /health

frontend-green-tg:
  Name: dce042-dev-frontend-green-tg
  Port: 80
  Target type: ip
  Health check: /health

backend-blue-tg:
  Name: dce042-dev-backend-blue-tg
  Port: 8080
  Target type: ip
  Health check: /actuator/health

backend-green-tg:
  Name: dce042-dev-backend-green-tg
  Port: 8080
  Target type: ip
  Health check: /actuator/health
```

说明：

- `target_type = ip` 是 ECS Fargate 的正确配置，因为 Fargate task 没有 EC2 instance ID，ALB 需要直接注册 task 的 private IP。
- `blue` target group 是当前 listener 默认转发目标。
- `green` target group 后面由 CodeDeploy blue/green deployment 使用。
- 目前 target groups 还没有 healthy targets，因为 ECS services 还没有创建；第 11 步创建 ECS 后再截图 healthy targets。
- HTTPS/443 暂未创建，因为本项目还没有 domain 和 ACM certificate。

Terraform 执行结果：

```text
Apply complete! Resources: 8 added, 0 changed, 0 destroyed.
```

查看输出：

```bash
cd terraform
terraform output alb
```

截图：

- 两个 ALB
- listener
- target groups
- health check path
- healthy targets：第 11 步 ECS service 创建后再截图

> 云端配置：AWS ECS Fargate + CloudWatch Logs

## 11. ECS Fargate 模块

创建：

- ECS cluster：已创建 `dce042-dev-ecs-cluster`
- frontend task definition：已创建 `dce042-frontend-task:1`
- backend task definition：已创建 `dce042-backend-task:1`
- frontend ECS service：已创建 `frontend-service`
- backend ECS service：已创建 `backend-service`
- CloudWatch log groups：已创建 `/ecs/dce042-frontend` 和 `/ecs/dce042-backend`

前置镜像：

```text
Frontend image:
  345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/dce042-frontend:latest

Backend image:
  345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/dce042-backend:latest
```

本地是 Apple Silicon (`arm64`)，为了匹配 ECS task definition 的 `X86_64` runtime platform，镜像使用以下方式构建并推送：

```bash
aws ecr get-login-password --region ap-southeast-2 \
  | docker login --username AWS --password-stdin 345594568549.dkr.ecr.ap-southeast-2.amazonaws.com

docker buildx build --platform linux/amd64 \
  -f backend/Dockerfile \
  -t 345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/dce042-backend:latest \
  --push backend

docker buildx build --platform linux/amd64 \
  -f frontend/Dockerfile \
  -t 345594568549.dkr.ecr.ap-southeast-2.amazonaws.com/dce042-frontend:latest \
  --push frontend
```

推荐 task size：

```text
cpu: 256
memory: 512
```

Frontend container:

```text
container name: frontend
image: dce042-frontend:latest
port: 80
health check: /health
environment:
  BACKEND_UPSTREAM=dce042-dev-backend-alb-1291356867.ap-southeast-2.elb.amazonaws.com
```

Backend container:

```text
container name: backend
image: dce042-backend:latest
port: 8080
health check: /actuator/health
environment:
  APP_DEPLOY_ENV=aws
  APP_DYNAMODB_ENABLED=true
  DYNAMODB_TABLE_NAME=dce042-users-posts
  AWS_REGION=<region>
```

ECS service 对应关系：

```text
frontend-service:
  task definition: frontend-task
  container: frontend
  ALB: frontend-alb
  target groups: frontend-blue-tg / frontend-green-tg

backend-service:
  task definition: backend-task
  container: backend
  ALB: backend-alb
  target groups: backend-blue-tg / backend-green-tg
```

注意：

- Frontend service 需要能把 `/api` 请求转发到 backend ALB 或其他 backend endpoint。
- Backend service 的 IAM task role 需要 DynamoDB table 访问权限。
- Backend 不需要额外数据库服务；云端部署只需要 DynamoDB table 和 task role permission。

实际运行状态：

```text
frontend-service:
  desiredCount: 1
  runningCount: 1
  pendingCount: 0
  task set stability: STEADY_STATE
  registered target: dce042-dev-frontend-blue-tg
  target health: healthy

backend-service:
  desiredCount: 1
  runningCount: 1
  pendingCount: 0
  task set stability: STEADY_STATE
  registered target: dce042-dev-backend-blue-tg
  target health: healthy
```

验证命令：

```bash
terraform output ecs

curl http://dce042-dev-frontend-alb-1508953672.ap-southeast-2.elb.amazonaws.com/health
curl http://dce042-dev-backend-alb-1291356867.ap-southeast-2.elb.amazonaws.com/actuator/health
curl http://dce042-dev-backend-alb-1291356867.ap-southeast-2.elb.amazonaws.com/api/users
curl http://dce042-dev-frontend-alb-1508953672.ap-southeast-2.elb.amazonaws.com/api/users
```

实际验证结果：

```text
Frontend /health:
  HTTP 200 OK
  Body: ok

Backend /actuator/health:
  HTTP 200 OK
  Body includes: "status":"UP"

Backend /api/users:
  HTTP 200 OK
  Returned users from DynamoDB

Frontend /api/users:
  HTTP 200 OK
  Returned users through frontend Nginx proxy to backend ALB
```

Terraform 执行结果：

```text
Apply complete! Resources: 7 added, 0 changed, 0 destroyed.
```

截图：

- ECS cluster
- services running
- task definition revisions
- running tasks
- CloudWatch logs
- target groups showing healthy registered targets
- frontend ALB `/health` output
- backend ALB `/actuator/health` output
- `/api/users` output through frontend ALB

> 云端配置：AWS S3 + AWS DynamoDB + AWS SNS

## 12. Data、Assets 和 Notifications

> 云端配置：AWS S3

### 12.1 S3

创建两个 buckets：

- `dce042-dev-pipeline-artifacts-345594568549-ap-southeast-2`
- `dce042-dev-app-assets-345594568549-ap-southeast-2`

配置：

- block public access 默认开启
- encryption 开启
- versioning 开启
- pipeline artifacts bucket 配置 lifecycle policy，30 天删除旧 artifacts，7 天删除旧版本

> 云端配置：AWS DynamoDB

### 12.2 DynamoDB

创建并由 Terraform 管理一个 table：

```text
name: dce042-users-posts
partition key: pk string
sort key: sk string
billing mode: PAY_PER_REQUEST
point-in-time recovery: ENABLED
```

DynamoDB 的作用：

- 满足作业要求的 application data table
- 存储 JSONPlaceholder users
- 存储 JSONPlaceholder posts
- 存储 raw source payload/hash，用于 sync traceability
- 存储 transaction demo records
- 通过 ECS task role 授权 backend 访问，不在代码或环境变量中写 AWS secret key

单表 key 设计：

```text
User item:
  pk = USER#<externalUserId>
  sk = PROFILE

Post item:
  pk = USER#<externalUserId>
  sk = POST#<externalPostId>

Raw source item:
  pk = RAW#<sourceType>#<externalId>
  sk = METADATA

Transaction demo item:
  pk = TRANSACTION#<transactionId>
  sk = METADATA
```

Backend task role 最少需要：

```text
dynamodb:PutItem
dynamodb:GetItem
dynamodb:Query
dynamodb:Scan
dynamodb:DeleteItem
```

Resource 限制到：

```text
arn:aws:dynamodb:<region>:<account-id>:table/dce042-users-posts
```

> 云端配置：AWS SNS

### 12.3 SNS

创建一个 topic：

```text
dce042-dev-critical-notifications
```

可以订阅自己的 email，用于展示告警通知。当前 `notification_email` 为空，所以 Terraform 只创建 SNS topic，暂时不创建 email subscription。

实际执行结果：

```text
terraform import:
  module.data.aws_dynamodb_table.application -> dce042-users-posts

terraform apply:
  10 added, 1 changed, 0 destroyed

S3 buckets:
  dce042-dev-pipeline-artifacts-345594568549-ap-southeast-2
  dce042-dev-app-assets-345594568549-ap-southeast-2

DynamoDB:
  dce042-users-posts
  status: ACTIVE
  billing mode: PAY_PER_REQUEST
  item count: 220
  point-in-time recovery: ENABLED

SNS:
  dce042-dev-critical-notifications
  arn: arn:aws:sns:ap-southeast-2:345594568549:dce042-dev-critical-notifications
```

截图：

- S3 buckets
- S3 bucket public access block / encryption / versioning
- DynamoDB table
- DynamoDB table items
- DynamoDB continuous backups / point-in-time recovery
- SNS topic
- email subscription confirmed，如果后续配置了邮箱

> 云端配置：AWS ECS Service Auto Scaling + AWS CloudWatch + AWS SNS

## 13. Autoscaling 和 Monitoring

创建结果：

```text
terraform apply:
  10 added, 0 changed, 0 destroyed

Scalable targets:
  service/dce042-dev-ecs-cluster/frontend-service
    min: 1
    max: 2

  service/dce042-dev-ecs-cluster/backend-service
    min: 1
    max: 2

Scaling policies:
  dce042-dev-frontend-up
  dce042-dev-frontend-down
  dce042-dev-backend-up
  dce042-dev-backend-down

CloudWatch CPU alarms:
  dce042-dev-frontend-cpu-high
    metric: CPUUtilization
    threshold: > 60
    actions: frontend scale-up policy + SNS topic

  dce042-dev-frontend-cpu-low
    metric: CPUUtilization
    threshold: < 20
    actions: frontend scale-down policy + SNS topic

  dce042-dev-backend-cpu-high
    metric: CPUUtilization
    threshold: > 60
    actions: backend scale-up policy + SNS topic

  dce042-dev-backend-cpu-low
    metric: CPUUtilization
    threshold: < 20
    actions: backend scale-down policy + SNS topic

SNS action:
  arn:aws:sns:ap-southeast-2:345594568549:dce042-dev-critical-notifications
```

说明：

- 作业要求 four autoscaling policies，本项目创建 4 个：frontend up/down + backend up/down。
- 每个 ECS service 的 desired count 最小是 1、最大是 2，这样可以展示扩缩容能力，同时控制费用。
- CloudWatch alarm 刚创建时可能显示 `INSUFFICIENT_DATA`，这是因为还没有足够 metric 数据。
- Low CPU alarm 在应用空闲时可能显示 `ALARM`，这是正常状态；因为 service 已经在 `min_capacity = 1`，所以不会缩到 0。
- Alarm action 同时连接 scaling policy 和 SNS topic，说明它既可以触发扩缩容，也可以通知。

截图：

- ECS service Auto Scaling 页面：frontend service，显示 min 1 / max 2。
- ECS service Auto Scaling 页面：backend service，显示 min 1 / max 2。
- Application Auto Scaling policies，显示 4 个 policies。
- CloudWatch alarms，显示 4 个 CPU alarms。
- 任意一个 CloudWatch alarm detail，显示 alarm action 包含 SNS topic 和 scaling policy。
- SNS topic `dce042-dev-critical-notifications`。
- CloudWatch log groups：`/ecs/dce042-frontend` 和 `/ecs/dce042-backend`。

> 云端配置：AWS CodePipeline + CodeBuild + CodeDeploy + ECR + ECS

## 14. CI/CD Pipeline

> 云端配置：AWS CodePipeline / CodeBuild / CodeDeploy

### 14.1 资源

创建：

- One CodePipeline:
  - `dce042-dev-pipeline`
- Two CodeBuild projects:
  - `dce042-dev-frontend-build`
  - `dce042-dev-backend-build`
- Two CodeDeploy applications:
  - `dce042-dev-frontend-deploy`
  - `dce042-dev-backend-deploy`
- Two CodeDeploy deployment groups:
  - `dce042-dev-frontend-dg`
  - `dce042-dev-backend-dg`
- Artifact bucket:
  - `dce042-dev-pipeline-artifacts-345594568549-ap-southeast-2`
- GitHub source:
  - `Lynn61Liu/users-posts-browser`
  - branch: `main`

> 云端配置：AWS CodePipeline stages

### 14.2 Pipeline flow

```text
Source: GitHub commit f61b353 / "12-13"
  |
  v
Parallel Build
  +--> frontend CodeBuild -> frontend ECR image frontend-f61b35361a24
  +--> backend CodeBuild -> backend ECR image backend-f61b35361a24
  |
  v
Parallel Deploy
  +--> frontend CodeDeploy Blue/Green -> frontend ECS service task definition :3
  +--> backend CodeDeploy Blue/Green -> backend ECS service task definition :3
```

实际执行结果：

```text
terraform apply:
  13 added, 0 changed, 0 destroyed

pipeline execution:
  d7c43ff6-e7d1-4270-9be1-d968570da089

Source:
  Succeeded
  commit: f61b35361a24fb70af8eb329fd990651f5435161

ParallelBuild:
  BuildFrontend: Succeeded
  BuildBackend: Succeeded

ParallelBlueGreenDeploy:
  DeployFrontend: Succeeded
    deployment id: d-LFMSP6QVJ
  DeployBackend: Succeeded
    deployment id: d-V5NDI8QVJ

Final ECS state:
  frontend-service:
    desired: 1
    running: 1
    primary task definition: dce042-frontend-task:3
    traffic: 100%

  backend-service:
    desired: 1
    running: 1
    primary task definition: dce042-backend-task:3
    traffic: 100%
```

修复记录：

```text
Issue 1:
  backend build failed on old GitHub commit because the old Dockerfile used Maven Wrapper during Docker build.
  Fix: pushed local commit f61b353 to GitHub.

Issue 2:
  CodeDeployToECS action failed because CodePipeline role lacked ecs:RegisterTaskDefinition.
  Fix: added ecs:RegisterTaskDefinition and ecs:DescribeTaskDefinition to dce042-dev-codepipeline-role policy.
```

验证：

```text
frontend /health:
  HTTP 200 OK

backend /actuator/health:
  HTTP 200 OK
  status: UP

frontend /api/users:
  returns user JSON from backend/DynamoDB
```

> 云端配置：AWS CodePipeline / CodeBuild / CodeDeploy / ECR / ECS / ALB Console

### 14.3 截图证据

- Pipeline `dce042-dev-pipeline` succeeded
- `dce042-dev-frontend-build` succeeded
- `dce042-dev-backend-build` succeeded
- frontend image pushed to `dce042-frontend` ECR
- backend image pushed to `dce042-backend` ECR
- frontend CodeDeploy blue/green deployment succeeded
- backend CodeDeploy blue/green deployment succeeded
- `frontend-service` running new task definition revision
- `backend-service` running new task definition revision
- frontend and backend ALB target groups healthy
- CodeDeploy deployment target lifecycle events all succeeded
- ECS task sets during deployment showed Blue and Green task sets
- final ECS services show task definitions `dce042-frontend-task:3` and `dce042-backend-task:3`

> 云端配置：Microsoft Entra admin center (Azure) + AWS IAM Console / Terraform

## 15. Azure Entra ID Federation

> 云端配置：Microsoft Entra admin center (Azure)，需要回填 AWS IAM role ARN

### 15.1 Azure 侧

正式实施时按以下顺序完成。第 2.4 节是准备清单，本节是最终配置和验证流程。

1. 确认 Enterprise Application 已创建：
   - Name: `AWS SAML Federation - DCE042`
2. 确认 SAML Basic Configuration：
   - Identifier / Entity ID: `urn:amazon:webservices`
   - Reply URL / ACS URL: `https://signin.aws.amazon.com/saml`
   - Sign on URL: `https://signin.aws.amazon.com/saml`
3. 确认 Entra groups 已创建并分配到 application：
   - `DevOpsEngineer`
   - `ReadOnlyAuditor`
4. 确认已下载 Federation Metadata XML。
5. 在 Terraform 中引用 metadata XML 创建 AWS IAM SAML provider。
6. Terraform 创建 AWS IAM roles 后，回到 Entra SAML claims 中补充 role mapping。
7. 确认 Conditional Access policy 对 AWS Enterprise Application 要求 MFA。

Role claim 示例：

```text
arn:aws:iam::<account-id>:role/DevOpsEngineer,arn:aws:iam::<account-id>:saml-provider/<provider-name>
```

RoleSessionName claim 示例：

```text
user.userprincipalname
```

> 云端配置：AWS IAM Console / Terraform，本地执行 Terraform 代码创建 AWS IAM federation 资源

### 15.2 AWS 侧

Terraform 创建：

- `aws_iam_saml_provider`
- `DevOpsEngineer` IAM role
- `ReadOnlyAuditor` IAM role
- trust policy 信任 SAML provider
- role permissions

示例 Terraform 方向：

```hcl
resource "aws_iam_saml_provider" "entra" {
  name                   = "entra-dce042"
  saml_metadata_document = file(var.entra_saml_metadata_file)
}
```

IAM role trust policy 需要允许 SAML federation：

```hcl
principals {
  type        = "Federated"
  identifiers = [aws_iam_saml_provider.entra.arn]
}

actions = ["sts:AssumeRoleWithSAML"]
```

并限制 SAML audience：

```hcl
condition {
  test     = "StringEquals"
  variable = "SAML:aud"
  values   = ["https://signin.aws.amazon.com/saml"]
}
```

建议权限：

- `DevOpsEngineer`：用于演示部署能力，可以绑定受限的 ECS/ECR/CodePipeline/CloudWatch 权限。课程 demo 如时间有限，可使用较宽权限但必须在报告中说明生产环境应最小权限。
- `ReadOnlyAuditor`：优先使用 AWS managed policy `ReadOnlyAccess`，用于审计和查看资源。

Terraform outputs 建议输出：

```text
entra_saml_provider_arn
devops_engineer_role_arn
readonly_auditor_role_arn
```

这些输出用于回填 Entra SAML role claims。

> 云端配置：Microsoft Entra My Apps / Azure SSO + AWS Console

### 15.3 验证

截图：

- Azure Enterprise Application SAML config
- Entra group assignment
- MFA / Conditional Access policy
- AWS IAM SAML provider
- AWS IAM roles
- 使用 Azure Entra ID 登录 AWS Console 成功

验证步骤：

1. 在 Entra Enterprise Application 页面点击 `Test single sign-on`，或打开用户的 My Apps portal。
2. 选择 AWS SAML application。
3. 使用被分配到 `DevOpsEngineer` group 的用户登录。
4. 完成 MFA challenge。
5. 如果出现 AWS role 选择页面，选择 `DevOpsEngineer`。
6. 进入 AWS Console 后，右上角应显示 federated role/session 信息。
7. 验证该用户可以查看或操作与 role 权限相符的 AWS 资源。
8. 使用 `ReadOnlyAuditor` 测试用户重复一次，只验证 read-only 访问。

常见问题排查：

- 如果 AWS 显示 SAML error，检查 Identifier 是否为 `urn:amazon:webservices`。
- 如果无法进入 AWS，检查 Reply URL 是否为 `https://signin.aws.amazon.com/saml`。
- 如果没有 role 可选，检查 Role claim 是否包含 `role ARN,saml-provider ARN`，顺序不要写反。
- 如果提示没有权限，检查 IAM role trust policy 是否允许 `sts:AssumeRoleWithSAML`。
- 如果没有触发 MFA，检查 Conditional Access policy 是否 targeting 到正确 application 和 group。

> 云端配置：本地工具 + AWS Console + Microsoft Entra admin center (Azure)，按阶段逐步开启

## 16. 推荐执行顺序

> 云端配置：无，本地准备，不创建云资源

### Phase 1: 本地准备，不产生云资源费用

1. 阅读作业要求和 rubric。
2. 确认 AWS/Azure 账号权限。
3. 设置 AWS Budget。
4. 本地运行 frontend/backend。
5. 确认 Docker build 成功。
6. 画第一版架构图。
7. 创建 Terraform 文件结构。
8. 编写 Terraform modules。
9. 运行 `terraform fmt` 和 `terraform validate`。

> 云端配置：AWS S3 + AWS DynamoDB

### Phase 2: 创建 backend state，低费用

1. 创建 Terraform bootstrap。
2. `terraform plan`。
3. `terraform apply` 创建 state bucket 和 lock table。
4. 截图。

> 云端配置：AWS VPC / ECS / ECR / ALB / IAM / S3 / DynamoDB / SNS / CloudWatch / CI/CD

### Phase 3: 创建核心资源，短时间运行

1. `terraform init`。
2. `terraform plan`。
3. 检查将要创建的资源数量。
4. 确认没有 NAT Gateway 或不必要大规格资源。
5. `terraform apply`。
6. 立即截图。

> 云端配置：AWS ECR + ECS + ALB + CodeBuild + CodeDeploy + CodePipeline

### Phase 4: 部署应用

1. 通过 `dce042-frontend-build` build frontend image。
2. push frontend image 到 `dce042-frontend` ECR。
3. 通过 `dce042-backend-build` build backend image。
4. push backend image 到 `dce042-backend` ECR。
5. 启动 `frontend-service`。
6. 启动 `backend-service`。
7. 确认 `frontend-alb` health check 通过。
8. 确认 `backend-alb` health check 通过。
9. Pipeline 完整运行一次。
10. Frontend CodeDeploy blue/green 验证一次。
11. Backend CodeDeploy blue/green 验证一次。

> 云端配置：Microsoft Entra admin center (Azure) + AWS IAM / AWS Console

### Phase 5: Azure SSO 验证

1. 配置 Entra Enterprise App。
2. 配置 AWS SAML provider 和 roles。
3. 用户通过 Azure 登录 AWS。
4. 截图。

> 云端配置：AWS Console + Terraform，本阶段用于删除云资源避免继续收费

### Phase 6: 收尾和删除资源

1. 收集所有截图。
2. 导出 Terraform outputs。
3. 写报告。
4. 运行：

```bash
terraform destroy -var-file=environments/dev/terraform.tfvars
```

5. 检查 AWS Console 确认资源删除：
   - ALB
   - ECS services/tasks
   - ECR repositories
   - CodePipeline
   - CodeBuild
   - CodeDeploy
   - S3 buckets
   - DynamoDB tables
   - CloudWatch alarms/log groups
   - SNS topics
   - IAM roles
   - VPC
6. 最后删除 bootstrap backend 资源，或保留到最终提交后再删除。

> 云端配置：无，报告架构图设计；图中需要体现 AWS、Azure 和本地 Terraform

## 17. 架构图应包含哪些部分

架构图必须包含：

- Developer / GitHub source
- Terraform IaC
- Terraform remote backend: S3 + DynamoDB lock
- AWS VPC
- Two AZs
- Public subnets
- Two ALBs
- ECS Fargate cluster
- frontend ECS service
- backend ECS service
- frontend ECR repository
- backend ECR repository
- CodePipeline
- Two CodeBuild projects
- Two CodeDeploy applications/deployment groups
- Blue/Green target groups
- S3 artifacts bucket
- S3 assets bucket
- DynamoDB application table: `dce042-users-posts`
- CloudWatch logs/alarms
- SNS topic
- Azure Entra ID
- SAML provider
- IAM roles

推荐数据流：

```text
Developer -> GitHub -> CodePipeline
CodePipeline -> CodeBuild -> ECR
CodePipeline -> CodeDeploy -> ECS Fargate
Users -> ALB Frontend -> Frontend ECS Service
Browser/Frontend -> ALB Backend -> Backend ECS Service
Backend ECS Service -> DynamoDB / S3 Assets
CloudWatch Alarms -> SNS
Azure Entra ID -> SAML -> AWS IAM Roles
Terraform -> AWS Resources
Terraform State -> S3 Backend + DynamoDB Lock
```

> 云端配置：无，报告写作

## 18. 报告写作建议

每个技术部分都按这个格式写：

```text
Purpose:
说明这个组件为什么需要。

Implementation:
说明 Terraform 创建了什么资源。

Security:
说明 security group / IAM / encryption / MFA 如何设计。

Validation:
列出截图证据和测试结果。

Reflection:
说明如果是生产环境会如何改进。
```

> 云端配置：AWS Console + Microsoft Entra admin center (Azure) + 本地 Terraform 截图

## 19. 最小完成清单

提交前确认：

- [ ] Terraform modules 存在且结构清楚
- [ ] `terraform fmt` 通过
- [ ] `terraform validate` 通过
- [ ] `terraform plan` 截图
- [ ] remote backend 配置截图
- [ ] local state 或 state migration 证据
- [ ] VPC + two AZ public subnets 截图
- [ ] two ALBs 截图
- [ ] two ECR repositories 截图
- [ ] two ECS services running 截图
- [ ] four scaling policies 截图
- [ ] four CloudWatch alarms 截图
- [ ] two CodeBuild projects 截图
- [ ] two CodeDeploy apps/deployments 截图
- [ ] one CodePipeline successful run 截图
- [ ] S3 buckets 截图
- [ ] DynamoDB table 截图
- [ ] SNS topic 截图
- [ ] Azure Entra ID SAML config 截图
- [ ] AWS IAM SAML provider 和 roles 截图
- [ ] Azure SSO 登录 AWS Console 截图
- [ ] 架构图
- [ ] cleanup / destroy 截图

> 云端配置：无，下一步本地准备为主

## 20. 下一步建议

方案已经确定为当前 `frontend` + `backend` 两个 ECS 服务，并使用完全 DynamoDB 数据路线。下一步先完成这些本地准备，不创建云资源：

1. 使用已准备好的 `terraform/` 项目结构。
2. 使用已准备好的 `buildspec-frontend.yml`。
3. 使用已准备好的 `buildspec-backend.yml`。
4. 使用已准备好的 `deploy/frontend/appspec.yml` 和 `deploy/frontend/taskdef.json`。
5. 使用已准备好的 `deploy/backend/appspec.yml` 和 `deploy/backend/taskdef.json`。
6. 在 Terraform / CodeBuild project 中设置 `BACKEND_ALB_DNS_NAME`，使 frontend ECS 环境可以转发 `/api` 到 backend ALB。
7. 在 Terraform / CodeBuild project 中设置 backend DynamoDB table 和 task role 权限，接入 `dce042-users-posts`。
8. 本地运行 frontend/backend build，确保两个 Docker image 都能 build 成功。
9. 从第 6 步开始逐步填充 Terraform modules 的真实 AWS resources，并持续运行 `terraform fmt` / `terraform validate`。

当前项目继续使用：

```text
frontend/ -> frontend-service
backend/  -> backend-service
```
