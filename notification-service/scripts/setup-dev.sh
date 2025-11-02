#!/bin/bash

# Notification Service Development Setup Script

echo "Setting up Notification Service development environment..."

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo "Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

# Function to wait for service to be ready
wait_for_service() {
    local host=$1
    local port=$2
    local service_name=$3
    
    echo "Waiting for $service_name to be ready..."
    while ! nc -z $host $port; do
        sleep 2
        echo "Waiting for $service_name..."
    done
    echo "$service_name is ready!"
}

# Create necessary directories
mkdir -p logs

# Start the infrastructure services
echo "Starting infrastructure services..."
docker-compose -f docker/docker-compose.yaml up -d mysql zookeeper kafka

# Wait for MySQL to be ready
wait_for_service localhost 3309 "MySQL"

# Wait for Kafka to be ready
wait_for_service localhost 9092 "Kafka"

# Run database initialization
echo "Initializing database..."
docker exec mysql-db-notifications mysql -uroot -proot -e "source /docker-entrypoint-initdb.d/init-db.sql"

# Create Kafka topics
echo "Creating Kafka topics..."
docker exec kafka-notifications kafka-topics --create --topic NOTIFICATION_SEND --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
docker exec kafka-notifications kafka-topics --create --topic order-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

echo "Development environment setup complete!"
echo ""
echo "Services running:"
echo "- MySQL: localhost:3309"
echo "- Kafka: localhost:9092"
echo "- Zookeeper: localhost:2181"
echo ""
echo "You can now run the notification service with:"
echo "mvn spring-boot:run"
echo ""
echo "To stop the services:"
echo "docker-compose -f docker/docker-compose.yaml down"
