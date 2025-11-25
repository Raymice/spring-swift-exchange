#!/bin/bash
set -e

SCRIPT_DIR=$(dirname "$(readlink -f "$0")")

# Scale down everything
docker-compose -f "${SCRIPT_DIR}/../docker-compose.yml" -f "${SCRIPT_DIR}/../docker-compose.app.yml" down

# Deploy everything
docker-compose -f "${SCRIPT_DIR}/../docker-compose.yml" -f "${SCRIPT_DIR}/../docker-compose.app.yml" up -d --remove-orphans