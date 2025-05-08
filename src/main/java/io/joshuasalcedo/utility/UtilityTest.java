package io.joshuasalcedo.utility;

import io.joshuasalcedo.model.javafile.ClassStructure;
import io.joshuasalcedo.model.javafile.MethodStructure;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test class for utility tools.
 */
public class UtilityTest {

    public static void main(String[] args) {
        try {
            // Get the current directory
            String testDir = System.getProperty("user.dir");
            System.out.println("Testing with directory: " + testDir);
            
            System.out.println("\n========== FileUtils Test ==========\n");
            testFileUtils(testDir);
            
            System.out.println("\n========== JsonUtils Test ==========\n");
            testJsonUtils();
            
            System.out.println("\n========== All Tests Completed Successfully ==========\n");
        } catch (Exception e) {
            System.err.println("Error during test: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test FileUtils functionality
     */
    private static void testFileUtils(String testDir) throws IOException {
        // Test 1: List all files
        System.out.println("Test 1: Listing all files (not respecting .gitignore)");
        List<File> allFiles = FileUtils.listAllFiles(testDir, false);
        System.out.println("Found " + allFiles.size() + " files");
        printFileSample(allFiles, 5);
        
        // Test 2: List all files respecting .gitignore
        System.out.println("\nTest 2: Listing all files (respecting .gitignore)");
        List<File> nonIgnoredFiles = FileUtils.listAllFiles(testDir, true);
        System.out.println("Found " + nonIgnoredFiles.size() + " files");
        printFileSample(nonIgnoredFiles, 5);
        
        // Test 3: List files by extension
        System.out.println("\nTest 3: Listing Java files");
        List<File> javaFiles = FileUtils.listFilesByExtension(testDir, "java");
        System.out.println("Found " + javaFiles.size() + " Java files");
        printFileSample(javaFiles, 5);
        
        // Test 4: List text files
        System.out.println("\nTest 4: Listing text files");
        List<File> textFiles = FileUtils.listTextFiles(testDir, false);
        System.out.println("Found " + textFiles.size() + " text files");
        printFileSample(textFiles, 5);
        
        // Test 5: List files matching a predicate (files larger than 10KB)
        System.out.println("\nTest 5: Listing files larger than 10KB");
        List<File> largeFiles = FileUtils.listFilesMatching(testDir, false, file -> file.length() > 10 * 1024);
        System.out.println("Found " + largeFiles.size() + " files larger than 10KB");
        printFileSample(largeFiles, 5);
        
        // Test 6: Individual file operations
        System.out.println("\nTest 6: Individual file operations");
        if (!javaFiles.isEmpty()) {
            File sampleFile = javaFiles.get(0);
            System.out.println("Sample file: " + sampleFile.getAbsolutePath());
            System.out.println("  Is text file: " + FileUtils.isTextFile(sampleFile));
            System.out.println("  Base name: " + FileUtils.getFileBaseName(sampleFile));
            System.out.println("  Extension: " + FileUtils.getFileExtension(sampleFile));
            System.out.println("  Size: " + FileUtils.getHumanReadableSize(sampleFile));
            System.out.println("  Is readable: " + FileUtils.isFileReadable(sampleFile));
            System.out.println("  Is writable: " + FileUtils.isFileWritable(sampleFile));
            
            if (FileUtils.isTextFile(sampleFile) && sampleFile.length() < 10_000) {
                String content = FileUtils.readFileAsString(sampleFile);
                System.out.println("  First 100 chars: " + content.substring(0, Math.min(100, content.length())) + "...");
            }
        }
        
        // Test 7: Find binary files
        System.out.println("\nTest 7: Finding binary files");
        List<File> binaryFiles = FileUtils.listFilesMatching(testDir, false, file -> !FileUtils.isTextFile(file));
        System.out.println("Found " + binaryFiles.size() + " binary files");
        printFileSample(binaryFiles, 5);
        
        // Test 8: Create and write to a test file
        System.out.println("\nTest 8: File writing");
        File tempFile = new File("fileutils_test.txt");
        FileUtils.writeStringToFile(tempFile, "This is a test file created by FileUtils\nLine 2\nLine 3");
        System.out.println("Created file: " + tempFile.getAbsolutePath());
        System.out.println("  Content: " + FileUtils.readFileAsString(tempFile));
        
        // Clean up test file
        if (tempFile.exists()) {
            tempFile.delete();
            System.out.println("  Test file deleted");
        }
    }
    
    /**
     * Test JsonUtils functionality
     */
    private static void testJsonUtils() {
        System.out.println("Test 1: Converting object to JSON");
        
        // Create a sample object
        Map<String, Object> sampleObj = new HashMap<>();
        sampleObj.put("name", "Test Object");
        sampleObj.put("value", 42);
        sampleObj.put("active", true);
        
        // Convert to JSON
        String json = JsonUtils.toJson(sampleObj);
        System.out.println("JSON output:");
        System.out.println(json);
        
        // Test 2: Converting a more complex object
        System.out.println("\nTest 2: Converting complex object to JSON");
        ClassStructure classStructure = createSampleClassStructure();
        String classJson = JsonUtils.toJson(classStructure);
        System.out.println("Class structure JSON (preview):");
        System.out.println(classJson.substring(0, Math.min(300, classJson.length())) + "...");
        
        // Test 3: Converting to Map
        System.out.println("\nTest 3: Converting object to Map");
        Map<String, Object> jsonMap = JsonUtils.toJsonMap(classStructure);
        System.out.println("Map contains " + jsonMap.size() + " top-level keys:");
        for (String key : jsonMap.keySet()) {
            Object value = jsonMap.get(key);
            System.out.println("  " + key + ": " + (value == null ? "null" : value.getClass().getSimpleName()));
        }
        
        // Test 4: Pretty printing
        System.out.println("\nTest 4: Pretty printing JSON");
        String prettyJson = JsonUtils.toPrettyJson(classStructure);
        String[] lines = prettyJson.split("\n");
        System.out.println("Pretty JSON has " + lines.length + " lines");
        System.out.println("First 5 lines:");
        for (int i = 0; i < Math.min(5, lines.length); i++) {
            System.out.println("  " + lines[i]);
        }
    }
    
    /**
     * Create a sample ClassStructure for testing
     */
    private static ClassStructure createSampleClassStructure() {
        // Create a simple method
        MethodStructure method = MethodStructure.builder()
                .methodName("testMethod")
                .accessModifier("public")
                .isStatic(false)
                .returnType("void")
                .body("{\n    System.out.println(\"Hello, world!\");\n}")
                .build();
        
        List<MethodStructure> methods = new ArrayList<>();
        methods.add(method);
        
        // Create a class structure
        return ClassStructure.builder()
                .fileName("TestClass.java")
                .packageName("io.joshuasalcedo.test")
                .className("TestClass")
                .classType("class")
                .methods(methods)
                .build();
    }
    
    /**
     * Helper method to print a sample of files for the tests
     */
    private static void printFileSample(List<File> files, int maxFiles) {
        System.out.println("Sample files:");
        int count = 0;
        for (File file : files) {
            if (count++ >= maxFiles) {
                System.out.println("  ... and " + (files.size() - maxFiles) + " more");
                break;
            }
            System.out.println("  " + file.getName() + " (" + FileUtils.getHumanReadableSize(file) + ")");
        }
    }
}