provider "aws" {
  region = var.aws_region
}

module "app_platform" {
  source = "../../modules/app_platform"

  project_name       = var.project_name
  environment        = var.environment
  aws_region         = var.aws_region
  instance_type      = var.instance_type
  enable_ssh_ingress = var.enable_ssh_ingress
  ssh_allowed_cidr   = var.ssh_allowed_cidr
  ssh_public_key     = var.ssh_public_key
  frontend_port      = var.frontend_port
  backend_port       = var.backend_port
  frontend_repo_name = var.frontend_repo_name
  backend_repo_name  = var.backend_repo_name
}
