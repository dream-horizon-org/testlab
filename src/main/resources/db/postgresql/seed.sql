-- Seed data based on chore/tagsApi branch test files
-- Project key: 123e4567-e89b-12d3-a456-426614174000 (from TagsIT.java)
-- Experiment IDs in UUID format

-- Create partitions for the project key
-- Note: Partition names are sanitized (hyphens removed) as they must be valid identifiers
CREATE TABLE IF NOT EXISTS experiment.tags_123e4567e89b12d3a456426614174000 
PARTITION OF experiment.tags FOR VALUES IN ('123e4567-e89b-12d3-a456-426614174000');

CREATE TABLE IF NOT EXISTS experiment.experiments_123e4567e89b12d3a456426614174000 
PARTITION OF experiment.experiments FOR VALUES IN ('123e4567-e89b-12d3-a456-426614174000');

CREATE TABLE IF NOT EXISTS experiment.experiment_update_log_123e4567e89b12d3a456426614174000 
PARTITION OF experiment.experiment_update_log FOR VALUES IN ('123e4567-e89b-12d3-a456-426614174000');

-- Insert tags data (from TagsIT.java: ui-test, feature-flag, performance)
-- Using UUID format experiment IDs
INSERT INTO experiment.tags (project_key, experiment_id, tag) VALUES
('123e4567-e89b-12d3-a456-426614174000', '550e8400-e29b-41d4-a716-446655440000', 'ui-test'),
('123e4567-e89b-12d3-a456-426614174000', '550e8400-e29b-41d4-a716-446655440001', 'feature-flag'),
('123e4567-e89b-12d3-a456-426614174000', '550e8400-e29b-41d4-a716-446655440002', 'performance')
ON CONFLICT (project_key, experiment_id, tag) DO NOTHING;

-- Insert experiment data for name availability testing
-- Using UUID format experiment IDs
INSERT INTO experiment.experiments (
    project_key, 
    experiment_id, 
    name, 
    description,
    status,
    created_by,
    created_at,
    updated_at
) VALUES
('123e4567-e89b-12d3-a456-426614174000', '550e8400-e29b-41d4-a716-446655440000', 'existing-experiment', 'An existing experiment', 'LIVE', 'test-user', NOW(), NOW()),
('123e4567-e89b-12d3-a456-426614174000', '550e8400-e29b-41d4-a716-446655440001', 'another-experiment', 'Another experiment', 'DRAFT', 'test-user', NOW(), NOW()),
('123e4567-e89b-12d3-a456-426614174000', '550e8400-e29b-41d4-a716-446655440002', 'test-experiment', 'Test experiment', 'PAUSED', 'test-user', NOW(), NOW())
ON CONFLICT (project_key, experiment_id) DO NOTHING;

-- Insert experiment history data
-- Using UUID format experiment IDs
INSERT INTO experiment.experiment_update_log (
    project_key,
    experiment_id,
    previous_data,
    current_data,
    updated_by,
    created_at,
    updated_at
) VALUES
('123e4567-e89b-12d3-a456-426614174000', '550e8400-e29b-41d4-a716-446655440000',
 '{"status":"DRAFT","name":"existing-experiment"}',
 '{"status":"LIVE","name":"existing-experiment"}',
 'test-user',
 NOW() - INTERVAL '2 hours',
 NOW() - INTERVAL '2 hours'),
('123e4567-e89b-12d3-a456-426614174000', '550e8400-e29b-41d4-a716-446655440001',
 '{"status":"DRAFT","name":"another-experiment"}',
 '{"status":"DRAFT","name":"another-experiment","description":"Updated description"}',
 'test-user',
 NOW() - INTERVAL '1 hour',
 NOW() - INTERVAL '1 hour')
ON CONFLICT (project_key, experiment_id) DO UPDATE SET
    previous_data = EXCLUDED.previous_data,
    current_data = EXCLUDED.current_data,
    updated_by = EXCLUDED.updated_by,
    created_at = EXCLUDED.created_at,
    updated_at = EXCLUDED.updated_at;
