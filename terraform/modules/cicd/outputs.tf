output "module_status" {
  description = "CI/CD module status."
  value       = "codebuild-codedeploy-codepipeline-ready"
}

output "cicd_resources" {
  description = "Created CI/CD resource names."
  value = {
    pipeline_name          = aws_codepipeline.main.name
    artifact_bucket        = var.artifact_bucket_name
    source_repository      = var.repository_full_name
    source_branch          = var.repository_branch
    frontend_build_project = aws_codebuild_project.service["frontend"].name
    backend_build_project  = aws_codebuild_project.service["backend"].name
    frontend_codedeploy = {
      application      = aws_codedeploy_app.service["frontend"].name
      deployment_group = aws_codedeploy_deployment_group.service["frontend"].deployment_group_name
    }
    backend_codedeploy = {
      application      = aws_codedeploy_app.service["backend"].name
      deployment_group = aws_codedeploy_deployment_group.service["backend"].deployment_group_name
    }
  }
}
