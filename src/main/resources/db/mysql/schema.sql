CREATE DATABASE IF NOT EXISTS experiment;

CREATE TABLE IF NOT EXISTS experiment.experiments (
    project_id          BINARY(16) NOT NULL,
    experiment_id       BINARY(16) NOT NULL,
    name                VARCHAR(64) NOT NULL,
    description         VARCHAR(255),
    hypothesis          text,
    status              ENUM('LIVE','PAUSED','DRAFT','CONCLUDED','TERMINATED') NOT NULL,
    type                ENUM('A/B'),
    guardrail_health_status ENUM('WARNING','PASSING','NO_CHECKS_AVAILABLE','FAILED'),
    cohorts             varchar(255),
    variant_weights     JSON,
    assignment_strategy ENUM('RANDOM', 'ROUND_ROBIN'),
    overrides           JSON,
    rule_attributes     JSON,
    winning_variant     JSON,
    exposure            int,
    threshold           bigint,
    start_time          bigint,
    end_time            bigint,
    created_by          varchar(255),
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    name_tokens varchar(255),

    PRIMARY KEY (project_id,experiment_id),
    UNIQUE KEY (project_id, name)
) ENGINE=InnoDB
PARTITION BY LIST COLUMNS(project_id)
(
  PARTITION dummyPartition VALUES IN('p_id')
);



CREATE TABLE IF NOT EXISTS experiment.owners (
    experiment_id BINARY(16) NOT NULL,
    project_id BINARY(16) NOT NULL,
    owner VARCHAR(255) NOT NULL,
    created_at                               TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                               TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (project_id, experiment_id, owner)
) ENGINE=InnoDB
PARTITION BY LIST COLUMNS(project_id)
(
  PARTITION dummyPartition VALUES IN('p_id')
);


CREATE TABLE IF NOT EXISTS experiment.tags (
    experiment_id BINARY(16) NOT NULL,
    project_id BINARY(16) NOT NULL,
    tag VARCHAR(255) NOT NULL,
    created_at                               TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                               TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (project_id, experiment_id, tag)
) ENGINE=InnoDB
PARTITION BY LIST COLUMNS(project_id)
(
  PARTITION dummyPartition VALUES IN('p_id')
);

CREATE TABLE IF NOT EXISTS experiment.experiment_update_log (
    project_id BINARY(16) NOT NULL,
    experiment_id BINARY(16) NOT NULL,
    previous_data JSON,
    current_data JSON,
    updated_by VARCHAR(255),
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (project_id, experiment_id)
) ENGINE=InnoDB
PARTITION BY LIST COLUMNS(project_id)
(
  PARTITION dummyPartition VALUES IN('p_id')
);

CREATE TABLE IF NOT EXISTS experiment.experiment_analysis (
    project_id BINARY(16) NOT NULL,
    experiment_id BINARY(16) NOT NULL,
    config VARCHAR(255),
    primary_metrics VARCHAR(255),
    secondary_metrics VARCHAR(255),
    metric_tokens VARCHAR(255),
    created_at                               TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                               TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (project_id, experiment_id)
) ENGINE=InnoDB
PARTITION BY LIST COLUMNS(project_id)
(
  PARTITION dummyPartition VALUES IN('p_id')
);

CREATE TABLE IF NOT EXISTS experiment.cron_process
(
    process_key         VARCHAR(255) NOT NULL,
    execution_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    executor_ip         VARCHAR(50),
    next_execution_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cron_expression     VARCHAR(255),
    PRIMARY KEY (process_key)
) ENGINE=InnoDB;
