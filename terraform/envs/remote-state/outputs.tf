output "state_bucket_name" {
  description = "S3 bucket name for Terraform remote state."
  value       = aws_s3_bucket.terraform_state.bucket
}

output "lock_table_name" {
  description = "DynamoDB table name for Terraform state locking."
  value       = aws_dynamodb_table.terraform_locks.name
}

output "dev_backend_key" {
  description = "Recommended S3 object key for terraform/envs/dev."
  value       = "${var.project_name}/dev/terraform.tfstate"
}

output "github_oidc_backend_key" {
  description = "Recommended S3 object key for terraform/envs/github-oidc."
  value       = "${var.project_name}/github-oidc/terraform.tfstate"
}
