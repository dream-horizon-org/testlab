-- Create database if not exists
CREATE DATABASE IF NOT EXISTS experiment;

-- Experiments table
CREATE TABLE IF NOT EXISTS experiment.experiments (
    project_id BINARY(16) NOT NULL,
    experiment_id BINARY(16) NOT NULL,
    name varchar(64) NOT NULL,
    description varchar(255),
    hypothesis text,
    rules_json JSON,
    actuals varchar(255),
    status ENUM('LIVE','PAUSED','DRAFT','CONCLUDED','TERMINATED') NOT NULL,
    start_time bigint,
    end_time bigint,
    cohort_id varchar(255),
    distribution_strategy varchar(255),
    assignment_domain text NOT NULL,
    percentage_distribution varchar(255),
    exposure int,
    created_by bigint,
    threshold bigint,
    is_exclusive boolean,
    health ENUM('WARNING','PASSING','NO_CHECKS_AVAILABLE','FAILED'),
    type enum('A/B'),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    name_tokens varchar(255),
    PRIMARY KEY (project_id, experiment_id),
    UNIQUE KEY (project_id, name),
    KEY idx_name_tokens (name_tokens)
) ENGINE=InnoDB;

-- Tags table
CREATE TABLE IF NOT EXISTS experiment.tags (
    experiment_id BINARY(16) NOT NULL,
    project_id BINARY(16) NOT NULL,
    tag VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (project_id, experiment_id, tag),
    KEY idx_project_experiment (project_id, experiment_id),
    FOREIGN KEY (project_id, experiment_id) REFERENCES experiment.experiments(project_id, experiment_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- Owners table
CREATE TABLE IF NOT EXISTS experiment.owners (
    experiment_id BINARY(16) NOT NULL,
    project_id BINARY(16) NOT NULL,
    owner VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (project_id, experiment_id, owner),
    KEY idx_project_experiment (project_id, experiment_id),
    FOREIGN KEY (project_id, experiment_id) REFERENCES experiment.experiments(project_id, experiment_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB;
