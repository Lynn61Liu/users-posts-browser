variable "project_name" {
  description = "Project name used for AWS resource naming."
  type        = string
}

variable "environment" {
  description = "Deployment environment name."
  type        = string
}

variable "aws_region" {
  description = "AWS region for ECS logs and task definitions."
  type        = string
}

variable "dynamodb_table_name" {
  description = "DynamoDB table name exposed to the backend task."
  type        = string
}

variable "public_subnet_ids" {
  description = "Public subnet IDs where ECS Fargate tasks are launched for the low-cost assessment design."
  type        = list(string)
}

variable "frontend_ecs_security_group_id" {
  description = "Security group ID for frontend ECS tasks."
  type        = string
}

variable "backend_ecs_security_group_id" {
  description = "Security group ID for backend ECS tasks."
  type        = string
}

variable "frontend_image_uri" {
  description = "Frontend container image URI."
  type        = string
}

variable "backend_image_uri" {
  description = "Backend container image URI."
  type        = string
}

variable "ecs_task_execution_role_arn" {
  description = "ECS task execution role ARN."
  type        = string
}

variable "frontend_task_role_arn" {
  description = "Frontend task role ARN."
  type        = string
}

variable "backend_task_role_arn" {
  description = "Backend task role ARN."
  type        = string
}

variable "frontend_blue_target_group_arn" {
  description = "Frontend blue target group ARN used by the initial ECS service."
  type        = string
}

variable "backend_blue_target_group_arn" {
  description = "Backend blue target group ARN used by the initial ECS service."
  type        = string
}

variable "frontend_listener_arn" {
  description = "Frontend ALB listener ARN. Used as an explicit dependency before ECS service creation."
  type        = string
}

variable "backend_listener_arn" {
  description = "Backend ALB listener ARN. Used as an explicit dependency before ECS service creation."
  type        = string
}

variable "backend_alb_dns_name" {
  description = "Backend ALB DNS name injected into the frontend Nginx container."
  type        = string
}

variable "frontend_desired_count" {
  description = "Desired number of frontend tasks."
  type        = number
  default     = 1
}

variable "backend_desired_count" {
  description = "Desired number of backend tasks."
  type        = number
  default     = 1
}

variable "common_tags" {
  description = "Common tags applied to AWS resources."
  type        = map(string)
}
