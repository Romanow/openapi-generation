variable "do_token" {
  description = "DigitalOcean access token"
  type        = string
}

variable "project_name" {
  description = "Project name"
  type        = string
  default     = "servers"
}

variable "domain" {
  description = "Base domain"
  type        = string
  default     = "romanow-alex.ru"
}

variable "region" {
  description = "Deployment region"
  type        = string
  default     = "ams3"
}

variable "backend_count" {
  description = "Instance count"
  type        = number
  default     = 3
}

variable "backend_size" {
  description = "Instance size"
  type        = string
  default     = "basic-s"
}

variable "application_port" {
  description = "Application port"
  type        = number
  default     = 8080
}

variable "application_image_repository" {
  description = "Instance count"
  type        = string
  default     = "romanowalex"
}

variable "application_image_name" {
  description = "Application image name"
  type        = string
  default     = "servers"
}

variable "application_image_tag" {
  description = "Application image tag"
  type        = string
  default     = "v1.0"
}

variable "application_profile" {
  description = "Application profile"
  type        = string
  default     = "do"
}

variable "database_version" {
  description = "Database version"
  type        = number
  default     = 14
}

variable "database_size" {
  description = "Database size"
  type        = string
  default     = "db-s-1vcpu-1gb"
}

variable "database_name" {
  description = "Database name"
  type        = string
  default     = "servers"
}

variable "database_user" {
  description = "Database user"
  type        = string
  default     = "program"
}
