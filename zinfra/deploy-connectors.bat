@echo off
echo Waiting for Kafka Connect to be ready...

:check_connect
curl -f http://localhost:8083/connectors >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo Kafka Connect not ready yet, waiting...
    timeout /t 5 >nul
    goto check_connect
)

echo Kafka Connect is ready! Deploying connectors...

for %%f in (kafka\connectors\*.json) do (
    echo Deploying connector: %%f
    curl -X POST -H "Content-Type: application/json" --data-binary @"%%f" http://localhost:8083/connectors/
    echo.
)

echo All connectors deployed successfully!
pause