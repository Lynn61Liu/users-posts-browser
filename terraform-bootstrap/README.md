# Terraform Backend Bootstrap

This bootstrap project creates the remote backend used by the main Terraform project:

- S3 bucket for Terraform state
- DynamoDB table for Terraform state locking

Run this project once before enabling the S3 backend in `terraform/backend.tf`.

```bash
cd terraform-bootstrap
terraform init
terraform plan
terraform apply
```

Applied backend resources:

```text
S3 state bucket: dce042-terraform-state-345594568549-ap-southeast-2
DynamoDB lock table: dce042-terraform-locks
Region: ap-southeast-2
```

The main Terraform project now uses these values in `terraform/backend.tf`.
