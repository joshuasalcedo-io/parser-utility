package io.joshuasalcedo.model.maven;

import lombok.Builder;
import lombok.Data; /**
 * Represents parent POM information.
 */
@Data
@Builder
public class ParentInfo {
    private String groupId;
    private String artifactId;
    private String version;
    private String relativePath;
}
