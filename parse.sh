#!/bin/bash

# Colors for better output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# Function to print section headers
print_header() {
    echo -e "\n${BOLD}${BLUE}========== $1 ==========${NC}\n"
}

# Function to run command with a header
run_command() {
    echo -e "${YELLOW}$ $1${NC}"
    eval $1
    echo -e "\n${GREEN}Command completed${NC}"
}

# Clear screen and show intro
clear
echo -e "${BOLD}${MAGENTA}"
echo "╔═══════════════════════════════════════════════════════╗"
echo "║             PARSER UTILITY DEMO SCRIPT                ║"
echo "╚═══════════════════════════════════════════════════════╝"
echo -e "${NC}"
echo -e "This script demonstrates the various features of the Parser Utility."
echo -e "It will compile the project and run several parser commands."
echo -e "Press ${BOLD}ENTER${NC} to begin..."
read

# Build the project
print_header "BUILDING PROJECT"
run_command "mvn clean compile"

# Display help information
print_header "DISPLAYING HELP INFORMATION"
run_command "mvn exec:java -Dexec.mainClass=\"io.joshuasalcedo.ParserRunner\" -Dexec.args=\"help\""

# Display version information
print_header "DISPLAYING VERSION INFORMATION"
run_command "mvn exec:java -Dexec.mainClass=\"io.joshuasalcedo.ParserRunner\" -Dexec.args=\"version\""

# Parse Java files in current directory
print_header "PARSING JAVA FILES IN PROJECT"
run_command "mvn exec:java -Dexec.mainClass=\"io.joshuasalcedo.ParserRunner\" -Dexec.args=\"java src/main/java\""

# Parse POM file
print_header "PARSING POM FILE"
run_command "mvn exec:java -Dexec.mainClass=\"io.joshuasalcedo.ParserRunner\" -Dexec.args=\"pom .\""

# Parse Git repository (if the project is in a git repo)
print_header "PARSING GIT REPOSITORY"
run_command "mvn exec:java -Dexec.mainClass=\"io.joshuasalcedo.ParserRunner\" -Dexec.args=\"git .\""

# Parse HTML files (if any)
print_header "PARSING HTML FILES"
run_command "mvn exec:java -Dexec.mainClass=\"io.joshuasalcedo.ParserRunner\" -Dexec.args=\"html src/main/resources\""

# Parse Markdown files
print_header "PARSING MARKDOWN FILES"
run_command "mvn exec:java -Dexec.mainClass=\"io.joshuasalcedo.ParserRunner\" -Dexec.args=\"markdown src/test/resources\""

# Parse everything (comprehensive analysis)
print_header "COMPREHENSIVE ANALYSIS (PARSING ALL)"
run_command "mvn exec:java -Dexec.mainClass=\"io.joshuasalcedo.ParserRunner\" -Dexec.args=\"all .\""

# Package the project as a JAR
print_header "PACKAGING AS JAR"
run_command "mvn package"

# Show how to run the JAR directly
JAR_FILE=$(find target -name "*.jar" ! -name "*-sources.jar" ! -name "*-javadoc.jar" | grep -v "original" | head -1)
if [ -n "$JAR_FILE" ]; then
    print_header "RUNNING FROM JAR FILE"
    echo -e "${CYAN}To run the parser directly from the JAR file:${NC}"
    echo -e "${YELLOW}$ java -jar $JAR_FILE help${NC}"
    echo -e "${YELLOW}$ java -jar $JAR_FILE all /path/to/project${NC}"
    echo -e "${YELLOW}$ java -jar $JAR_FILE java /path/to/java/files${NC}"
    echo -e "${YELLOW}$ java -jar $JAR_FILE markdown /path/to/markdown/files${NC}"
fi

# Conclusion
print_header "DEMONSTRATION COMPLETE"
echo -e "${GREEN}The Parser Utility demonstration is complete.${NC}"
echo -e "You can run more commands using:"
echo -e "${YELLOW}mvn exec:java -Dexec.mainClass=\"io.joshuasalcedo.ParserRunner\" -Dexec.args=\"[command] [args]\"${NC}"
echo -e "Or using the JAR file:"
echo -e "${YELLOW}java -jar $JAR_FILE [command] [args]${NC}"
echo -e "\nThank you for using the Parser Utility!"