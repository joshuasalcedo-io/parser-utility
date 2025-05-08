# Parser Utility

A versatile Java utility for parsing and analyzing various file types in a project, including Java files, Maven POM files, Git repositories, HTML files, and Markdown files. The utility extracts structured information from these files and outputs it as JSON.

## Features

- **Multiple Parser Types**:
  - **Java Parser**: Analyzes Java source files to extract class structures, methods, fields, and more
  - **POM Parser**: Extracts information from Maven POM files
  - **Git Parser**: Retrieves Git repository information including commits, branches, and tags
  - **HTML Parser**: Parses HTML files to extract document structure
  - **Markdown Parser**: Analyzes Markdown files to extract headings, links, and content structure
  - **All-in-One**: Comprehensive analysis of all supported file types in a directory

- **JSON Output**: All parsing results are output as structured JSON
- **File Saving**: Results are automatically saved to the `.parsed` directory
- **Command-Line Interface**: Easy-to-use CLI with multiple commands

## Installation

### Maven Dependency

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.joshuasalcedo</groupId>
    <artifactId>parser-utility</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Building from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/joshuasalcedo-io/parser-utility.git
   cd parser-utility
   ```

2. Build with Maven:
   ```bash
   mvn clean package
   ```

3. The executable JAR will be created in the `target` directory.

## Usage

### Command-Line Interface

The parser utility can be run from the command line using the following syntax:

```bash
java -jar parser-utility.jar [command] [arguments]
```

#### Available Commands

- `help` - Show help message
- `version` - Show version information
- `all [dir]` - Parse all supported files in the directory
- `java [dir]` - Parse Java files in the directory
- `git [dir]` - Parse Git repository in the directory
- `pom [dir]` - Parse POM files in the directory
- `html [dir]` - Parse HTML files in the directory
- `markdown [dir]` - Parse Markdown files in the directory
- `[file]` - Parse the specified file based on its extension

#### Examples

```bash
# Show help information
java -jar parser-utility.jar help

# Parse all supported files in the current directory
java -jar parser-utility.jar all .

# Parse Java files in a specific directory
java -jar parser-utility.jar java src/main/java

# Parse a Git repository
java -jar parser-utility.jar git /path/to/repo

# Parse POM files
java -jar parser-utility.jar pom .

# Parse HTML files
java -jar parser-utility.jar html src/main/resources

# Parse Markdown files
java -jar parser-utility.jar markdown docs
```

### Using with Maven

You can also run the parser directly with Maven:

```bash
mvn exec:java -Dexec.mainClass="io.joshuasalcedo.ParserRunner" -Dexec.args="all ."
```

### Demo Script

The repository includes a demo script `parse.sh` that demonstrates various features of the parser utility:

```bash
# Make the script executable
chmod +x parse.sh

# Run the demo
./parse.sh
```

## Output

The parser outputs results in two ways:

1. **Console Output**: Results are displayed in the console in a formatted JSON structure
2. **File Output**: Results are saved to JSON files in the `.parsed` directory with filenames like `parser_all.json`, `parser_java.json`, etc.

## Programmatic Usage

You can also use the parser programmatically in your Java code:

```java
import io.joshuasalcedo.parsers.Parser;
import java.util.Map;

// Parse Java files
Map<String, Object> javaResults = Parser.parse.java("/path/to/java/files");

// Parse Git repository
Map<String, Object> gitResults = Parser.parse.git("/path/to/repo");

// Parse all supported files
Map<String, Object> allResults = Parser.parse.all("/path/to/project");
```

## Deployment to GitHub Packages

This project is configured to deploy to GitHub Packages. To deploy:

1. Make sure you have a GitHub Personal Access Token with the appropriate permissions (`read:packages`, `write:packages`).

2. Configure your Maven settings.xml (usually located at `~/.m2/settings.xml`) to include your GitHub credentials:

```xml
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_GITHUB_TOKEN</password>
    </server>
  </servers>
</settings>
```

3. Deploy using the GitHub profile:

```bash
mvn clean deploy -P github
```

This will build the project and deploy it to GitHub Packages, making it available as a Maven dependency for other projects.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

Copyright © 2024 Joshua Salcedo
