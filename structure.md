```java

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * JavaStructureAnalyzer - A utility to analyze Java source files and output their structure in JSON format.
 * This program walks through a directory, finds all Java files, and extracts information about packages,
 * classes, methods, parameters, return types, method bodies, and comments.
 */
public class JavaStructureAnalyzer {

    // Regular expressions for parsing Java code
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("package\\s+([\\w.]+)\\s*;");
    private static final Pattern CLASS_PATTERN = Pattern.compile("(public|private|protected)?\\s*(static)?\\s*(class|interface|enum)\\s+(\\w+)");
    private static final Pattern METHOD_PATTERN = Pattern.compile("(public|private|protected)?\\s*(static)?\\s*(\\w+(?:<.*>)?)\\s+(\\w+)\\s*\\(([^)]*)\\)");
    private static final Pattern METHOD_BODY_PATTERN = Pattern.compile("(public|private|protected)?\\s*(static)?\\s*(\\w+(?:<.*>)?)\\s+(\\w+)\\s*\\(([^)]*)\\)\\s*(?:throws\\s+[\\w,\\s.]+)?\\s*\\{");
    
    // Regular expressions for parsing JavaDoc and comments
    private static final Pattern JAVADOC_PATTERN = Pattern.compile("/\\*\\*(.*?)\\*/", Pattern.DOTALL);
    private static final Pattern SINGLE_LINE_COMMENT_PATTERN = Pattern.compile("//(.*)$", Pattern.MULTILINE);
    private static final Pattern MULTI_LINE_COMMENT_PATTERN = Pattern.compile("/\\*(.*?)\\*/", Pattern.DOTALL);
    private static final Pattern JAVADOC_TAG_PATTERN = Pattern.compile("@(\\w+)\\s+(.*?)(?=\\s*@|\\s*\\*/|$)", Pattern.DOTALL);

    /**
     * Main method that prompts for a directory and processes all Java files.
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the root directory to analyze: ");
        String rootDirectory = scanner.nextLine();
        scanner.close();

        try {
            File rootDir = new File(rootDirectory);
            if (!rootDir.exists() || !rootDir.isDirectory()) {
                System.err.println("Invalid directory: " + rootDirectory);
                return;
            }

            Map<String, List<ClassStructure>> packageToClassesMap = new HashMap<>();
            
            // Find all Java files in the directory and its subdirectories
            List<File> javaFiles = findJavaFiles(rootDir);
            System.out.println("Found " + javaFiles.size() + " Java files to analyze.");
            
            // Process each Java file
            for (File file : javaFiles) {
                try {
                    String content = Files.readString(file.toPath());
                    
                    // Extract package name
                    String packageName = extractPackageName(content);
                    
                    // Extract class structure
                    ClassStructure classStructure = extractClassStructure(file.getName(), content);
                    classStructure.setPackageName(packageName);
                    
                    // Extract class comments
                    classStructure.setJavadoc(extractClassJavadoc(content));
                    classStructure.setComments(extractClassComments(content));
                    
                    // Add to the package map
                    packageToClassesMap.computeIfAbsent(packageName, k -> new ArrayList<>())
                                      .add(classStructure);
                    
                    System.out.println("Processed: " + file.getPath());
                } catch (IOException e) {
                    System.err.println("Error processing file " + file.getPath() + ": " + e.getMessage());
                }
            }
            
            // Convert to JSON and save to file
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(packageToClassesMap);
            
            String outputFile = rootDirectory + File.separator + "java-structure.json";
            try (FileWriter writer = new FileWriter(outputFile)) {
                writer.write(json);
                System.out.println("Analysis complete. JSON output saved to: " + outputFile);
            }
            
        } catch (Exception e) {
            System.err.println("Error during analysis: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Find all Java files in a directory and its subdirectories.
     */
    private static List<File> findJavaFiles(File directory) {
        List<File> javaFiles = new ArrayList<>();
        findJavaFilesRecursive(directory, javaFiles);
        return javaFiles;
    }

    /**
     * Recursive helper method to find all Java files.
     */
    private static void findJavaFilesRecursive(File directory, List<File> javaFiles) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    findJavaFilesRecursive(file, javaFiles);
                } else if (file.getName().endsWith(".java")) {
                    javaFiles.add(file);
                }
            }
        }
    }

    /**
     * Extract the package name from a Java file content.
     */
    private static String extractPackageName(String content) {
        Matcher matcher = PACKAGE_PATTERN.matcher(content);
        return matcher.find() ? matcher.group(1) : "default";
    }

    /**
     * Extract the class JavaDoc from a Java file content.
     */
    private static JavadocStructure extractClassJavadoc(String content) {
        // Find class declaration position
        Matcher classMatcher = CLASS_PATTERN.matcher(content);
        if (!classMatcher.find()) {
            return null;
        }
        
        // Get position of class declaration
        int classPosition = classMatcher.start();
        
        // Search for JavaDoc before class declaration
        String contentBeforeClass = content.substring(0, classPosition);
        Matcher javadocMatcher = JAVADOC_PATTERN.matcher(contentBeforeClass);
        
        // Find the last JavaDoc before class declaration
        JavadocStructure javadoc = null;
        while (javadocMatcher.find()) {
            String javadocText = javadocMatcher.group(1).trim();
            javadoc = parseJavadoc(javadocText);
        }
        
        return javadoc;
    }

    /**
     * Extract all non-JavaDoc comments around the class declaration.
     */
    private static List<String> extractClassComments(String content) {
        List<String> comments = new ArrayList<>();
        
        // Find class declaration position
        Matcher classMatcher = CLASS_PATTERN.matcher(content);
        if (!classMatcher.find()) {
            return comments;
        }
        
        // Get position of class declaration
        int classPosition = classMatcher.start();
        
        // Define a window around the class declaration (200 chars before and after)
        int startPos = Math.max(0, classPosition - 200);
        int endPos = Math.min(content.length(), classPosition + 200);
        String windowContent = content.substring(startPos, endPos);
        
        // Extract single-line comments
        Matcher singleLineCommentMatcher = SINGLE_LINE_COMMENT_PATTERN.matcher(windowContent);
        while (singleLineCommentMatcher.find()) {
            comments.add(singleLineCommentMatcher.group(1).trim());
        }
        
        // Extract multi-line comments (excluding JavaDoc)
        Matcher multiLineCommentMatcher = MULTI_LINE_COMMENT_PATTERN.matcher(windowContent);
        while (multiLineCommentMatcher.find()) {
            String commentText = multiLineCommentMatcher.group(1).trim();
            // If it doesn't start with *, it's not a JavaDoc
            if (!commentText.startsWith("*")) {
                comments.add(commentText);
            }
        }
        
        return comments;
    }

    /**
     * Extract the class structure from a Java file content.
     */
    private static ClassStructure extractClassStructure(String fileName, String content) {
        ClassStructure classStructure = new ClassStructure();
        classStructure.setFileName(fileName);
        
        // Extract class name
        Matcher classMatcher = CLASS_PATTERN.matcher(content);
        if (classMatcher.find()) {
            classStructure.setClassName(classMatcher.group(4));
            classStructure.setClassType(classMatcher.group(3)); // class, interface, or enum
        } else {
            classStructure.setClassName(fileName.replace(".java", ""));
            classStructure.setClassType("class");
        }
        
        // Extract methods
        List<MethodStructure> methods = extractMethods(content);
        classStructure.setMethods(methods);
        
        return classStructure;
    }

    /**
     * Extract all methods from a Java file content.
     */
    private static List<MethodStructure> extractMethods(String content) {
        List<MethodStructure> methods = new ArrayList<>();
        Matcher methodMatcher = METHOD_PATTERN.matcher(content);
        
        while (methodMatcher.find()) {
            MethodStructure method = new MethodStructure();
            
            String accessModifier = methodMatcher.group(1);
            String staticModifier = methodMatcher.group(2);
            String returnType = methodMatcher.group(3);
            String methodName = methodMatcher.group(4);
            String parameters = methodMatcher.group(5);
            
            method.setAccessModifier(accessModifier != null ? accessModifier : "");
            method.setStatic(staticModifier != null);
            method.setReturnType(returnType);
            method.setMethodName(methodName);
            method.setParameters(parseParameters(parameters));
            
            // Extract method comments and JavaDoc
            int methodStart = methodMatcher.start();
            method.setJavadoc(extractMethodJavadoc(content, methodStart));
            method.setComments(extractMethodComments(content, methodStart));
            
            // Find the method body
            String methodSignature = methodMatcher.group(0);
            methodStart = content.indexOf(methodSignature);
            if (methodStart >= 0) {
                // Position after the method signature
                int bodyStart = methodStart + methodSignature.length();
                
                // Search for the opening brace if not part of the signature
                if (!methodSignature.trim().endsWith("{")) {
                    bodyStart = content.indexOf("{", bodyStart);
                    if (bodyStart < 0) continue; // Skip if no body found
                }
                
                // Find the matching closing brace
                int bodyEnd = findMatchingBrace(content, bodyStart);
                if (bodyEnd > bodyStart) {
                    // Extract the method body including braces
                    String body = content.substring(bodyStart, bodyEnd + 1);
                    method.setBody(body);
                }
            }
            
            methods.add(method);
        }
        
        return methods;
    }

    /**
     * Extract JavaDoc for a method.
     */
    private static JavadocStructure extractMethodJavadoc(String content, int methodPosition) {
        // Search for JavaDoc before method
        int searchStart = Math.max(0, methodPosition - 500); // Look up to 500 chars before method
        String contentBefore = content.substring(searchStart, methodPosition);
        
        Matcher javadocMatcher = JAVADOC_PATTERN.matcher(contentBefore);
        
        // Find the last JavaDoc before method
        JavadocStructure javadoc = null;
        while (javadocMatcher.find()) {
            String javadocText = javadocMatcher.group(1).trim();
            javadoc = parseJavadoc(javadocText);
        }
        
        return javadoc;
    }

    /**
     * Extract non-JavaDoc comments for a method.
     */
    private static List<String> extractMethodComments(String content, int methodPosition) {
        List<String> comments = new ArrayList<>();
        
        // Define a window around the method declaration
        int startPos = Math.max(0, methodPosition - 200);
        int endPos = Math.min(content.length(), methodPosition + 100);
        String windowContent = content.substring(startPos, endPos);
        
        // Extract single-line comments
        Matcher singleLineCommentMatcher = SINGLE_LINE_COMMENT_PATTERN.matcher(windowContent);
        while (singleLineCommentMatcher.find()) {
            comments.add(singleLineCommentMatcher.group(1).trim());
        }
        
        // Extract multi-line comments (excluding JavaDoc)
        Matcher multiLineCommentMatcher = MULTI_LINE_COMMENT_PATTERN.matcher(windowContent);
        while (multiLineCommentMatcher.find()) {
            String commentText = multiLineCommentMatcher.group(1).trim();
            // If it doesn't start with *, it's not a JavaDoc
            if (!commentText.startsWith("*")) {
                comments.add(commentText);
            }
        }
        
        return comments;
    }

    /**
     * Parse a JavaDoc comment text into a structured representation.
     */
    private static JavadocStructure parseJavadoc(String javadocText) {
        JavadocStructure javadoc = new JavadocStructure();
        
        // Clean up the JavaDoc text by removing leading asterisks and extra whitespace
        String cleanJavadoc = javadocText.replaceAll("^\\s*\\*\\s*", "")
                                          .replaceAll("\\n\\s*\\*\\s*", "\n")
                                          .trim();
        
        // Split into main description and tags
        int firstTagIndex = cleanJavadoc.indexOf("@");
        
        if (firstTagIndex == -1) {
            // No tags, whole text is description
            javadoc.setDescription(cleanJavadoc);
        } else {
            // Extract description (text before first @tag)
            javadoc.setDescription(cleanJavadoc.substring(0, firstTagIndex).trim());
            
            // Extract tags
            List<JavadocTag> tags = new ArrayList<>();
            Matcher tagMatcher = JAVADOC_TAG_PATTERN.matcher(cleanJavadoc);
            
            while (tagMatcher.find()) {
                String tagName = tagMatcher.group(1);
                String tagValue = tagMatcher.group(2).trim();
                
                JavadocTag tag = new JavadocTag();
                tag.setName(tagName);
                tag.setValue(tagValue);
                tags.add(tag);
            }
            
            javadoc.setTags(tags);
        }
        
        return javadoc;
    }

    /**
     * Find the position of the matching closing brace for an opening brace.
     */
    private static int findMatchingBrace(String content, int openingBracePos) {
        int count = 1;
        for (int i = openingBracePos + 1; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') {
                count++;
            } else if (c == '}') {
                count--;
                if (count == 0) {
                    return i;
                }
            }
        }
        return -1; // No matching brace found
    }

    /**
     * Parse method parameters into a list of Parameter objects.
     */
    private static List<Parameter> parseParameters(String parameterString) {
        List<Parameter> parameters = new ArrayList<>();
        
        if (parameterString == null || parameterString.trim().isEmpty()) {
            return parameters;
        }
        
        String[] paramArray = parameterString.split(",");
        for (String param : paramArray) {
            param = param.trim();
            if (!param.isEmpty()) {
                String[] parts = param.split("\\s+");
                if (parts.length >= 2) {
                    Parameter parameter = new Parameter();
                    parameter.setType(parts[0]);
                    parameter.setName(parts[1].replace(")", ""));
                    parameters.add(parameter);
                }
            }
        }
        
        return parameters;
    }

    /**
     * Structure to represent a Java class.
     */
    static class ClassStructure {
        private String fileName;
        private String packageName;
        private String className;
        private String classType; // class, interface, or enum
        private List<MethodStructure> methods;
        private JavadocStructure javadoc;
        private List<String> comments;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getClassType() {
            return classType;
        }

        public void setClassType(String classType) {
            this.classType = classType;
        }

        public List<MethodStructure> getMethods() {
            return methods;
        }

        public void setMethods(List<MethodStructure> methods) {
            this.methods = methods;
        }

        public JavadocStructure getJavadoc() {
            return javadoc;
        }

        public void setJavadoc(JavadocStructure javadoc) {
            this.javadoc = javadoc;
        }

        public List<String> getComments() {
            return comments;
        }

        public void setComments(List<String> comments) {
            this.comments = comments;
        }
    }

    /**
     * Structure to represent a Java method.
     */
    static class MethodStructure {
        private String accessModifier;
        private boolean isStatic;
        private String returnType;
        private String methodName;
        private List<Parameter> parameters;
        private String body;
        private JavadocStructure javadoc;
        private List<String> comments;

        public String getAccessModifier() {
            return accessModifier;
        }

        public void setAccessModifier(String accessModifier) {
            this.accessModifier = accessModifier;
        }

        public boolean isStatic() {
            return isStatic;
        }

        public void setStatic(boolean isStatic) {
            this.isStatic = isStatic;
        }

        public String getReturnType() {
            return returnType;
        }

        public void setReturnType(String returnType) {
            this.returnType = returnType;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public List<Parameter> getParameters() {
            return parameters;
        }

        public void setParameters(List<Parameter> parameters) {
            this.parameters = parameters;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public JavadocStructure getJavadoc() {
            return javadoc;
        }

        public void setJavadoc(JavadocStructure javadoc) {
            this.javadoc = javadoc;
        }

        public List<String> getComments() {
            return comments;
        }

        public void setComments(List<String> comments) {
            this.comments = comments;
        }
    }

    /**
     * Structure to represent a method parameter.
     */
    static class Parameter {
        private String type;
        private String name;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
    
    /**
     * Structure to represent a JavaDoc comment.
     */
    static class JavadocStructure {
        private String description;
        private List<JavadocTag> tags;
        
        public JavadocStructure() {
            this.tags = new ArrayList<>();
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public List<JavadocTag> getTags() {
            return tags;
        }
        
        public void setTags(List<JavadocTag> tags) {
            this.tags = tags;
        }
    }
    
    /**
     * Structure to represent a JavaDoc tag.
     */
    static class JavadocTag {
        private String name;
        private String value;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getValue() {
            return value;
        }
        
        public void setValue(String value) {
            this.value = value;
        }
    }
}
```