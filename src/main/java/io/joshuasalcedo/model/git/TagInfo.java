package io.joshuasalcedo.model.git;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * Represents information about a Git tag.
 */
@Data
@Builder
public class TagInfo {
    /**
     * The name of the tag.
     */
    private String name;
    
    /**
     * The commit ID that the tag points to.
     */
    private String commitId;
    
    /**
     * Whether this is an annotated tag.
     */
    private boolean annotated;
    
    /**
     * The message of the tag (for annotated tags).
     */
    private String message;
    
    /**
     * The name of the tagger (for annotated tags).
     */
    private String taggerName;
    
    /**
     * The email of the tagger (for annotated tags).
     */
    private String taggerEmail;
    
    /**
     * The date when the tag was created (for annotated tags).
     */
    private Date taggerDate;
}