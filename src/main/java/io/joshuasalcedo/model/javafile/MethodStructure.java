package io.joshuasalcedo.model.javafile;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class MethodStructure {
    private String accessModifier;
    private boolean isStatic;
    private String returnType;
    private String methodName;
    private List<Parameter> parameters;
    private String body;
    private JavadocStructure javadoc;
    private List<String> comments;

}
