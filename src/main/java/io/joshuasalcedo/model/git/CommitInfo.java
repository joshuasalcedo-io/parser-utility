package io.joshuasalcedo.model.git;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Represents information about a Git commit.
 */
@Data
@Builder
public class CommitInfo {
    /**
     * The SHA-1 hash of the commit.
     */
    private String id;
    
    /**
     * Short version of the commit ID.
     */
    private String shortId;
    
    /**
     * The commit message.
     */
    private String message;
    
    /**
     * The name of the commit author.
     */
    private String authorName;
    
    /**
     * The email of the commit author.
     */
    private String authorEmail;
    
    /**
     * The date when the commit was authored.
     */
    private Date authorDate;
    
    /**
     * The name of the committer.
     */
    private String committerName;
    
    /**
     * The email of the committer.
     */
    private String committerEmail;
    
    /**
     * The date when the commit was committed.
     */
    private Date commitDate;
    
    /**
     * List of parent commit IDs.
     */
    private List<String> parentIds;
    
    /**
     * List of files changed in this commit.
     */
    private List<FileChange> changedFiles;
}