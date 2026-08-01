variable "project_name" {
  description = "Project name used for AWS resource naming."
  type        = string
}

variable "environment" {
  description = "Deployment environment name."
  type        = string
}

variable "aws_region" {
  description = "AWS region for CI/CD resources."
  type        = string
}

variable "frontend_repository_name" {
  description = "ECR repository name for the frontend image."
  type        = string
}

variable "backend_repository_name" {
  description = "ECR repository name for the backend image."
  type        = string
}

variable "artifact_bucket_name" {
  description = "S3 bucket used by CodePipeline for artifacts."
  type        = string
}

variable "artifact_bucket_arn" {
  description = "S3 artifact bucket ARN."
  type        = string
}

variable "repository_full_name" {
  description = "GitHub repository full name, for example owner/repository."
  type        = string
}

variable "repository_branch" {
  description = "Git branch used by CodePipeline source."
  type        = string
}

variable "codeconnection_arn" {
  description = "AWS CodeConnections GitHub connection ARN."
  type        = string
}

variable "ecr_repository_arns" {
  description = "ECR repository ARNs for frontend and backend."
  type = object({
    frontend = string
    backend  = string
  })
}

variable "ecs_cluster_name" {
  description = "ECS cluster name."
  type        = string
}

variable "ecs_service_names" {
  description = "ECS service names."
  type = object({
    frontend = string
    backend  = string
  })
}

variable "ecs_task_execution_role_arn" {
  description = "ECS task execution role ARN."
  type        = string
}

variable "frontend_task_role_arn" {
  description = "Frontend ECS task role ARN."
  type        = string
}

variable "backend_task_role_arn" {
  description = "Backend ECS task role ARN."
  type        = string
}

variable "backend_alb_dns_name" {
  description = "Backend ALB DNS name injected into frontend task definitions."
  type        = string
}

variable "dynamodb_table_name" {
  description = "DynamoDB table name injected into backend task definitions."
  type        = string
}

variable "listener_arns" {
  description = "ALB listener ARNs used by CodeDeploy blue/green deployment groups."
  type = object({
    frontend = string
    backend  = string
  })
}

variable "target_group_names" {
  description = "Blue and green target group names used by CodeDeploy."
  type = object({
    frontend_blue  = string
    frontend_green = string
    backend_blue   = string
    backend_green  = string
  })
}

variable "common_tags" {
  description = "Common tags applied to AWS resources."
  type        = map(string)
}
