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

  project_name = var.project_name
  environment  = var.environment
  common_tags  = local.common_tags
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

  project_name = var.project_name
  environment  = var.environment
  common_tags  = local.common_tags
}

module "ecs" {
  source = "./modules/ecs"

  project_name        = var.project_name
  environment         = var.environment
  aws_region          = var.aws_region
  dynamodb_table_name = var.dynamodb_table_name
  common_tags         = local.common_tags
}

module "autoscaling" {
  source = "./modules/autoscaling"

  project_name = var.project_name
  environment  = var.environment
  common_tags  = local.common_tags
}

module "data" {
  source = "./modules/data"

  project_name        = var.project_name
  environment         = var.environment
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

  project_name             = var.project_name
  environment              = var.environment
  frontend_repository_name = var.frontend_repository_name
  backend_repository_name  = var.backend_repository_name
  common_tags              = local.common_tags
}

module "entra_federation" {
  source = "./modules/entra-federation"

  project_name        = var.project_name
  environment         = var.environment
  entra_metadata_file = var.entra_metadata_file
  common_tags         = local.common_tags
}
