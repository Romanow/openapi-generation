resource "digitalocean_app" "application" {
  spec {
    name   = "${var.project_name}-app"
    region = var.region

    domain {
      name = "${var.project_name}.${var.domain}"
      zone = var.domain
      type = "PRIMARY"
    }

    service {
      name               = "app"
      instance_count     = var.backend_count
      instance_size_slug = var.backend_size
      http_port          = var.application_port

      image {
        registry_type = "DOCKER_HUB"
        registry      = var.application_image_repository
        repository    = var.application_image_name
        tag           = var.application_image_tag
      }

      env {
        key   = "SPRING_PROFILES_ACTIVE"
        value = var.application_profile
      }

      env {
        key   = "DATABASE_URL"
        value = "$${${var.project_name}.JDBC_DATABASE_URL}"
      }

      env {
        key   = "DATABASE_USER"
        value = var.database_user
      }

      env {
        key   = "DATABASE_PASSWORD"
        value = "$${${var.project_name}.PASSWORD}"
        type  = "SECRET"
      }

      health_check {
        http_path             = "/manage/health"
        initial_delay_seconds = 20
        period_seconds        = 10
        success_threshold     = 1
        failure_threshold     = 10
      }
    }

    database {
      name         = var.project_name
      cluster_name = "${var.project_name}-db"
      engine       = "pg"
      production   = false
      db_name      = var.database_name
      db_user      = var.database_user
      version      = var.database_version
    }
  }
}
