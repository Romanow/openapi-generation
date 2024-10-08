resource "digitalocean_database_cluster" "postgres" {
  engine           = "PG"
  name             = var.database_name
  version          = var.database_version
  size             = var.database_size
  storage_size_mib = var.database_disk_size
  region           = var.region
  node_count       = 1
  maintenance_window {
    day  = "monday"
    hour = "00:00:00"
  }
}

resource "digitalocean_database_db" "database" {
  cluster_id = digitalocean_database_cluster.postgres.id
  name       = var.database_name
}

resource "digitalocean_database_user" "user" {
  cluster_id = digitalocean_database_cluster.postgres.id
  name       = var.database_user
}

resource "digitalocean_database_connection_pool" "connection_pool" {
  cluster_id = digitalocean_database_cluster.postgres.id
  db_name    = var.database_name
  user       = var.database_user
  mode       = "transaction"
  name       = var.project_name
  size       = 10
  depends_on = [
    digitalocean_database_cluster.postgres,
    digitalocean_database_db.database,
    digitalocean_database_user.user
  ]
}

resource "time_sleep" "wait_30_seconds" {
  create_duration = "30s"
  depends_on      = [digitalocean_database_connection_pool.connection_pool]
}

resource "digitalocean_app" "application" {
  spec {
    name   = "${var.project_name}-app"
    region = var.region

    domain {
      name = "sqadays.${var.domain}"
      zone = var.domain
      type = "PRIMARY"
    }

    service {
      name               = "app"
      instance_count     = var.backend_count
      instance_size_slug = var.backend_size
      http_port          = var.application_port

      image {
        registry_type        = "DOCKER_HUB"
        registry             = var.application_image_repository
        repository           = var.application_image_name
        tag                  = var.application_image_tag
        registry_credentials = "${var.docker_hub_login}:${var.docker_hub_token}"
      }

      env {
        key   = "SPRING_PROFILES_ACTIVE"
        value = var.application_profile
      }

      env {
        key   = "DATABASE_URL"
        value = digitalocean_database_connection_pool.connection_pool.host
      }

      env {
        key   = "DATABASE_PORT"
        value = digitalocean_database_connection_pool.connection_pool.port
      }

      env {
        key   = "DATABASE_NAME"
        value = digitalocean_database_connection_pool.connection_pool.name
      }

      env {
        key   = "DATABASE_USER"
        value = var.database_user
      }

      env {
        key   = "DATABASE_PASSWORD"
        value = digitalocean_database_user.user.password
      }
      health_check {
        http_path             = "/manage/health"
        initial_delay_seconds = 20
        period_seconds        = 10
        success_threshold     = 1
        failure_threshold     = 10
      }
    }
  }
}
