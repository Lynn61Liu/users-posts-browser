locals {
  name_prefix = "${var.project_name}-${var.environment}"

  common_tags = {
    Project     = var.project_name
    Environment = var.environment
    ManagedBy   = "Terraform"
    Assessment  = "DCE04.2"
  }
}

module "network" {
  source = "./modules/network"

  project_name        = var.project_name
  environment         = var.environment
  aws_region          = var.aws_region
  vpc_cidr            = var.vpc_cidr
  availability_zones  = var.availability_zones
  public_subnet_cidrs = var.public_subnet_cidrs
  enable_nat_gateway  = var.enable_nat_gateway
  common_tags         = local.common_tags
}

module "security" {
  source = "./modules/security"

  project_name        = var.project_name
  environment         = var.environment
  vpc_id              = module.network.vpc_id
  dynamodb_table_name = var.dynamodb_table_name
  common_tags         = local.common_tags
}

module "ecr" {
  source = "./modules/ecr"

  project_name             = var.project_name
  environment              = var.environment
  frontend_repository_name = var.frontend_repository_name
  backend_repository_name  = var.backend_repository_name
  common_tags              = local.common_tags
}

module "alb" {
  source = "./modules/alb"

  project_name                   = var.project_name
  environment                    = var.environment
  vpc_id                         = module.network.vpc_id
  public_subnet_ids              = module.network.public_subnet_ids
  frontend_alb_security_group_id = module.security.security_group_ids.frontend_alb
  backend_alb_security_group_id  = module.security.security_group_ids.backend_alb
  common_tags                    = local.common_tags
}

module "ecs" {
  source = "./modules/ecs"

  project_name                   = var.project_name
  environment                    = var.environment
  aws_region                     = var.aws_region
  dynamodb_table_name            = var.dynamodb_table_name
  public_subnet_ids              = module.network.public_subnet_ids
  frontend_ecs_security_group_id = module.security.security_group_ids.frontend_ecs
  backend_ecs_security_group_id  = module.security.security_group_ids.backend_ecs
  frontend_image_uri             = "${module.ecr.repository_urls.frontend}:latest"
  backend_image_uri              = "${module.ecr.repository_urls.backend}:latest"
  ecs_task_execution_role_arn    = module.security.iam_role_arns.ecs_task_execution
  frontend_task_role_arn         = module.security.iam_role_arns.frontend_task
  backend_task_role_arn          = module.security.iam_role_arns.backend_task
  frontend_blue_target_group_arn = module.alb.target_group_arns.frontend_blue
  backend_blue_target_group_arn  = module.alb.target_group_arns.backend_blue
  frontend_listener_arn          = module.alb.listener_arns.frontend
  backend_listener_arn           = module.alb.listener_arns.backend
  backend_alb_dns_name           = module.alb.alb_dns_names.backend
  common_tags                    = local.common_tags
}

module "autoscaling" {
  source = "./modules/autoscaling"

  project_name  = var.project_name
  environment   = var.environment
  cluster_name  = module.ecs.cluster_name
  service_names = module.ecs.service_names
  sns_topic_arn = module.monitoring.notification_resources.sns_topic_arn
  common_tags   = local.common_tags
}

module "data" {
  source = "./modules/data"

  project_name        = var.project_name
  environment         = var.environment
  aws_region          = var.aws_region
  dynamodb_table_name = var.dynamodb_table_name
  common_tags         = local.common_tags
}

module "monitoring" {
  source = "./modules/monitoring"

  project_name       = var.project_name
  environment        = var.environment
  notification_email = var.notification_email
  common_tags        = local.common_tags
}

module "cicd" {
  source = "./modules/cicd"

  project_name                = var.project_name
  environment                 = var.environment
  aws_region                  = var.aws_region
  frontend_repository_name    = var.frontend_repository_name
  backend_repository_name     = var.backend_repository_name
  artifact_bucket_name        = module.data.storage_resources.artifact_bucket_name
  artifact_bucket_arn         = module.data.storage_resources.artifact_bucket_arn
  repository_full_name        = var.repository_full_name
  repository_branch           = var.repository_branch
  codeconnection_arn          = var.codeconnection_arn
  ecr_repository_arns         = module.ecr.repository_arns
  ecs_cluster_name            = module.ecs.cluster_name
  ecs_service_names           = module.ecs.service_names
  ecs_task_execution_role_arn = module.security.iam_role_arns.ecs_task_execution
  frontend_task_role_arn      = module.security.iam_role_arns.frontend_task
  backend_task_role_arn       = module.security.iam_role_arns.backend_task
  backend_alb_dns_name        = module.alb.alb_dns_names.backend
  dynamodb_table_name         = var.dynamodb_table_name
  listener_arns               = module.alb.listener_arns
  target_group_names          = module.alb.target_group_names
  common_tags                 = local.common_tags
}

module "entra_federation" {
  source = "./modules/entra-federation"

  project_name        = var.project_name
  environment         = var.environment
  entra_metadata_file = var.entra_metadata_file
  common_tags         = local.common_tags
}
