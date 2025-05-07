# Coding Guidelines

## Table of Contents
1. [Introduction](#introduction)
2. [Code Style and Formatting](#code-style-and-formatting)
3. [Naming Conventions](#naming-conventions)
4. [Documentation](#documentation)
5. [Testing](#testing)
6. [Error Handling](#error-handling)
7. [Project Structure](#project-structure)
8. [Version Control](#version-control)
9. [Dependencies](#dependencies)
10. [Performance Considerations](#performance-considerations)

## Introduction

This document outlines the coding guidelines and best practices for the Parser Utility project. Following these guidelines ensures code consistency, maintainability, and readability across the codebase.

## Code Style and Formatting

### General Guidelines
- Use 4 spaces for indentation, not tabs
- Maximum line length should be 120 characters
- Use trailing commas in multi-line lists and arrays
- Always use braces for control structures (if, for, while, etc.), even for single-line statements
- Place opening braces on the same line as the declaration
- Use a space after keywords like `if`, `for`, `while`, etc.
- Use a space around operators (`=`, `+`, `-`, etc.)

### Java-Specific Guidelines
- Follow the [Oracle Java Code Conventions](https://www.oracle.com/java/technologies/javase/codeconventions-contents.html) with the modifications specified in this document
- Use the latest Java language features when appropriate
- Prefer lambda expressions and method references over anonymous classes
- Use the diamond operator (`<>`) for generic type inference when possible
- Use `var` for local variables when the type is obvious from the context

### Class Structure
- Order class members as follows:
  1. Static fields
  2. Instance fields
  3. Constructors
  4. Public methods
  5. Protected methods
  6. Private methods
  7. Inner classes/interfaces
- Group related methods together
- Keep methods focused on a single responsibility

## Naming Conventions

### General Guidelines
- Use meaningful and descriptive names
- Avoid abbreviations unless they are widely understood
- Be consistent with existing code

### Specific Naming Rules
- **Classes**: Use PascalCase (e.g., `PomParser`, `GitRepositoryInfo`)
- **Interfaces**: Use PascalCase (e.g., `Parser`, `Repository`)
- **Methods**: Use camelCase, start with a verb (e.g., `parsePom()`, `extractDependencies()`)
- **Variables**: Use camelCase (e.g., `pomFile`, `dependencies`)
- **Constants**: Use UPPER_SNAKE_CASE (e.g., `MAX_RETRY_COUNT`, `DEFAULT_TIMEOUT`)
- **Packages**: Use lowercase, with dots as separators (e.g., `io.joshuasalcedo.parsers`)
- **Test Classes**: Name should match the class being tested with a "Test" suffix (e.g., `PomParserTest`)
- **Test Methods**: Use descriptive names that explain the test scenario (e.g., `testExtractDependencies()`)

## Documentation

### Javadoc
- All public classes, interfaces, and methods must have Javadoc comments
- Document parameters, return values, and exceptions
- Include examples in Javadoc when it helps clarify usage
- Keep Javadoc concise but complete

### Example Javadoc Format
```java
/**
 * Brief description of what the method does.
 * 
 * @param paramName Description of the parameter
 * @return Description of the return value
 * @throws ExceptionType Description of when this exception is thrown
 */
public ReturnType methodName(ParamType paramName) throws ExceptionType {
    // Method implementation
}
```

### Code Comments
- Use comments to explain "why" not "what"
- Keep comments up-to-date with code changes
- Use TODO comments for temporary code or future improvements, but include a JIRA ticket number when possible

## Testing

### General Guidelines
- Write unit tests for all public methods
- Aim for high test coverage (at least 80%)
- Tests should be independent and repeatable
- Use descriptive test method names that explain what is being tested

### JUnit Guidelines
- Use JUnit 5 for all new tests
- Use appropriate assertions with descriptive error messages
- Structure tests using the Arrange-Act-Assert pattern
- Use `@BeforeEach` for common setup code
- Use `@AfterEach` for cleanup if necessary
- Group related tests in the same test class

### Test Resources
- Place test resources in the `src/test/resources` directory
- Organize test resources to mirror the structure of the code they test
- Use descriptive names for test resource files

## Error Handling

### Exception Guidelines
- Use checked exceptions for recoverable errors
- Use unchecked exceptions for programming errors
- Create custom exceptions when appropriate
- Always include a descriptive error message
- Include the cause when wrapping exceptions
- Don't catch exceptions without handling them properly
- Don't use exceptions for flow control

### Logging
- Use appropriate log levels (ERROR, WARN, INFO, DEBUG, TRACE)
- Include relevant context in log messages
- Don't log sensitive information
- Use parameterized logging to avoid string concatenation

## Project Structure

### Directory Structure
- Follow Maven standard directory layout:
  - `src/main/java`: Java source files
  - `src/main/resources`: Resources
  - `src/test/java`: Test source files
  - `src/test/resources`: Test resources

### Package Organization
- Organize packages by feature, not by layer
- Keep related classes in the same package
- Use subpackages to organize complex features

## Version Control

### Git Guidelines
- Write clear, concise commit messages
- Start commit messages with a verb in the imperative form (e.g., "Add", "Fix", "Update")
- Reference issue numbers in commit messages when applicable
- Keep commits focused on a single change
- Regularly pull changes from the main branch
- Create feature branches for new features or significant changes
- Delete branches after they are merged

### Pull Requests
- Keep pull requests focused on a single feature or bug fix
- Include a clear description of the changes
- Reference related issues
- Ensure all tests pass before submitting
- Address code review comments promptly

## Dependencies

### General Guidelines
- Keep dependencies up-to-date
- Minimize the number of dependencies
- Prefer well-established libraries over obscure ones
- Document why a dependency is needed if it's not obvious
- Use dependency management to ensure consistent versions

### Maven Guidelines
- Use properties for version numbers
- Group related dependencies together in the POM file
- Specify appropriate scopes for dependencies (compile, test, provided, etc.)
- Use exclusions to avoid transitive dependency conflicts

## Performance Considerations

### General Guidelines
- Write efficient code, but prioritize readability and maintainability
- Optimize only when necessary and after profiling
- Document performance-critical sections
- Consider resource usage (memory, CPU, I/O)
- Use appropriate data structures and algorithms

### Specific Recommendations
- Close resources properly (use try-with-resources)
- Avoid unnecessary object creation
- Be mindful of boxing/unboxing overhead
- Use streams and parallel processing appropriately
- Consider caching for expensive operations