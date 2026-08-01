variable "project_name" {
  description = "Project name used for AWS resource naming."
  type        = string
}

variable "environment" {
  description = "Deployment environment name."
  type        = string
}

variable "aws_region" {
  description = "AWS region for networking resources."
  type        = string
}

variable "vpc_cidr" {
  description = "CIDR block for the assessment VPC."
  type        = string
}

variable "availability_zones" {
  description = "Optional explicit Availability Zones."
  type        = list(string)

  validation {
    condition     = length(var.availability_zones) == 0 || length(var.availability_zones) >= length(var.public_subnet_cidrs)
    error_message = "If availability_zones is provided, it must contain at least one AZ for each public subnet CIDR."
  }
}

variable "public_subnet_cidrs" {
  description = "CIDR blocks for public subnets."
  type        = list(string)

  validation {
    condition     = length(var.public_subnet_cidrs) >= 2
    error_message = "At least two public subnet CIDR blocks are required for high availability across two AZs."
  }
}

variable "common_tags" {
  description = "Common tags applied to AWS resources."
  type        = map(string)
}

variable "enable_nat_gateway" {
  description = "Whether to create a NAT Gateway. Disabled by default to avoid hourly NAT charges for the assessment demo."
  type        = bool
  default     = false
}
