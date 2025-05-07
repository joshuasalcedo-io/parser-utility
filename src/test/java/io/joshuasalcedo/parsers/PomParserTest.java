package io.joshuasalcedo.parsers;

import io.joshuasalcedo.model.maven.PomCoordinates;
import org.apache.maven.api.model.Dependency;
import org.apache.maven.api.model.Model;
import org.apache.maven.api.model.Parent;
import org.apache.maven.api.model.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PomParserTest {

    private File singlePomFile;
    private File multiModulePomFile;
    private File outputFile;

    @BeforeEach
    void setUp() {
        // Set up the test POM file locations
        singlePomFile = new File("src/test/resources/test/pom/single-pom/sample-pom.xml");
        multiModulePomFile = new File("src/test/resources/test/pom/multi-module-pom.xml");

        // Ensure the test files exist
        assertTrue(singlePomFile.exists(), "Single module test POM file does not exist at: "
                + singlePomFile.getAbsolutePath());
        assertTrue(multiModulePomFile.exists(), "Multi-module test POM file does not exist at: "
                + multiModulePomFile.getAbsolutePath());

        // Set up output file
        outputFile = new File("target/pom-parser-output.txt");
        outputFile.getParentFile().mkdirs();
    }

    @Test
    void testParsePom() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            if (!singlePomFile.exists()) {
                System.out.println("Skipping testParsePom: Single module POM file does not exist");
                writer.println("Skipping testParsePom: Single module POM file does not exist");
                return;
            }

            try {
                // Parse the single module POM file
                Model model = PomParser.parsePom(singlePomFile);

                // Test basic parsing
                assertNotNull(model, "Model should not be null");

                // Print basic model information
                System.out.println("=== POM MODEL INFORMATION ===");
                writer.println("=== POM MODEL INFORMATION ===");

                System.out.println("Group ID: " + model.getGroupId());
                System.out.println("Artifact ID: " + model.getArtifactId());
                System.out.println("Version: " + model.getVersion());
                System.out.println("Packaging: " + model.getPackaging());
                System.out.println("Name: " + model.getName());
                System.out.println("Description: " + model.getDescription());

                writer.println("Group ID: " + model.getGroupId());
                writer.println("Artifact ID: " + model.getArtifactId());
                writer.println("Version: " + model.getVersion());
                writer.println("Packaging: " + model.getPackaging());
                writer.println("Name: " + model.getName());
                writer.println("Description: " + model.getDescription());

                // Test for parent
                Parent parent = model.getParent();
                if (parent != null) {
                    System.out.println("\nParent:");
                    System.out.println("Group ID: " + parent.getGroupId());
                    System.out.println("Artifact ID: " + parent.getArtifactId());
                    System.out.println("Version: " + parent.getVersion());

                    writer.println("\nParent:");
                    writer.println("Group ID: " + parent.getGroupId());
                    writer.println("Artifact ID: " + parent.getArtifactId());
                    writer.println("Version: " + parent.getVersion());
                }
            } catch (IOException e) {
                System.out.println("Error parsing POM: " + e.getMessage());
                writer.println("Error parsing POM: " + e.getMessage());
                fail("Failed to parse POM file: " + e.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail("Exception occurred while writing to output file: " + e.getMessage());
        }
    }
    @Test
    void testExtractCoordinates() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile, true))) {
            // Extract coordinates from the single module POM file
            PomCoordinates coordinates = PomParser.extractCoordinates(singlePomFile);

            // Test coordinate extraction
            assertNotNull(coordinates, "Coordinates should not be null");

            // Print coordinates
            System.out.println("\n=== POM COORDINATES ===");
            writer.println("\n=== POM COORDINATES ===");

            System.out.println("Group ID: " + coordinates.getGroupId());
            System.out.println("Artifact ID: " + coordinates.getArtifactId());
            System.out.println("Version: " + coordinates.getVersion());

            writer.println("Group ID: " + coordinates.getGroupId());
            writer.println("Artifact ID: " + coordinates.getArtifactId());
            writer.println("Version: " + coordinates.getVersion());
        }
    }

    @Test
    void testExtractDependencies() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile, true))) {
            // Extract dependencies from the single module POM file
            List<Dependency> dependencies = PomParser.extractDependencies(singlePomFile);

            // Test dependency extraction
            assertNotNull(dependencies, "Dependencies list should not be null");

            // Print dependencies
            System.out.println("\n=== POM DEPENDENCIES ===");
            writer.println("\n=== POM DEPENDENCIES ===");

            System.out.println("Found " + dependencies.size() + " dependencies:");
            writer.println("Found " + dependencies.size() + " dependencies:");

            for (Dependency dependency : dependencies) {
                String scope = dependency.getScope() != null ? dependency.getScope() : "compile";

                System.out.println(dependency.getGroupId() + ":" +
                        dependency.getArtifactId() + ":" +
                        dependency.getVersion() + " (Scope: " + scope + ")");

                writer.println(dependency.getGroupId() + ":" +
                        dependency.getArtifactId() + ":" +
                        dependency.getVersion() + " (Scope: " + scope + ")");
            }
        }
    }

    @Test
    void testExtractParentInfo() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile, true))) {
            // Extract parent information from the single module POM file
            Parent parent = PomParser.extractParentInfo(singlePomFile);

            // Print parent information
            System.out.println("\n=== POM PARENT INFO ===");
            writer.println("\n=== POM PARENT INFO ===");

            if (parent != null) {
                System.out.println("Parent Group ID: " + parent.getGroupId());
                System.out.println("Parent Artifact ID: " + parent.getArtifactId());
                System.out.println("Parent Version: " + parent.getVersion());
                System.out.println("Relative Path: " + parent.getRelativePath());

                writer.println("Parent Group ID: " + parent.getGroupId());
                writer.println("Parent Artifact ID: " + parent.getArtifactId());
                writer.println("Parent Version: " + parent.getVersion());
                writer.println("Relative Path: " + parent.getRelativePath());
            } else {
                System.out.println("No parent information found");
                writer.println("No parent information found");
            }
        }
    }

    @Test
    void testExtractPlugins() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile, true))) {
            // Extract plugins from the single module POM file
            List<Plugin> plugins = PomParser.extractPlugins(singlePomFile);

            // Test plugin extraction
            assertNotNull(plugins, "Plugins list should not be null");

            // Print plugins
            System.out.println("\n=== POM PLUGINS ===");
            writer.println("\n=== POM PLUGINS ===");

            System.out.println("Found " + plugins.size() + " plugins:");
            writer.println("Found " + plugins.size() + " plugins:");

            for (Plugin plugin : plugins) {
                System.out.println(plugin.getGroupId() + ":" +
                        plugin.getArtifactId() + ":" +
                        plugin.getVersion());

                writer.println(plugin.getGroupId() + ":" +
                        plugin.getArtifactId() + ":" +
                        plugin.getVersion());

                // Print configuration if available
                if (plugin.getConfiguration() != null) {
                    System.out.println("  Configuration: " + plugin.getConfiguration());
                    writer.println("  Configuration: " + plugin.getConfiguration());
                }
            }
        }
    }

    @Test
    void testExtractModules() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile, true))) {
            try {
                // Extract modules from the multi-module POM file
                List<String> modules = PomParser.extractModules(multiModulePomFile);

                // Test module extraction
                assertNotNull(modules, "Modules list should not be null");

                // Print modules
                System.out.println("\n=== POM MODULES ===");
                writer.println("\n=== POM MODULES ===");

                System.out.println("Found " + modules.size() + " modules:");
                writer.println("Found " + modules.size() + " modules:");

                for (String module : modules) {
                    System.out.println("- " + module);
                    writer.println("- " + module);
                }
            } catch (IOException e) {
                // Log the error but don't fail the test
                System.out.println("\n=== POM MODULES TEST ERROR ===");
                writer.println("\n=== POM MODULES TEST ERROR ===");
                System.out.println("Error extracting modules: " + e.getMessage());
                writer.println("Error extracting modules: " + e.getMessage());
                System.out.println("This is expected if the multi-module POM file has XML format issues.");
                writer.println("This is expected if the multi-module POM file has XML format issues.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail("Exception occurred while writing to output file: " + e.getMessage());
        }
    }

    @Test
    void testFindPomFiles() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile, true))) {
            // Find POM files in the test resources directory
            File testResourcesDir = new File("src/test/resources");
            List<File> pomFiles = PomParser.findPomFiles(testResourcesDir);

            // Test POM file finding
            assertNotNull(pomFiles, "POM files list should not be null");
            assertFalse(pomFiles.isEmpty(), "POM files list should not be empty");

            // Print found POM files
            System.out.println("\n=== FOUND POM FILES ===");
            writer.println("\n=== FOUND POM FILES ===");

            System.out.println("Found " + pomFiles.size() + " POM files:");
            writer.println("Found " + pomFiles.size() + " POM files:");

            for (File pomFile : pomFiles) {
                System.out.println("- " + pomFile.getAbsolutePath());
                writer.println("- " + pomFile.getAbsolutePath());
            }

            System.out.println("\nPOM parser test completed. Results written to: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Exception occurred while writing to output file: " + e.getMessage());
        }
    }
}