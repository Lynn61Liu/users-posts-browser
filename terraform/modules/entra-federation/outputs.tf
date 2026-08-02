output "module_status" {
  description = "Entra federation module status."
  value       = local.create_federation ? "created-iam-saml-provider-and-federated-roles" : "waiting-for-entra-metadata-file"
}

output "planned_federated_roles" {
  description = "Planned AWS IAM roles for Azure Entra SAML federation."
  value = {
    devops_engineer   = local.role_names.devops_engineer
    readonly_auditor  = local.role_names.readonly_auditor
    metadata_file_set = var.entra_metadata_file != ""
  }
}

output "saml_provider_arn" {
  description = "AWS IAM SAML provider ARN created from Azure Entra metadata."
  value       = local.create_federation ? aws_iam_saml_provider.entra[0].arn : null
}

output "role_arns" {
  description = "Federated IAM role ARNs for Azure Entra SAML role claims."
  value = local.create_federation ? {
    devops_engineer  = aws_iam_role.devops_engineer[0].arn
    readonly_auditor = aws_iam_role.readonly_auditor[0].arn
  } : {}
}

output "role_claim_values" {
  description = "Role claim values to configure in Azure Entra SAML attributes."
  value = local.create_federation ? {
    devops_engineer  = "${aws_iam_role.devops_engineer[0].arn},${aws_iam_saml_provider.entra[0].arn}"
    readonly_auditor = "${aws_iam_role.readonly_auditor[0].arn},${aws_iam_saml_provider.entra[0].arn}"
  } : {}
}
