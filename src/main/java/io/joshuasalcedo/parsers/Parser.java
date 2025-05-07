package io.joshuasalcedo.parsers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.joshuasalcedo.model.git.GitRepositoryInfo;
import io.joshuasalcedo.model.javafile.ClassStructure;
import org.apache.maven.api.model.Model;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
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
                if (!dir.exists() || !dir.isDirectory()) {
                    result.put("error", "Directory does not exist or is not a directory: " + directory);
                    return result;
                }

                // Parse Git repository if it's a valid Git repository
                if (GitParser.isValidRepository(directory)) {
                    GitRepositoryInfo repoInfo = GitParser.parseRepository(directory);
                    result.put("gitRepository", repoInfo);
                }

                // List all files in the directory respecting .gitignore
                List<File> files;
                try {
                    files = GitParser.listFilesRespectingGitignore(directory);
                } catch (IOException e) {
                    // If there's an error listing files with .gitignore, fall back to listing all files
                    files = new ArrayList<>();
                    listFilesRecursive(dir, files);
                }

                // Parse POM files
                List<File> pomFiles = new ArrayList<>();
                List<Map<String, Object>> pomResults = new ArrayList<>();

                for (File file : files) {
                    if (file.getName().equals("pom.xml")) {
                        pomFiles.add(file);
                    }
                }

                for (File pomFile : pomFiles) {
                    try {
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

                        pomResults.add(pomInfo);
                    } catch (Exception e) {
                        // Skip this POM file if there's an error
                        Map<String, Object> errorInfo = new HashMap<>();
                        errorInfo.put("path", pomFile.getAbsolutePath());
                        errorInfo.put("error", "Failed to parse POM file: " + e.getMessage());
                        pomResults.add(errorInfo);
                    }
                }

                if (!pomResults.isEmpty()) {
                    result.put("pomFiles", pomResults);
                }

                // Parse Java files
                List<File> javaFiles = new ArrayList<>();
                List<Map<String, Object>> javaResults = new ArrayList<>();

                for (File file : files) {
                    if (file.getName().endsWith(".java")) {
                        javaFiles.add(file);
                    }
                }

                for (File javaFile : javaFiles) {
                    try {
                        String content = new String(java.nio.file.Files.readAllBytes(javaFile.toPath()));
                        ClassStructure classStructure = JavaParser.extractClassStructure(javaFile.getName(), content);

                        Map<String, Object> javaInfo = new HashMap<>();
                        javaInfo.put("path", javaFile.getAbsolutePath());
                        javaInfo.put("packageName", classStructure.getPackageName());
                        javaInfo.put("className", classStructure.getClassName());
                        javaInfo.put("methods", classStructure.getMethods());

                        javaResults.add(javaInfo);
                    } catch (Exception e) {
                        // Skip this Java file if there's an error
                        Map<String, Object> errorInfo = new HashMap<>();
                        errorInfo.put("path", javaFile.getAbsolutePath());
                        errorInfo.put("error", "Failed to parse Java file: " + e.getMessage());
                        javaResults.add(errorInfo);
                    }
                }

                if (!javaResults.isEmpty()) {
                    result.put("javaFiles", javaResults);
                }

                // Parse HTML files
                List<File> htmlFiles = new ArrayList<>();
                List<Map<String, Object>> htmlResults = new ArrayList<>();

                for (File file : files) {
                    String name = file.getName().toLowerCase();
                    if (name.endsWith(".html") || name.endsWith(".htm")) {
                        htmlFiles.add(file);
                    }
                }

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
                        Map<String, Object> errorInfo = new HashMap<>();
                        errorInfo.put("path", htmlFile.getAbsolutePath());
                        errorInfo.put("error", "Failed to parse HTML file: " + e.getMessage());
                        htmlResults.add(errorInfo);
                    }
                }

                if (!htmlResults.isEmpty()) {
                    result.put("htmlFiles", htmlResults);
                }

                // Add file statistics
                Map<String, Object> fileStats = new HashMap<>();
                fileStats.put("totalFiles", files.size());
                fileStats.put("pomFiles", pomFiles.size());
                fileStats.put("javaFiles", javaFiles.size());
                fileStats.put("htmlFiles", htmlFiles.size());
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
                    if (GitParser.isValidRepository(directory)) {
                        GitRepositoryInfo repoInfo = GitParser.parseRepository(directory);
                        result.put("gitRepository", repoInfo);
                    } else {
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
                if (!dir.exists() || !dir.isDirectory()) {
                    result.put("error", "Directory does not exist or is not a directory: " + directory);
                    return result;
                }

                List<File> javaFiles = new ArrayList<>();
                JavaParser.findJavaFiles(dir).forEach(javaFiles::add);

                List<Map<String, Object>> javaResults = new ArrayList<>();

                for (File javaFile : javaFiles) {
                    try {
                        String content = new String(java.nio.file.Files.readAllBytes(javaFile.toPath()));
                        ClassStructure classStructure = JavaParser.extractClassStructure(javaFile.getName(), content);

                        Map<String, Object> javaInfo = new HashMap<>();
                        javaInfo.put("path", javaFile.getAbsolutePath());
                        javaInfo.put("packageName", classStructure.getPackageName());
                        javaInfo.put("className", classStructure.getClassName());
                        javaInfo.put("methods", classStructure.getMethods());

                        javaResults.add(javaInfo);
                    } catch (Exception e) {
                        // Skip this Java file if there's an error
                        Map<String, Object> errorInfo = new HashMap<>();
                        errorInfo.put("path", javaFile.getAbsolutePath());
                        errorInfo.put("error", "Failed to parse Java file: " + e.getMessage());
                        javaResults.add(errorInfo);
                    }
                }

                result.put("javaFiles", javaResults);
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
                if (!dir.exists() || !dir.isDirectory()) {
                    result.put("error", "Directory does not exist or is not a directory: " + directory);
                    return result;
                }

                List<File> pomFiles = PomParser.findPomFiles(dir);
                List<Map<String, Object>> pomResults = new ArrayList<>();

                for (File pomFile : pomFiles) {
                    try {
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

                        pomResults.add(pomInfo);
                    } catch (Exception e) {
                        // Skip this POM file if there's an error
                        Map<String, Object> errorInfo = new HashMap<>();
                        errorInfo.put("path", pomFile.getAbsolutePath());
                        errorInfo.put("error", "Failed to parse POM file: " + e.getMessage());
                        pomResults.add(errorInfo);
                    }
                }

                result.put("pomFiles", pomResults);
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
                if (!dir.exists() || !dir.isDirectory()) {
                    result.put("error", "Directory does not exist or is not a directory: " + directory);
                    return result;
                }

                List<File> htmlFiles = new ArrayList<>();
                listFilesRecursive(dir, htmlFiles, ".html", ".htm");

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
                        Map<String, Object> errorInfo = new HashMap<>();
                        errorInfo.put("path", htmlFile.getAbsolutePath());
                        errorInfo.put("error", "Failed to parse HTML file: " + e.getMessage());
                        htmlResults.add(errorInfo);
                    }
                }

                result.put("htmlFiles", htmlResults);
                return result;
            } catch (Exception e) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Failed to parse HTML files: " + e.getMessage());
                return error;
            }
        }
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
