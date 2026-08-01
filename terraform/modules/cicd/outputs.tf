output "module_status" {
  description = "CI/CD module skeleton status."
  value       = "ready-for-codebuild-codedeploy-codepipeline"
}

output "planned_cicd_resources" {
  description = "Planned CI/CD resource names and buildspec paths."
  value = {
    pipeline            = "${local.name_prefix}-pipeline"
    frontend_build      = "${local.name_prefix}-frontend-build"
    backend_build       = "${local.name_prefix}-backend-build"
    frontend_buildspec  = "buildspec-frontend.yml"
    backend_buildspec   = "buildspec-backend.yml"
    frontend_ecr        = var.frontend_repository_name
    backend_ecr         = var.backend_repository_name
    frontend_codedeploy = "${local.name_prefix}-frontend-deploy"
    backend_codedeploy  = "${local.name_prefix}-backend-deploy"
  }
}
