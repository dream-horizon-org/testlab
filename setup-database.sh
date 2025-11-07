#!/bin/bash

# Database Setup Script for Name Availability API
# This script sets up the required database schema and partitions

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}Setting up database for Name Availability API...${NC}\n"

# Check if required environment variables are set
# Support both POSTGRES_USER/POSTGRES_PASSWORD and PG_USER/PG_PASSWORD
if [ -z "$POSTGRES_USER" ] && [ -z "$PG_USER" ]; then
    echo -e "${YELLOW}Warning: POSTGRES_USER or PG_USER not set.${NC}"
    echo "Please set them:"
    echo "  export POSTGRES_USER=your_username"
    echo "  export POSTGRES_PASSWORD=your_password"
    echo ""
    read -p "Enter PostgreSQL username: " POSTGRES_USER
    read -sp "Enter PostgreSQL password: " POSTGRES_PASSWORD
    echo ""
fi

# Use POSTGRES_USER if set, otherwise fall back to PG_USER
PG_USER=${POSTGRES_USER:-$PG_USER}
PG_PASSWORD=${POSTGRES_PASSWORD:-$PG_PASSWORD}

# Get database name (default to 'experiment' or ask user)
if [ -z "$PG_DATABASE" ] && [ -z "$POSTGRES_DATABASE" ]; then
    read -p "Enter database name (default: experiment): " PG_DATABASE
    PG_DATABASE=${PG_DATABASE:-experiment}
fi
# Use POSTGRES_DATABASE if set, otherwise use PG_DATABASE
PG_DATABASE=${POSTGRES_DATABASE:-${PG_DATABASE:-experiment}}

# Get host (default to localhost)
if [ -z "$PG_HOST" ]; then
    read -p "Enter PostgreSQL host (default: localhost): " PG_HOST
    PG_HOST=${PG_HOST:-localhost}
fi

# Get port (default to 5432)
if [ -z "$PG_PORT" ]; then
    read -p "Enter PostgreSQL port (default: 5432): " PG_PORT
    PG_PORT=${PG_PORT:-5432}
fi

echo ""
echo -e "${GREEN}Connecting to database: ${PG_DATABASE}@${PG_HOST}:${PG_PORT}${NC}\n"

# Export password for psql
export PGPASSWORD=$PG_PASSWORD

# Check if database exists, create if not
echo -e "${YELLOW}Checking if database exists...${NC}"
if psql -h "$PG_HOST" -p "$PG_PORT" -U "$PG_USER" -lqt | cut -d \| -f 1 | grep -qw "$PG_DATABASE"; then
    echo -e "${GREEN}Database '$PG_DATABASE' exists.${NC}\n"
else
    echo -e "${YELLOW}Database '$PG_DATABASE' does not exist. Creating...${NC}"
    createdb -h "$PG_HOST" -p "$PG_PORT" -U "$PG_USER" "$PG_DATABASE" || {
        echo -e "${RED}Failed to create database. You may need to create it manually.${NC}"
        echo "Run: createdb -U $PG_USER $PG_DATABASE"
        exit 1
    }
    echo -e "${GREEN}Database created.${NC}\n"
fi

# Run schema file
SCHEMA_FILE="src/main/resources/db/postgresql/schema.sql"
if [ ! -f "$SCHEMA_FILE" ]; then
    echo -e "${RED}Schema file not found: $SCHEMA_FILE${NC}"
    exit 1
fi

echo -e "${YELLOW}Creating database schema...${NC}"
# Remove database creation commands that can't run in transaction
psql -h "$PG_HOST" -p "$PG_PORT" -U "$PG_USER" -d "$PG_DATABASE" <<EOF
-- Create schema if not exists
CREATE SCHEMA IF NOT EXISTS experiment;

-- Create types
DO \$\$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'experiment_status') THEN
        CREATE TYPE experiment_status AS ENUM ('LIVE','PAUSED','DRAFT','CONCLUDED','TERMINATED');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'experiment_type') THEN
        CREATE TYPE experiment_type AS ENUM ('A/B');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'experiment_health') THEN
        CREATE TYPE experiment_health AS ENUM ('WARNING','PASSING','NO_CHECKS_AVAILABLE','FAILED');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'experiment_strategy') THEN
        CREATE TYPE experiment_strategy AS ENUM ('RANDOM', 'ROUND_ROBIN');
    END IF;
END \$\$;

-- Create main table
CREATE TABLE IF NOT EXISTS experiment.experiments (
    project_key          VARCHAR(255) NOT NULL,
    experiment_id       VARCHAR(36) NOT NULL,
    name                VARCHAR(64) NOT NULL,
    description         VARCHAR(255),
    hypothesis          TEXT,
    status              experiment_status NOT NULL,
    type                experiment_type,
    guardrail_health_status experiment_health,
    cohorts             VARCHAR(255) ARRAY,
    variant_weights     JSONB,
    assignment_strategy experiment_strategy,
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
    name_tsvector       TSVECTOR,
    PRIMARY KEY ( project_key, experiment_id),
    CONSTRAINT name_unique_check UNIQUE (project_key, name)
) PARTITION BY LIST (project_key);

-- Create index
CREATE INDEX IF NOT EXISTS idx_name_tsvector ON experiment.experiments USING GIN (name_tsvector);

EOF

if [ $? -eq 0 ]; then
    echo -e "${GREEN}Schema created successfully!${NC}\n"
else
    echo -e "${RED}Failed to create schema.${NC}"
    exit 1
fi

# Ask if user wants to create a test partition
echo -e "${YELLOW}The experiments table is partitioned. You need to create a partition for each project key.${NC}"
read -p "Do you want to create a test partition? (y/n, default: y): " CREATE_PARTITION
CREATE_PARTITION=${CREATE_PARTITION:-y}

if [ "$CREATE_PARTITION" = "y" ] || [ "$CREATE_PARTITION" = "Y" ]; then
    read -p "Enter project key for test partition (default: test-project): " PROJECT_KEY
    PROJECT_KEY=${PROJECT_KEY:-test-project}
    
    echo -e "${YELLOW}Creating partition for project key: $PROJECT_KEY${NC}"
    psql -h "$PG_HOST" -p "$PG_PORT" -U "$PG_USER" -d "$PG_DATABASE" <<EOF
CREATE TABLE IF NOT EXISTS experiment.experiments_p_${PROJECT_KEY//-/_} 
PARTITION OF experiment.experiments 
FOR VALUES IN ('$PROJECT_KEY');
EOF
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}Partition created successfully for project key: $PROJECT_KEY${NC}\n"
    else
        echo -e "${RED}Failed to create partition.${NC}"
        exit 1
    fi
fi

echo -e "${GREEN}Database setup complete!${NC}\n"
echo -e "${YELLOW}You can now test the API with:${NC}"
echo "  curl -X GET \"http://localhost:8080/v1/experiments/name-availability?name=test\" \\"
echo "    -H \"x-project-key: ${PROJECT_KEY:-test-project}\""

# Unset password
unset PGPASSWORD

