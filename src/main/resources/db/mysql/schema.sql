CREATE DATABASE IF NOT EXISTS experiment;
CREATE TABLE IF NOT EXISTS experiment.experiments

(
    project_id        BINARY(16) NOT NULL,
    experiment_id    BINARY(16) NOT NULL,
    name             varchar(64) NOT NULL,
    description      varchar(255),
    hypothesis       text,
    rules_json JSON,
    actuals          varchar(255),
    status           ENUM('LIVE','PAUSED','DRAFT','CONCLUDED','TERMINATED') NOT NULL,
    start_time       bigint,
    end_time         bigint,
    cohort_id        varchar(255),
    distribution_strategy     varchar(255),
    assignment_domain text NOT NULL,
    percentage_distribution   varchar(255),
    exposure        int,
    created_by      bigint,
    threshold       bigint,
    is_exclusive       boolean,
    health     ENUM('WARNING','PASSING','NO_CHECKS_AVAILABLE','FAILED'")
    type        enum('A/B'),
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    name_tokens varchar(255)
    FULL_TEXT(name_tokens)
    PRIMARY KEY (project_id,experiment_id),
    UNIQUE KEY (project_id, name)
)ENGINE=InnoDB;
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
    PRIMARY KEY (project_id, experiment_id, owner),
    FOREIGN KEY (project_id, experiment_id) REFERENCES experiment.experiments(project_id, experiment_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS experiment.tags (
    experiment_id BINARY(16) NOT NULL,
    project_id BINARY(16) NOT NULL,
    tag VARCHAR(255) NOT NULL,
    created_at                               TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                               TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (project_id, experiment_id, tag),
    FOREIGN KEY (project_id, experiment_id) REFERENCES experiment.experiments(project_id, experiment_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB;
PARTITION BY LIST COLUMNS(project_id)
(
  PARTITION dummyPartition VALUES IN('p_id')
);

CREATE TABLE IF NOT EXISTS experiment.experiment_update_log (
    project_id BINARY(16) NOT NULL,
    experiment_id BINARY(16) NOT NULL,
    updated_by VARCHAR(16),
    previous_data JSON,
    current_data JSON,
     created_at                               TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                               TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (project_id, experiment_id)
) ENGINE=InnoDB;
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
    PRIMARY KEY (project_id, experiment_id),
    FULL_TEXT(metric_tokens)
     FOREIGN KEY (project_id, experiment_id) REFERENCES experiment.experiments(project_id, experiment_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB;
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
);
