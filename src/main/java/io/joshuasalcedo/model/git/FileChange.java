package io.joshuasalcedo.model.git;

import lombok.Builder;
import lombok.Data;

/**
 * Represents information about a file change in a Git commit.
 */
@Data
@Builder
public class FileChange {
    /**
     * The change type (ADD, MODIFY, DELETE, RENAME, COPY).
     */
    private ChangeType type;
    
    /**
     * The path of the file.
     */
    private String path;
    
    /**
     * The old path of the file (for renames).
     */
    private String oldPath;
    
    /**
     * The number of lines added.
     */
    private int linesAdded;
    
    /**
     * The number of lines deleted.
     */
    private int linesDeleted;
    
    /**
     * The file mode.
     */
    private int mode;
    
    /**
     * Enum representing the type of change to a file.
     */
    public enum ChangeType {
        /**
         * File was added.
         */
        ADD,
        
        /**
         * File was modified.
         */
        MODIFY,
        
        /**
         * File was deleted.
         */
        DELETE,
        
        /**
         * File was renamed.
         */
        RENAME,
        
        /**
         * File was copied.
         */
        COPY
    }
}