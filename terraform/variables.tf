variable "project_name" {
  description = "Project name used for AWS resource naming."
  type        = string
  default     = "dce042"
}

variable "environment" {
  description = "Deployment environment name."
  type        = string
  default     = "dev"
}

variable "aws_region" {
  description = "AWS region for all resources."
  type        = string
  default     = "ap-southeast-2"
}

variable "availability_zones" {
  description = "Optional explicit Availability Zones. Leave empty to select them in the network module later."
  type        = list(string)
  default     = []
}

variable "vpc_cidr" {
  description = "CIDR block for the assessment VPC."
  type        = string
  default     = "10.42.0.0/16"
}

variable "public_subnet_cidrs" {
  description = "CIDR blocks for public subnets across two Availability Zones."
  type        = list(string)
  default     = ["10.42.1.0/24", "10.42.2.0/24"]
}

variable "enable_nat_gateway" {
  description = "Whether to create a NAT Gateway. Keep false for the low-cost public-subnet assessment design."
  type        = bool
  default     = false
}

variable "frontend_repository_name" {
  description = "ECR repository name for the frontend image."
  type        = string
  default     = "dce042-frontend"
}

variable "backend_repository_name" {
  description = "ECR repository name for the backend image."
  type        = string
  default     = "dce042-backend"
}

variable "dynamodb_table_name" {
  description = "DynamoDB application table used by the backend."
  type        = string
  default     = "dce042-users-posts"
}

variable "notification_email" {
  description = "Email address for SNS notifications. Leave empty until monitoring is configured."
  type        = string
  default     = ""
}

variable "entra_metadata_file" {
  description = "Path to the Azure Entra SAML metadata XML file. Leave empty until federation is configured."
  type        = string
  default     = ""
}
