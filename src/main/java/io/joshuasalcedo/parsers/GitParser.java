package io.joshuasalcedo.parsers;

import io.joshuasalcedo.model.git.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * Utility class for parsing Git repositories using JGit.
 * This class provides static methods to analyze Git repositories and extract structural information.
 */
public final class GitParser {
    
    // Prevent instantiation
    private GitParser() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Parse a Git repository and extract comprehensive information.
     * 
     * @param repoPath The path to the Git repository
     * @return GitRepositoryInfo object containing repository information
     * @throws IOException If any I/O errors occur
     * @throws GitAPIException If any Git API errors occur
     */
    public static GitRepositoryInfo parseRepository(String repoPath) throws IOException, GitAPIException {
        File gitDir = new File(repoPath);
        try (Git git = Git.open(gitDir)) {
            Repository repository = git.getRepository();
            
            // Get current branch
            String currentBranch = repository.getBranch();
            
            // Get remote URL
            String remoteUrl = null;
            remoteUrl = repository.getConfig().getString("remote", "origin", "url");
            
            // Get latest commit
            RevCommit latestCommit = getLatestCommit(git);
            CommitInfo latestCommitInfo = latestCommit != null ? convertToCommitInfo(latestCommit, repository) : null;
            
            // Get all branches
            List<BranchInfo> branches = getAllBranches(git);
            
            // Get all tags
            List<TagInfo> tags = getAllTags(git);
            
            // Check for uncommitted changes
            boolean hasUncommittedChanges = hasUncommittedChanges(git);
            
            // Count commits in current branch
            int commitCount = countCommits(git);
            
            return GitRepositoryInfo.builder()
                    .name(gitDir.getName())
                    .path(gitDir.getAbsolutePath())
                    .currentBranch(currentBranch)
                    .remoteUrl(remoteUrl)
                    .latestCommit(latestCommitInfo)
                    .branches(branches)
                    .tags(tags)
                    .hasUncommittedChanges(hasUncommittedChanges)
                    .commitCount(commitCount)
                    .build();
        }
    }
    
    /**
     * Get the latest commit in the repository.
     * 
     * @param git The Git instance
     * @return The latest RevCommit or null if the repository is empty
     * @throws GitAPIException If any Git API errors occur
     */
    private static RevCommit getLatestCommit(Git git) throws GitAPIException {
        Iterable<RevCommit> commits = git.log().setMaxCount(1).call();
        return commits.iterator().hasNext() ? commits.iterator().next() : null;
    }
    
    /**
     * Convert a RevCommit to a CommitInfo object.
     * 
     * @param commit The RevCommit to convert
     * @param repository The repository containing the commit
     * @return A CommitInfo object
     * @throws IOException If any I/O errors occur
     */
    private static CommitInfo convertToCommitInfo(RevCommit commit, Repository repository) throws IOException {
        PersonIdent authorIdent = commit.getAuthorIdent();
        PersonIdent committerIdent = commit.getCommitterIdent();
        
        // Get parent commits
        List<String> parentIds = new ArrayList<>();
        for (RevCommit parent : commit.getParents()) {
            parentIds.add(parent.getName());
        }
        
        // Get file changes
        List<FileChange> fileChanges = getFileChanges(commit, repository);
        
        return CommitInfo.builder()
                .id(commit.getName())
                .shortId(commit.getName().substring(0, 7))
                .message(commit.getFullMessage())
                .authorName(authorIdent.getName())
                .authorEmail(authorIdent.getEmailAddress())
                .authorDate(authorIdent.getWhen())
                .committerName(committerIdent.getName())
                .committerEmail(committerIdent.getEmailAddress())
                .commitDate(committerIdent.getWhen())
                .parentIds(parentIds)
                .changedFiles(fileChanges)
                .build();
    }
    
