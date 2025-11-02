@echo off
REM Notification Service Development Setup Script for Windows

echo Setting up Notification Service development environment...

REM Check if Docker is installed
docker --version >nul 2>&1
if errorlevel 1 (
    echo Docker is not installed. Please install Docker Desktop first.
    pause
    exit /b 1
)

REM Check if Docker Compose is installed
docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo Docker Compose is not installed. Please install Docker Compose first.
    pause
    exit /b 1
)

REM Create necessary directories
if not exist "logs" mkdir logs

REM Start the infrastructure services
echo Starting infrastructure services...
docker-compose -f docker/docker-compose.yaml up -d mysql zookeeper kafka

REM Wait for services to be ready
echo Waiting for services to start...
timeout /t 30 /nobreak >nul

REM Create Kafka topics
echo Creating Kafka topics...
docker exec kafka-notifications kafka-topics --create --topic NOTIFICATION_SEND --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1 2>nul
docker exec kafka-notifications kafka-topics --create --topic order-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1 2>nul

echo Development environment setup complete!
echo.
echo Services running:
echo - MySQL: localhost:3309
echo - Kafka: localhost:9092
echo - Zookeeper: localhost:2181
echo.
echo You can now run the notification service with:
echo mvn spring-boot:run
echo.
echo To stop the services:
echo docker-compose -f docker/docker-compose.yaml down

pause
