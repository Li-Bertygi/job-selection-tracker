variable "aws_region" {
  description = "AWS region for the Terraform remote state backend."
  type        = string
  default     = "ap-northeast-1"
}

variable "project_name" {
  description = "Base project name used for tags and resource names."
  type        = string
  default     = "job-selection-tracker"
}

variable "environment" {
  description = "Environment name for shared Terraform backend resources."
  type        = string
  default     = "shared"
}

variable "state_bucket_name" {
  description = "Globally unique S3 bucket name for Terraform state files."
  type        = string
}

variable "lock_table_name" {
  description = "DynamoDB table name for Terraform state locking."
  type        = string
  default     = "job-selection-tracker-terraform-locks"
}
