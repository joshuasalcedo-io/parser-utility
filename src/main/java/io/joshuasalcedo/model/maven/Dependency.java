package io.joshuasalcedo.model.maven;

import lombok.Builder;
import lombok.Data; /**
 * Represents a dependency in the POM file.
 */
@Data
@Builder
public class Dependency {
    private String groupId;
    private String artifactId;
    private String version;
    private String scope;
    private String type;
}
