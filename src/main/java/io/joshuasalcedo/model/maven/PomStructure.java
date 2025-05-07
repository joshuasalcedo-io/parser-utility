package io.joshuasalcedo.model.maven;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

/**
 * Model classes for Maven POM file structure using Lombok.
 */

/**
 * Represents the overall structure of a Maven POM file.
 */
@Data
@Builder
public class PomStructure {
    private String groupId;
    private String artifactId;
    private String version;
    private String packaging;
    private String name;
    private String description;
    private ParentInfo parentInfo;
    
    @Singular
    private List<Property> properties;
    
    @Singular
    private List<Dependency> dependencies;
    
    @Singular
    private List<Plugin> plugins;
    
    @Singular
    private List<String> modules;
}

