package io.joshuasalcedo.parsers;

import io.joshuasalcedo.model.git.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GitParserTest {

    private static File repoDir;
    private static File outputFile;

    @BeforeAll
    static void setUp() throws Exception {
        // Set up the test Git repository location
        repoDir = new File("src/test/resources/test/repo");

        // Create the output file
        outputFile = new File("src/test/resources/test/repo/git-parser-output.txt");

        // Check if the repository exists, if not, run the script to create it
        if (!repoDir.exists() || !new File(repoDir, ".git").exists()) {
            System.out.println("Test Git repository not found, creating it...");

            // Execute the repo-test.sh script
            File scriptFile = new File("src/test/resources/static/repo-test.sh");
            if (!scriptFile.exists()) {
                fail("Repository creation script not found at: " + scriptFile.getAbsolutePath());
            }

            // Make the script executable
            scriptFile.setExecutable(true);

            // Create the parent directory if it doesn't exist
            repoDir.getParentFile().mkdirs();

            // Execute the script
            ProcessBuilder pb = new ProcessBuilder("/bin/bash", scriptFile.getAbsolutePath());
            pb.inheritIO(); // Redirect script output to console
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                fail("Repository creation script failed with exit code: " + exitCode);
            }

            System.out.println("Test Git repository created successfully");
        }
    }

    @Test
    void testParseRepository() throws Exception {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            // Parse the repository
            GitRepositoryInfo repoInfo = GitParser.parseRepository(repoDir.getAbsolutePath());

            // Print and assert basic repository information
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

            // Print latest commit information
            CommitInfo latestCommit = repoInfo.getLatestCommit();
            if (latestCommit != null) {
                System.out.println("\n=== LATEST COMMIT ===");
                writer.println("\n=== LATEST COMMIT ===");

                System.out.println("Commit ID: " + latestCommit.getId());
                System.out.println("Short ID: " + latestCommit.getShortId());
                System.out.println("Message: " + latestCommit.getMessage());
                System.out.println("Author: " + latestCommit.getAuthorName() + " <" + latestCommit.getAuthorEmail() + ">");
                System.out.println("Date: " + latestCommit.getAuthorDate());
                System.out.println("Committer: " + latestCommit.getCommitterName() + " <" + latestCommit.getCommitterEmail() + ">");
                System.out.println("Parent IDs: " + latestCommit.getParentIds());

                writer.println("Commit ID: " + latestCommit.getId());
                writer.println("Short ID: " + latestCommit.getShortId());
                writer.println("Message: " + latestCommit.getMessage());
                writer.println("Author: " + latestCommit.getAuthorName() + " <" + latestCommit.getAuthorEmail() + ">");
                writer.println("Date: " + latestCommit.getAuthorDate());
                writer.println("Committer: " + latestCommit.getCommitterName() + " <" + latestCommit.getCommitterEmail() + ">");
                writer.println("Parent IDs: " + latestCommit.getParentIds());

                // Print changed files in the latest commit
                List<FileChange> changedFiles = latestCommit.getChangedFiles();
                if (changedFiles != null && !changedFiles.isEmpty()) {
                    System.out.println("\nChanged Files:");
                    writer.println("\nChanged Files:");

                    for (FileChange change : changedFiles) {
                        System.out.println("  " + change.getType() + ": " + change.getPath());
                        writer.println("  " + change.getType() + ": " + change.getPath());
                    }
                }
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

                    System.out.println("  " + branch.getName() + current + remote + " - " + branch.getCommitId().substring(0, 7));
                    writer.println("  " + branch.getName() + current + remote + " - " + branch.getCommitId().substring(0, 7));
                }
            }

            // Print tags
            List<TagInfo> tags = repoInfo.getTags();
            if (tags != null && !tags.isEmpty()) {
                System.out.println("\n=== TAGS ===");
                writer.println("\n=== TAGS ===");
                System.out.println("Found " + tags.size() + " tags:");
                writer.println("Found " + tags.size() + " tags:");

                for (TagInfo tag : tags) {
                    String annotated = tag.isAnnotated() ? " (annotated)" : "";
                    String message = tag.getMessage() != null ? " - " + tag.getMessage() : "";

                    System.out.println("  " + tag.getName() + annotated + " - " + tag.getCommitId().substring(0, 7) + message);
                    writer.println("  " + tag.getName() + annotated + " - " + tag.getCommitId().substring(0, 7) + message);

                    if (tag.isAnnotated()) {
                        System.out.println("    Tagger: " + tag.getTaggerName() + " <" + tag.getTaggerEmail() + ">");
                        System.out.println("    Date: " + tag.getTaggerDate());

                        writer.println("    Tagger: " + tag.getTaggerName() + " <" + tag.getTaggerEmail() + ">");
                        writer.println("    Date: " + tag.getTaggerDate());
                    }
                }
            }

            System.out.println("\nGit parsing test completed. Results written to: " + outputFile.getAbsolutePath());
        }
    }

    @Test
    void testGetRecentCommits() throws Exception {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile, true))) {
            // Get the 5 most recent commits
            List<CommitInfo> recentCommits = GitParser.getRecentCommits(repoDir.getAbsolutePath(), 5);

            System.out.println("\n=== RECENT COMMITS ===");
            writer.println("\n=== RECENT COMMITS ===");
            System.out.println("Found " + recentCommits.size() + " recent commits:");
            writer.println("Found " + recentCommits.size() + " recent commits:");

            for (CommitInfo commit : recentCommits) {
                System.out.println("\nCommit: " + commit.getShortId());
                System.out.println("Author: " + commit.getAuthorName());
                System.out.println("Date: " + commit.getAuthorDate());
                System.out.println("Message: " + commit.getMessage());

                writer.println("\nCommit: " + commit.getShortId());
                writer.println("Author: " + commit.getAuthorName());
                writer.println("Date: " + commit.getAuthorDate());
                writer.println("Message: " + commit.getMessage());

                // Print changed files
                if (commit.getChangedFiles() != null && !commit.getChangedFiles().isEmpty()) {
                    System.out.println("Changed Files:");
                    writer.println("Changed Files:");

                    for (FileChange change : commit.getChangedFiles()) {
                        System.out.println("  " + change.getType() + ": " + change.getPath());
                        writer.println("  " + change.getType() + ": " + change.getPath());
                    }
                }
            }
        }
    }

    @Test
    void testFindGitRepositories() {
        File baseDir = new File("src/test/resources");
        List<File> gitRepos = GitParser.findGitRepositories(baseDir);

        System.out.println("\n=== GIT REPOSITORIES IN DIRECTORY ===");
        System.out.println("Found " + gitRepos.size() + " Git repositories:");

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile, true))) {
            writer.println("\n=== GIT REPOSITORIES IN DIRECTORY ===");
            writer.println("Found " + gitRepos.size() + " Git repositories:");

            for (File repo : gitRepos) {
                System.out.println(repo.getAbsolutePath());
                writer.println(repo.getAbsolutePath());

                // Verify it's a valid repository
                boolean isValid = GitParser.isValidRepository(repo.getAbsolutePath());
                System.out.println("  Valid repository: " + isValid);
                writer.println("  Valid repository: " + isValid);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}