-- Insert test experiment
INSERT INTO experiment.experiments 
  (project_id, experiment_id, name, description, hypothesis, rules_json, actuals, status, 
   start_time, end_time, cohort_id, distribution_strategy, assignment_domain, 
   percentage_distribution, exposure, created_by, threshold, is_exclusive, health, 
   type, name_tokens)
VALUES 
  (
    UUID_TO_BIN('123e4567-e89b-12d3-a456-426614174000', true),
    UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440000', true),
    'Test Experiment 1',
    'A test experiment for tags',
    'This is a hypothesis for testing',
    '{"rules": [{"condition": "true", "variant": "control"}]}',
    'actual_value_1',
    'LIVE',
    1696118400000,
    1735689599000,
    'cohort_123',
    'uniform',
    'user',
    '50:50',
    50,
    1001,
    10000,
    false,
    'PASSING',
    'A/B',
    'Test Experiment 1'
  );

-- Insert test tags
INSERT INTO experiment.tags (project_id, experiment_id, tag)
VALUES 
  (UUID_TO_BIN('123e4567-e89b-12d3-a456-426614174000', true), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440000', true), 'A/B-test'),
  (UUID_TO_BIN('123e4567-e89b-12d3-a456-426614174000', true), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440000', true), 'performance'),
  (UUID_TO_BIN('123e4567-e89b-12d3-a456-426614174000', true), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440000', true), 'ui-test'),
  (UUID_TO_BIN('123e4567-e89b-12d3-a456-426614174000', true), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440000', true), 'feature-flag');

-- Insert test owners
INSERT INTO experiment.owners (project_id, experiment_id, owner)
VALUES 
  (UUID_TO_BIN('123e4567-e89b-12d3-a456-426614174000', true), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440000', true), 'test.user@example.com');
