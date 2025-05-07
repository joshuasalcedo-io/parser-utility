package io.joshuasalcedo.model.javafile;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Parameter {
    private String type;
    private String name;
}
