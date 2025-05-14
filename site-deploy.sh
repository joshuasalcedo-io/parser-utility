#!/bin/bash

# Ensure we have GitHub CLI authentication
if ! gh auth status &>/dev/null; then
  echo "GitHub CLI not authenticated. Please run 'gh auth login' first."
  exit 1
fi

# Generate the Maven site
mvn clean site

# Deploy to GitHub Wiki
# Option 1: Using Maven SCM Publish Plugin
mvn site-deploy

# Alternatively, Option 2: Manual deployment via GitHub CLI
# gh repo clone joshuasalcedo-io/parser-utility.wiki /tmp/wiki-repo
# cp -r target/site/* /tmp/wiki-repo/
# cd /tmp/wiki-repo
# git add .
# git commit -m "Update wiki documentation"
# git push

echo "Site successfully deployed to GitHub Wiki!"