#!/bin/bash

# Script to deploy to both Nexus and GitHub Packages

# Variables
ARTIFACT_ID=java-structure
VERSION=1.0-SNAPSHOT
GROUP_ID=io.joshuasalcedo
GITHUB_USERNAME=joshuasalcedo-dev
GITHUB_REPO=java-structure

# Build the project
echo "Building project..."
mvn clean package

# Standard deploy to Nexus Repository
echo "Deploying to Nexus Repository..."
mvn deploy

# Deploy to GitHub Packages
echo "Deploying to GitHub Packages..."
mvn deploy:deploy-file \
  -DgroupId=$GROUP_ID \
  -DartifactId=$ARTIFACT_ID \
  -Dversion=$VERSION \
  -Dpackaging=jar \
  -Dfile=target/$ARTIFACT_ID-$VERSION.jar \
  -DrepositoryId=github \
  -Durl=https://maven.pkg.github.com/$GITHUB_USERNAME/$GITHUB_REPO

# Also deploy the jar-with-dependencies
echo "Deploying jar-with-dependencies to GitHub Packages..."
mvn deploy:deploy-file \
  -DgroupId=$GROUP_ID \
  -DartifactId=$ARTIFACT_ID \
  -Dversion=$VERSION \
  -Dpackaging=jar \
  -Dclassifier=jar-with-dependencies \
  -Dfile=target/$ARTIFACT_ID-$VERSION-jar-with-dependencies.jar \
  -DrepositoryId=github \
  -Durl=https://maven.pkg.github.com/$GITHUB_USERNAME/$GITHUB_REPO

# Deploy source JAR
echo "Deploying sources JAR to GitHub Packages..."
mvn deploy:deploy-file \
  -DgroupId=$GROUP_ID \
  -DartifactId=$ARTIFACT_ID \
  -Dversion=$VERSION \
  -Dpackaging=jar \
  -Dclassifier=sources \
  -Dfile=target/$ARTIFACT_ID-$VERSION-sources.jar \
  -DrepositoryId=github \
  -Durl=https://maven.pkg.github.com/$GITHUB_USERNAME/$GITHUB_REPO

# Deploy javadoc JAR
echo "Deploying javadoc JAR to GitHub Packages..."
mvn deploy:deploy-file \
  -DgroupId=$GROUP_ID \
  -DartifactId=$ARTIFACT_ID \
  -Dversion=$VERSION \
  -Dpackaging=jar \
  -Dclassifier=javadoc \
  -Dfile=target/$ARTIFACT_ID-$VERSION-javadoc.jar \
  -DrepositoryId=github \
  -Durl=https://maven.pkg.github.com/$GITHUB_USERNAME/$GITHUB_REPO

echo "Deployment complete!"