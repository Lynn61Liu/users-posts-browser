output "module_status" {
  description = "Data module status."
  value       = "s3-buckets-and-dynamodb-application-table-ready"
}

output "storage_resources" {
  description = "Created storage resources."
  value = {
    artifact_bucket_name = aws_s3_bucket.pipeline_artifacts.bucket
    artifact_bucket_arn  = aws_s3_bucket.pipeline_artifacts.arn
    assets_bucket_name   = aws_s3_bucket.app_assets.bucket
    assets_bucket_arn    = aws_s3_bucket.app_assets.arn
    dynamodb_table_name  = aws_dynamodb_table.application.name
    dynamodb_table_arn   = aws_dynamodb_table.application.arn
  }
}
