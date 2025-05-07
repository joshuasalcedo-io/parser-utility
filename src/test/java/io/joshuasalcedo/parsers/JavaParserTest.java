package io.joshuasalcedo.parsers;

import io.joshuasalcedo.model.javafile.ClassStructure;
import io.joshuasalcedo.model.javafile.JavadocStructure;
import io.joshuasalcedo.model.javafile.JavadocTag;
import io.joshuasalcedo.model.javafile.MethodStructure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JavaParserTest {

    private File testJavaFile;
    private File outputFile;

    @BeforeEach
    void setUp() {
        // Set up the test Java file location
        testJavaFile = new File("src/test/resources/test/java/TestSample.java");
        assertTrue(testJavaFile.exists(), "Test Java file does not exist");

        // Set up output file
        outputFile = new File("src/test/resources/test/java/java-parser-output.txt");
    }

    @Test
    void testParseJavaFile() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            // Read the Java file content
            String content = new String(Files.readAllBytes(testJavaFile.toPath()));

            // Extract package name
            String packageName = JavaParser.extractPackageName(content);
            System.out.println("=== PACKAGE NAME ===");
            System.out.println(packageName);
            writer.println("=== PACKAGE NAME ===");
            writer.println(packageName);

            // Extract class JavaDoc
            JavadocStructure classJavadoc = JavaParser.extractClassJavadoc(content);
            System.out.println("\n=== CLASS JAVADOC ===");
            writer.println("\n=== CLASS JAVADOC ===");
            if (classJavadoc != null) {
                System.out.println("Description: " + classJavadoc.getDescription());
                writer.println("Description: " + classJavadoc.getDescription());

                System.out.println("\nJavaDoc Tags:");
                writer.println("\nJavaDoc Tags:");
                for (JavadocTag tag : classJavadoc.getTags()) {
                    System.out.println("@" + tag.getName() + " " + tag.getValue());
                    writer.println("@" + tag.getName() + " " + tag.getValue());
                }
            } else {
                System.out.println("No class JavaDoc found");
                writer.println("No class JavaDoc found");
            }

            // Extract class comments
            List<String> classComments = JavaParser.extractClassComments(content);
            System.out.println("\n=== CLASS COMMENTS ===");
            writer.println("\n=== CLASS COMMENTS ===");
            if (!classComments.isEmpty()) {
                for (String comment : classComments) {
                    System.out.println(comment);
                    writer.println(comment);
                }
            } else {
                System.out.println("No class comments found");
                writer.println("No class comments found");
            }

            // Extract class structure
            ClassStructure classStructure = JavaParser.extractClassStructure(testJavaFile.getName(), content);
            System.out.println("\n=== CLASS STRUCTURE ===");
            writer.println("\n=== CLASS STRUCTURE ===");
            System.out.println("File Name: " + classStructure.getFileName());
            System.out.println("Package Name: " + classStructure.getPackageName());
            System.out.println("Class Name: " + classStructure.getClassName());
            System.out.println("Class Type: " + classStructure.getClassType());
            writer.println("File Name: " + classStructure.getFileName());
            writer.println("Package Name: " + classStructure.getPackageName());
            writer.println("Class Name: " + classStructure.getClassName());
            writer.println("Class Type: " + classStructure.getClassType());

            // Extract methods
            List<MethodStructure> methods = JavaParser.extractMethods(content);
            System.out.println("\n=== METHODS ===");
            writer.println("\n=== METHODS ===");
            System.out.println("Found " + methods.size() + " methods:");
            writer.println("Found " + methods.size() + " methods:");

            for (MethodStructure method : methods) {
                System.out.println("\nMethod: " + method.getMethodName());
                System.out.println("Access Modifier: " + method.getAccessModifier());
                System.out.println("Static: " + method.isStatic());
                System.out.println("Return Type: " + method.getReturnType());
                System.out.println("Parameters: " + method.getParameters().size());

                writer.println("\nMethod: " + method.getMethodName());
                writer.println("Access Modifier: " + method.getAccessModifier());
                writer.println("Static: " + method.isStatic());
                writer.println("Return Type: " + method.getReturnType());
                writer.println("Parameters: " + method.getParameters().size());

                // Print parameters
                if (!method.getParameters().isEmpty()) {
                    System.out.println("Parameter list:");
                    writer.println("Parameter list:");
                    method.getParameters().forEach(param -> {
                        System.out.println("  - " + param.getType() + " " + param.getName());
                        writer.println("  - " + param.getType() + " " + param.getName());
                    });
                }

                // Print method JavaDoc if available
                if (method.getJavadoc() != null) {
                    System.out.println("Method JavaDoc:");
                    writer.println("Method JavaDoc:");
                    System.out.println("  Description: " + method.getJavadoc().getDescription());
                    writer.println("  Description: " + method.getJavadoc().getDescription());

                    if (!method.getJavadoc().getTags().isEmpty()) {
                        System.out.println("  Tags:");
                        writer.println("  Tags:");
                        method.getJavadoc().getTags().forEach(tag -> {
                            System.out.println("    @" + tag.getName() + " " + tag.getValue());
                            writer.println("    @" + tag.getName() + " " + tag.getValue());
                        });
                    }
                }

                // Print method body (truncated for readability)
                if (method.getBody() != null && !method.getBody().isEmpty()) {
                    String bodyPreview = method.getBody().length() > 100
                            ? method.getBody().substring(0, 100) + "..."
                            : method.getBody();
                    System.out.println("Body preview: " + bodyPreview.replace("\n", " "));
                    writer.println("Body preview: " + bodyPreview.replace("\n", " "));
                }
            }

            System.out.println("\nJava parsing test completed. Results written to: " + outputFile.getAbsolutePath());
        }
    }

    // Optional: Add test for finding all Java files in a directory
    @Test
    void testFindJavaFiles() {
        File testDir = new File("src/test/resources/test/java");
        List<File> javaFiles = JavaParser.findJavaFiles(testDir);

        System.out.println("\n=== JAVA FILES IN DIRECTORY ===");
        System.out.println("Found " + javaFiles.size() + " Java files:");

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile, true))) {
            writer.println("\n=== JAVA FILES IN DIRECTORY ===");
            writer.println("Found " + javaFiles.size() + " Java files:");

            for (File file : javaFiles) {
                System.out.println(file.getName());
                writer.println(file.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}