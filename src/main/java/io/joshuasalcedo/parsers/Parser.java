package io.joshuasalcedo.parsers;

import io.joshuasalcedo.model.git.GitRepositoryInfo;
import io.joshuasalcedo.model.javafile.ClassStructure;
import io.joshuasalcedo.model.markdown.*;
import org.apache.maven.api.model.Model;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main parser utility class that serves as a facade for all individual parsers.
 * This class provides static methods to parse different types of content and return JSON structures.
 */
public class Parser {

    /**
     * Nested static class for parsing different types of content.
     */
    public static class parse {

        /**
         * Parse a directory and extract all available information using all parsers.
         * This method respects .gitignore rules when listing files.
         *
         * @param directory The path to the directory to parse
         * @return Map containing all parsed information
         */
        public static Map<String, Object> all(String directory) {
            try {
                // Create a map to hold all parsed information
                Map<String, Object> result = new HashMap<>();

                // Check if directory exists
                File dir = new File(directory);
                if (!validateDirectory(dir, result)) {
                    return result;
                }

                // Parse Git repository if it's a valid Git repository
                parseGitRepository(directory, result);

                // List all files in the directory respecting .gitignore
                List<File> files = listAllFiles(directory, dir);

                // Parse different file types
                Map<String, List<File>> filesByType = categorizeFiles(files);

                List<File> pomFiles = filesByType.getOrDefault("pom", new ArrayList<>());
                List<File> javaFiles = filesByType.getOrDefault("java", new ArrayList<>());
                List<File> htmlFiles = filesByType.getOrDefault("html", new ArrayList<>());
                List<File> markdownFiles = filesByType.getOrDefault("markdown", new ArrayList<>());

                // Parse each file type
                result.putAll(parsePomFiles(pomFiles, directory));
                result.putAll(parseJavaFiles(javaFiles, directory));
                result.putAll(parseHtmlFiles(htmlFiles));
                result.putAll(parseMarkdownFiles(markdownFiles));

                // Add file statistics
                Map<String, Object> fileStats = new HashMap<>();
                fileStats.put("totalFiles", files.size());
                fileStats.put("pomFiles", pomFiles.size());
                fileStats.put("javaFiles", javaFiles.size());
                fileStats.put("htmlFiles", htmlFiles.size());
                fileStats.put("markdownFiles", markdownFiles.size());
                result.put("fileStatistics", fileStats);

                return result;
            } catch (Exception e) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Failed to parse directory: " + e.getMessage());
                return error;
            }
        }

