#!/bin/bash

# Set script to exit on error
set -e

echo "Building parser utility application..."

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven is not installed. Please install Maven first."
    exit 1
fi

# Compile the project
echo "Compiling project..."
mvn clean compile

# Run the application
echo "Starting application..."
mvn exec:java