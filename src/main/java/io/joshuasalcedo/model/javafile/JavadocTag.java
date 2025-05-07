package io.joshuasalcedo.model.javafile;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class JavadocTag {
    private String name;
    private String value;

}
