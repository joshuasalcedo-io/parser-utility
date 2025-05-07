package io.joshuasalcedo.model.maven;

import lombok.Builder;
import lombok.Data; /**
 * Represents a configuration item for a plugin.
 */
@Data
@Builder
public class PluginConfiguration {
    private String name;
    private String value;
}
