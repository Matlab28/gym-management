#!/usr/bin/env ruby

require "json"
require "yaml"

docs_dir = File.expand_path(__dir__)
gym = YAML.load_file(File.join(docs_dir, "gym-management.yaml"))
workload = YAML.load_file(File.join(docs_dir, "trainer-workload.yaml"))
origin = ENV.fetch("PUBLIC_ORIGIN", "http://localhost:8080").sub(%r{/+$}, "")

combined = Marshal.load(Marshal.dump(gym))
combined["openapi"] = "3.0.3"
combined["info"]["title"] = "Gym Management Backend API"
combined["info"]["description"] = <<~DESCRIPTION.strip
  Custom OpenAPI documentation for the Gym Management microservices. The platform
  manages authentication, trainees, trainers, assignments, training sessions, and
  synchronized monthly trainer workload.
DESCRIPTION
combined["servers"] = [
  {
    "url" => origin,
    "description" => "Gym Management microservice"
  },
  {
    "url" => "#{origin}/workload",
    "description" => "Trainer Workload microservice"
  }
]
combined["tags"] = (gym["tags"] || []) + (workload["tags"] || [])
combined["paths"] = (gym["paths"] || {}).merge(workload["paths"] || {})
combined["components"] ||= {}

(workload["components"] || {}).each do |section, values|
  combined["components"][section] =
    (combined["components"][section] || {}).merge(values || {})
end

File.write(File.join(docs_dir, "gym-platform.yaml"), combined.to_yaml(line_width: -1))
File.write(File.join(docs_dir, "gym-platform.json"), JSON.pretty_generate(combined) + "\n")
