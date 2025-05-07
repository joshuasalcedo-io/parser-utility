package io.joshuasalcedo.model.javafile;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ClassStructure {

        private String fileName;
        private String packageName;
        private String className;
        private String classType; // class, interface, or enum
        private List<MethodStructure> methods;
        private JavadocStructure javadoc;
        private List<String> comments;
}
