locals {
  services = {
    frontend = {
      service_name = var.service_names.frontend
      label        = "frontend"
    }
    backend = {
      service_name = var.service_names.backend
      label        = "backend"
    }
  }

  scaling_policies = merge(
    {
      for key, service in local.services : "${key}_scale_up" => {
        service_key        = key
        service_name       = service.service_name
        direction          = "up"
        scaling_adjustment = 1
        cooldown           = 60
      }
    },
    {
      for key, service in local.services : "${key}_scale_down" => {
        service_key        = key
        service_name       = service.service_name
        direction          = "down"
        scaling_adjustment = -1
        cooldown           = 120
      }
    }
  )
}

resource "aws_appautoscaling_target" "ecs_desired_count" {
  for_each = local.services

  max_capacity       = var.max_capacity
  min_capacity       = var.min_capacity
  resource_id        = "service/${var.cluster_name}/${each.value.service_name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}

resource "aws_appautoscaling_policy" "ecs_step" {
  for_each = local.scaling_policies

  name               = "${var.project_name}-${var.environment}-${each.value.service_key}-${each.value.direction}"
  policy_type        = "StepScaling"
  resource_id        = aws_appautoscaling_target.ecs_desired_count[each.value.service_key].resource_id
  scalable_dimension = aws_appautoscaling_target.ecs_desired_count[each.value.service_key].scalable_dimension
  service_namespace  = aws_appautoscaling_target.ecs_desired_count[each.value.service_key].service_namespace

  step_scaling_policy_configuration {
    adjustment_type         = "ChangeInCapacity"
    cooldown                = each.value.cooldown
    metric_aggregation_type = "Average"

    step_adjustment {
      metric_interval_lower_bound = each.value.direction == "up" ? 0 : null
      metric_interval_upper_bound = each.value.direction == "down" ? 0 : null
      scaling_adjustment          = each.value.scaling_adjustment
    }
  }
}

resource "aws_cloudwatch_metric_alarm" "cpu_high" {
  for_each = local.services

  alarm_name          = "${var.project_name}-${var.environment}-${each.key}-cpu-high"
  alarm_description   = "Scale up ${each.value.service_name} when average CPU is high."
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "CPUUtilization"
  namespace           = "AWS/ECS"
  period              = 60
  statistic           = "Average"
  threshold           = 60
  treat_missing_data  = "notBreaching"

  dimensions = {
    ClusterName = var.cluster_name
    ServiceName = each.value.service_name
  }

  alarm_actions = [
    aws_appautoscaling_policy.ecs_step["${each.key}_scale_up"].arn,
    var.sns_topic_arn,
  ]

  ok_actions = [var.sns_topic_arn]

  tags = merge(var.common_tags, {
    Name    = "${var.project_name}-${var.environment}-${each.key}-cpu-high"
    Purpose = "ECS scale up alarm"
  })
}

resource "aws_cloudwatch_metric_alarm" "cpu_low" {
  for_each = local.services

  alarm_name          = "${var.project_name}-${var.environment}-${each.key}-cpu-low"
  alarm_description   = "Scale down ${each.value.service_name} when average CPU is low."
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/ECS"
  period              = 60
  statistic           = "Average"
  threshold           = 20
  treat_missing_data  = "notBreaching"

  dimensions = {
    ClusterName = var.cluster_name
    ServiceName = each.value.service_name
  }

  alarm_actions = [
    aws_appautoscaling_policy.ecs_step["${each.key}_scale_down"].arn,
    var.sns_topic_arn,
  ]

  ok_actions = [var.sns_topic_arn]

  tags = merge(var.common_tags, {
    Name    = "${var.project_name}-${var.environment}-${each.key}-cpu-low"
    Purpose = "ECS scale down alarm"
  })
}
