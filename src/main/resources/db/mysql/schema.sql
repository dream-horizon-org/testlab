-- Experiments table
CREATE TABLE IF NOT EXISTS experiments (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  tenant_id VARCHAR(128) NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT NULL,
  metrics JSON NOT NULL,
  assignment_domain VARCHAR(64) NOT NULL,
  exposure INT NOT NULL,
  threshold INT NOT NULL,
  type VARCHAR(32) NOT NULL,
  end_date TIMESTAMP NULL,
  tags JSON NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_tenant_name (tenant_id, name)
);

