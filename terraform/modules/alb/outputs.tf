output "module_status" {
  description = "ALB module skeleton status."
  value       = "two-public-albs-and-blue-green-target-groups-ready"
}

output "alb_arns" {
  description = "Application Load Balancer ARNs."
  value = {
    for service, alb in aws_lb.this : service => alb.arn
  }
}

output "alb_dns_names" {
  description = "Application Load Balancer DNS names."
  value = {
    for service, alb in aws_lb.this : service => alb.dns_name
  }
}

output "listener_arns" {
  description = "HTTP listener ARNs."
  value = {
    for service, listener in aws_lb_listener.http : service => listener.arn
  }
}

output "target_group_arns" {
  description = "Blue and green target group ARNs."
  value = {
    for name, target_group in aws_lb_target_group.this : name => target_group.arn
  }
}

output "target_group_names" {
  description = "Blue and green target group names."
  value = {
    for name, target_group in aws_lb_target_group.this : name => target_group.name
  }
}
