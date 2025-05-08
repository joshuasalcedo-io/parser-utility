package io.joshuasalcedo.parsers;

import io.joshuasalcedo.model.git.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
     * @throws IOException     If any I/O errors occur
     * @throws GitAPIException If any Git API errors occur
     */
    public static GitRepositoryInfo parseRepository(String repoPath) throws IOException, GitAPIException {
        File gitDir = new File(repoPath);
        try (Git git = Git.open(gitDir)) {
            Repository repository = git.getRepository();

            // Get current branch
            String currentBranch = repository.getBranch();

            // Get remote URL
            String remoteUrl = repository.getConfig().getString("remote", "origin", "url");

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

            // Get top contributors
            List<ContributorInfo> topContributors = getTopContributors(git, 5);

            // Get repository statistics
            Map<String, Object> statistics = getRepositoryStatistics(git);

            // Get file extension counts
            Map<String, Integer> fileExtensionCounts = getFileExtensionCounts(git);

            // Get repository creation and last updated dates
            Date creationDate = getRepositoryCreationDate(git);
            Date lastUpdatedDate = latestCommit != null ?
                    latestCommit.getAuthorIdent().getWhen() : null;

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
                    .statistics(statistics)
                    .topContributors(topContributors)
                    .creationDate(creationDate)
                    .lastUpdatedDate(lastUpdatedDate)
                    .fileExtensionCounts(fileExtensionCounts)
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
     * @param commit     The RevCommit to convert
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
     * @param commit     The commit to analyze
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

                // Calculate line changes for this file
                int linesAdded = 0;
                int linesDeleted = 0;

                try {
                    for (Edit edit : formatter.toFileHeader(diff).toEditList()) {
                        linesAdded += edit.getEndB() - edit.getBeginB();
                        linesDeleted += edit.getEndA() - edit.getBeginA();
                    }
                } catch (Exception e) {
                    // In case of binary files or other issues
                }

                changes.add(FileChange.builder()
                        .type(type)
                        .path(diff.getNewPath())
                        .oldPath(diff.getOldPath())
                        .linesAdded(linesAdded)
                        .linesDeleted(linesDeleted)
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
     * @throws IOException     If any I/O errors occur
     */
    private static List<BranchInfo> getAllBranches(Git git) throws GitAPIException, IOException {
        List<BranchInfo> branches = new ArrayList<>();

        // Get current branch name
        String currentBranch = git.getRepository().getBranch();

        // Get local branches
        List<Ref> localBranches = git.branchList().call();
        for (Ref branch : localBranches) {
            String branchName = branch.getName().substring(branch.getName().lastIndexOf("/") + 1);

            // Check if branch is merged
            boolean merged = false;
            try {
                List<Ref> mergedBranches = git.branchList().setContains(currentBranch).call();
                for (Ref mergedBranch : mergedBranches) {
                    if (mergedBranch.getName().equals(branch.getName())) {
                        merged = true;
                        break;
                    }
                }
            } catch (Exception e) {
                // Ignore errors checking merged status
            }

            // Get tracking branch
            String trackingBranch = null;
            try {
                trackingBranch = git.getRepository().getConfig().getString("branch", branchName, "merge");
                if (trackingBranch != null && trackingBranch.startsWith("refs/heads/")) {
                    trackingBranch = trackingBranch.substring("refs/heads/".length());
                }
            } catch (Exception e) {
                // Ignore errors getting tracking branch
            }

            branches.add(BranchInfo.builder()
                    .name(branchName)
                    .current(branchName.equals(currentBranch))
                    .remote(false)
                    .commitId(branch.getObjectId().getName())
                    .merged(merged)
                    .trackingBranch(trackingBranch)
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
     * @throws IOException     If any I/O errors occur
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
     * Get the top contributors to the repository.
     *
     * @param git      The Git instance
     * @param maxCount Maximum number of contributors to return
     * @return List of ContributorInfo objects
     * @throws GitAPIException If any Git API errors occur
     * @throws IOException     If any I/O errors occur
     */
    private static List<ContributorInfo> getTopContributors(Git git, int maxCount) throws GitAPIException, IOException {
        // Maps to track contributor statistics
        Map<String, ContributorInfo.ContributorInfoBuilder> contributors = new HashMap<>();
        Map<String, Date> firstCommitDates = new HashMap<>();
        Map<String, Date> lastCommitDates = new HashMap<>();

        // Get all commits
        Iterable<RevCommit> commits = git.log().all().call();

        for (RevCommit commit : commits) {
            PersonIdent author = commit.getAuthorIdent();
            String authorEmail = author.getEmailAddress();

            // Initialize contributor if not exists
            if (!contributors.containsKey(authorEmail)) {
                contributors.put(authorEmail, ContributorInfo.builder()
                        .name(author.getName())
                        .email(authorEmail)
                        .commitCount(0)
                        .linesAdded(0)
                        .linesDeleted(0));

                firstCommitDates.put(authorEmail, author.getWhen());
                lastCommitDates.put(authorEmail, author.getWhen());
            }

            // Update commit count
            ContributorInfo.ContributorInfoBuilder contributor = contributors.get(authorEmail);
            contributor.commitCount(contributor.build().getCommitCount() + 1);

            // Update dates
            Date commitDate = author.getWhen();
            if (commitDate.before(firstCommitDates.get(authorEmail))) {
                firstCommitDates.put(authorEmail, commitDate);
            }
            if (commitDate.after(lastCommitDates.get(authorEmail))) {
                lastCommitDates.put(authorEmail, commitDate);
            }

            // Update line changes
            if (commit.getParentCount() > 0) {
                List<FileChange> changes = getFileChanges(commit, git.getRepository());
                int linesAdded = 0;
                int linesDeleted = 0;

                for (FileChange change : changes) {
                    linesAdded += change.getLinesAdded();
                    linesDeleted += change.getLinesDeleted();
                }

                contributor.linesAdded(contributor.build().getLinesAdded() + linesAdded);
                contributor.linesDeleted(contributor.build().getLinesDeleted() + linesDeleted);
            }
        }

        // Build final contributor list
        List<ContributorInfo> result = new ArrayList<>();
        for (String email : contributors.keySet()) {
            ContributorInfo.ContributorInfoBuilder builder = contributors.get(email);
            builder.firstCommitDate(firstCommitDates.get(email));
            builder.lastCommitDate(lastCommitDates.get(email));
            result.add(builder.build());
        }

        // Sort by commit count and limit to maxCount
        result.sort((c1, c2) -> Integer.compare(c2.getCommitCount(), c1.getCommitCount()));
        return result.stream().limit(maxCount).collect(Collectors.toList());
    }

    /**
     * Get various statistics about the repository.
     *
     * @param git The Git instance
     * @return Map of statistics
     * @throws GitAPIException If any Git API errors occur
     * @throws IOException     If any I/O errors occur
     */
    private static Map<String, Object> getRepositoryStatistics(Git git) throws GitAPIException, IOException {
        Map<String, Object> statistics = new HashMap<>();

        // Get all commits
        Iterable<RevCommit> commits = git.log().all().call();
        List<RevCommit> commitList = StreamSupport.stream(commits.spliterator(), false)
                .collect(Collectors.toList());

        // Calculate commits per day of week
        Map<String, Integer> commitsPerDayOfWeek = new HashMap<>();
        String[] daysOfWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        for (String day : daysOfWeek) {
            commitsPerDayOfWeek.put(day, 0);
        }

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
        for (RevCommit commit : commitList) {
            Date commitDate = commit.getAuthorIdent().getWhen();
            String dayOfWeek = dayFormat.format(commitDate);
            commitsPerDayOfWeek.put(dayOfWeek, commitsPerDayOfWeek.get(dayOfWeek) + 1);
        }

        // Calculate commits per hour
        Map<Integer, Integer> commitsPerHour = new HashMap<>();
        for (int i = 0; i < 24; i++) {
            commitsPerHour.put(i, 0);
        }

        Calendar calendar = Calendar.getInstance();
        for (RevCommit commit : commitList) {
            Date commitDate = commit.getAuthorIdent().getWhen();
            calendar.setTime(commitDate);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            commitsPerHour.put(hour, commitsPerHour.get(hour) + 1);
        }

        // Calculate commits per month
        Map<String, Integer> commitsPerMonth = new HashMap<>();
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        for (String month : months) {
            commitsPerMonth.put(month, 0);
        }

        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM");
        for (RevCommit commit : commitList) {
            Date commitDate = commit.getAuthorIdent().getWhen();
            String month = monthFormat.format(commitDate);
            commitsPerMonth.put(month, commitsPerMonth.get(month) + 1);
        }

        // Calculate average commit size (in changed files)
        int totalChangedFiles = 0;
        for (RevCommit commit : commitList) {
            if (commit.getParentCount() > 0) {
                List<FileChange> changes = getFileChanges(commit, git.getRepository());
                totalChangedFiles += changes.size();
            }
        }

        double avgChangedFiles = commitList.isEmpty() ? 0 : (double) totalChangedFiles / commitList.size();

        // Add statistics to the map
        statistics.put("totalCommits", commitList.size());
        statistics.put("commitsPerDayOfWeek", commitsPerDayOfWeek);
        statistics.put("commitsPerHour", commitsPerHour);
        statistics.put("commitsPerMonth", commitsPerMonth);
        statistics.put("averageChangedFilesPerCommit", avgChangedFiles);

        return statistics;
    }

    /**
     * Get counts of files by extension in the repository.
     *
     * @param git The Git instance
     * @return Map of file extensions to counts
     * @throws GitAPIException If any Git API errors occur
     * @throws IOException     If any I/O errors occur
     */
    private static Map<String, Integer> getFileExtensionCounts(Git git) throws GitAPIException, IOException {
        Map<String, Integer> extensionCounts = new HashMap<>();

        RevCommit headCommit = getLatestCommit(git);
        if (headCommit == null) {
            return extensionCounts;
        }

        Repository repository = git.getRepository();
        try (TreeWalk treeWalk = new TreeWalk(repository)) {
            treeWalk.addTree(headCommit.getTree());
            treeWalk.setRecursive(true);

            while (treeWalk.next()) {
                String path = treeWalk.getPathString();
                int dotIndex = path.lastIndexOf('.');
                if (dotIndex > 0) {
                    String extension = path.substring(dotIndex + 1).toLowerCase();
                    extensionCounts.put(extension, extensionCounts.getOrDefault(extension, 0) + 1);
                } else {
                    extensionCounts.put("(no extension)", extensionCounts.getOrDefault("(no extension)", 0) + 1);
                }
            }
        }

        return extensionCounts;
    }

    /**
     * Get the creation date of the repository (date of the first commit).
     *
     * @param git The Git instance
     * @return The creation date or null if no commits exist
     * @throws GitAPIException If any Git API errors occur
     * @throws IOException     If any I/O errors occur
     */
    private static Date getRepositoryCreationDate(Git git) throws GitAPIException, IOException {
        Repository repository = git.getRepository();

        try (RevWalk revWalk = new RevWalk(repository)) {
            revWalk.sort(RevSort.COMMIT_TIME_DESC, true);
            revWalk.sort(RevSort.REVERSE, true);

            Iterable<RevCommit> commits = git.log().all().call();
            for (RevCommit commit : commits) {
                revWalk.parseCommit(commit);
                return commit.getAuthorIdent().getWhen();
            }
        }

        return null;
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
     * @param directory    The directory to search
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
     * @throws IOException     If any I/O errors occur
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
     * @throws IOException     If any I/O errors occur
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
     * Get a list of all commits in a repository.
     *
     * @param repoPath The path to the Git repository
     * @return List of CommitInfo objects for all commits
     * @throws IOException If any I/O errors occur
     * @throws GitAPIException If any Git API errors occur
     */
    public static List<CommitInfo> getAllCommits(String repoPath) throws IOException, GitAPIException {
        try (Git git = Git.open(new File(repoPath))) {
            Repository repository = git.getRepository();

            Iterable<RevCommit> commits = git.log().all().call();
            List<CommitInfo> commitInfos = new ArrayList<>();

            for (RevCommit commit : commits) {
                commitInfos.add(convertToCommitInfo(commit, repository));
            }

            return commitInfos;
        }
    }

    /**
     * Get commits by a specific author.
     *
     * @param repoPath The path to the Git repository
     * @param authorName The name or email of the author
     * @return List of CommitInfo objects for commits by the author
     * @throws IOException If any I/O errors occur
     * @throws GitAPIException If any Git API errors occur
     */
    public static List<CommitInfo> getCommitsByAuthor(String repoPath, String authorName)
            throws IOException, GitAPIException {
        try (Git git = Git.open(new File(repoPath))) {
            Repository repository = git.getRepository();

            Iterable<RevCommit> commits = git.log().all().call();
            List<CommitInfo> authorCommits = new ArrayList<>();

            for (RevCommit commit : commits) {
                PersonIdent author = commit.getAuthorIdent();
                if (author.getName().contains(authorName) || author.getEmailAddress().contains(authorName)) {
                    authorCommits.add(convertToCommitInfo(commit, repository));
                }
            }

            return authorCommits;
        }
    }

    /**
     * Get commits within a date range.
     *
     * @param repoPath The path to the Git repository
     * @param since Start date (inclusive)
     * @param until End date (inclusive)
     * @return List of CommitInfo objects for commits within the date range
     * @throws IOException If any I/O errors occur
     * @throws GitAPIException If any Git API errors occur
     */
    public static List<CommitInfo> getCommitsByDateRange(String repoPath, Date since, Date until)
            throws IOException, GitAPIException {
        try (Git git = Git.open(new File(repoPath))) {
            Repository repository = git.getRepository();

            Iterable<RevCommit> commits = git.log().all().call();
            List<CommitInfo> dateRangeCommits = new ArrayList<>();

            for (RevCommit commit : commits) {
                Date commitDate = commit.getAuthorIdent().getWhen();
                if ((commitDate.equals(since) || commitDate.after(since)) &&
                        (commitDate.equals(until) || commitDate.before(until))) {
                    dateRangeCommits.add(convertToCommitInfo(commit, repository));
                }
            }

            return dateRangeCommits;
        }
    }

    /**
     * Get the history of a specific file.
     *
     * @param repoPath The path to the Git repository
     * @param filePath The path to the file within the repository
     * @return List of CommitInfo objects for commits that changed the file
     * @throws IOException If any I/O errors occur
     * @throws GitAPIException If any Git API errors occur
     */
    public static List<CommitInfo> getFileHistory(String repoPath, String filePath)
            throws IOException, GitAPIException {
        try (Git git = Git.open(new File(repoPath))) {
            Repository repository = git.getRepository();

            Iterable<RevCommit> commits = git.log().addPath(filePath).call();
            List<CommitInfo> fileCommits = new ArrayList<>();

            for (RevCommit commit : commits) {
                fileCommits.add(convertToCommitInfo(commit, repository));
            }

            return fileCommits;
        }
    }

    /**
     * Get blame information for a specific file, showing who last modified each line.
     *
     * @param repoPath The path to the Git repository
     * @param filePath The path to the file within the repository
     * @return String representing blame information
     * @throws IOException If any I/O errors occur
     * @throws GitAPIException If any Git API errors occur
     */
    public static String getFileBlame(String repoPath, String filePath)
            throws IOException, GitAPIException {
        try (Git git = Git.open(new File(repoPath))) {
            Repository repository = git.getRepository();

            BlameResult blameResult = git.blame()
                    .setFilePath(filePath)
                    .call();

            StringBuilder result = new StringBuilder();

            int lineCount = blameResult.getResultContents().size();
            for (int i = 0; i < lineCount; i++) {
                RevCommit commit = blameResult.getSourceCommit(i);
                PersonIdent author = commit.getAuthorIdent();
                String shortCommitId = commit.getName().substring(0, 7);
                String line = blameResult.getResultContents().getString(i);

                result.append(String.format("%s (%s - %s): %s\n",
                        shortCommitId,
                        author.getName(),
                        new SimpleDateFormat("yyyy-MM-dd").format(author.getWhen()),
                        line));
            }

            return result.toString();
        }
    }

    /**
     * Get diff between two commits.
     *
     * @param repoPath The path to the Git repository
     * @param oldCommitId The ID of the old commit
     * @param newCommitId The ID of the new commit
     * @return String representing the diff
     * @throws IOException If any I/O errors occur
     * @throws GitAPIException If any Git API errors occur
     */
    public static String getDiffBetweenCommits(String repoPath, String oldCommitId, String newCommitId)
            throws IOException, GitAPIException {
        try (Git git = Git.open(new File(repoPath))) {
            Repository repository = git.getRepository();

            ObjectId oldId = repository.resolve(oldCommitId);
            ObjectId newId = repository.resolve(newCommitId);

            if (oldId == null || newId == null) {
                return "Invalid commit IDs";
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (DiffFormatter formatter = new DiffFormatter(outputStream)) {
                formatter.setRepository(repository);

                RevCommit oldCommit = repository.parseCommit(oldId);
                RevCommit newCommit = repository.parseCommit(newId);

                AbstractTreeIterator oldTreeIterator = getCanonicalTreeParser(repository, oldCommit);
                AbstractTreeIterator newTreeIterator = getCanonicalTreeParser(repository, newCommit);

                List<DiffEntry> diffs = git.diff()
                        .setOldTree(oldTreeIterator)
                        .setNewTree(newTreeIterator)
                        .call();

                for (DiffEntry diff : diffs) {
                    formatter.format(diff);
                    formatter.flush();
                }

                return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
            }
        }
    }

    /**
     * Helper method to get a tree parser for a commit.
     */
    private static AbstractTreeIterator getCanonicalTreeParser(Repository repository, RevCommit commit)
            throws IOException {
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commitTree = walk.parseCommit(commit.getId());
            try (ObjectReader reader = repository.newObjectReader()) {
                CanonicalTreeParser treeParser = new CanonicalTreeParser();
                treeParser.reset(reader, commitTree.getTree().getId());
                return treeParser;
            }
        }
    }

    /**
     * Get a summary of contributions by day of week.
     *
     * @param repoPath The path to the Git repository
     * @return Map with days of week as keys and commit counts as values
     * @throws IOException If any I/O errors occur
     * @throws GitAPIException If any Git API errors occur
     */
    public static Map<String, Integer> getContributionsByDayOfWeek(String repoPath)
            throws IOException, GitAPIException {
        try (Git git = Git.open(new File(repoPath))) {
            // Initialize days of week map
            Map<String, Integer> daysOfWeek = new LinkedHashMap<>();
            daysOfWeek.put("Monday", 0);
            daysOfWeek.put("Tuesday", 0);
            daysOfWeek.put("Wednesday", 0);
            daysOfWeek.put("Thursday", 0);
            daysOfWeek.put("Friday", 0);
            daysOfWeek.put("Saturday", 0);
            daysOfWeek.put("Sunday", 0);

            // Get all commits
            Iterable<RevCommit> commits = git.log().all().call();
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");

            // Count commits by day of week
            for (RevCommit commit : commits) {
                Date commitDate = commit.getAuthorIdent().getWhen();
                String dayOfWeek = dayFormat.format(commitDate);
                daysOfWeek.put(dayOfWeek, daysOfWeek.get(dayOfWeek) + 1);
            }

            return daysOfWeek;
        }
    }

    /**
     * Get a summary of contributions by hour of day.
     *
     * @param repoPath The path to the Git repository
     * @return Map with hours as keys and commit counts as values
     * @throws IOException If any I/O errors occur
     * @throws GitAPIException If any Git API errors occur
     */
    public static Map<Integer, Integer> getContributionsByHourOfDay(String repoPath)
            throws IOException, GitAPIException {
        try (Git git = Git.open(new File(repoPath))) {
            // Initialize hours map (0-23)
            Map<Integer, Integer> hourMap = new LinkedHashMap<>();
            for (int i = 0; i < 24; i++) {
                hourMap.put(i, 0);
            }

            // Get all commits
            Iterable<RevCommit> commits = git.log().all().call();
            Calendar calendar = Calendar.getInstance();

            // Count commits by hour
            for (RevCommit commit : commits) {
                Date commitDate = commit.getAuthorIdent().getWhen();
                calendar.setTime(commitDate);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                hourMap.put(hour, hourMap.get(hour) + 1);
            }

            return hourMap;
        }
    }

    /**
     * Generate a monthly contribution heat map.
     *
     * @param repoPath The path to the Git repository
     * @param year The year to analyze
     * @return Map with days as keys (format: yyyy-MM-dd) and commit counts as values
     * @throws IOException If any I/O errors occur
     * @throws GitAPIException If any Git API errors occur
     */
    public static Map<String, Integer> getContributionHeatMap(String repoPath, int year)
            throws IOException, GitAPIException {
        try (Git git = Git.open(new File(repoPath))) {
            // Create a calendar for the requested year
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.set(year, Calendar.JANUARY, 1, 0, 0, 0);
            Date startDate = startCalendar.getTime();

            Calendar endCalendar = Calendar.getInstance();
            endCalendar.set(year, Calendar.DECEMBER, 31, 23, 59, 59);
            Date endDate = endCalendar.getTime();

            // Initialize heat map
            Map<String, Integer> heatMap = new LinkedHashMap<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            // Initialize all days of the year with zero counts
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            while (calendar.getTime().before(endDate) || calendar.getTime().equals(endDate)) {
                heatMap.put(dateFormat.format(calendar.getTime()), 0);
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            // Get commits for the year
            Iterable<RevCommit> commits = git.log().all().call();

            // Count commits by day
            for (RevCommit commit : commits) {
                Date commitDate = commit.getAuthorIdent().getWhen();
                if ((commitDate.after(startDate) || commitDate.equals(startDate)) &&
                        (commitDate.before(endDate) || commitDate.equals(endDate))) {

                    String dateStr = dateFormat.format(commitDate);
                    if (heatMap.containsKey(dateStr)) {
                        heatMap.put(dateStr, heatMap.get(dateStr) + 1);
                    }
                }
            }

            return heatMap;
        }
    }

    /**
     * Get the most active files in the repository (most frequently changed).
     *
     * @param repoPath The path to the Git repository
     * @param limit Maximum number of files to return
     * @return Map with file paths as keys and change counts as values
     * @throws IOException If any I/O errors occur
     * @throws GitAPIException If any Git API errors occur
     */
    public static Map<String, Integer> getMostActiveFiles(String repoPath, int limit)
            throws IOException, GitAPIException {
        try (Git git = Git.open(new File(repoPath))) {
            Repository repository = git.getRepository();

            // Get all commits
            Iterable<RevCommit> commits = git.log().all().call();

            // Count changes by file path
            Map<String, Integer> fileChangeCounts = new HashMap<>();

            for (RevCommit commit : commits) {
                if (commit.getParentCount() > 0) {
                    List<FileChange> changes = getFileChanges(commit, repository);
                    for (FileChange change : changes) {
                        String path = change.getPath();
                        if (path != null && !path.equals("/dev/null")) {
                            fileChangeCounts.put(path, fileChangeCounts.getOrDefault(path, 0) + 1);
                        }
                    }
                }
            }

            // Sort files by change count and limit results
            return fileChangeCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(limit)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));
        }
    }

    /**
     * Generate a commit log in a readable format.
     *
     * @param repoPath The path to the Git repository
     * @param maxCount Maximum number of commits to include
     * @return String representing the commit log
     * @throws IOException If any I/O errors occur
     * @throws GitAPIException If any Git API errors occur
     */
    public static String getFormattedCommitLog(String repoPath, int maxCount)
            throws IOException, GitAPIException {
        try (Git git = Git.open(new File(repoPath))) {
            Repository repository = git.getRepository();

            Iterable<RevCommit> commits = git.log().setMaxCount(maxCount).call();
            StringBuilder log = new StringBuilder();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            for (RevCommit commit : commits) {
                PersonIdent author = commit.getAuthorIdent();

                log.append("Commit: ").append(commit.getName()).append("\n");
                log.append("Author: ").append(author.getName())
                        .append(" <").append(author.getEmailAddress()).append(">\n");
                log.append("Date:   ").append(dateFormat.format(author.getWhen())).append("\n\n");
                log.append("    ").append(commit.getFullMessage().replace("\n", "\n    ")).append("\n\n");

                // Add file changes if not the first commit
                if (commit.getParentCount() > 0) {
                    List<FileChange> changes = getFileChanges(commit, repository);

                    if (!changes.isEmpty()) {
                        log.append("    Changed files:\n");
                        for (FileChange change : changes) {
                            String prefix = "";
                            switch (change.getType()) {
                                case ADD:
                                    prefix = "A";
                                    break;
                                case MODIFY:
                                    prefix = "M";
                                    break;
                                case DELETE:
                                    prefix = "D";
                                    break;
                                case RENAME:
                                    prefix = "R";
                                    break;
                                case COPY:
                                    prefix = "C";
                                    break;
                            }

                            log.append("      ").append(prefix).append(" ").append(change.getPath()).append("\n");
                        }
                        log.append("\n");
                    }
                }

                log.append("-------------------------------------------------------------------------------\n\n");
            }

            return log.toString();
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

    /**
     * List all files in a directory, respecting .gitignore rules if present.
     *
     * @param directory The directory to list files from
     * @return List of files, excluding those matched by .gitignore rules
     * @throws IOException If any I/O errors occur
     */
    public static List<File> listFilesRespectingGitignore(String directory) throws IOException {
        Path dirPath = Paths.get(directory);
        File gitignoreFile = new File(directory, ".gitignore");

        // List to store ignore patterns
        List<String> ignorePatterns = new ArrayList<>();

        // Load .gitignore patterns if the file exists
        if (gitignoreFile.exists() && gitignoreFile.isFile()) {
            try {
                List<String> lines = Files.readAllLines(gitignoreFile.toPath(), StandardCharsets.UTF_8);
                for (String line : lines) {
                    // Skip empty lines and comments
                    if (line.trim().isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    ignorePatterns.add(line.trim());
                }
            } catch (IOException e) {
                // If there's an error reading .gitignore, log it but continue without ignore rules
                System.err.println("Error reading .gitignore file: " + e.getMessage());
            }
        }

        List<File> result = new ArrayList<>();

        // Walk the directory tree
        try (Stream<Path> paths = Files.walk(dirPath)) {
            paths.filter(Files::isRegularFile)
                 .forEach(path -> {
                     // Get the relative path from the base directory
                     Path relativePath = dirPath.relativize(path);
                     String pathStr = relativePath.toString().replace('\\', '/');

                     // Check if the file matches any ignore pattern
                     boolean isIgnored = false;
                     for (String pattern : ignorePatterns) {
                         if (matchesGitignorePattern(pathStr, pattern)) {
                             isIgnored = true;
                             break;
                         }
                     }

                     // Add the file to the result if it's not ignored
                     if (!isIgnored) {
                         result.add(path.toFile());
                     }
                 });
        }

        return result;
    }

    /**
     * Check if a file path matches a gitignore pattern.
     * This is a simplified implementation and doesn't handle all gitignore pattern features.
     *
     * @param path The file path to check
     * @param pattern The gitignore pattern
     * @return true if the path matches the pattern, false otherwise
     */
    private static boolean matchesGitignorePattern(String path, String pattern) {
        // Handle negation (patterns starting with !)
        boolean negate = pattern.startsWith("!");
        if (negate) {
            pattern = pattern.substring(1);
        }

        // Handle directory-only patterns (ending with /)
        boolean dirOnly = pattern.endsWith("/");
        if (dirOnly) {
            pattern = pattern.substring(0, pattern.length() - 1);
        }

        // Convert gitignore glob pattern to regex
        pattern = pattern
            .replace(".", "\\.")   // Escape dots
            .replace("*", ".*")    // * becomes .*
            .replace("?", ".");    // ? becomes .

        // Match the pattern against the path
        boolean matches = path.matches(pattern);

        // Return the result, taking negation into account
        return negate ? !matches : matches;
    }


}
