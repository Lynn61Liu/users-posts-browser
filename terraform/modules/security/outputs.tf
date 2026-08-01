output "module_status" {
  description = "Security module skeleton status."
  value       = "alb-ecs-security-groups-and-ecs-iam-roles-ready"
}

output "security_group_ids" {
  description = "Security group IDs for ALBs and ECS tasks."
  value = {
    frontend_alb = aws_security_group.frontend_alb.id
    backend_alb  = aws_security_group.backend_alb.id
    frontend_ecs = aws_security_group.frontend_ecs.id
    backend_ecs  = aws_security_group.backend_ecs.id
  }
}

output "iam_role_arns" {
  description = "IAM role ARNs for ECS task execution and task permissions."
  value = {
    ecs_task_execution = aws_iam_role.ecs_task_execution.arn
    frontend_task      = aws_iam_role.frontend_task.arn
    backend_task       = aws_iam_role.backend_task.arn
  }
}
