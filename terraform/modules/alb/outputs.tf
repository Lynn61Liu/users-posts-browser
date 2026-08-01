output "module_status" {
  description = "ALB module skeleton status."
  value       = "ready-for-two-public-albs-and-blue-green-target-groups"
}

output "planned_alb_names" {
  description = "Planned ALB names."
  value = {
    frontend = "${local.name_prefix}-frontend-alb"
    backend  = "${local.name_prefix}-backend-alb"
  }
}
