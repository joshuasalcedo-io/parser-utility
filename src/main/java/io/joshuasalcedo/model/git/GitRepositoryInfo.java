package io.joshuasalcedo.model.git;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Represents information about a Git repository.
 */
@Data
@Builder
public class GitRepositoryInfo {
    /**
     * The name of the repository.
     */
    private String name;

    /**
     * The path to the repository.
     */
    private String path;

    /**
     * The name of the current branch.
     */
    private String currentBranch;

    /**
     * The remote URL of the repository.
     */
    private String remoteUrl;

    /**
     * The latest commit information.
     */
    private CommitInfo latestCommit;

    /**
     * List of all branches in the repository.
     */
    private List<BranchInfo> branches;

    /**
     * List of all tags in the repository.
     */
    private List<TagInfo> tags;

    /**
     * True if the repository has uncommitted changes.
     */
    private boolean hasUncommittedChanges;

    /**
     * The number of commits in the current branch.
     */
    private int commitCount;

    /**
     * Map containing various repository statistics.
     */
    private Map<String, Object> statistics;

    /**
     * List of top contributors.
     */
    private List<ContributorInfo> topContributors;

    /**
     * Creation date of the repository (earliest commit).
     */
    private Date creationDate;

    /**
     * Last updated date of the repository (latest commit).
     */
    private Date lastUpdatedDate;

    /**
     * Map of file extension counts.
     */
    private Map<String, Integer> fileExtensionCounts;
}