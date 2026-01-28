DROP DATABASE IF EXISTS experiment;
DROP SCHEMA IF EXISTS experiment CASCADE;

CREATE DATABASE experiment;
\c experiment;
CREATE SCHEMA IF NOT EXISTS experiment;

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TYPE experiment.experiment_status AS ENUM ('TEST', 'LIVE','PAUSED','DRAFT','CONCLUDED','TERMINATED');
CREATE TYPE experiment.experiment_type AS ENUM ('A/A','A/B');
CREATE TYPE experiment.experiment_health AS ENUM ('WARNING','PASSED','NO_CHECKS_AVAILABLE','FAILED');
CREATE TYPE experiment.experiment_strategy AS ENUM ('RANDOM','ROUND_ROBIN');
CREATE TYPE experiment.assignment_domain AS ENUM ('STRATIFIED', 'COHORT');

CREATE TABLE IF NOT EXISTS experiment.experiments (
    project_key         VARCHAR(255) NOT NULL,
    experiment_id       VARCHAR(36) NOT NULL,
    name                VARCHAR(255) NOT NULL,
    experiment_key      VARCHAR(255) NOT NULL,
    description         VARCHAR(255),
    hypothesis          TEXT,
    status              experiment.experiment_status NOT NULL,
    type                experiment.experiment_type,
    guardrail_health_status experiment.experiment_health,
    cohorts             VARCHAR(255) ARRAY,
    variant_weights     JSONB,
    variants            JSONB,
    distribution_strategy experiment.experiment_strategy NOT NULL,
    assignment_domain   experiment.assignment_domain NOT NULL,
    overrides           JSONB,
    rule_attributes     JSONB,
    winning_variant     JSONB,
    exposure            INTEGER,
    threshold           bigint,
    start_time          bigint,
    end_time            bigint,
    created_by          VARCHAR(255),
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( project_key, experiment_id),
    CONSTRAINT experiment_key_unique_check UNIQUE (project_key, experiment_key)
) PARTITION BY LIST (project_key);

CREATE INDEX idx_name ON experiment.experiments USING GIN (name gin_trgm_ops);


CREATE TABLE IF NOT EXISTS experiment.owners (
    experiment_id  VARCHAR(36) NOT NULL,
    project_key    VARCHAR(36) NOT NULL,
    owner          VARCHAR(255) NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (project_key, experiment_id, owner)
) PARTITION BY LIST (project_key);


CREATE TABLE IF NOT EXISTS experiment.tags (
    experiment_id  VARCHAR(36) NOT NULL,
    project_key    VARCHAR(255) NOT NULL,
    tag            VARCHAR(255) NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( project_key, experiment_id, tag)
) PARTITION BY LIST (project_key);


CREATE TABLE IF NOT EXISTS experiment.experiment_update_log (
    project_key    VARCHAR(255) NOT NULL,
    experiment_id  VARCHAR(36) NOT NULL,
    previous_data  JSONB,
    current_data   JSONB,
    updated_by     VARCHAR(255),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (project_key, experiment_id, created_at)
) PARTITION BY LIST (project_key);


CREATE TABLE IF NOT EXISTS experiment.experiment_analysis (
    project_key       VARCHAR(255) NOT NULL,
    experiment_id     VARCHAR(36) NOT NULL,
    config            VARCHAR(255),
    primary_metrics   VARCHAR(255) ARRAY,
    secondary_metrics VARCHAR(255) ARRAY,
    metric_tokens     VARCHAR(255),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (project_key, experiment_id)
) PARTITION BY LIST (project_key);