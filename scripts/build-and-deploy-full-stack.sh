#!/bin/bash
set -e

SCRIPT_DIR=$(dirname "$(readlink -f "$0")")

# Build last version of the application
"${SCRIPT_DIR}/../mvnw" spring-boot:build-image -Dmaven.test.skip=true

# Scale down everything
docker-compose -f "${SCRIPT_DIR}/../docker-compose.yml" -f "${SCRIPT_DIR}/../docker-compose.app.yml" down

# Deploy everything
docker-compose -f "${SCRIPT_DIR}/../docker-compose.yml" -f "${SCRIPT_DIR}/../docker-compose.app.yml" up -d --remove-orphans