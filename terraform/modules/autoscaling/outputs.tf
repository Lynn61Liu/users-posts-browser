output "module_status" {
  description = "Autoscaling module status."
  value       = "four-scaling-policies-and-four-cpu-alarms-ready"
}

output "autoscaling_targets" {
  description = "ECS autoscaling targets."
  value = {
    for key, target in aws_appautoscaling_target.ecs_desired_count : key => {
      resource_id  = target.resource_id
      min_capacity = target.min_capacity
      max_capacity = target.max_capacity
    }
  }
}

output "scaling_policy_names" {
  description = "Four ECS step scaling policies."
  value = {
    for key, policy in aws_appautoscaling_policy.ecs_step : key => policy.name
  }
}

output "cloudwatch_alarm_names" {
  description = "Four CPU CloudWatch alarms."
  value = {
    frontend_cpu_high = aws_cloudwatch_metric_alarm.cpu_high["frontend"].alarm_name
    frontend_cpu_low  = aws_cloudwatch_metric_alarm.cpu_low["frontend"].alarm_name
    backend_cpu_high  = aws_cloudwatch_metric_alarm.cpu_high["backend"].alarm_name
    backend_cpu_low   = aws_cloudwatch_metric_alarm.cpu_low["backend"].alarm_name
  }
}
