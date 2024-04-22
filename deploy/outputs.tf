output "application_url" {
  value       = digitalocean_app.application.live_url
  description = "Application URL"
}
