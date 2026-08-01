output "module_status" {
  description = "ECR module skeleton status."
  value       = "ready-for-two-ecr-repositories"
}

output "planned_repository_names" {
  description = "Planned ECR repository names."
  value       = local.repositories
}
