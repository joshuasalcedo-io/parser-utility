package io.joshuasalcedo.model.maven;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List; /**
 * Represents a build plugin in the POM file.
 */
@Data
@Builder
public class Plugin {
    private String groupId;
    private String artifactId;
    private String version;
    
    @Singular
    private List<PluginConfiguration> configurationItems;
}
