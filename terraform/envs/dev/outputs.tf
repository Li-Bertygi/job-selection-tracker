output "ec2_public_ip" {
  description = "Public IP address of the EC2 instance."
  value       = module.app_platform.ec2_public_ip
}

output "ec2_public_dns" {
  description = "Public DNS name of the EC2 instance."
  value       = module.app_platform.ec2_public_dns
}

output "ec2_instance_id" {
  description = "EC2 instance ID used by SSM Run Command deployment."
  value       = module.app_platform.ec2_instance_id
}

output "frontend_repository_url" {
  description = "ECR URL for the frontend repository."
  value       = module.app_platform.frontend_repository_url
}

output "backend_repository_url" {
  description = "ECR URL for the backend repository."
  value       = module.app_platform.backend_repository_url
}

output "frontend_url" {
  description = "Expected frontend URL after deployment."
  value       = "http://${module.app_platform.ec2_public_ip}:${var.frontend_port}"
}

output "backend_url" {
  description = "Expected backend URL after deployment."
  value       = "http://${module.app_platform.ec2_public_ip}:${var.backend_port}"
}
