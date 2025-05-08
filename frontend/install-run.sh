#!/bin/bash

# Create logs directory if it doesn't exist
mkdir -p logs

# Clean and build the project
echo "Building project with Maven..."
mvn clean install > logs/maven-build.log 2>&1

# Find the jar file
JAR_FILE=$(find target -name "*.jar" | head -1)

if [ -z "$JAR_FILE" ]; then
  echo "Error: No JAR file found in target directory!" | tee -a logs/error.log
  exit 1
fi

echo "Starting application using JAR: $JAR_FILE"

# Start the application with nohup and redirect output to logs
nohup java -jar $JAR_FILE > logs/application.log 2>&1 &

# Get the process ID
PID=$!
echo $PID > logs/application.pid
echo "Application started with PID: $PID"
echo "Logs are being written to logs/application.log"
echo "You can monitor the logs with: tail -f logs/application.log"