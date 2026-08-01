output "module_status" {
  description = "Network module skeleton status."
  value       = var.enable_nat_gateway ? "vpc-public-subnets-igw-route-table-nat-ready" : "vpc-public-subnets-igw-route-table-ready"
}

output "vpc_id" {
  description = "VPC ID."
  value       = aws_vpc.main.id
}

output "vpc_cidr" {
  description = "VPC CIDR block."
  value       = aws_vpc.main.cidr_block
}

output "public_subnet_ids" {
  description = "Public subnet IDs."
  value       = aws_subnet.public[*].id
}

output "public_subnet_cidrs" {
  description = "Public subnet CIDR blocks."
  value       = aws_subnet.public[*].cidr_block
}

output "public_subnet_availability_zones" {
  description = "Availability Zones used by public subnets."
  value       = aws_subnet.public[*].availability_zone
}

output "internet_gateway_id" {
  description = "Internet Gateway ID."
  value       = aws_internet_gateway.main.id
}

output "public_route_table_id" {
  description = "Public route table ID."
  value       = aws_route_table.public.id
}

output "nat_gateway_id" {
  description = "NAT Gateway ID, if enabled."
  value       = var.enable_nat_gateway ? aws_nat_gateway.main[0].id : null
}
