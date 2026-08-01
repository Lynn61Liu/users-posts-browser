output "module_status" {
  description = "Security module skeleton status."
  value       = "ready-for-alb-and-ecs-security-groups"
}

output "planned_security_groups" {
  description = "Planned security group names."
  value = {
    frontend_alb = "${local.name_prefix}-frontend-alb-sg"
    backend_alb  = "${local.name_prefix}-backend-alb-sg"
    frontend_ecs = "${local.name_prefix}-frontend-ecs-sg"
    backend_ecs  = "${local.name_prefix}-backend-ecs-sg"
  }
}
