#!/usr/bin/env bash

set -euo pipefail

DD_JAVA_AGENT_VERSION=1.21.0
echo "Downloading dd java agent jar version ${DD_JAVA_AGENT_VERSION}"

mkdir -p /opt/datadog/
wget -O /opt/datadog/dd-java-agent.jar https://repo1.maven.org/maven2/com/datadoghq/dd-java-agent/${DD_JAVA_AGENT_VERSION}/dd-java-agent-${DD_JAVA_AGENT_VERSION}.jar