    /**
     * Get file changes for a commit.
     * 
     * @param commit The commit to analyze
     * @param repository The repository containing the commit
     * @return List of FileChange objects
     * @throws IOException If any I/O errors occur
     */
    private static List<FileChange> getFileChanges(RevCommit commit, Repository repository) throws IOException {
        List<FileChange> changes = new ArrayList<>();
        
        // If this is the first commit, there's nothing to compare with
        if (commit.getParentCount() == 0) {
            return changes;
        }
        
        RevCommit parent = commit.getParent(0);
        
        try (ObjectReader reader = repository.newObjectReader();
             DiffFormatter formatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
            
            formatter.setRepository(repository);
            formatter.setDiffComparator(RawTextComparator.DEFAULT);
            
            CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
            oldTreeParser.reset(reader, parent.getTree());
            
            CanonicalTreeParser newTreeParser = new CanonicalTreeParser();
            newTreeParser.reset(reader, commit.getTree());
            
            List<DiffEntry> diffs = formatter.scan(oldTreeParser, newTreeParser);
            
            for (DiffEntry diff : diffs) {
                FileChange.ChangeType type = convertChangeType(diff.getChangeType());
                
                changes.add(FileChange.builder()
                        .type(type)
                        .path(diff.getNewPath())
                        .oldPath(diff.getOldPath())
                        .mode(diff.getNewMode().getBits())
                        .build());
            }
        }
        
        return changes;
    }
    
    /**
     * Convert JGit change type to our own ChangeType enum.
     * 
     * @param jgitChangeType The JGit DiffEntry.ChangeType
     * @return Our FileChange.ChangeType
     */
    private static FileChange.ChangeType convertChangeType(DiffEntry.ChangeType jgitChangeType) {
        switch (jgitChangeType) {
            case ADD:
                return FileChange.ChangeType.ADD;
            case MODIFY:
                return FileChange.ChangeType.MODIFY;
            case DELETE:
                return FileChange.ChangeType.DELETE;
            case RENAME:
                return FileChange.ChangeType.RENAME;
            case COPY:
                return FileChange.ChangeType.COPY;
            default:
                return FileChange.ChangeType.MODIFY;
        }
    }
    
    /**
     * Get all branches in the repository.
     * 
     * @param git The Git instance
     * @return List of BranchInfo objects
     * @throws GitAPIException If any Git API errors occur
     * @throws IOException If any I/O errors occur
     */
    private static List<BranchInfo> getAllBranches(Git git) throws GitAPIException, IOException {
        List<BranchInfo> branches = new ArrayList<>();
        
        // Get current branch name
        String currentBranch = git.getRepository().getBranch();
        
        // Get local branches
        List<Ref> localBranches = git.branchList().call();
        for (Ref branch : localBranches) {
            String branchName = branch.getName().substring(branch.getName().lastIndexOf("/") + 1);
            branches.add(BranchInfo.builder()
                    .name(branchName)
                    .current(branchName.equals(currentBranch))
                    .remote(false)
                    .commitId(branch.getObjectId().getName())
                    .build());
        }
        
        // Get remote branches
        List<Ref> remoteBranches = git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();
        for (Ref branch : remoteBranches) {
            String fullName = branch.getName();
            String remoteName = fullName.substring(fullName.indexOf("/") + 1, fullName.lastIndexOf("/"));
            String branchName = fullName.substring(fullName.lastIndexOf("/") + 1);
            branches.add(BranchInfo.builder()
                    .name(branchName)
                    .current(false)
                    .remote(true)
                    .remoteName(remoteName)
                    .commitId(branch.getObjectId().getName())
                    .build());
        }
        
        return branches;
    }
    
    /**
     * Get all tags in the repository.
     * 
     * @param git The Git instance
     * @return List of TagInfo objects
     * @throws GitAPIException If any Git API errors occur
     * @throws IOException If any I/O errors occur
     */
    private static List<TagInfo> getAllTags(Git git) throws GitAPIException, IOException {
        List<TagInfo> tags = new ArrayList<>();
        List<Ref> tagRefs = git.tagList().call();
        
        try (RevWalk revWalk = new RevWalk(git.getRepository())) {
            for (Ref tagRef : tagRefs) {
                String tagName = tagRef.getName().substring(tagRef.getName().lastIndexOf("/") + 1);
                ObjectId objectId = tagRef.getObjectId();
                
                boolean isAnnotated = false;
                RevTag revTag = null;
                RevCommit commit = null;
                
                try {
                    revTag = revWalk.parseTag(objectId);
                    isAnnotated = true;
                    commit = revWalk.parseCommit(revTag.getObject());
                } catch (IOException e) {
                    // Not an annotated tag
                    commit = revWalk.parseCommit(objectId);
                }
                
                TagInfo.TagInfoBuilder builder = TagInfo.builder()
                        .name(tagName)
                        .commitId(commit.getName())
                        .annotated(isAnnotated);
                
                if (isAnnotated && revTag != null) {
                    PersonIdent taggerIdent = revTag.getTaggerIdent();
                    builder.message(revTag.getFullMessage())
                           .taggerName(taggerIdent.getName())
                           .taggerEmail(taggerIdent.getEmailAddress())
                           .taggerDate(taggerIdent.getWhen());
                }
                
                tags.add(builder.build());
            }
        }
        
        return tags;
    }
    
