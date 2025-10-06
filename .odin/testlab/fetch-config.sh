#!/usr/bin/env bash

set -euo pipefail

SECRET_JSON=$(aws secretsmanager get-secret-value --secret-id ${SERVICE_NAME} --output json | jq -c '.SecretString | fromjson')
SECRETS=$(echo $SECRET_JSON | jq -r "to_entries | map(\"export \(.key)=\(.value | tostring)\") | .[]")

echo $SECRETS > .config
