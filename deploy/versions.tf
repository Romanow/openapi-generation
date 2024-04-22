terraform {
  required_version = ">= 1.6"
  required_providers {
    digitalocean = {
      source  = "digitalocean/digitalocean"
      version = ">= 2.36"
    }
  }
  cloud {
    organization = "Romanow-Pride"
    workspaces {
      name = "openapi-generation"
    }
  }
}

provider "digitalocean" {
  token = var.do_token
}
