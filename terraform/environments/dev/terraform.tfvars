project_name = "dce042"
environment  = "dev"
aws_region   = "ap-southeast-2"

vpc_cidr            = "10.42.0.0/16"
public_subnet_cidrs = ["10.42.1.0/24", "10.42.2.0/24"]
enable_nat_gateway  = false

frontend_repository_name = "dce042-frontend"
backend_repository_name  = "dce042-backend"
dynamodb_table_name      = "dce042-users-posts"

# Fill this before creating SNS subscriptions in the monitoring module.
notification_email = ""

# Fill this after downloading Azure Entra federation metadata.
# Example: "secrets/entra-federation-metadata.xml"
entra_metadata_file = ""
