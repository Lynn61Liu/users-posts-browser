output "module_status" {
  description = "Monitoring module status."
  value       = "sns-topic-ready-for-cloudwatch-alarms"
}

output "notification_resources" {
  description = "Created notification resources."
  value = {
    frontend_log_group = "/ecs/dce042-frontend"
    backend_log_group  = "/ecs/dce042-backend"
    sns_topic_name     = aws_sns_topic.critical_notifications.name
    sns_topic_arn      = aws_sns_topic.critical_notifications.arn
    notification_email = var.notification_email
    email_subscription = var.notification_email == "" ? "not configured" : aws_sns_topic_subscription.email[0].arn
  }
}
