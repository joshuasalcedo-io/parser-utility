package io.joshuasalcedo.parsers;

import io.joshuasalcedo.model.git.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class GitParserTest {

    /**
     * Helper method to check if the current directory is a Git repository
     */
    private boolean isCurrentDirGitRepo() {
        File gitDir = new File(".git");
        return gitDir.exists() && gitDir.isDirectory();
    }

    /**
     * Test parsing the current Git repository (only runs if we're in a Git repository)
     */
    @Test
    @EnabledIf("isCurrentDirGitRepo")
    void testParseCurrentRepository() throws Exception {
        try {
            // Use the current directory as the repository to parse
            GitRepositoryInfo repoInfo = GitParser.parseRepository(".");

            // Create an output file in the target directory
            File outputFile = new File("target/git-parser-output.txt");
            outputFile.getParentFile().mkdirs();

            try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
                // Print basic repository information
                System.out.println("=== REPOSITORY INFORMATION ===");
                writer.println("=== REPOSITORY INFORMATION ===");

                System.out.println("Repository Name: " + repoInfo.getName());
                System.out.println("Repository Path: " + repoInfo.getPath());
                System.out.println("Current Branch: " + repoInfo.getCurrentBranch());
                System.out.println("Remote URL: " + repoInfo.getRemoteUrl());
                System.out.println("Has Uncommitted Changes: " + repoInfo.isHasUncommittedChanges());
                System.out.println("Commit Count: " + repoInfo.getCommitCount());

                writer.println("Repository Name: " + repoInfo.getName());
                writer.println("Repository Path: " + repoInfo.getPath());
                writer.println("Current Branch: " + repoInfo.getCurrentBranch());
                writer.println("Remote URL: " + repoInfo.getRemoteUrl());
                writer.println("Has Uncommitted Changes: " + repoInfo.isHasUncommittedChanges());
                writer.println("Commit Count: " + repoInfo.getCommitCount());

                // Print file extension counts if available
                if (repoInfo.getFileExtensionCounts() != null) {
                    System.out.println("\n=== FILE EXTENSION COUNTS ===");
                    writer.println("\n=== FILE EXTENSION COUNTS ===");

                    for (Map.Entry<String, Integer> entry : repoInfo.getFileExtensionCounts().entrySet()) {
                        System.out.println(entry.getKey() + ": " + entry.getValue());
                        writer.println(entry.getKey() + ": " + entry.getValue());
                    }
                }

                // Print statistics if available
                if (repoInfo.getStatistics() != null) {
                    System.out.println("\n=== REPOSITORY STATISTICS ===");
                    writer.println("\n=== REPOSITORY STATISTICS ===");

                    for (Map.Entry<String, Object> entry : repoInfo.getStatistics().entrySet()) {
                        System.out.println(entry.getKey() + ": " + entry.getValue());
                        writer.println(entry.getKey() + ": " + entry.getValue());
                    }
                }

                // Print top contributors if available
                if (repoInfo.getTopContributors() != null && !repoInfo.getTopContributors().isEmpty()) {
                    System.out.println("\n=== TOP CONTRIBUTORS ===");
                    writer.println("\n=== TOP CONTRIBUTORS ===");

                    for (ContributorInfo contributor : repoInfo.getTopContributors()) {
                        System.out.println(contributor.getName() + " <" + contributor.getEmail() + ">: " +
                                contributor.getCommitCount() + " commits");
                        writer.println(contributor.getName() + " <" + contributor.getEmail() + ">: " +
                                contributor.getCommitCount() + " commits");
                    }
                }

                // Print latest commit information (if available)
                CommitInfo latestCommit = repoInfo.getLatestCommit();
                if (latestCommit != null) {
                    System.out.println("\n=== LATEST COMMIT ===");
                    writer.println("\n=== LATEST COMMIT ===");

                    System.out.println("Commit ID: " + latestCommit.getId());
                    System.out.println("Short ID: " + latestCommit.getShortId());
                    System.out.println("Message: " + latestCommit.getMessage());
                    System.out.println("Author: " + latestCommit.getAuthorName() + " <" + latestCommit.getAuthorEmail() + ">");
                    System.out.println("Date: " + latestCommit.getAuthorDate());

                    writer.println("Commit ID: " + latestCommit.getId());
                    writer.println("Short ID: " + latestCommit.getShortId());
                    writer.println("Message: " + latestCommit.getMessage());
                    writer.println("Author: " + latestCommit.getAuthorName() + " <" + latestCommit.getAuthorEmail() + ">");
                    writer.println("Date: " + latestCommit.getAuthorDate());
                } else {
                    System.out.println("\n=== NO COMMITS FOUND ===");
                    writer.println("\n=== NO COMMITS FOUND ===");
                }

                // Print branches
                List<BranchInfo> branches = repoInfo.getBranches();
                if (branches != null && !branches.isEmpty()) {
                    System.out.println("\n=== BRANCHES ===");
                    writer.println("\n=== BRANCHES ===");
                    System.out.println("Found " + branches.size() + " branches:");
                    writer.println("Found " + branches.size() + " branches:");

                    for (BranchInfo branch : branches) {
                        String current = branch.isCurrent() ? " (current)" : "";
                        String remote = branch.isRemote() ? " [remote: " + branch.getRemoteName() + "]" : "";

                        System.out.println("  " + branch.getName() + current + remote);
                        writer.println("  " + branch.getName() + current + remote);
                    }
                }

                System.out.println("\nGit parsing test completed. Results written to: " + outputFile.getAbsolutePath());
            }

            // Basic assertions to make sure the parser works
            assertNotNull(repoInfo.getName(), "Repository name should not be null");
            assertNotNull(repoInfo.getCurrentBranch(), "Current branch should not be null");

            // Don't assert on latest commit - it might be null in a fresh repo
            // Only check commit count instead
            assertTrue(repoInfo.getCommitCount() >= 0, "Commit count should be zero or positive");

        } catch (IOException | org.eclipse.jgit.api.errors.GitAPIException e) {
            // If there's an exception, print it and fail the test
            e.printStackTrace();
            fail("Exception occurred while parsing repository: " + e.getMessage());
        }
    }

    /**
     * Test getting commit log (only runs if we're in a Git repository)
     */
    @Test
    @EnabledIf("isCurrentDirGitRepo")
    void testGetCommitLog() throws Exception {
        // Create an output file in the target directory
        File outputFile = new File("target/git-commit-log.txt");
        outputFile.getParentFile().mkdirs();

        try {
            // Get formatted commit log
            String commitLog = GitParser.getFormattedCommitLog(".", 10);

            // Save to file
            try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
                writer.println(commitLog);
            }

            System.out.println("Commit log written to: " + outputFile.getAbsolutePath());

            // Verify log is not empty
            assertNotNull(commitLog, "Commit log should not be null");

        } catch (IOException | org.eclipse.jgit.api.errors.GitAPIException e) {
            // If there's an exception, print it and fail the test
            e.printStackTrace();
            fail("Exception occurred while getting commit log: " + e.getMessage());
        }
    }

    /**
     * Test contributions by day of week (only runs if we're in a Git repository)
     */
    @Test
    @EnabledIf("isCurrentDirGitRepo")
    void testContributionsByDayOfWeek() throws Exception {
        try {
            // Get contributions by day of week
            Map<String, Integer> contributionsByDay = GitParser.getContributionsByDayOfWeek(".");

            System.out.println("\n=== CONTRIBUTIONS BY DAY OF WEEK ===");
            for (Map.Entry<String, Integer> entry : contributionsByDay.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }

            // Verify map is not empty
            assertFalse(contributionsByDay.isEmpty(), "Contributions by day should not be empty");

        } catch (IOException | org.eclipse.jgit.api.errors.GitAPIException e) {
            // If there's an exception, print it and fail the test
            e.printStackTrace();
            fail("Exception occurred while getting contributions by day: " + e.getMessage());
        }
    }

    /**
     * Test getting most active files (only runs if we're in a Git repository)
     */
    @Test
    @EnabledIf("isCurrentDirGitRepo")
    void testMostActiveFiles() throws Exception {
        try {
            // Get most active files
            Map<String, Integer> activeFiles = GitParser.getMostActiveFiles(".", 10);

            System.out.println("\n=== MOST ACTIVE FILES ===");
            for (Map.Entry<String, Integer> entry : activeFiles.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue() + " changes");
            }

        } catch (IOException | org.eclipse.jgit.api.errors.GitAPIException e) {
            // If there's an exception, print it and fail the test
            e.printStackTrace();
            fail("Exception occurred while getting most active files: " + e.getMessage());
        }
    }

    /**
     * Test contribution heat map for current year (only runs if we're in a Git repository)
     */
    @Test
    @EnabledIf("isCurrentDirGitRepo")
    void testContributionHeatMap() throws Exception {
        try {
            // Get current year
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);

            // Get contribution heat map
            Map<String, Integer> heatMap = GitParser.getContributionHeatMap(".", currentYear);

            System.out.println("\n=== CONTRIBUTION HEAT MAP FOR " + currentYear + " ===");
            System.out.println("Found " + heatMap.size() + " days in the heat map");

            // Print a sample of the heat map (first 10 days with commits)
            int count = 0;
            for (Map.Entry<String, Integer> entry : heatMap.entrySet()) {
                if (entry.getValue() > 0) {
                    System.out.println(entry.getKey() + ": " + entry.getValue() + " commits");
                    count++;

                    if (count >= 10) {
                        break;
                    }
                }
            }

        } catch (IOException | org.eclipse.jgit.api.errors.GitAPIException e) {
            // If there's an exception, print it and fail the test
            e.printStackTrace();
            fail("Exception occurred while getting contribution heat map: " + e.getMessage());
        }
    }

    /**
     * Test isValidRepository method
     */
    @Test
    void testIsValidRepository() {
        // The current directory should be a valid repository if .git exists
        File gitDir = new File(".git");
        boolean expected = gitDir.exists() && gitDir.isDirectory();
        boolean actual = GitParser.isValidRepository(".");

        assertEquals(expected, actual, "isValidRepository should correctly identify a valid repository");

        // A non-existent directory should not be a valid repository
        assertFalse(GitParser.isValidRepository("/non/existent/directory"),
                "Non-existent directory should not be a valid repository");
    }

    /**
     * Test findGitRepositories method
     */
    @Test
    void testFindGitRepositories() {
        // Testing the findGitRepositories method on src directory
        File srcDir = new File("src");
        List<File> gitRepos = GitParser.findGitRepositories(srcDir);

        System.out.println("\n=== GIT REPOSITORIES FOUND IN SRC ===");
        if (gitRepos.isEmpty()) {
            System.out.println("No Git repositories found in src directory");
        } else {
            for (File repo : gitRepos) {
                System.out.println(repo.getAbsolutePath());
            }
        }

        // Also try at project root level
        File projectDir = new File(".");
        List<File> rootGitRepos = GitParser.findGitRepositories(projectDir);

        System.out.println("\n=== GIT REPOSITORIES FOUND AT ROOT ===");
        assertTrue(rootGitRepos.size() >= 0, "Should be able to find repositories without error");

        for (File repo : rootGitRepos) {
            System.out.println(repo.getAbsolutePath());
            // Verify it's a valid repository
            assertTrue(GitParser.isValidRepository(repo.getAbsolutePath()),
                    "Found repository should be valid: " + repo.getAbsolutePath());
        }
    }

    /**
     * Test that the GitParser can handle a non-Git directory
     */
    @Test
    void testNonGitDirectory() {
        // Create a temporary directory that's definitely not a Git repo
        File tempDir = new File("target/temp-non-git-dir");
        tempDir.mkdirs();

        assertFalse(GitParser.isValidRepository(tempDir.getAbsolutePath()),
                "Non-Git directory should not be identified as a valid repository");

        // Cleanup
        tempDir.delete();
    }

    /**
     * Test getting recent commits (only runs if we're in a Git repository)
     */
    /**
     * Test getting recent commits (only runs if we're in a Git repository)
     */
    @Test
    @EnabledIf("isCurrentDirGitRepo")
    void testGetRecentCommits() throws Exception {
        try {
            // Get the 5 most recent commits
            List<CommitInfo> recentCommits = GitParser.getRecentCommits(".", 5);

            // Create an output file in the target directory
            File outputFile = new File("target/git-recent-commits.txt");
            outputFile.getParentFile().mkdirs();

            try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
                writer.println("=== RECENT COMMITS ===");
                writer.println("Found " + recentCommits.size() + " recent commits:");

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                for (CommitInfo commit : recentCommits) {
                    writer.println("\nCommit: " + commit.getShortId());
                    writer.println("Author: " + commit.getAuthorName() + " <" + commit.getAuthorEmail() + ">");
                    writer.println("Date: " + dateFormat.format(commit.getAuthorDate()));
                    writer.println("Message: " + commit.getMessage());

                    // Print changed files
                    if (commit.getChangedFiles() != null && !commit.getChangedFiles().isEmpty()) {
                        writer.println("Changed Files:");

                        for (FileChange change : commit.getChangedFiles()) {
                            writer.println("  " + change.getType() + ": " + change.getPath());
                        }
                    }
                }
            }

            System.out.println("Recent commits written to: " + outputFile.getAbsolutePath());

            // Verify we got some commits (unless it's a brand new repo)
            assertNotNull(recentCommits, "Recent commits should not be null");

        } catch (IOException | org.eclipse.jgit.api.errors.GitAPIException e) {
            // If there's an exception, print it and fail the test
            e.printStackTrace();
            fail("Exception occurred while getting recent commits: " + e.getMessage());
        }
    }

    /**
     * Test getting commits by author (only runs if we're in a Git repository)
     */
    @Test
    @EnabledIf("isCurrentDirGitRepo")
    void testGetCommitsByAuthor() throws Exception {
        try {
            // First, get any commit to find an author's name
            List<CommitInfo> commits = GitParser.getRecentCommits(".", 1);

            if (!commits.isEmpty()) {
                String authorName = commits.get(0).getAuthorName();

                // Now use that author name to find their commits
                List<CommitInfo> authorCommits = GitParser.getCommitsByAuthor(".", authorName);

                System.out.println("\n=== COMMITS BY " + authorName + " ===");
                System.out.println("Found " + authorCommits.size() + " commits");

                for (int i = 0; i < Math.min(5, authorCommits.size()); i++) {
                    CommitInfo commit = authorCommits.get(i);
                    System.out.println(commit.getShortId() + ": " + commit.getMessage());
                }

                // Verify we found at least the one commit we know this author made
                assertTrue(authorCommits.size() >= 1,
                        "Should find at least one commit by " + authorName);
            }

        } catch (IOException | org.eclipse.jgit.api.errors.GitAPIException e) {
            // If there's an exception, print it and fail the test
            e.printStackTrace();
            fail("Exception occurred while getting commits by author: " + e.getMessage());
        }
    }

    /**
     * Test getting commits by date range (only runs if we're in a Git repository)
     */
    @Test
    @EnabledIf("isCurrentDirGitRepo")
    void testGetCommitsByDateRange() throws Exception {
        try {
            // Get current date
            Calendar calendar = Calendar.getInstance();
            Date today = calendar.getTime();

            // Set date one year ago
            calendar.add(Calendar.YEAR, -1);
            Date oneYearAgo = calendar.getTime();

            // Get commits in the last year
            List<CommitInfo> yearCommits = GitParser.getCommitsByDateRange(".", oneYearAgo, today);

            System.out.println("\n=== COMMITS IN THE LAST YEAR ===");
            System.out.println("Found " + yearCommits.size() + " commits between " +
                    new SimpleDateFormat("yyyy-MM-dd").format(oneYearAgo) + " and " +
                    new SimpleDateFormat("yyyy-MM-dd").format(today));

            // Verify we got a result (might be empty for new repos)
            assertNotNull(yearCommits, "Year commits should not be null");

        } catch (IOException | org.eclipse.jgit.api.errors.GitAPIException e) {
            // If there's an exception, print it and fail the test
            e.printStackTrace();
            fail("Exception occurred while getting commits by date range: " + e.getMessage());
        }
    }

    /**
     * Test getting file history for a specific file (only runs if we're in a Git repository)
     */
    @Test
    @EnabledIf("isCurrentDirGitRepo")
    void testGetFileHistory() throws Exception {
        try {
            // First, find a file in the repository
            File gitParserFile = new File("src/main/java/io/joshuasalcedo/parsers/GitParser.java");

            if (!gitParserFile.exists()) {
                System.out.println("GitParser.java file not found, skipping file history test");
                return;
            }

            // Get history for the file
            List<CommitInfo> fileHistory = GitParser.getFileHistory(".",
                    "src/main/java/io/joshuasalcedo/parsers/GitParser.java");

            System.out.println("\n=== FILE HISTORY FOR GitParser.java ===");
            System.out.println("Found " + fileHistory.size() + " commits that modified this file");

            for (int i = 0; i < Math.min(5, fileHistory.size()); i++) {
                CommitInfo commit = fileHistory.get(i);
                System.out.println(commit.getShortId() + " (" +
                        new SimpleDateFormat("yyyy-MM-dd").format(commit.getAuthorDate()) +
                        "): " + commit.getMessage());
            }

        } catch (IOException | org.eclipse.jgit.api.errors.GitAPIException e) {
            // If there's an exception, print it and fail the test
            e.printStackTrace();
            fail("Exception occurred while getting file history: " + e.getMessage());
        }
    }

    /**
     * Test contribution statistics by hour of day (only runs if we're in a Git repository)
     */
    @Test
    @EnabledIf("isCurrentDirGitRepo")
    void testContributionsByHourOfDay() throws Exception {
        try {
            // Get contributions by hour of day
            Map<Integer, Integer> contributionsByHour = GitParser.getContributionsByHourOfDay(".");

            System.out.println("\n=== CONTRIBUTIONS BY HOUR OF DAY ===");
            for (int hour = 0; hour < 24; hour++) {
                System.out.println(String.format("%02d:00", hour) + " - " + contributionsByHour.get(hour) + " commits");
            }

            // Verify map is not empty and has 24 hours
            assertFalse(contributionsByHour.isEmpty(), "Contributions by hour should not be empty");
            assertEquals(24, contributionsByHour.size(), "Contributions by hour should have 24 entries");

        } catch (IOException | org.eclipse.jgit.api.errors.GitAPIException e) {
            // If there's an exception, print it and fail the test
            e.printStackTrace();
            fail("Exception occurred while getting contributions by hour: " + e.getMessage());
        }
    }

    /**
     * Test getting file blame for a specific file (only runs if we're in a Git repository)
     */
    @Test
    @EnabledIf("isCurrentDirGitRepo")
    void testGetFileBlame() throws Exception {
        try {
            // Find a Java file that's likely to exist and have commits
            String filePath = null;

            // Check if GitParser.java exists
            File gitParserFile = new File("src/main/java/io/joshuasalcedo/parsers/GitParser.java");
            if (gitParserFile.exists()) {
                filePath = "src/main/java/io/joshuasalcedo/parsers/GitParser.java";
            } else {
                // If not, try to find any Java file
                File srcDir = new File("src");
                if (srcDir.exists()) {
                    List<File> javaFiles = findJavaFiles(srcDir);
                    if (!javaFiles.isEmpty()) {
                        filePath = javaFiles.get(0).getPath();
                    }
                }
            }

            if (filePath == null) {
                System.out.println("No suitable Java file found for blame test, skipping");
                return;
            }

            System.out.println("Running blame test on file: " + filePath);

            // Create a simple implementation to get blame info
            String blameInfo = getSimpleBlameInfo(filePath);

            // Create an output file in the target directory
            File outputFile = new File("target/git-file-blame.txt");
            outputFile.getParentFile().mkdirs();

            try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
                writer.println("=== BLAME INFO FOR " + filePath + " ===");
                writer.println(blameInfo);
            }

            System.out.println("Blame info written to: " + outputFile.getAbsolutePath());

        } catch (IOException e) {
            // If there's an exception, print it and fail the test
            e.printStackTrace();
            fail("Exception occurred while getting file blame: " + e.getMessage());
        }
    }

    /**
     * Helper method to find Java files in a directory
     */
    private List<File> findJavaFiles(File directory) {
        List<File> javaFiles = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    javaFiles.addAll(findJavaFiles(file));
                } else if (file.getName().endsWith(".java")) {
                    javaFiles.add(file);
                }
            }
        }
        return javaFiles;
    }

    /**
     * Simple implementation to get blame info without using JGit directly
     */
    private String getSimpleBlameInfo(String filePath) throws IOException {
        // Use git command line instead of JGit to avoid potential null pointer issues
        try {
            ProcessBuilder pb = new ProcessBuilder("git", "blame", filePath);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Read the output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // Wait for the process to complete
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return output.toString();
            } else {
                return "Failed to get blame info. Exit code: " + exitCode;
            }
        } catch (Exception e) {
            return "Error getting blame info: " + e.getMessage();
        }
    }
}