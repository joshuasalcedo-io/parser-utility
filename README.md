# Parser Utility

A versatile Java utility for parsing and analyzing various file types in a project, including Java files, Maven POM files, Git repositories, HTML files, and Markdown files. The utility extracts structured information from these files and outputs it as JSON.

## Usage 
```typescript
Output Structure
The parser generates a JSON structure with the following type definition:
typescript// Main Parser Output
interface ParserOutput {
  git: GitRepositoryInfo;
  java: Record<string, ClassStructure>;
  pom: Record<string, PomStructure>;
  markdown: Record<string, MarkdownContent>;
  html: Record<string, HtmlDocumentInfo>;
}

// Git Repository Models
interface GitRepositoryInfo {
  name: string;
  path: string;
  currentBranch: string;
  remoteUrl: string;
  latestCommit: CommitInfo;
  branches: BranchInfo[];
  tags: TagInfo[];
  hasUncommittedChanges: boolean;
  commitCount: number;
  statistics: Record<string, any>;
  topContributors: ContributorInfo[];
  creationDate: Date;
  lastUpdatedDate: Date;
  fileExtensionCounts: Record<string, number>;
}

interface CommitInfo {
  id: string;
  shortId: string;
  message: string;
  authorName: string;
  authorEmail: string;
  authorDate: Date;
  committerName: string;
  committerEmail: string;
  commitDate: Date;
  parentIds: string[];
  changedFiles: FileChange[];
}

interface FileChange {
  type: ChangeType;
  path: string;
  oldPath: string;
  linesAdded: number;
  linesDeleted: number;
  mode: number;
}

enum ChangeType {
  ADD,
  MODIFY,
  DELETE,
  RENAME,
  COPY
}

interface BranchInfo {
  name: string;
  current: boolean;
  remote: boolean;
  remoteName: string;
  commitId: string;
  trackingBranch: string;
  merged: boolean;
}

interface TagInfo {
  name: string;
  commitId: string;
  annotated: boolean;
  message: string;
  taggerName: string;
  taggerEmail: string;
  taggerDate: Date;
}

interface ContributorInfo {
  name: string;
  email: string;
  commitCount: number;
  linesAdded: number;
  linesDeleted: number;
  firstCommitDate: Date;
  lastCommitDate: Date;
}

// Java File Models
interface ClassStructure {
  name: string;
  packageName: string;
  path: string;
  type: string;
  modifiers: string[];
  superclass: string;
  interfaces: string[];
  methods: MethodStructure[];
  fields: FieldStructure[];
  innerClasses: ClassStructure[];
  javadoc: JavadocStructure;
  imports: string[];
  annotations: string[];
}

interface MethodStructure {
  name: string;
  returnType: string;
  modifiers: string[];
  parameters: Parameter[];
  exceptions: string[];
  javadoc: JavadocStructure;
  annotations: string[];
  body: string;
}

interface Parameter {
  name: string;
  type: string;
  annotations: string[];
}

interface JavadocStructure {
  description: string;
  tags: JavadocTag[];
}

interface JavadocTag {
  name: string;
  value: string;
}

interface FieldStructure {
  name: string;
  type: string;
  modifiers: string[];
  initialValue: string;
  javadoc: JavadocStructure;
  annotations: string[];
}

// Maven POM Models
interface PomStructure {
  coordinates: PomCoordinates;
  parent: ParentInfo;
  name: string;
  description: string;
  url: string;
  dependencies: Dependency[];
  plugins: Plugin[];
  properties: Property[];
  modules: string[];
  profiles: string[];
}

interface PomCoordinates {
  groupId: string;
  artifactId: string;
  version: string;
  packaging: string;
}

interface ParentInfo {
  groupId: string;
  artifactId: string;
  version: string;
  relativePath: string;
}

interface Dependency {
  groupId: string;
  artifactId: string;
  version: string;
  scope: string;
  optional: boolean;
}

interface Plugin {
  groupId: string;
  artifactId: string;
  version: string;
  configuration: PluginConfiguration;
}

interface PluginConfiguration {
  properties: Record<string, string>;
}

interface Property {
  name: string;
  value: string;
}

// Markdown Models
interface MarkdownContent {
  headings: MarkdownHeading[];
  links: MarkdownLink[];
  images: MarkdownImage[];
  codeBlocks: MarkdownCodeBlock[];
  plainText: string;
}

interface MarkdownHeading {
  level: number;
  text: string;
  id: string;
}

interface MarkdownLink {
  text: string;
  url: string;
  title: string;
}

interface MarkdownImage {
  altText: string;
  url: string;
  title: string;
}

interface MarkdownCodeBlock {
  language: string;
  code: string;
}

// HTML Models
interface HtmlDocumentInfo {
  title: string;
  headings: HtmlHeading[];
  links: HtmlLink[];
  images: HtmlImage[];
  metadata: Record<string, string>;
}

interface HtmlHeading {
  level: number;
  text: string;
  id: string;
}

interface HtmlLink {
  text: string;
  href: string;
  rel: string;
}

interface HtmlImage {
  src: string;
  alt: string;
  width: number;
  height: number;
}
````
Example Output
```
json{
  "git": {
    "name": "parser-utility",
    "path": "/path/to/repository",
    "currentBranch": "main",
    // More Git information...
  },
  "java": {
    "io.joshuasalcedo.ParserRunner": {
      "name": "ParserRunner",
      "packageName": "io.joshuasalcedo",
      // More Java class information...
    }
    // More Java classes...
  },
  // More sections for POM, Markdown, and HTML...
}
```

[.parsed](.parsed)
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

### JSON Structure

The parser returns a structured JSON object that contains information about the parsed files. The structure of the JSON output varies depending on the type of parser used:

- **Git Parser**: Returns information about the Git repository, including commits, branches, tags, and contributors
- **Java Parser**: Returns information about Java classes, methods, fields, and Javadoc
- **POM Parser**: Returns information about Maven POM files, including dependencies, plugins, and properties
- **Markdown Parser**: Returns information about Markdown files, including headings, links, images, and code blocks
- **HTML Parser**: Returns information about HTML files, including headings, links, images, and metadata

A UML diagram of the JSON structure is available in the `json-structure.puml` file in the project root directory. This diagram shows the relationships between the different components of the JSON output.

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

## GitHub Wiki Documentation

This project is configured to automatically generate and deploy documentation to GitHub Wiki. The documentation includes:

1. Project information and usage guides
2. Javadoc API documentation
3. Code examples and tutorials

### Generating Documentation

To generate the documentation locally:

```bash
mvn site
```

This will create a documentation site in the `target/site` directory that you can browse locally.

### Deploying to GitHub Wiki

To deploy the documentation to GitHub Wiki:

1. Make sure you have a GitHub Personal Access Token with the appropriate permissions (`repo`).

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

3. Deploy the documentation to GitHub Wiki:

```bash
mvn site site:deploy
```

This will generate the documentation and push it to the GitHub Wiki associated with this repository.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

Copyright © 2024 Joshua Salcedo
