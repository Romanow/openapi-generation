terraform {
  required_version = ">= 1.8"
  required_providers {
    digitalocean = {
      source  = "digitalocean/digitalocean"
      version = ">= 2.36"
    }
  }
  cloud {
    organization = "Romanow-Pride"
    workspaces {
      name = "main"
    }
  }
}

provider "digitalocean" {
  token = var.do_token
}
