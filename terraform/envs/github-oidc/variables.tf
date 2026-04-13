variable "aws_region" {
  description = "AWS region used by the GitHub Actions deploy workflow."
  type        = string
  default     = "ap-northeast-1"
}

variable "project_name" {
  description = "Base project name used for tags and resource names."
  type        = string
  default     = "job-selection-tracker"
}

variable "environment" {
  description = "Environment name for the deploy identity."
  type        = string
  default     = "prod"
}

variable "github_repository" {
  description = "GitHub repository allowed to assume the deploy role, in owner/repo format."
  type        = string
  default     = "Li-Bertygi/job-selection-tracker"
}

variable "github_deploy_branch" {
  description = "GitHub branch allowed to assume the deploy role."
  type        = string
  default     = "main"
}

variable "github_oidc_provider_arn" {
  description = "Existing GitHub Actions OIDC provider ARN. Leave empty to create a provider."
  type        = string
  default     = ""
}

variable "frontend_repo_name" {
  description = "ECR repository name for the frontend image pushed by deploy-ec2.yml."
  type        = string
  default     = "job-selection-tracker-frontend"
}

variable "backend_repo_name" {
  description = "ECR repository name for the backend image pushed by deploy-ec2.yml."
  type        = string
  default     = "job-selection-tracker-backend"
}
