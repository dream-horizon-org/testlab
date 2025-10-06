#!/usr/bin/env bash

set -e

echo "APP_DIR: ${APP_DIR}"
echo "ENV: ${ENV}"
echo "TEAM_SUFFIX: ${TEAM_SUFFIX}"
echo "PRIVATE_HOSTED_ZONE: ${PRIVATE_HOSTED_ZONE}"
echo "SERVICE_NAME: ${SERVICE_NAME}"
echo "DEPLOYMENT_TYPE: ${DEPLOYMENT_TYPE}"

# set up command-line options
JAVA_OPTS=(
  "-XX:+UseG1GC"
  "-XX:MaxGCPauseMillis=200"
  "-XX:+HeapDumpOnOutOfMemoryError"
  "-XX:InitiatingHeapOccupancyPercent=50"
)
DATADOG_OPTS=("-javaagent:/opt/datadog/dd-java-agent.jar")
JMX_OPTS=(
  "-Dcom.sun.management.jmxremote=true"
  "-Dcom.sun.management.jmxremote.port=1099"
  "-Dcom.sun.management.jmxremote.ssl=false"
  "-Dcom.sun.management.jmxremote.authenticate=false"
)
APP_OPTS=(
  "-Dapp.environment=${ENV}"
  "-Dvertx.disableDnsResolver=true"
  "-Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory"
)
LOG_OPTS=("-Dlog.file.name=${SERVICE_NAME}.log")

if [[ "$DEPLOYMENT_TYPE" = "container" ]]; then
  LOG_OPTS+=("-Dlog.directory.path=/app/logs" "-Dlogback.configurationFile=${APP_DIR}/resources/logback/logback-dev.xml")
  MEM_OPTS=("-Xms512m" "-Xmx512m" "-Xmn256m")
else
  LOG_OPTS+=("-Dlog.directory.path=/opt/logs" "-Dlogback.configurationFile=${APP_DIR}/resources/logback/logback.xml")

  totalMem=$(free -m | head -2 | tail -1 | awk '{print $2}')
  heapSize=$((totalMem * 70 / 100))
  halfHeapSize=$((heapSize / 2))
  MEM_OPTS=("-Xms${heapSize}m" "-Xmx${heapSize}m" "-Xmn${halfHeapSize}m")
fi

if [[ "$ENV" = "load" ]]; then
  DATADOG_OPTS+=("-Ddd.logs.injection=true")
fi

PATH_TO_APP_JAR="${APP_DIR}/${SERVICE_NAME}-1.0-fat.jar"

cd "${APP_DIR}" || exit

# load config-store values
bash ./.odin/fetch-config.sh
source .config

# execute jar
if [[ "$DEPLOYMENT_TYPE" = "container" ]]; then
  java -jar "${JAVA_OPTS[@]}" "${MEM_OPTS[@]}" "${DATADOG_OPTS[@]}" "${JMX_OPTS[@]}" "${APP_OPTS[@]}" "${LOG_OPTS[@]}" "${PATH_TO_APP_JAR}"
else
  sed -i 's/apm_config:/apm_config:\n  probabilistic_sampler:\n    enabled: true\n    sampling_percentage: 1/1' /etc/datadog-agent/datadog.yaml
  sudo systemctl restart datadog-agent
  nohup java -jar "${JAVA_OPTS[@]}" "${MEM_OPTS[@]}" "${DATADOG_OPTS[@]}" "${JMX_OPTS[@]}" "${APP_OPTS[@]}" "${LOG_OPTS[@]}" "${PATH_TO_APP_JAR}" </dev/null >/dev/null 2>&1 &
  pid=$!
  [ -n "$PID_PATH" ] && echo $pid >"${PID_PATH}"
fi
