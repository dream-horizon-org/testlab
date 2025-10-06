#!/usr/bin/env bash

set -e

echo "APP_DIR: ${APP_DIR}"
echo "ENV: ${ENV}"
echo "TEAM_SUFFIX: ${TEAM_SUFFIX}"
echo "PRIVATE_HOSTED_ZONE: ${PRIVATE_HOSTED_ZONE}"
echo "SERVICE_NAME: ${SERVICE_NAME}"

install_mysql_client () {
  DEBIAN_FRONTEND=noninteractive apt-get update
  DEBIAN_FRONTEND=noninteractive apt-get install -y default-mysql-client --fix-missing
}

initialize_mysql_db () {
  local resources_dir="./resources/db/mysql/"
  local config_file="./resources/config/mysql/default.conf"
  local host=$(grep 'writerConfig.connectOptions.host' $config_file | awk -F '=' '{print $2}')

  echo "Creating Schema..."
  mysql -h $host -u "${MYSQL_USER}" -p"${MYSQL_PASSWORD}" < $resources_dir/schema.sql
  echo "Schema Created"

  echo "Inserting Seed data..."
  mysql -h $host -u "${MYSQL_USER}" -p"${MYSQL_PASSWORD}" < $resources_dir/seed.sql
  echo "Seed data inserted"
}

main () {
  # Skip pre-deploy for production and uat
  if [[ "$ENV" = "prod" || "$ENV" = "uat" ]]; then
    echo "Migrations are not run for prod and uat environments"
    exit 0
  else
    echo "Installing dependent clients..."
    install_mysql_client

    echo "loading config-store values..."
    bash ./.odin/fetch-config.sh
    source .config

    initialize_mysql_db
  fi
}

main