    /**
     * Check if the repository has uncommitted changes.
     * 
     * @param git The Git instance
     * @return true if there are uncommitted changes, false otherwise
     * @throws GitAPIException If any Git API errors occur
     */
    private static boolean hasUncommittedChanges(Git git) throws GitAPIException {
        Status status = git.status().call();
        return !status.isClean();
    }
    
    /**
     * Count the number of commits in the current branch.
     * 
     * @param git The Git instance
     * @return The number of commits
     * @throws GitAPIException If any Git API errors occur
     */
    private static int countCommits(Git git) throws GitAPIException {
        LogCommand logCommand = git.log();
        Iterable<RevCommit> commits = logCommand.call();
        return (int) StreamSupport.stream(commits.spliterator(), false).count();
    }
    
    /**
     * Find all Git repositories in a directory and its subdirectories.
     * 
     * @param directory The directory to search
     * @return List of Git repository directories
     */
    public static List<File> findGitRepositories(File directory) {
        List<File> repositories = new ArrayList<>();
        findGitRepositoriesRecursive(directory, repositories);
        return repositories;
    }
    
    /**
     * Recursive helper method to find all Git repositories.
     * 
     * @param directory The directory to search
     * @param repositories List to collect Git repository directories
     */
    private static void findGitRepositoriesRecursive(File directory, List<File> repositories) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (file.getName().equals(".git")) {
                        repositories.add(directory);
                    } else if (!file.getName().equals(".git")) {
                        findGitRepositoriesRecursive(file, repositories);
                    }
                }
            }
        }
    }
    
    /**
     * Get specific information about a commit.
     * 
     * @param repoPath The path to the Git repository
     * @param commitId The commit ID
     * @return CommitInfo object containing commit information
     * @throws IOException If any I/O errors occur
     * @throws GitAPIException If any Git API errors occur
     */
    public static CommitInfo getCommitInfo(String repoPath, String commitId) throws IOException, GitAPIException {
        try (Git git = Git.open(new File(repoPath))) {
            Repository repository = git.getRepository();
            
            try (RevWalk revWalk = new RevWalk(repository)) {
                ObjectId objectId = repository.resolve(commitId);
                if (objectId == null) {
                    return null;
                }
                
                RevCommit commit = revWalk.parseCommit(objectId);
                return convertToCommitInfo(commit, repository);
            }
        }
    }
    
    /**
     * Get a list of recent commits.
     * 
     * @param repoPath The path to the Git repository
     * @param maxCount The maximum number of commits to retrieve
     * @return List of CommitInfo objects
     * @throws IOException If any I/O errors occur
     * @throws GitAPIException If any Git API errors occur
     */
    public static List<CommitInfo> getRecentCommits(String repoPath, int maxCount) throws IOException, GitAPIException {
        try (Git git = Git.open(new File(repoPath))) {
            Repository repository = git.getRepository();
            
            Iterable<RevCommit> commits = git.log().setMaxCount(maxCount).call();
            List<CommitInfo> commitInfos = new ArrayList<>();
            
            for (RevCommit commit : commits) {
                commitInfos.add(convertToCommitInfo(commit, repository));
            }
            
            return commitInfos;
        }
    }
    
    /**
     * Check if a path is a valid Git repository.
     * 
     * @param path The path to check
     * @return true if the path is a valid Git repository, false otherwise
     */
    public static boolean isValidRepository(String path) {
        File gitDir = new File(path);
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder()
                    .setGitDir(new File(gitDir, ".git"))
                    .readEnvironment()
                    .findGitDir();
                    
            if (builder.getGitDir() == null) {
                return false;
            }
            
            try (Repository repository = builder.build()) {
                return repository.getObjectDatabase().exists();
            }
        } catch (IOException e) {
            return false;
        }
    }
}