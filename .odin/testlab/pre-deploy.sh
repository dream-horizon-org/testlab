#!/usr/bin/env bash

set -e

echo "APP_DIR: ${APP_DIR}"
echo "ENV: ${ENV}"
echo "TEAM_SUFFIX: ${TEAM_SUFFIX}"
echo "PRIVATE_HOSTED_ZONE: ${PRIVATE_HOSTED_ZONE}"
echo "SERVICE_NAME: ${SERVICE_NAME}"

install_deps () {
  apt --allow-releaseinfo-change update
  apt install jq
  apt install unzip
  curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
  unzip -q awscliv2.zip
  ./aws/install
}

install_client () {
  apt install -f
  echo "install -f done"
  apt install -y postgresql-client
  echo "install postgresql-client done"
}

initialize_db () {
  local resources_dir="./resources/db/postgresql/"
  local config_file="./resources/config/postgresql/default.conf"
  local host=$(grep 'writerConfig.connectOptions.host' $config_file | awk -F '=' '{print $2}')
  local db_name=$(grep 'writerConfig.connectOptions.database' $config_file | awk -F '= ' '{print $2}' | tr -d '"')

  pg_check_cmd=$(PGUSER=${PG_USER} PGPASSWORD=${PG_PASSWORD} psql -h "$(eval echo "$host")" -d postgres -tc "SELECT 1 FROM pg_database WHERE datname = '${db_name}'" | wc -l)

  echo "pg_check command ran"
  if [ "$pg_check_cmd" -gt 1 ]; then
    echo "Schema already exists. Hence skipping..."
  else
    echo "Creating Schema..."
    PGUSER=${PG_USER} PGPASSWORD=${PG_PASSWORD} psql -h "$(eval echo "$host")" -d postgres -f $resources_dir/schema.sql
    echo "Schema Created"

    echo "Inserting Seed data..."
    PGUSER=${PG_USER} PGPASSWORD=${PG_PASSWORD} psql -h "$(eval echo "$host")" -d postgres -f $resources_dir/seed.sql
    echo "Seed data inserted"
  fi
}

main () {
  # Skip pre-deploy for production and uat
  if [[ "$ENV" = "prod" || "$ENV" = "uat" ]]; then
    echo "Migrations are not run for prod and uat environments"
    exit 0
  else
    echo "Installing dependents..."
    install_deps
    echo "Installing client..."
    install_client

    echo "loading config-store values..."
    bash ./.odin/fetch-config.sh
    source .config

    initialize_db
  fi
}

main
