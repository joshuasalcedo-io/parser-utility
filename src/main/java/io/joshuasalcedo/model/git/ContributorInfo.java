package io.joshuasalcedo.model.git;

import lombok.Builder;
import lombok.Data;

/**
 * Represents information about a Git repository contributor.
 */
@Data
@Builder
public class ContributorInfo {
    /**
     * The name of the contributor.
     */
    private String name;
    
    /**
     * The email of the contributor.
     */
    private String email;
    
    /**
     * The number of commits made by this contributor.
     */
    private int commitCount;
    
    /**
     * The number of lines added by this contributor.
     */
    private int linesAdded;
    
    /**
     * The number of lines deleted by this contributor.
     */
    private int linesDeleted;
    
    /**
     * The date of the first commit by this contributor.
     */
    private java.util.Date firstCommitDate;
    
    /**
     * The date of the most recent commit by this contributor.
     */
    private java.util.Date lastCommitDate;
}