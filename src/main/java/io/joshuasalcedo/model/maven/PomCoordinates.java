package io.joshuasalcedo.model.maven;

import lombok.Builder;
import lombok.Data;

/**
 * Represents the core coordinates of a Maven project.
 */
@Data
@Builder
public class PomCoordinates {
    /**
     * The group ID of the project.
     */
    private String groupId;
    
    /**
     * The artifact ID of the project.
     */
    private String artifactId;
    
    /**
     * The version of the project.
     */
    private String version;
    
    /**
     * The packaging type of the project (jar, war, pom, etc.).
     */
    private String packaging;
}