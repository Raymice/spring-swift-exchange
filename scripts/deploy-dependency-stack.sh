#!/bin/bash
set -e

SCRIPT_DIR=$(dirname "$(readlink -f "$0")")

# Scale down everything
docker-compose -f "${SCRIPT_DIR}/../docker-compose.yml" -f "${SCRIPT_DIR}/../docker-compose.app.yml" down

# Deploy stack dependencies
docker-compose -f "${SCRIPT_DIR}/../docker-compose.yml" up -d --remove-orphans
