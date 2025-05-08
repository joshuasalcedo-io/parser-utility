#!/bin/bash

# Script to run a complete Maven build and deployment for parser-utility
# Created for Joshua Salcedo (joshuagarrysalcedo@gmail.com)

set -e  # Exit on error
CYAN='\033[0;36m'
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${CYAN}=========================================================${NC}"
echo -e "${CYAN}   Complete Maven Build & Deployment Script              ${NC}"
echo -e "${CYAN}   For: parser-utility (io.joshuasalcedo)                ${NC}"
echo -e "${CYAN}=========================================================${NC}"

# Check if GPG key is available for signing
echo -e "\n${YELLOW}Checking GPG key configuration...${NC}"
if gpg --list-secret-keys | grep -q "4A567E82B7714477"; then
    echo -e "${GREEN}GPG key found. Signing will be possible.${NC}"
else
    echo -e "${RED}WARNING: GPG key not found. Maven Central deployment will fail.${NC}"
    echo -e "${YELLOW}Please ensure your GPG key (4A567E82B7714477) is properly set up.${NC}"
    read -p "Continue anyway? (y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# Clean the project
echo -e "\n${YELLOW}Step 1: Cleaning the project${NC}"
mvn clean

# Validate project
echo -e "\n${YELLOW}Step 2: Validating project${NC}"
mvn validate

# Compile the project
echo -e "\n${YELLOW}Step 3: Compiling the project${NC}"
mvn compile

# Run tests
echo -e "\n${YELLOW}Step 4: Running tests${NC}"
mvn test

# Package the project
echo -e "\n${YELLOW}Step 5: Packaging the project${NC}"
mvn package

# Verify the package
echo -e "\n${YELLOW}Step 6: Verifying the package${NC}"
mvn verify

# Install to local repository
echo -e "\n${YELLOW}Step 7: Installing to local repository${NC}"
mvn install

# Generate site documentation
echo -e "\n${YELLOW}Step 8: Generating site documentation${NC}"


# Deploy site to GitHub Wiki
echo -e "\n${YELLOW}Step 9: Deploying site to GitHub Wiki...${NC}"
echo -e "${YELLOW}This will push documentation to GitHub repository wiki${NC}"
    mvn site site:deploy
echo

# Perform a release deploy with all profiles
echo -e "\n${YELLOW}Step 10: Starting complete deployment process${NC}"
echo -e "${YELLOW}This will deploy to all configured repositories${NC}"
read -p "Do you want to proceed with deployment? (y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    # Deploy to GitHub Packages
    echo -e "\n${YELLOW}Deploying to GitHub Packages...${NC}"
    mvn deploy -P github

    # Deploy to Custom Nexus
    echo -e "\n${YELLOW}Deploying to Custom Nexus Repository...${NC}"
    mvn deploy -P nexus

    # Deploy to Maven Central (requires GPG signing)
    echo -e "\n${YELLOW}Deploying to Maven Central...${NC}"
    echo -e "${YELLOW}This requires GPG signing and proper OSSRH credentials${NC}"
    read -p "Proceed with Maven Central deployment? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        mvn deploy -P ossrh
    else
        echo -e "${YELLOW}Skipping Maven Central deployment.${NC}"
    fi

    # Site deploy to GitHub Pages (redundant but included for completeness)
    echo -e "\n${YELLOW}Running complete site-deploy goal...${NC}"
    mvn site-deploy
else
    echo -e "${YELLOW}Skipping deployment process.${NC}"
fi

# Run all reporting plugins
echo -e "\n${YELLOW}Step 11: Running reporting plugins${NC}"
mvn site

# Dependency analysis
echo -e "\n${YELLOW}Step 12: Analyzing dependencies${NC}"
mvn dependency:analyze
mvn dependency:tree

# Run various project info reports
echo -e "\n${YELLOW}Step 13: Generating project info reports${NC}"
mvn project-info-reports:dependencies
mvn javadoc:javadoc

echo -e "\n${GREEN}=========================================================${NC}"
echo -e "${GREEN}   Build and deployment process completed                ${NC}"
echo -e "${GREEN}=========================================================${NC}"
echo -e "\n${YELLOW}Summary of artifacts:${NC}"
find target -name "*.jar" | while read -r file; do
    echo -e "${GREEN}- $(basename "$file")${NC}"
done

echo -e "\n${YELLOW}Site documentation:${NC}"
echo -e "${GREEN}- Site generated at: ./target/site/${NC}"
echo -e "${GREEN}- GitHub Wiki updated (if you chose to deploy)${NC}"

echo -e "\n${YELLOW}Check for any deployment credentials issues in the logs.${NC}"
echo -e "${YELLOW}Maven Central deployment may require additional verification in the OSSRH repository.${NC}"