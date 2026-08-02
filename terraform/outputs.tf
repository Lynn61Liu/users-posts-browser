output "project_context" {
  description = "Shared project settings for this Terraform workspace."
  value = {
    project_name        = var.project_name
    environment         = var.environment
    aws_region          = var.aws_region
    name_prefix         = local.name_prefix
    dynamodb_table_name = var.dynamodb_table_name
  }
}

output "module_status" {
  description = "Step 5 module skeleton status. These outputs will be replaced by real resource outputs in later steps."
  value = {
    network          = module.network.module_status
    security         = module.security.module_status
    ecr              = module.ecr.module_status
    alb              = module.alb.module_status
    ecs              = module.ecs.module_status
    autoscaling      = module.autoscaling.module_status
    data             = module.data.module_status
    monitoring       = module.monitoring.module_status
    cicd             = module.cicd.module_status
    entra_federation = module.entra_federation.module_status
  }
}

output "network" {
  description = "Created AWS network resources for the assessment environment."
  value = {
    vpc_id                           = module.network.vpc_id
    vpc_cidr                         = module.network.vpc_cidr
    public_subnet_ids                = module.network.public_subnet_ids
    public_subnet_cidrs              = module.network.public_subnet_cidrs
    public_subnet_availability_zones = module.network.public_subnet_availability_zones
    internet_gateway_id              = module.network.internet_gateway_id
    public_route_table_id            = module.network.public_route_table_id
    nat_gateway_id                   = module.network.nat_gateway_id
  }
}

output "security" {
  description = "Created security groups and ECS IAM roles."
  value = {
    security_group_ids = module.security.security_group_ids
    iam_role_arns      = module.security.iam_role_arns
  }
}

output "ecr" {
  description = "Created ECR repositories for frontend and backend images."
  value = {
    repository_names = module.ecr.repository_names
    repository_urls  = module.ecr.repository_urls
    repository_arns  = module.ecr.repository_arns
  }
}

output "alb" {
  description = "Created public ALBs, listeners, and blue/green target groups."
  value = {
    alb_arns           = module.alb.alb_arns
    alb_dns_names      = module.alb.alb_dns_names
    listener_arns      = module.alb.listener_arns
    target_group_names = module.alb.target_group_names
    target_group_arns  = module.alb.target_group_arns
  }
}

output "ecs" {
  description = "Created ECS Fargate cluster, task definitions, services, and log groups."
  value = {
    cluster_name         = module.ecs.cluster_name
    cluster_arn          = module.ecs.cluster_arn
    service_names        = module.ecs.service_names
    service_arns         = module.ecs.service_arns
    task_definition_arns = module.ecs.task_definition_arns
    log_group_names      = module.ecs.log_group_names
  }
}

output "data" {
  description = "Created S3 and DynamoDB data resources."
  value       = module.data.storage_resources
}

output "monitoring" {
  description = "Created notification resources and monitoring integration points."
  value       = module.monitoring.notification_resources
}

output "autoscaling" {
  description = "Created ECS autoscaling targets, policies, and alarms."
  value = {
    targets          = module.autoscaling.autoscaling_targets
    scaling_policies = module.autoscaling.scaling_policy_names
    cpu_alarms       = module.autoscaling.cloudwatch_alarm_names
  }
}

output "cicd" {
  description = "Created CI/CD resources."
  value       = module.cicd.cicd_resources
}

output "entra_federation" {
  description = "Azure Entra ID SAML federation resources and role claim values."
  value = {
    status            = module.entra_federation.module_status
    planned_roles     = module.entra_federation.planned_federated_roles
    saml_provider_arn = module.entra_federation.saml_provider_arn
    role_arns         = module.entra_federation.role_arns
    role_claim_values = module.entra_federation.role_claim_values
  }
}
