@component
Feature: Gym Management training component
  The training component must enforce the assignment rules before storing training sessions.

  Background:
    Given an active trainee and Yoga trainer exist

  Scenario: Add a training for an assigned trainer
    Given the trainer is assigned to the trainee
    When the administrator adds a 60 minute Yoga training
    Then the training is stored and an ADD workload event is published

  Scenario: Reject a training for an unassigned trainer
    Given the trainer is not assigned to the trainee
    When the administrator adds a 60 minute Yoga training
    Then the training is rejected because the trainer is not assigned
