variable "project_name" {
  description = "Project name used for AWS resource naming."
  type        = string
}

variable "environment" {
  description = "Deployment environment name."
  type        = string
}

variable "cluster_name" {
  description = "ECS cluster name."
  type        = string
}

variable "service_names" {
  description = "ECS service names to configure autoscaling for."
  type = object({
    frontend = string
    backend  = string
  })
}

variable "sns_topic_arn" {
  description = "SNS topic ARN used by CloudWatch alarm notification actions."
  type        = string
}

variable "min_capacity" {
  description = "Minimum desired count for each ECS service."
  type        = number
  default     = 1
}

variable "max_capacity" {
  description = "Maximum desired count for each ECS service."
  type        = number
  default     = 2
}

variable "common_tags" {
  description = "Common tags applied to AWS resources."
  type        = map(string)
}