        /**
         * Parse a Git repository and return a Map containing the repository information.
         *
         * @param directory The path to the directory containing the Git repository
         * @return Map containing the parsed Git repository information
         */
        public static Map<String, Object> git(String directory) {
            try {
                // Create a map to hold all parsed information
                Map<String, Object> result = new HashMap<>();

                // Parse Git repository
                File dir = new File(directory);
                if (dir.exists() && dir.isDirectory()) {
                    boolean isValidRepo = parseGitRepository(directory, result);
                    if (!isValidRepo) {
                        result.put("error", "Not a valid Git repository: " + directory);
                    }
                } else {
                    result.put("error", "Directory does not exist: " + directory);
                }

                return result;
            } catch (IOException | GitAPIException e) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Failed to parse Git repository: " + e.getMessage());
                return error;
            }
        }

        /**
         * Parse Java files in a directory and return a Map containing the Java code structure.
         *
         * @param directory The path to the directory containing Java files
         * @return Map containing the parsed Java files information
         */
        public static Map<String, Object> java(String directory) {
            try {
                // Create a map to hold all parsed information
                Map<String, Object> result = new HashMap<>();

                // Find all Java files in the directory
                File dir = new File(directory);
                if (!validateDirectory(dir, result)) {
                    return result;
                }

                List<File> javaFiles = new ArrayList<>();
                JavaParser.findJavaFiles(dir).forEach(javaFiles::add);

                result.putAll(parseJavaFiles(javaFiles, directory));
                return result;
            } catch (Exception e) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Failed to parse Java files: " + e.getMessage());
                return error;
            }
        }

        /**
         * Parse POM files in a directory and return a Map containing the Maven project information.
         *
         * @param directory The path to the directory containing POM files
         * @return Map containing the parsed POM files information
         */
        public static Map<String, Object> pom(String directory) {
            try {
                // Create a map to hold all parsed information
                Map<String, Object> result = new HashMap<>();

                // Find all POM files in the directory
                File dir = new File(directory);
                if (!validateDirectory(dir, result)) {
                    return result;
                }

                List<File> pomFiles = PomParser.findPomFiles(dir);
                result.putAll(parsePomFiles(pomFiles, directory));
                return result;
            } catch (Exception e) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Failed to parse POM files: " + e.getMessage());
                return error;
            }
        }

        /**
         * Parse HTML files in a directory and return a Map containing the HTML structure.
         *
         * @param directory The path to the directory containing HTML files
         * @return Map containing the parsed HTML files information
         */
        public static Map<String, Object> html(String directory) {
            try {
                // Create a map to hold all parsed information
                Map<String, Object> result = new HashMap<>();

                // Find all HTML files in the directory
                File dir = new File(directory);
                if (!validateDirectory(dir, result)) {
                    return result;
                }

                List<File> htmlFiles = new ArrayList<>();
                listFilesRecursive(dir, htmlFiles, ".html", ".htm");

                result.putAll(parseHtmlFiles(htmlFiles));
                return result;
            } catch (Exception e) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Failed to parse HTML files: " + e.getMessage());
                return error;
            }
        }

        /**
         * Parse Markdown files in a directory and return a Map containing Markdown content information.
         *
         * @param directory The path to the directory containing Markdown files
         * @return Map containing the parsed Markdown files information
         */
        public static Map<String, Object> markdown(String directory) {
            try {
                // Create a map to hold all parsed information
                Map<String, Object> result = new HashMap<>();

                // Find all Markdown files in the directory
                File dir = new File(directory);
                if (!validateDirectory(dir, result)) {
                    return result;
                }

                List<File> markdownFiles = new ArrayList<>();
                listFilesRecursive(dir, markdownFiles, ".md", ".markdown");

                result.putAll(parseMarkdownFiles(markdownFiles));
                return result;
            } catch (Exception e) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Failed to parse Markdown files: " + e.getMessage());
                return error;
            }
        }
    }

    /**
     * Validates if a directory exists and is actually a directory.
     *
     * @param directory The directory to validate
     * @param result The result map to store error if validation fails
     * @return boolean indicating whether the directory is valid
     */
    private static boolean validateDirectory(File directory, Map<String, Object> result) {
        if (!directory.exists() || !directory.isDirectory()) {
            result.put("error", "Directory does not exist or is not a directory: " + directory);
            return false;
        }
        return true;
    }

    /**
     * Parse Git repository information and add it to the result map.
     *
     * @param directory The directory path of the Git repository
     * @param result The result map to store the Git repository information
     * @return boolean indicating whether the repository was successfully parsed
     * @throws IOException If an I/O error occurs
     * @throws GitAPIException If a Git operation fails
     */
    private static boolean parseGitRepository(String directory, Map<String, Object> result) throws IOException, GitAPIException {
        if (GitParser.isValidRepository(directory)) {
            GitRepositoryInfo repoInfo = GitParser.parseRepository(directory);
            result.put("gitRepository", repoInfo);
            return true;
        }
        return false;
    }
    /**
     * List all files in a directory, respecting .gitignore rules if possible.
     *
     * @param directory The directory path to search
     * @param dir The directory File object
     * @return List of files found
     */
    private static List<File> listAllFiles(String directory, File dir) {
        List<File> files;
        try {
            files = GitParser.listFilesRespectingGitignore(directory);
        } catch (IOException e) {
            // If there's an error listing files with .gitignore, fall back to listing all files
            files = new ArrayList<>();
            listFilesRecursive(dir, files);
        }
        return files;
    }

    /**
     * Categorize files by type for further processing.
     *
     * @param files List of files to categorize
     * @return Map of file lists by type
     */
    private static Map<String, List<File>> categorizeFiles(List<File> files) {
        Map<String, List<File>> filesByType = new HashMap<>();
        filesByType.put("pom", new ArrayList<>());
        filesByType.put("java", new ArrayList<>());
        filesByType.put("html", new ArrayList<>());
        filesByType.put("markdown", new ArrayList<>());

        for (File file : files) {
            String name = file.getName().toLowerCase();
            if (file.getName().equals("pom.xml")) {
                filesByType.get("pom").add(file);
            } else if (name.endsWith(".java")) {
                filesByType.get("java").add(file);
            } else if (name.endsWith(".html") || name.endsWith(".htm")) {
                filesByType.get("html").add(file);
            } else if (name.endsWith(".md") || name.endsWith(".markdown")) {
                filesByType.get("markdown").add(file);
            }
        }
        return filesByType;
    }

    /**
     * Parse POM files and create a map of results.
     *
     * @param pomFiles List of POM files to parse
     * @return Map containing parsed POM files information
     */
    private static Map<String, Object> parsePomFiles(List<File> pomFiles, String baseDirectory) {
        Map<String, Object> result = new HashMap<>();
        if (pomFiles.isEmpty()) {
            return result;
        }

        Map<String, Object> pomFileMap = new HashMap<>();
        File baseDir = new File(baseDirectory);

        for (File pomFile : pomFiles) {
            try {
                // Get relative path from base directory
                String relativePath = baseDir.toURI().relativize(pomFile.toURI()).getPath();

                Model model = PomParser.parsePom(pomFile);
                Map<String, Object> pomInfo = new HashMap<>();
                pomInfo.put("path", pomFile.getAbsolutePath());
                pomInfo.put("groupId", model.getGroupId());
                pomInfo.put("artifactId", model.getArtifactId());
                pomInfo.put("version", model.getVersion());
                pomInfo.put("name", model.getName());
                pomInfo.put("description", model.getDescription());

                // Add dependencies
                List<Map<String, String>> dependencies = new ArrayList<>();
                for (org.apache.maven.api.model.Dependency dependency : model.getDependencies()) {
                    Map<String, String> dep = new HashMap<>();
                    dep.put("groupId", dependency.getGroupId());
                    dep.put("artifactId", dependency.getArtifactId());
                    dep.put("version", dependency.getVersion());
                    dep.put("scope", dependency.getScope());
                    dependencies.add(dep);
                }
                pomInfo.put("dependencies", dependencies);

                // Use the relative path as the key
                pomFileMap.put(relativePath, pomInfo);
            } catch (Exception e) {
                // Skip this POM file if there's an error
                Map<String, Object> errorInfo = new HashMap<>();
                errorInfo.put("path", pomFile.getAbsolutePath());
                errorInfo.put("error", "Failed to parse POM file: " + e.getMessage());
                pomFileMap.put("error_" + pomFile.getName(), errorInfo);
            }
        }

        if (!pomFileMap.isEmpty()) {
            result.put("pomFiles", pomFileMap);
        }
        return result;
    }

    private static Map<String, Object> parseJavaFiles(List<File> javaFiles, String baseDirectory) {
        Map<String, Object> result = new HashMap<>();
        if (javaFiles.isEmpty()) {
            return result;
        }

        // Map to hold java file info by relative path
        Map<String, Object> javaFileMap = new HashMap<>();
        File baseDir = new File(baseDirectory);

        for (File javaFile : javaFiles) {
            try {
                // Get relative path from base directory
                String relativePath = baseDir.toURI().relativize(javaFile.toURI()).getPath();

                String content = new String(Files.readAllBytes(javaFile.toPath()));
                ClassStructure classStructure = JavaParser.extractClassStructure(javaFile.getName(), content);

                Map<String, Object> javaInfo = new HashMap<>();
                javaInfo.put("path", javaFile.getAbsolutePath());
                javaInfo.put("packageName", classStructure.getPackageName());
                javaInfo.put("className", classStructure.getClassName());
                javaInfo.put("methods", classStructure.getMethods());

                // Use the relative path as the key
                javaFileMap.put(relativePath, javaInfo);
            } catch (Exception e) {
                // Skip this Java file if there's an error
                Map<String, Object> errorInfo = new HashMap<>();
                errorInfo.put("path", javaFile.getAbsolutePath());
                errorInfo.put("error", "Failed to parse Java file: " + e.getMessage());
                javaFileMap.put("error_" + javaFile.getName(), errorInfo);
            }
        }

        // If we found Java files, add them to the result map
        if (!javaFileMap.isEmpty()) {
            result.put("javaFiles", javaFileMap);
        }

        return result;
    }
    /**
     * Parse HTML files and create a map of results.
     *
     * @param htmlFiles List of HTML files to parse
     * @return Map containing parsed HTML files information
     */
    private static Map<String, Object> parseHtmlFiles(List<File> htmlFiles) {
        Map<String, Object> result = new HashMap<>();
        if (htmlFiles.isEmpty()) {
            return result;
        }

        List<Map<String, Object>> htmlResults = new ArrayList<>();

        for (File htmlFile : htmlFiles) {
            try {
                Document document = HtmlParser.parse(htmlFile, "UTF-8", "");

                Map<String, Object> htmlInfo = new HashMap<>();
                htmlInfo.put("path", htmlFile.getAbsolutePath());
                htmlInfo.put("title", document.title());
                htmlInfo.put("headings", HtmlParser.getHeadings(document).size());
                htmlInfo.put("links", HtmlParser.getLinks(document).size());
                htmlInfo.put("images", HtmlParser.getImages(document).size());
                htmlInfo.put("metadata", HtmlParser.getMetadata(document));

                htmlResults.add(htmlInfo);
            } catch (Exception e) {
                // Skip this HTML file if there's an error
                addErrorInfo(htmlResults, htmlFile, "Failed to parse HTML file: " + e.getMessage());
            }
        }

        if (!htmlResults.isEmpty()) {
            result.put("htmlFiles", htmlResults);
        }
        return result;
    }

    /**
     * Parse Markdown files and create a map of results.
     *
     * @param markdownFiles List of Markdown files to parse
     * @return Map containing parsed Markdown files information
     */
    private static Map<String, Object> parseMarkdownFiles(List<File> markdownFiles) {
        Map<String, Object> result = new HashMap<>();
        if (markdownFiles.isEmpty()) {
            return result;
        }

        List<Map<String, Object>> markdownResults = new ArrayList<>();

        for (File markdownFile : markdownFiles) {
            try {
                String content = new String(Files.readAllBytes(markdownFile.toPath()));
                MarkdownContent parsedContent = MarkdownParser.parseMarkdown(content);

                Map<String, Object> mdInfo = createMarkdownInfo(markdownFile, parsedContent);
                markdownResults.add(mdInfo);
            } catch (Exception e) {
                // Skip this Markdown file if there's an error
                addErrorInfo(markdownResults, markdownFile, "Failed to parse Markdown file: " + e.getMessage());
            }
        }

        if (!markdownResults.isEmpty()) {
            result.put("markdownFiles", markdownResults);
        }
        return result;
    }

    /**
     * Create a map of Markdown file information from parsed content.
     *
     * @param markdownFile The Markdown file
     * @param parsedContent The parsed Markdown content
     * @return Map containing the Markdown file information
     */
    private static Map<String, Object> createMarkdownInfo(File markdownFile, MarkdownContent parsedContent) {
        Map<String, Object> mdInfo = new HashMap<>();
        mdInfo.put("path", markdownFile.getAbsolutePath());
        mdInfo.put("title", parsedContent.getTitle());
        mdInfo.put("wordCount", parsedContent.getWordCount());
        mdInfo.put("readingTimeMinutes", parsedContent.getReadingTimeMinutes());

        // Add headings
        List<Map<String, Object>> headings = new ArrayList<>();
        for (MarkdownHeading heading : parsedContent.getHeadings()) {
            Map<String, Object> h = new HashMap<>();
            h.put("level", heading.getLevel());
            h.put("text", heading.getText());
            h.put("id", heading.getId());
            headings.add(h);
        }
        mdInfo.put("headings", headings);

        // Add links
        List<Map<String, Object>> links = new ArrayList<>();
        for (MarkdownLink link : parsedContent.getLinks()) {
            Map<String, Object> l = new HashMap<>();
            l.put("text", link.getText());
            l.put("url", link.getUrl());
            l.put("title", link.getTitle());
            l.put("internal", link.isInternal());
            links.add(l);
        }
        mdInfo.put("links", links);

        // Add images
        List<Map<String, Object>> images = new ArrayList<>();
        for (MarkdownImage image : parsedContent.getImages()) {
            Map<String, Object> img = new HashMap<>();
            img.put("altText", image.getAltText());
            img.put("url", image.getUrl());
            img.put("title", image.getTitle());
            img.put("local", image.isLocal());
            images.add(img);
        }
        mdInfo.put("images", images);

        // Add code blocks
        List<Map<String, Object>> codeBlocks = new ArrayList<>();
        for (MarkdownCodeBlock codeBlock : parsedContent.getCodeBlocks()) {
            Map<String, Object> cb = new HashMap<>();
            cb.put("language", codeBlock.getLanguage());
            cb.put("content", codeBlock.getContent());
            cb.put("fenced", codeBlock.isFenced());
            codeBlocks.add(cb);
        }
        mdInfo.put("codeBlocks", codeBlocks);

        // Add front matter if available
        if (parsedContent.getFrontMatter() != null && !parsedContent.getFrontMatter().isEmpty()) {
            mdInfo.put("frontMatter", parsedContent.getFrontMatter());
        }

        return mdInfo;
    }

    /**
     * Add error information to a list of results.
     *
     * @param resultsList The list to add error information to
     * @param file The file that caused the error
     * @param errorMessage The error message
     */
    private static void addErrorInfo(List<Map<String, Object>> resultsList, File file, String errorMessage) {
        Map<String, Object> errorInfo = new HashMap<>();
        errorInfo.put("path", file.getAbsolutePath());
        errorInfo.put("error", errorMessage);
        resultsList.add(errorInfo);
    }

    /**
     * Helper method to recursively list all files in a directory.
     *
     * @param directory The directory to search
     * @param files List to collect files
     */
    private static void listFilesRecursive(File directory, List<File> files) {
        File[] fileList = directory.listFiles();
        if (fileList != null) {
            for (File file : fileList) {
                if (file.isDirectory()) {
                    listFilesRecursive(file, files);
                } else {
                    files.add(file);
                }
            }
        }
    }

    /**
     * Helper method to recursively list files with specific extensions in a directory.
     *
     * @param directory The directory to search
     * @param files List to collect files
     * @param extensions File extensions to include
     */
    private static void listFilesRecursive(File directory, List<File> files, String... extensions) {
        File[] fileList = directory.listFiles();
        if (fileList != null) {
            for (File file : fileList) {
                if (file.isDirectory()) {
                    listFilesRecursive(file, files, extensions);
                } else {
                    String name = file.getName().toLowerCase();
                    for (String extension : extensions) {
                        if (name.endsWith(extension.toLowerCase())) {
                            files.add(file);
                            break;
                        }
                    }
                }
            }
        }
    }
}
