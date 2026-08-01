locals {
  name_prefix = "${var.project_name}-${var.environment}"

  cluster_name            = "${local.name_prefix}-ecs-cluster"
  frontend_service_name   = "frontend-service"
  backend_service_name    = "backend-service"
  frontend_task_family    = "${var.project_name}-frontend-task"
  backend_task_family     = "${var.project_name}-backend-task"
  frontend_log_group_name = "/ecs/${var.project_name}-frontend"
  backend_log_group_name  = "/ecs/${var.project_name}-backend"
}

resource "aws_ecs_cluster" "main" {
  name = local.cluster_name

  tags = merge(var.common_tags, {
    Name = local.cluster_name
  })
}

resource "aws_cloudwatch_log_group" "frontend" {
  name              = local.frontend_log_group_name
  retention_in_days = 7

  tags = merge(var.common_tags, {
    Name    = local.frontend_log_group_name
    Service = "frontend"
  })
}

resource "aws_cloudwatch_log_group" "backend" {
  name              = local.backend_log_group_name
  retention_in_days = 7

  tags = merge(var.common_tags, {
    Name    = local.backend_log_group_name
    Service = "backend"
  })
}

resource "aws_ecs_task_definition" "frontend" {
  family                   = local.frontend_task_family
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = var.ecs_task_execution_role_arn
  task_role_arn            = var.frontend_task_role_arn

  runtime_platform {
    cpu_architecture        = "X86_64"
    operating_system_family = "LINUX"
  }

  container_definitions = jsonencode([
    {
      name      = "frontend"
      image     = var.frontend_image_uri
      essential = true
      portMappings = [
        {
          containerPort = 80
          hostPort      = 80
          protocol      = "tcp"
        }
      ]
      environment = [
        {
          name  = "BACKEND_UPSTREAM"
          value = var.backend_alb_dns_name
        }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = local.frontend_log_group_name
          awslogs-region        = var.aws_region
          awslogs-stream-prefix = "ecs"
        }
      }
    }
  ])

  tags = merge(var.common_tags, {
    Name    = local.frontend_task_family
    Service = "frontend"
  })
}

resource "aws_ecs_task_definition" "backend" {
  family                   = local.backend_task_family
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = var.ecs_task_execution_role_arn
  task_role_arn            = var.backend_task_role_arn

  runtime_platform {
    cpu_architecture        = "X86_64"
    operating_system_family = "LINUX"
  }

  container_definitions = jsonencode([
    {
      name      = "backend"
      image     = var.backend_image_uri
      essential = true
      portMappings = [
        {
          containerPort = 8080
          hostPort      = 8080
          protocol      = "tcp"
        }
      ]
      environment = [
        {
          name  = "SERVER_PORT"
          value = "8080"
        },
        {
          name  = "APP_DEPLOY_ENV"
          value = "aws"
        },
        {
          name  = "APP_DYNAMODB_ENABLED"
          value = "true"
        },
        {
          name  = "DYNAMODB_TABLE_NAME"
          value = var.dynamodb_table_name
        },
        {
          name  = "AWS_REGION"
          value = var.aws_region
        },
        {
          name  = "APP_DEV_RESET_ENABLED"
          value = "false"
        }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = local.backend_log_group_name
          awslogs-region        = var.aws_region
          awslogs-stream-prefix = "ecs"
        }
      }
    }
  ])

  tags = merge(var.common_tags, {
    Name    = local.backend_task_family
    Service = "backend"
  })
}

resource "aws_ecs_service" "frontend" {
  name            = local.frontend_service_name
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.frontend.arn
  desired_count   = var.frontend_desired_count
  launch_type     = "FARGATE"

  health_check_grace_period_seconds = 60

  deployment_controller {
    type = "CODE_DEPLOY"
  }

  network_configuration {
    subnets          = var.public_subnet_ids
    security_groups  = [var.frontend_ecs_security_group_id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = var.frontend_blue_target_group_arn
    container_name   = "frontend"
    container_port   = 80
  }

  lifecycle {
    ignore_changes = [
      task_definition,
      load_balancer
    ]
  }

  tags = merge(var.common_tags, {
    Name    = local.frontend_service_name
    Service = "frontend"
  })

  depends_on = [
    aws_cloudwatch_log_group.frontend,
    var.frontend_listener_arn
  ]
}

resource "aws_ecs_service" "backend" {
  name            = local.backend_service_name
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.backend.arn
  desired_count   = var.backend_desired_count
  launch_type     = "FARGATE"

  health_check_grace_period_seconds = 60

  deployment_controller {
    type = "CODE_DEPLOY"
  }

  network_configuration {
    subnets          = var.public_subnet_ids
    security_groups  = [var.backend_ecs_security_group_id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = var.backend_blue_target_group_arn
    container_name   = "backend"
    container_port   = 8080
  }

  lifecycle {
    ignore_changes = [
      task_definition,
      load_balancer
    ]
  }

  tags = merge(var.common_tags, {
    Name    = local.backend_service_name
    Service = "backend"
  })

  depends_on = [
    aws_cloudwatch_log_group.backend,
    var.backend_listener_arn
  ]
}
