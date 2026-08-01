output "module_status" {
  description = "ECS module skeleton status."
  value       = "ecs-cluster-task-definitions-services-and-log-groups-ready"
}

output "cluster_name" {
  description = "ECS cluster name."
  value       = aws_ecs_cluster.main.name
}

output "cluster_arn" {
  description = "ECS cluster ARN."
  value       = aws_ecs_cluster.main.arn
}

output "service_names" {
  description = "ECS service names."
  value = {
    frontend = aws_ecs_service.frontend.name
    backend  = aws_ecs_service.backend.name
  }
}

output "service_arns" {
  description = "ECS service ARNs."
  value = {
    frontend = aws_ecs_service.frontend.id
    backend  = aws_ecs_service.backend.id
  }
}

output "task_definition_arns" {
  description = "ECS task definition ARNs."
  value = {
    frontend = aws_ecs_task_definition.frontend.arn
    backend  = aws_ecs_task_definition.backend.arn
  }
}

output "log_group_names" {
  description = "CloudWatch log group names."
  value = {
    frontend = aws_cloudwatch_log_group.frontend.name
    backend  = aws_cloudwatch_log_group.backend.name
  }
}
