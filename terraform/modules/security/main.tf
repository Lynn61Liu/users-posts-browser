locals {
  name_prefix = "${var.project_name}-${var.environment}"
}

data "aws_caller_identity" "current" {}

data "aws_partition" "current" {}

data "aws_region" "current" {}

locals {
  dynamodb_table_arn       = "arn:${data.aws_partition.current.partition}:dynamodb:${data.aws_region.current.region}:${data.aws_caller_identity.current.account_id}:table/${var.dynamodb_table_name}"
  dynamodb_table_index_arn = "${local.dynamodb_table_arn}/index/*"
}

resource "aws_security_group" "frontend_alb" {
  name        = "${local.name_prefix}-frontend-alb-sg"
  description = "Allow public HTTP traffic to the frontend Application Load Balancer."
  vpc_id      = var.vpc_id

  tags = merge(var.common_tags, {
    Name = "${local.name_prefix}-frontend-alb-sg"
  })
}

resource "aws_vpc_security_group_ingress_rule" "frontend_alb_http" {
  security_group_id = aws_security_group.frontend_alb.id
  description       = "Allow HTTP access to the frontend ALB."
  cidr_ipv4         = var.allowed_http_cidrs[0]
  from_port         = 80
  ip_protocol       = "tcp"
  to_port           = 80
}

resource "aws_vpc_security_group_egress_rule" "frontend_alb_to_frontend_ecs" {
  security_group_id            = aws_security_group.frontend_alb.id
  description                  = "Allow frontend ALB to reach frontend ECS tasks."
  referenced_security_group_id = aws_security_group.frontend_ecs.id
  from_port                    = var.frontend_container_port
  ip_protocol                  = "tcp"
  to_port                      = var.frontend_container_port
}

resource "aws_security_group" "backend_alb" {
  name        = "${local.name_prefix}-backend-alb-sg"
  description = "Allow public HTTP traffic to the backend Application Load Balancer."
  vpc_id      = var.vpc_id

  tags = merge(var.common_tags, {
    Name = "${local.name_prefix}-backend-alb-sg"
  })
}

resource "aws_vpc_security_group_ingress_rule" "backend_alb_http" {
  security_group_id = aws_security_group.backend_alb.id
  description       = "Allow HTTP access to the backend ALB for API demo and health checks."
  cidr_ipv4         = var.allowed_http_cidrs[0]
  from_port         = 80
  ip_protocol       = "tcp"
  to_port           = 80
}

resource "aws_vpc_security_group_egress_rule" "backend_alb_to_backend_ecs" {
  security_group_id            = aws_security_group.backend_alb.id
  description                  = "Allow backend ALB to reach backend ECS tasks."
  referenced_security_group_id = aws_security_group.backend_ecs.id
  from_port                    = var.backend_container_port
  ip_protocol                  = "tcp"
  to_port                      = var.backend_container_port
}

resource "aws_security_group" "frontend_ecs" {
  name        = "${local.name_prefix}-frontend-ecs-sg"
  description = "Allow frontend ECS tasks to receive traffic only from the frontend ALB."
  vpc_id      = var.vpc_id

  tags = merge(var.common_tags, {
    Name = "${local.name_prefix}-frontend-ecs-sg"
  })
}

resource "aws_vpc_security_group_ingress_rule" "frontend_ecs_from_frontend_alb" {
  security_group_id            = aws_security_group.frontend_ecs.id
  description                  = "Allow frontend ALB to reach frontend containers."
  referenced_security_group_id = aws_security_group.frontend_alb.id
  from_port                    = var.frontend_container_port
  ip_protocol                  = "tcp"
  to_port                      = var.frontend_container_port
}

resource "aws_vpc_security_group_egress_rule" "frontend_ecs_all" {
  security_group_id = aws_security_group.frontend_ecs.id
  description       = "Allow outbound traffic for backend API calls and AWS service access."
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1"
}

resource "aws_security_group" "backend_ecs" {
  name        = "${local.name_prefix}-backend-ecs-sg"
  description = "Allow backend ECS tasks to receive traffic only from the backend ALB."
  vpc_id      = var.vpc_id

  tags = merge(var.common_tags, {
    Name = "${local.name_prefix}-backend-ecs-sg"
  })
}

resource "aws_vpc_security_group_ingress_rule" "backend_ecs_from_backend_alb" {
  security_group_id            = aws_security_group.backend_ecs.id
  description                  = "Allow backend ALB to reach backend containers."
  referenced_security_group_id = aws_security_group.backend_alb.id
  from_port                    = var.backend_container_port
  ip_protocol                  = "tcp"
  to_port                      = var.backend_container_port
}

resource "aws_vpc_security_group_egress_rule" "backend_ecs_all" {
  security_group_id = aws_security_group.backend_ecs.id
  description       = "Allow outbound traffic for DynamoDB and AWS service access."
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1"
}

data "aws_iam_policy_document" "ecs_task_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "ecs_task_execution" {
  name               = "${local.name_prefix}-ecs-task-execution-role"
  assume_role_policy = data.aws_iam_policy_document.ecs_task_assume_role.json

  tags = merge(var.common_tags, {
    Name = "${local.name_prefix}-ecs-task-execution-role"
  })
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_managed" {
  role       = aws_iam_role.ecs_task_execution.name
  policy_arn = "arn:${data.aws_partition.current.partition}:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role" "frontend_task" {
  name               = "${local.name_prefix}-frontend-task-role"
  assume_role_policy = data.aws_iam_policy_document.ecs_task_assume_role.json

  tags = merge(var.common_tags, {
    Name = "${local.name_prefix}-frontend-task-role"
  })
}

resource "aws_iam_role" "backend_task" {
  name               = "${local.name_prefix}-backend-task-role"
  assume_role_policy = data.aws_iam_policy_document.ecs_task_assume_role.json

  tags = merge(var.common_tags, {
    Name = "${local.name_prefix}-backend-task-role"
  })
}

data "aws_iam_policy_document" "backend_dynamodb_access" {
  statement {
    sid = "BackendReadWriteApplicationTable"
    actions = [
      "dynamodb:BatchGetItem",
      "dynamodb:BatchWriteItem",
      "dynamodb:DeleteItem",
      "dynamodb:DescribeTable",
      "dynamodb:GetItem",
      "dynamodb:PutItem",
      "dynamodb:Query",
      "dynamodb:Scan",
      "dynamodb:UpdateItem"
    ]
    resources = [
      local.dynamodb_table_arn,
      local.dynamodb_table_index_arn
    ]
  }
}

resource "aws_iam_role_policy" "backend_dynamodb_access" {
  name   = "${local.name_prefix}-backend-dynamodb-access"
  role   = aws_iam_role.backend_task.id
  policy = data.aws_iam_policy_document.backend_dynamodb_access.json
}
