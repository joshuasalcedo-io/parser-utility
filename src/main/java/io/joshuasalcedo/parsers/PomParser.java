package io.joshuasalcedo.parsers;

import io.joshuasalcedo.model.maven.PomCoordinates;
import io.joshuasalcedo.utility.FileUtils;
import io.joshuasalcedo.utility.JsonUtils;
import org.apache.maven.api.model.Dependency;
import org.apache.maven.api.model.Model;
import org.apache.maven.api.model.Parent;
import org.apache.maven.api.model.Plugin;
import org.apache.maven.model.v4.MavenStaxReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        try {
            // Use FileUtils to find all files named "pom.xml"
            return FileUtils.listFilesMatching(
                directory.getAbsolutePath(), 
                false, 
                file -> file.getName().equals("pom.xml")
            );
        } catch (IOException e) {
            System.err.println("Error finding POM files: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Convert a POM Model to JSON.
     *
     * @param model The Maven Model object
     * @return JSON representation of the model
     */
    public static String modelToJson(Model model) {
        return JsonUtils.toPrettyJson(model);
    }

    /**
     * Convert POM coordinates to JSON.
     *
     * @param coordinates The PomCoordinates object
     * @return JSON representation of the coordinates
     */
    public static String coordinatesToJson(PomCoordinates coordinates) {
        return JsonUtils.toPrettyJson(coordinates);
    }

    /**
     * Convert a list of dependencies to JSON.
     *
     * @param dependencies The list of dependencies
     * @return JSON representation of the dependencies
     */
    public static String dependenciesToJson(List<Dependency> dependencies) {
        return JsonUtils.toPrettyJson(dependencies);
    }

    /**
     * Convert parent POM information to JSON.
     *
     * @param parent The Parent object
     * @return JSON representation of the parent information
     */
    public static String parentToJson(Parent parent) {
        return JsonUtils.toPrettyJson(parent);
    }

    /**
     * Convert a list of plugins to JSON.
     *
     * @param plugins The list of plugins
     * @return JSON representation of the plugins
     */
    public static String pluginsToJson(List<Plugin> plugins) {
        return JsonUtils.toPrettyJson(plugins);
    }

    /**
     * Convert a list of modules to JSON.
     *
     * @param modules The list of module names
     * @return JSON representation of the modules
     */
    public static String modulesToJson(List<String> modules) {
        return JsonUtils.toPrettyJson(modules);
    }
}
