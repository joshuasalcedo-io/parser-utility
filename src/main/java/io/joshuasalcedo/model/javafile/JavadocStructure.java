package io.joshuasalcedo.model.javafile;

import lombok.Builder;
import lombok.Data;

import java.util.List;
@Builder
@Data
public class JavadocStructure {
    private String description;
    private List<JavadocTag> tags;
}
