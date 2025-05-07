package io.joshuasalcedo.parsers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the Parser facade.
 */
class ParserTest {

    /**
     * Check if the current directory is a Git repository.
     * This is used to conditionally enable tests that require a Git repository.
     *
     * @return true if the current directory is a Git repository
     */
    private boolean isCurrentDirGitRepo() {
        File gitDir = new File(".git");
        return gitDir.exists() && gitDir.isDirectory();
    }

    /**
     * Test the Parser.parse.git method with the current directory.
     * This test is only enabled if the current directory is a Git repository.
     */
    @Test
    @EnabledIf("isCurrentDirGitRepo")
    void testParseGit() {
        // Get the current directory
        String currentDir = System.getProperty("user.dir");

        // Parse the Git repository
        Map<String, Object> resultMap = Parser.parse.git(currentDir);

        // Verify the result is not null
        assertNotNull(resultMap, "Result map should not be null");
        assertFalse(resultMap.isEmpty(), "Result map should not be empty");

        // Verify the map contains the expected Git repository information
        assertTrue(resultMap.containsKey("gitRepository"), "Result should contain gitRepository key");

        // Print the result for inspection
        System.out.println("Git Repository Result:");
        System.out.println(resultMap);
    }

    /**
     * Test the Parser.parse.git method with a non-existent directory.
     */
    @Test
    void testParseGitNonExistentDirectory() {
        // Parse a non-existent directory
        Map<String, Object> resultMap = Parser.parse.git("non-existent-directory");

        // Verify the result is not null
        assertNotNull(resultMap, "Result map should not be null");
        assertFalse(resultMap.isEmpty(), "Result map should not be empty");

        // Verify the map contains an error message
        assertTrue(resultMap.containsKey("error"), "Result should contain error key");
        assertTrue(((String)resultMap.get("error")).contains("Directory does not exist"), 
                "Error message should indicate directory does not exist");

        // Print the result for inspection
        System.out.println("Non-existent Directory Result:");
        System.out.println(resultMap);
    }

    /**
     * Test the Parser.parse.git method with a directory that is not a Git repository.
     */
    @Test
    void testParseGitNonGitDirectory() throws IOException {
        // Create a temporary directory
        File tempDir = File.createTempFile("temp", "dir");
        tempDir.delete();
        tempDir.mkdir();
        tempDir.deleteOnExit();

        // Parse the non-Git directory
        Map<String, Object> resultMap = Parser.parse.git(tempDir.getAbsolutePath());

        // Verify the result is not null
        assertNotNull(resultMap, "Result map should not be null");
        assertFalse(resultMap.isEmpty(), "Result map should not be empty");

        // Verify the map contains an error message
        assertTrue(resultMap.containsKey("error"), "Result should contain error key");
        assertTrue(((String)resultMap.get("error")).contains("Not a valid Git repository"), 
                "Error message should indicate not a valid Git repository");

        // Print the result for inspection
        System.out.println("Non-Git Directory Result:");
        System.out.println(resultMap);
    }

    /**
     * Test the Parser.parse.all method with the current directory.
     * This test parses the current directory using all available parsers.
     */
    @Test
    void testParseAll() {
        // Get the current directory
        String currentDir = System.getProperty("user.dir");

        // Parse the directory using all parsers
        Map<String, Object> resultMap = Parser.parse.all(currentDir);

        // Verify the result is not null
        assertNotNull(resultMap, "Result map should not be null");
        assertFalse(resultMap.isEmpty(), "Result map should not be empty");

        // Verify the map contains file statistics
        assertTrue(resultMap.containsKey("fileStatistics"), "Result should contain fileStatistics key");

        // Print the result for inspection
        System.out.println("All Parsers Result:");
        System.out.println(resultMap);
    }

    /**
     * Test the Parser.parse.java method with the current directory.
     */
    @Test
    void testParseJava() {
        // Get the current directory
        String currentDir = System.getProperty("user.dir");

        // Parse the Java files in the directory
        Map<String, Object> resultMap = Parser.parse.java(currentDir);

        // Verify the result is not null
        assertNotNull(resultMap, "Result map should not be null");
        assertFalse(resultMap.isEmpty(), "Result map should not be empty");

        // Verify the map contains Java files information
        assertTrue(resultMap.containsKey("javaFiles"), "Result should contain javaFiles key");

        // Print the result for inspection
        System.out.println("Java Parser Result:");
        System.out.println(resultMap);
    }

    /**
     * Test the Parser.parse.pom method with the current directory.
     */
    @Test
    void testParsePom() {
        // Get the current directory
        String currentDir = System.getProperty("user.dir");

        // Parse the POM files in the directory
        Map<String, Object> resultMap = Parser.parse.pom(currentDir);

        // Verify the result is not null
        assertNotNull(resultMap, "Result map should not be null");
        assertFalse(resultMap.isEmpty(), "Result map should not be empty");

        // Verify the map contains POM files information
        assertTrue(resultMap.containsKey("pomFiles"), "Result should contain pomFiles key");

        // Print the result for inspection
        System.out.println("POM Parser Result:");
        System.out.println(resultMap);
    }

    /**
     * Test the Parser.parse.html method with the current directory.
     */
    @Test
    void testParseHtml() {
        // Get the current directory
        String currentDir = System.getProperty("user.dir");

        // Parse the HTML files in the directory
        Map<String, Object> resultMap = Parser.parse.html(currentDir);

        // Verify the result is not null
        assertNotNull(resultMap, "Result map should not be null");
        assertFalse(resultMap.isEmpty(), "Result map should not be empty");

        // Verify the map contains HTML files information
        assertTrue(resultMap.containsKey("htmlFiles"), "Result should contain htmlFiles key");

        // Print the result for inspection
        System.out.println("HTML Parser Result:");
        System.out.println(resultMap);
    }
}
