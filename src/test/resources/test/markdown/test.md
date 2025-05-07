# Parser Test Resources

This document explains the test resources provided for testing the parser utility classes.

## Directory Structure

The test resources are organized as follows:

```
src/test/resources/
├── sample-pom.xml                 # Sample POM file for PomParser tests
├── multi-module-pom.xml           # Multi-module POM file for PomParser tests
├── SampleClass.java               # Sample Java file for JavaParser tests
├── sample.html                    # Sample HTML file for HtmlParser tests
├── repo/                          # Git repository for GitParser tests
└── multi-module-project/          # Directory structure for multi-module project tests
    ├── pom.xml                    # Parent POM file
    ├── module-a/
    │   └── pom.xml               # Module A POM file
    ├── module-b/
    │   └── pom.xml               # Module B POM file
    └── module-c/
        └── pom.xml               # Module C POM file
```

## Setting Up Test Resources

1. **Create Resource Directories**:
   ```bash
   mkdir -p src/test/resources/multi-module-project/module-a
   mkdir -p src/test/resources/multi-module-project/module-b
   mkdir -p src/test/resources/multi-module-project/module-c
   mkdir -p src/test/resources/java-files
   ```

2. **Copy Test Files**:
   Copy the provided test files to their respective locations.

3. **Create Git Test Repository**:
   Run the provided `create-test-repo.sh` script to create a Git test repository:
   ```bash
   chmod +x create-test-repo.sh
   ./create-test-repo.sh
   ```

## Running Tests

Once the test resources are set up, you can run the JUnit tests for each parser:

```bash
# Run all tests
mvn test

# Run specific test classes
mvn test -Dtest=PomParserTest
mvn test -Dtest=JavaParserTest
mvn test -Dtest=HtmlParserTest
mvn test -Dtest=GitParserTest
```

## Test Contents

### PomParser Tests

The POM files contain:
- Parent-child relationships
- Dependencies
- Plugins
- Modules (for multi-module POM)
- Properties

### JavaParser Tests

The Java files contain:
- Classes with different access modifiers
- Methods with various return types and parameters
- JavaDoc comments
- Regular comments (single-line and multi-line)
- Nested classes
- Various Java language features

### HtmlParser Tests

The HTML file contains:
- Document structure (head, body)
- Various HTML elements (headings, paragraphs, lists, tables)
- Forms with input elements
- Links and images
- CSS classes and IDs
- Metadata tags

### GitParser Tests

The Git repository contains:
- Multiple branches
- Multiple tags
- Commit history with various changes
- Different file types and directory structures

## Expected Test Failures

The tests are designed to initially fail because:
1. Implementation details in the parser classes might not match the expected behavior
2. The tests expect specific content in the test resources that should match the assertions

Implementing the tests and making them pass is part of the development process.