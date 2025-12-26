#!/bin/bash
set -e


PROJECT_KEY="${PROJECT_KEY:-550e8400-e29b-41d4-a716-446655440001}"

echo "Creating partitions and seed data for PROJECT_KEY: $PROJECT_KEY"

PARTITION_SUFFIX=$(echo "$PROJECT_KEY" | tr '-' '_')

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DATABASE" <<-EOSQL
    -- Create partitions for the project key
    CREATE TABLE IF NOT EXISTS experiments_${PARTITION_SUFFIX}
        PARTITION OF experiment.experiments
            FOR VALUES IN ('${PROJECT_KEY}');

    CREATE TABLE IF NOT EXISTS tags_${PARTITION_SUFFIX}
        PARTITION OF experiment.tags
            FOR VALUES IN ('${PROJECT_KEY}');

    CREATE TABLE IF NOT EXISTS owners_${PARTITION_SUFFIX}
        PARTITION OF experiment.owners
            FOR VALUES IN ('${PROJECT_KEY}');

    CREATE TABLE IF NOT EXISTS update_log_${PARTITION_SUFFIX}
        PARTITION OF experiment.experiment_update_log
            FOR VALUES IN ('${PROJECT_KEY}');

    CREATE TABLE IF NOT EXISTS analysis_${PARTITION_SUFFIX}
        PARTITION OF experiment.experiment_analysis
            FOR VALUES IN ('${PROJECT_KEY}');

    -- Insert seed data using the PROJECT_KEY
    INSERT INTO experiment.experiments (project_key, experiment_id, name, experiment_key, description, hypothesis, status,
                                        type, guardrail_health_status, cohorts, variant_weights, variants,
                                        distribution_strategy, assignment_domain, overrides, rule_attributes,
                                        winning_variant, exposure, threshold, start_time, end_time, created_by, created_at,
                                        updated_at)
    VALUES ('${PROJECT_KEY}', 'a0c89f3e-4e12-45bb-b10e-cd14266538fd', 'Test Experiment', 'test_experiment', 'Description',
            'Click rate with be increased by 5%', 'LIVE', 'A/B', null, '{}', '{
        "type": "COHORT",
        "weights": {
          "control": 34.0,
          "variant1": 33.0,
          "variant2": 33.0
        }
      }', '{
        "control": {
          "variables": [
            {
              "key": "color",
              "value": "red",
              "data_type": "STRING"
            }
          ],
          "display_name": "Control Group"
        },
        "variant1": {
          "variables": [
            {
              "key": "color",
              "value": "green",
              "data_type": "STRING"
            }
          ],
          "display_name": "Variant 1"
        },
        "variant2": {
          "variables": [
            {
              "key": "color",
              "value": "blue",
              "data_type": "STRING"
            }
          ],
          "display_name": "Variant 2"
        }
      }', 'RANDOM', 'COHORT', null, '[
        {
          "name": "Targeting Rule",
          "conditions": [
            {
              "value": "ios",
              "operand": "platform",
              "operator": "=",
              "operandDataType": "STRING"
            },
            {
              "value": "2.0.0",
              "operand": "app_version",
              "operator": "=",
              "operandDataType": "SEMVER_STRING"
            }
          ]
        }
      ]', null, 100, 0, null, null, 'user@example.com', NOW(),
            NOW()) ON CONFLICT DO NOTHING;

    INSERT INTO experiment.tags (experiment_id, project_key, tag, created_at, updated_at)
    VALUES (
        'a0c89f3e-4e12-45bb-b10e-cd14266538fd',
        '${PROJECT_KEY}', 
        'feature-test',
        NOW(), 
        NOW()
    ) ON CONFLICT DO NOTHING;

    INSERT INTO experiment.owners (experiment_id, project_key, owner, created_at, updated_at)
    VALUES (
        'a0c89f3e-4e12-45bb-b10e-cd14266538fd',
        '${PROJECT_KEY}', 
        'product_team@example.com',
        NOW(), 
        NOW()
    ) ON CONFLICT DO NOTHING;

EOSQL

echo "Partitions and seed data created successfully for PROJECT_KEY: $PROJECT_KEY"

