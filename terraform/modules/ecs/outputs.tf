output "module_status" {
  description = "ECS module skeleton status."
  value       = "ready-for-fargate-cluster-task-definitions-and-services"
}

output "planned_services" {
  description = "Planned ECS service names."
  value = {
    cluster  = "${local.name_prefix}-ecs-cluster"
    frontend = "frontend-service"
    backend  = "backend-service"
  }
}

output "backend_dynamodb_table_name" {
  description = "DynamoDB table name planned for backend ECS task environment."
  value       = var.dynamodb_table_name
}
