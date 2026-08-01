locals {
  name_prefix = "${var.project_name}-${var.environment}"
}

resource "aws_sns_topic" "critical_notifications" {
  name = "${local.name_prefix}-critical-notifications"

  tags = merge(var.common_tags, {
    Name    = "${local.name_prefix}-critical-notifications"
    Purpose = "Deployment scaling and monitoring notifications"
  })
}

resource "aws_sns_topic_subscription" "email" {
  count = var.notification_email == "" ? 0 : 1

  topic_arn = aws_sns_topic.critical_notifications.arn
  protocol  = "email"
  endpoint  = var.notification_email
}
