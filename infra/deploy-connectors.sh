#!/bin/bash

echo "Waiting for Kafka Connect to be ready..."

until curl -f http://localhost:8083/connectors > /dev/null 2>&1; do
  echo "Kafka Connect not ready yet, waiting..."
  sleep 5
done

echo "Kafka Connect is ready! Deploying connectors..."

for connector in kafka/connectors/*.json; do
  if [ -f "$connector" ]; then
    echo "Deploying connector: $connector"
    curl -X POST -H "Content-Type: application/json" \
         --data-binary @"$connector" \
         http://localhost:8083/connectors/
    echo ""
  fi
done

echo "All connectors deployed successfully!"