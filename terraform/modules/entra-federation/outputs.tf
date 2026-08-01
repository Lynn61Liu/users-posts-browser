output "module_status" {
  description = "Entra federation module skeleton status."
  value       = "ready-for-iam-saml-provider-and-federated-roles"
}

output "planned_federated_roles" {
  description = "Planned AWS IAM roles for Azure Entra SAML federation."
  value = {
    devops_engineer   = "${local.name_prefix}-DevOpsEngineer"
    readonly_auditor  = "${local.name_prefix}-ReadOnlyAuditor"
    metadata_file_set = var.entra_metadata_file != ""
  }
}
