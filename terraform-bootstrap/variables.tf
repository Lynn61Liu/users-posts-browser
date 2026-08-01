variable "project_name" {
  description = "Project name used for backend resource naming."
  type        = string
  default     = "dce042"
}

variable "environment" {
  description = "Deployment environment name."
  type        = string
  default     = "dev"
}

variable "aws_region" {
  description = "AWS region for Terraform backend resources."
  type        = string
  default     = "ap-southeast-2"
}

variable "state_bucket_name" {
  description = "Globally unique S3 bucket name for Terraform remote state."
  type        = string
  default     = "dce042-terraform-state-345594568549-ap-southeast-2"
}

variable "lock_table_name" {
  description = "DynamoDB table name for Terraform state locking."
  type        = string
  default     = "dce042-terraform-locks"
}
