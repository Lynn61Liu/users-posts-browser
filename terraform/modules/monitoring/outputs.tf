output "module_status" {
  description = "Monitoring module skeleton status."
  value       = "ready-for-cloudwatch-alarms-log-groups-and-sns"
}

output "planned_monitoring_resources" {
  description = "Planned monitoring resources."
  value = {
    frontend_log_group = "/ecs/dce042-frontend"
    backend_log_group  = "/ecs/dce042-backend"
    sns_topic          = "${local.name_prefix}-alerts"
    notification_email = var.notification_email
  }
}
