package io.joshuasalcedo.model.maven;

import lombok.Builder;
import lombok.Data; /**
 * Represents a property in the POM file.
 */
@Data
@Builder
public class Property {
    private String name;
    private String value;
}
