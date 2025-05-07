#!/bin/bash
# Script to create a test Git repository with branches, tags, and commits

set -e  # Exit on error

# Define the target directory
REPO_DIR="src/test/resources/repo"

# Create the directory structure if it doesn't exist
mkdir -p "$REPO_DIR"
cd "$REPO_DIR"

echo "Creating Git repository in $REPO_DIR..."

# Initialize the repository
git init

# Configure local Git user (required for commits)
git config user.name "Test User"
git config user.email "test@example.com"

# Create initial files
echo "# Test Repository" > README.md
echo "public class Main { public static void main(String[] args) { System.out.println(\"Hello World\"); } }" > Main.java
echo "dependencies {}" > build.gradle
mkdir -p src/main/java
echo "package com.test; public class App { }" > src/main/java/App.java

# Initial commit
git add .
git commit -m "Initial commit"

# Create and switch to a feature branch
git checkout -b feature/add-functionality

# Add more files on this branch
echo "public class Feature { public void doSomething() { } }" > Feature.java
mkdir -p src/main/java/com/test/feature
echo "package com.test.feature; public class FeatureClass { }" > src/main/java/com/test/feature/FeatureClass.java

# Commit changes
git add .
git commit -m "Add feature class"

# Make another change and commit
echo "public class Utils { public static void helper() { } }" > Utils.java
git add Utils.java
git commit -m "Add utilities"

# Create a tag
git tag -a "v0.1-feature" -m "Feature implementation version 0.1"

# Return to main branch
git checkout main

# Create a bugfix branch
git checkout -b bugfix/issue-123

# Make changes for the bugfix
echo "// Fixed bug in Main.java" >> Main.java
git add Main.java
git commit -m "Fix bug #123"

# Tag the bugfix
git tag -a "v0.1.1-bugfix" -m "Bugfix version 0.1.1"

# Go back to main
git checkout main

# Create a release branch
git checkout -b release/v1.0

# Update version information
echo "version=1.0.0" > version.properties
git add version.properties
git commit -m "Prepare for release 1.0.0"

# Tag the release
git tag -a "v1.0.0" -m "Release version 1.0.0"

# Merge the bugfix into main
git checkout main
git merge --no-ff bugfix/issue-123 -m "Merge bugfix #123"

# Make a change directly on main
echo "# Additional documentation" >> README.md
git add README.md
git commit -m "Update documentation"

# Create a development branch
git checkout -b develop

# Add some development files
mkdir -p docs
echo "# Development Documentation" > docs/development.md
echo "# API Documentation" > docs/api.md
git add docs
git commit -m "Add development documentation"

# Make multiple commits
for i in {1..5}; do
  echo "// TODO: implement feature $i" >> TODO.txt
  git add TODO.txt
  git commit -m "Add TODO item $i"
done

# Create a tag for development milestone
git tag -a "dev-milestone-1" -m "Development milestone 1"

# Create conflicting changes (to have some interesting history)
git checkout main
echo "# Contact Information" > CONTACT.md
git add CONTACT.md
git commit -m "Add contact information"

git checkout develop
echo "# Project Contacts" > CONTACT.md
git add CONTACT.md
git commit -m "Add project contacts"

# Create another feature branch
git checkout -b feature/new-module

# Add new module files
mkdir -p src/main/java/com/test/module
echo "package com.test.module; public class Module { }" > src/main/java/com/test/module/Module.java
git add src
git commit -m "Add new module"

# Make more commits
echo "# Module Documentation" > docs/module.md
git add docs/module.md
git commit -m "Add module documentation"

# Go back to main and merge development branch
git checkout main
git merge --no-ff develop -m "Merge development branch" || git merge --abort && echo "Merge conflict detected (which is good for testing)"

# Add a final commit with config files
echo "*.log\n.DS_Store\n.idea/\n*.iml" > .gitignore
echo "language: java" > .travis.yml
git add .gitignore .travis.yml
git commit -m "Add config files"

# Create a final release tag
git tag -a "v1.1.0" -m "Release version 1.1.0"

echo ""
echo "Git repository created successfully with the following elements:"
echo "- Multiple branches: main, develop, feature branches, bugfix branch, release branch"
echo "- Multiple tags: v0.1-feature, v0.1.1-bugfix, v1.0.0, dev-milestone-1, v1.1.0"
echo "- Multiple commits across different branches"
echo "- Various file types and directory structures"
echo ""
echo "Repository location: $REPO_DIR"

# Print some repository information
echo ""
echo "Branch information:"
git branch -v

echo ""
echo "Tag information:"
git tag -l -n1

echo ""
echo "Commit history (recent commits):"
git log --oneline -n 10
