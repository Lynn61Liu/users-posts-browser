terraform {
  backend "s3" {
    bucket         = "dce042-terraform-state-345594568549-ap-southeast-2"
    key            = "dce042/dev/terraform.tfstate"
    region         = "ap-southeast-2"
    dynamodb_table = "dce042-terraform-locks"
    encrypt        = true
  }
}
