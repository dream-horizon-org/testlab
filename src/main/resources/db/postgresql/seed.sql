\c experiment;

CREATE TABLE experiments_666118b4_a1a5_4849_9837_9f7685a09569
    PARTITION OF experiment.experiments
        FOR VALUES IN ('550e8400-e29b-41d4-a716-446655440001');

CREATE TABLE tags_666118b4_a1a5_4849_9837_9f7685a09568
    PARTITION OF experiment.tags
        FOR VALUES IN ('550e8400-e29b-41d4-a716-446655440001');

CREATE TABLE owners_666118b4_a1a5_4849_9837_9f7685a09568
    PARTITION OF experiment.owners
        FOR VALUES IN ('550e8400-e29b-41d4-a716-446655440001');

CREATE TABLE update_log_666118b4_a1a5_4849_9837_9f7685a09568
    PARTITION OF experiment.experiment_update_log
        FOR VALUES IN ('550e8400-e29b-41d4-a716-446655440001');

CREATE TABLE analysis_666118b4_a1a5_4849_9837_9f7685a09568
    PARTITION OF experiment.experiment_analysis
        FOR VALUES IN ('550e8400-e29b-41d4-a716-446655440001');

INSERT INTO experiment.experiments (project_key, experiment_id, name, description, hypothesis, status, type,
                                    guardrail_health_status, cohorts, variant_weights, variants, distribution_strategy,
                                    assignment_domain, overrides, rule_attributes, winning_variant, exposure, threshold,
                                    start_time, end_time, created_by, created_at, updated_at, experiment_key)
VALUES ('550e8400-e29b-41d4-a716-446655440001', '123e4567-e89b-12d3-a456-426614174003', 'Button Color Test2',
        'Testing if changing button color improves click-through rate.',
        'Changing the primary CTA button from blue to green will increase CTR by at least 5%.', 'LIVE', 'A/B',
        'PASSED', '{}', '{
    "weights": {
      "control": 0.5,
      "variant_a": 0.5
    }
  }', '{
    "control": {
      "variables": [
        {
          "key": "isEnabled",
          "value": "{}",
          "dataType": "OBJECT"
        }
      ],
      "display_name": "control"
    },
    "variant1": {
      "variables": [
        {
          "key": "isEnabled",
          "value": "{}",
          "dataType": "OBJECT"
        }
      ],
      "display_name": "variant11"
    }
  }', 'RANDOM', 'COHORT', '{"control":["123"],"variant1":["124"]}', '[
    {
      "name": "rule 2",
      "conditions": [
        {
          "value": "1.0.0",
          "operand": "app_version",
          "operator": "=",
          "operandDataType": "SEMVER_STRING"
        }
      ]
    }
  ]', '{}', 5000, 10000, 1762428451, 1765020451, 'jane.doe@example.com', '2025-11-06 11:27:30.604194 +00:00',
        '2025-11-06 11:27:30.604194 +00:00', 'exp1');


INSERT INTO experiment.tags (experiment_id, project_key, tag, created_at, updated_at)
VALUES ('ee017033-feb2-4659-a7b7-ea98106e66eb', '550e8400-e29b-41d4-a716-446655440001', 'feature-test',
        '2025-12-02 10:11:12.133077 +00:00', '2025-12-02 10:11:12.133077 +00:00');


INSERT INTO experiment.owners (experiment_id, project_key, owner, created_at, updated_at)
VALUES ('ee017033-feb2-4659-a7b7-ea98106e66eb', '550e8400-e29b-41d4-a716-446655440001', 'product_team@example.com',
        '2025-12-02 10:11:12.133077 +00:00', '2025-12-02 10:11:12.133077 +00:00');
