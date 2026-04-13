output "github_actions_role_arn" {
  description = "IAM Role ARN to register as the GitHub secret AWS_ROLE_TO_ASSUME."
  value       = aws_iam_role.github_actions_deploy.arn
}

output "github_oidc_provider_arn" {
  description = "GitHub Actions OIDC provider ARN used by the deploy role."
  value       = local.oidc_provider_arn
}
