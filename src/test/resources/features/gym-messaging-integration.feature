@messaging @integration
Feature: Gym Management workload event publishing
  Gym Management must publish JSON workload events that follow the Trainer Workload contract.

  Background:
    Given a completed training for trainer "trainer.integration"

  Scenario: Publish a valid workload event
    When Gym Management publishes an ADD workload event
    Then the Trainer Workload queue contains the JSON-compatible workload event

  Scenario: Report an unavailable message broker
    When the message broker becomes unavailable before publishing
    Then Gym Management reports a messaging failure
