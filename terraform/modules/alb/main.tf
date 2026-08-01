locals {
  name_prefix = "${var.project_name}-${var.environment}"

  services = {
    frontend = {
      alb_name          = "${local.name_prefix}-frontend-alb"
      security_group_id = var.frontend_alb_security_group_id
      container_port    = var.frontend_container_port
      health_check_path = var.frontend_health_check_path
    }
    backend = {
      alb_name          = "${local.name_prefix}-backend-alb"
      security_group_id = var.backend_alb_security_group_id
      container_port    = var.backend_container_port
      health_check_path = var.backend_health_check_path
    }
  }

  target_groups = {
    frontend_blue = {
      service           = "frontend"
      name              = "${local.name_prefix}-frontend-blue-tg"
      container_port    = var.frontend_container_port
      health_check_path = var.frontend_health_check_path
    }
    frontend_green = {
      service           = "frontend"
      name              = "${local.name_prefix}-frontend-green-tg"
      container_port    = var.frontend_container_port
      health_check_path = var.frontend_health_check_path
    }
    backend_blue = {
      service           = "backend"
      name              = "${local.name_prefix}-backend-blue-tg"
      container_port    = var.backend_container_port
      health_check_path = var.backend_health_check_path
    }
    backend_green = {
      service           = "backend"
      name              = "${local.name_prefix}-backend-green-tg"
      container_port    = var.backend_container_port
      health_check_path = var.backend_health_check_path
    }
  }
}

resource "aws_lb" "this" {
  for_each = local.services

  name               = each.value.alb_name
  internal           = false
  load_balancer_type = "application"
  security_groups    = [each.value.security_group_id]
  subnets            = var.public_subnet_ids
  idle_timeout       = 60

  enable_deletion_protection = false

  tags = merge(var.common_tags, {
    Name    = each.value.alb_name
    Service = each.key
  })
}

resource "aws_lb_target_group" "this" {
  for_each = local.target_groups

  name        = each.value.name
  port        = each.value.container_port
  protocol    = "HTTP"
  target_type = "ip"
  vpc_id      = var.vpc_id

  deregistration_delay = 30

  health_check {
    enabled             = true
    path                = each.value.health_check_path
    protocol            = "HTTP"
    matcher             = "200-399"
    interval            = 30
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 3
  }

  tags = merge(var.common_tags, {
    Name    = each.value.name
    Service = each.value.service
    Color   = endswith(each.key, "blue") ? "blue" : "green"
  })
}

resource "aws_lb_listener" "http" {
  for_each = local.services

  load_balancer_arn = aws_lb.this[each.key].arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.this["${each.key}_blue"].arn
  }

  tags = merge(var.common_tags, {
    Name    = "${local.name_prefix}-${each.key}-http-listener"
    Service = each.key
  })
}
