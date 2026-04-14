variable "project_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "aws_region" {
  type = string
}

variable "instance_type" {
  type = string
}

variable "ssh_allowed_cidr" {
  type = string
}

variable "enable_ssh_ingress" {
  type = bool
}

variable "ssh_public_key" {
  type      = string
  sensitive = true
}

variable "frontend_port" {
  type = number
}

variable "backend_port" {
  type = number
}

variable "frontend_repo_name" {
  type = string
}

variable "backend_repo_name" {
  type = string
}
