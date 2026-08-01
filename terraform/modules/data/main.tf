locals {
  s3_artifact_bucket_prefix = "${var.project_name}-${var.environment}-pipeline-artifacts"
  s3_assets_bucket_prefix   = "${var.project_name}-${var.environment}-app-assets"
}
