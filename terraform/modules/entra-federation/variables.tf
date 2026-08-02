variable "project_name" {
  description = "Project name used for AWS resource naming."
  type        = string
}

variable "environment" {
  description = "Deployment environment name."
  type        = string
}

variable "entra_metadata_file" {
  description = "Path to Azure Entra SAML metadata XML."
  type        = string

  validation {
    condition     = var.entra_metadata_file == "" || fileexists(var.entra_metadata_file)
    error_message = "entra_metadata_file must be empty or point to an existing Azure Entra federation metadata XML file."
  }
}

variable "saml_provider_name" {
  description = "AWS IAM SAML provider name for Azure Entra ID."
  type        = string
  default     = ""
}

variable "max_session_duration" {
  description = "Maximum session duration in seconds for the federated IAM roles."
  type        = number
  default     = 3600
}

variable "common_tags" {
  description = "Common tags applied to AWS resources."
  type        = map(string)
}
