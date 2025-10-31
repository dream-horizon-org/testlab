-- Experiments table
CREATE TABLE IF NOT EXISTS experiments (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  tenant_id BINARY(16) NOT NULL,
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
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_tenant_experiment (tenant_id, experiment_id),
  KEY idx_tenant_name (tenant_id, name)
);

-- Experiment tags table
CREATE TABLE IF NOT EXISTS experiment_tags (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  tenant_id BINARY(16) NOT NULL,
  experiment_id BINARY(16) NOT NULL,
  tag VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_tenant_experiment (tenant_id, experiment_id),
  KEY idx_tenant_tag (tenant_id, tag)
);

