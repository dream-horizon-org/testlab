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
    INSERT INTO experiment.experiments (
        project_key, experiment_id, name, description, hypothesis, status, type,
        guardrail_health_status, cohorts, variant_weights, variants, distribution_strategy,
        assignment_domain, overrides, rule_attributes, winning_variant, exposure, threshold,
        start_time, end_time, created_by, created_at, updated_at, experiment_key
    ) VALUES (
        '${PROJECT_KEY}', 
        '123e4567-e89b-12d3-a456-426614174003', 
        'Button Color Test',
        'Testing if changing button color improves click-through rate.',
        'Changing the primary CTA button from blue to green will increase CTR by at least 5%.', 
        'LIVE', 
        'A/B',
        'PASSED', 
        '{}', 
        '{"weights": {"control": 50, "variant1": 50}}', 
        '{"control": {"variables": [{"key": "buttonColor", "value": "blue", "dataType": "STRING"}], "display_name": "Control"}, "variant1": {"variables": [{"key": "buttonColor", "value": "green", "dataType": "STRING"}], "display_name": "Variant 1"}}', 
        'RANDOM', 
        'COHORT', 
        '{"control":["user-123"],"variant1":["user-456"]}', 
        '[{"name": "rule1", "conditions": [{"value": "1.0.0", "operand": "app_version", "operator": ">=", "operandDataType": "SEMVER_STRING"}]}]', 
        '{}', 
        100, 
        10000, 
        EXTRACT(EPOCH FROM NOW()) * 1000, 
        EXTRACT(EPOCH FROM NOW() + INTERVAL '30 days') * 1000, 
        'seed@example.com', 
        NOW(),
        NOW(), 
        'button-color-test'
    ) ON CONFLICT DO NOTHING;

    INSERT INTO experiment.tags (experiment_id, project_key, tag, created_at, updated_at)
    VALUES (
        '123e4567-e89b-12d3-a456-426614174003', 
        '${PROJECT_KEY}', 
        'feature-test',
        NOW(), 
        NOW()
    ) ON CONFLICT DO NOTHING;

    INSERT INTO experiment.owners (experiment_id, project_key, owner, created_at, updated_at)
    VALUES (
        '123e4567-e89b-12d3-a456-426614174003', 
        '${PROJECT_KEY}', 
        'product_team@example.com',
        NOW(), 
        NOW()
    ) ON CONFLICT DO NOTHING;

EOSQL

echo "Partitions and seed data created successfully for PROJECT_KEY: $PROJECT_KEY"

