variable "aws_region" {
  description = "AWS region for the development environment."
  type        = string
  default     = "ap-northeast-1"
}

variable "project_name" {
  description = "Base project name used for tags and resource names."
  type        = string
  default     = "job-selection-tracker"
}

variable "environment" {
  description = "Environment name."
  type        = string
  default     = "dev"
}

variable "instance_type" {
  description = "EC2 instance type for the application host."
  type        = string
  default     = "t3.small"
}

variable "ssh_allowed_cidr" {
  description = "CIDR block allowed to connect to the instance over SSH."
  type        = string
  default     = "0.0.0.0/0"
}

variable "enable_ssh_ingress" {
  description = "Whether to open inbound SSH on the application security group. SSM deployment does not require SSH."
  type        = bool
  default     = false
}

variable "ssh_public_key" {
  description = "Optional SSH public key to register as an EC2 key pair."
  type        = string
  default     = ""
  sensitive   = true
}

variable "frontend_port" {
  description = "Frontend application port."
  type        = number
  default     = 3000
}

variable "backend_port" {
  description = "Backend application port."
  type        = number
  default     = 8080
}

variable "frontend_repo_name" {
  description = "ECR repository name for the frontend image."
  type        = string
  default     = "job-selection-tracker-frontend"
}

variable "backend_repo_name" {
  description = "ECR repository name for the backend image."
  type        = string
  default     = "job-selection-tracker-backend"
}
