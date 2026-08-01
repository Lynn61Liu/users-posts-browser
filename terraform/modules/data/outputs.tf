output "module_status" {
  description = "Data module skeleton status."
  value       = "ready-for-s3-buckets-and-dynamodb-application-table"
}

output "planned_data_resources" {
  description = "Planned storage resources."
  value = {
    dynamodb_table_name       = var.dynamodb_table_name
    artifact_bucket_prefix    = local.s3_artifact_bucket_prefix
    application_bucket_prefix = local.s3_assets_bucket_prefix
  }
}
