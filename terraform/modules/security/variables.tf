variable "project_name" {
  description = "Project name used for AWS resource naming."
  type        = string
}

variable "environment" {
  description = "Deployment environment name."
  type        = string
}

variable "vpc_id" {
  description = "VPC ID where ALB and ECS security groups are created."
  type        = string
}

variable "frontend_container_port" {
  description = "Frontend container port."
  type        = number
  default     = 80
}

variable "backend_container_port" {
  description = "Backend container port."
  type        = number
  default     = 8080
}

variable "allowed_http_cidrs" {
  description = "CIDR blocks allowed to access public ALBs over HTTP."
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "dynamodb_table_name" {
  description = "Application DynamoDB table name used by the backend task role."
  type        = string
}

variable "common_tags" {
  description = "Common tags applied to AWS resources."
  type        = map(string)
}
