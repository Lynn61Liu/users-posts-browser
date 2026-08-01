output "module_status" {
  description = "ECR module skeleton status."
  value       = "two-ecr-repositories-ready"
}

output "repository_names" {
  description = "ECR repository names."
  value = {
    for service, repository in aws_ecr_repository.this : service => repository.name
  }
}

output "repository_urls" {
  description = "ECR repository URLs."
  value = {
    for service, repository in aws_ecr_repository.this : service => repository.repository_url
  }
}

output "repository_arns" {
  description = "ECR repository ARNs."
  value = {
    for service, repository in aws_ecr_repository.this : service => repository.arn
  }
}
