package io.joshuasalcedo.parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.joshuasalcedo.model.maven.PomCoordinates;
import org.apache.maven.api.model.Model;
import org.apache.maven.api.model.Dependency;
import org.apache.maven.api.model.Parent;
import org.apache.maven.api.model.Plugin;
import org.apache.maven.model.v4.MavenStaxReader;

/**
 * Utility class for parsing Maven POM (pom.xml) files using the official Maven Model API.
 * This class provides static methods to analyze Maven project files and extract structural information.
 */
public final class PomParser {

    // Prevent instantiation
    private PomParser() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Parse a POM file and extract its Maven Model.
     * 
     * @param pomFile The POM file to parse
     * @return Model object representing the POM file
     * @throws IOException If any I/O errors occur
     */
    public static Model parsePom(File pomFile) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(pomFile)) {
            MavenStaxReader mavenReader = new MavenStaxReader();
            try {
                // Use the MavenStaxReader to read the POM file directly into a Model

                // Return the model as is - no conversion needed
                return mavenReader.read(inputStream);
            } catch (Exception e) {
                throw new IOException("Error parsing POM file: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Extract project coordinates (groupId, artifactId, version) from a POM file.
     * 
     * @param pomFile The POM file to parse
     * @return PomCoordinates object with the project coordinates
     * @throws IOException If any I/O errors occur
     */
    public static PomCoordinates extractCoordinates(File pomFile) throws IOException {
        Model model = parsePom(pomFile);

        String groupId = model.getGroupId();
        String artifactId = model.getArtifactId();
        String version = model.getVersion();

        // If coordinates are not directly specified, they might be inherited from parent
        if (groupId == null && model.getParent() != null) {
            groupId = model.getParent().getGroupId();
        }

        if (version == null && model.getParent() != null) {
            version = model.getParent().getVersion();
        }

        return PomCoordinates.builder()
                .groupId(groupId)
                .artifactId(artifactId)
                .version(version)
                .build();
    }

    /**
     * Extract all dependencies from a POM file.
     * 
     * @param pomFile The POM file to parse
     * @return List of Dependency objects
     * @throws IOException If any I/O errors occur
     */
    public static List<Dependency> extractDependencies(File pomFile) throws IOException {
        Model model = parsePom(pomFile);
        return model.getDependencies();
    }

    /**
     * Extract parent POM information if available.
     * 
     * @param pomFile The POM file to parse
     * @return Parent object or null if no parent exists
     * @throws IOException If any I/O errors occur
     */
    public static Parent extractParentInfo(File pomFile) throws IOException {
        Model model = parsePom(pomFile);
        return model.getParent();
    }

    /**
     * Extract build plugins from a POM file.
     * 
     * @param pomFile The POM file to parse
     * @return List of Plugin objects or empty list if no plugins exist
     * @throws IOException If any I/O errors occur
     */
    public static List<Plugin> extractPlugins(File pomFile) throws IOException {
        Model model = parsePom(pomFile);

        if (model.getBuild() == null || model.getBuild().getPlugins() == null) {
            return new ArrayList<>();
        }

        return model.getBuild().getPlugins();
    }

    /**
     * Extract module names for multi-module projects.
     * 
     * @param pomFile The POM file to parse
     * @return List of module names or empty list if not a multi-module project
     * @throws IOException If any I/O errors occur
     */
    public static List<String> extractModules(File pomFile) throws IOException {
        Model model = parsePom(pomFile);

        if (model == null) {
            return new ArrayList<>();
        }

        return model.getSubprojects();
    }

    /**
     * Find all POM files in a directory and its subdirectories.
     * 
     * @param directory The directory to search
     * @return List of POM files
     */
    public static List<File> findPomFiles(File directory) {
        List<File> pomFiles = new ArrayList<>();
        findPomFilesRecursive(directory, pomFiles);
        return pomFiles;
    }

    /**
     * Recursive helper method to find all POM files.
     * 
     * @param directory The directory to search
     * @param pomFiles List to collect POM files
     */
    private static void findPomFilesRecursive(File directory, List<File> pomFiles) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    findPomFilesRecursive(file, pomFiles);
                } else if (file.getName().equals("pom.xml")) {
                    pomFiles.add(file);
                }
            }
        }
    }
}
