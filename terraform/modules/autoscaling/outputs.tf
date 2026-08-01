output "module_status" {
  description = "Autoscaling module skeleton status."
  value       = "ready-for-four-scaling-policies"
}

output "planned_target_services" {
  description = "Services that will receive scaling policies."
  value       = local.target_services
}
