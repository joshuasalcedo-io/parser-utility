package io.joshuasalcedo.model.git;

import lombok.Builder;
import lombok.Data;

/**
 * Represents information about a Git branch.
 */
@Data
@Builder
public class BranchInfo {
    /**
     * The name of the branch.
     */
    private String name;
    
    /**
     * Whether this is the current branch.
     */
    private boolean current;
    
    /**
     * Whether this is a remote branch.
     */
    private boolean remote;
    
    /**
     * The name of the remote if this is a remote branch.
     */
    private String remoteName;
    
    /**
     * The commit ID that the branch points to.
     */
    private String commitId;
    
    /**
     * The tracking branch name, if any.
     */
    private String trackingBranch;
    
    /**
     * Whether this branch is merged into the current branch.
     */
    private boolean merged;
}