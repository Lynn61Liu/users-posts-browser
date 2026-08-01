variable "project_name" {
  description = "Project name used for AWS resource naming."
  type        = string
}

variable "environment" {
  description = "Deployment environment name."
  type        = string
}

variable "vpc_id" {
  description = "VPC ID where target groups are created."
  type        = string
}

variable "public_subnet_ids" {
  description = "Public subnet IDs where ALBs are deployed."
  type        = list(string)
}

variable "frontend_alb_security_group_id" {
  description = "Security group ID for the frontend ALB."
  type        = string
}

variable "backend_alb_security_group_id" {
  description = "Security group ID for the backend ALB."
  type        = string
}

variable "frontend_container_port" {
  description = "Frontend target group port."
  type        = number
  default     = 80
}

variable "backend_container_port" {
  description = "Backend target group port."
  type        = number
  default     = 8080
}

variable "frontend_health_check_path" {
  description = "Frontend target group health check path."
  type        = string
  default     = "/health"
}

variable "backend_health_check_path" {
  description = "Backend target group health check path."
  type        = string
  default     = "/actuator/health"
}

variable "common_tags" {
  description = "Common tags applied to AWS resources."
  type        = map(string)
}
