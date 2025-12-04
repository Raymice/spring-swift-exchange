#!/bin/bash
set -e

SCRIPT_DIR=$(dirname "$(readlink -f "$0")")

STACK_NAME="stack"

# Initialize Docker Swarm if not already initialized
docker swarm init || true

# Remove existing stack if it exists
docker stack rm "${STACK_NAME}" || true

# Wait for stack to be removed (network issues can occur if we proceed too quickly)
echo "Waiting for existing stack to be removed..."
sleep 15

# Set up environment variables
set -a; . "${SCRIPT_DIR}/../.env"; set +a

# Deploy stack dependencies
docker stack deploy -c "${SCRIPT_DIR}/../docker-compose.yml"  "${STACK_NAME}" --detach=true
