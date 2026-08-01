variable "project_name" {
  description = "Project name used for AWS resource naming."
  type        = string
}

variable "environment" {
  description = "Deployment environment name."
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

variable "common_tags" {
  description = "Common tags applied to AWS resources."
  type        = map(string)
}
