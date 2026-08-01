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
