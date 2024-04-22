resource "digitalocean_database_cluster" "postgres" {
  name             = "${var.project_name}-db"
  engine           = "pg"
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
  mode       = "transaction"
  name       = "${var.project_name}-connection-pool"
  size       = 10
  user       = var.database_user
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
        value = digitalocean_database_connection_pool.connection_pool.host
      }

      env {
        key   = "DATABASE_PORT"
        value = digitalocean_database_connection_pool.connection_pool.port
      }

      env {
        key   = "DATABASE_NAME"
        value = var.database_name
      }

      env {
        key   = "DATABASE_USER"
        value = var.database_user
      }

      env {
        key   = "DATABASE_PASSWORD"
        value = digitalocean_database_connection_pool.connection_pool.password
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
      cluster_name = "${var.project_name}-db"
      engine       = "pg"
      production   = false
    }
  }
}