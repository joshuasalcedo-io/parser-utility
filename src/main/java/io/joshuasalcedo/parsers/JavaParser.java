package io.joshuasalcedo.parsers;

import io.joshuasalcedo.model.javafile.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing Java files and extracting structural information.
 * This class provides static methods to analyze Java source code.
 */
public final class JavaParser {
    // Prevent instantiation
    private JavaParser() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    // Regular expression patterns
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("package\\s+([\\w.]+)\\s*;");
    private static final Pattern CLASS_PATTERN = Pattern.compile("(public|private|protected)?\\s*(static)?\\s*(class|interface|enum)\\s+(\\w+)");
    private static final Pattern METHOD_PATTERN = Pattern.compile("(public|private|protected)?\\s*(static)?\\s*(\\w+(?:<.*>)?)\\s+(\\w+)\\s*\\(([^)]*)\\)");
    private static final Pattern METHOD_BODY_PATTERN = Pattern.compile("(public|private|protected)?\\s*(static)?\\s*(\\w+(?:<.*>)?)\\s+(\\w+)\\s*\\(([^)]*)\\)\\s*(?:throws\\s+[\\w,\\s.]+)?\\s*\\{");
    private static final Pattern JAVADOC_PATTERN = Pattern.compile("/\\*\\*(.*?)\\*/", Pattern.DOTALL);
    private static final Pattern SINGLE_LINE_COMMENT_PATTERN = Pattern.compile("//(.*)$", Pattern.MULTILINE);
    private static final Pattern MULTI_LINE_COMMENT_PATTERN = Pattern.compile("/\\*(.*?)\\*/", Pattern.DOTALL);
    private static final Pattern JAVADOC_TAG_PATTERN = Pattern.compile("@(\\w+)\\s+(.*?)(?=\\s*@|\\s*\\*/|$)", Pattern.DOTALL);

    /**
     * Find all Java files in a directory and its subdirectories.
     *
     * @param directory The directory to search
     * @return List of Java files
     */
    public static List<File> findJavaFiles(File directory) {
        List<File> javaFiles = new ArrayList<>();
        findJavaFilesRecursive(directory, javaFiles);
        return javaFiles;
    }

    /**
     * Recursive helper method to find all Java files.
     *
     * @param directory The directory to search
     * @param javaFiles List to collect Java files
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
     * Extract the package name from Java file content.
     *
     * @param content The Java file content
     * @return The package name or "default" if not found
     */
    public static String extractPackageName(String content) {
        Matcher matcher = PACKAGE_PATTERN.matcher(content);
        return matcher.find() ? matcher.group(1) : "default";
    }

    /**
     * Extract the class JavaDoc from Java file content.
     *
     * @param content The Java file content
     * @return JavadocStructure object or null if not found
     */
    public static JavadocStructure extractClassJavadoc(String content) {
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
     *
     * @param content The Java file content
     * @return List of comments
     */
    public static List<String> extractClassComments(String content) {
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
     * Extract the class structure from Java file content.
     *
     * @param fileName The Java file name
     * @param content The Java file content
     * @return ClassStructure object
     */
    public static ClassStructure extractClassStructure(String fileName, String content) {
        String packageName = extractPackageName(content);

        // Extract class name and type
        String className = fileName.replace(".java", "");
        String classType = "class";

        Matcher classMatcher = CLASS_PATTERN.matcher(content);
        if (classMatcher.find()) {
            className = classMatcher.group(4);
            classType = classMatcher.group(3); // class, interface, or enum
        }

        // Extract methods
        List<MethodStructure> methods = extractMethods(content);

        // Extract JavaDoc and comments
        JavadocStructure javadoc = extractClassJavadoc(content);
        List<String> comments = extractClassComments(content);

        // Assuming you've built a proper ClassStructure builder elsewhere
        return ClassStructure.builder()
                .fileName(fileName)
                .packageName(packageName)
                .className(className)
                .classType(classType)
                .methods(methods)
                .javadoc(javadoc)
                .comments(comments)
                .build();
    }

    /**
     * Extract all methods from a Java file content.
     *
     * @param content The Java file content
     * @return List of MethodStructure objects
     */
    public static List<MethodStructure> extractMethods(String content) {
        List<MethodStructure> methods = new ArrayList<>();
        Matcher methodMatcher = METHOD_PATTERN.matcher(content);

        while (methodMatcher.find()) {
            String accessModifier = methodMatcher.group(1);
            String staticModifier = methodMatcher.group(2);
            String returnType = methodMatcher.group(3);
            String methodName = methodMatcher.group(4);
            String parameters = methodMatcher.group(5);

            // Extract method comments and JavaDoc
            int methodStart = methodMatcher.start();
            JavadocStructure javadoc = extractMethodJavadoc(content, methodStart);
            List<String> comments = extractMethodComments(content, methodStart);

            // Find the method body
            String body = "";
            String methodSignature = methodMatcher.group(0);
            methodStart = content.indexOf(methodSignature);
            if (methodStart >= 0) {
                // Position after the method signature
                int bodyStart = methodStart + methodSignature.length();

                // Search for the opening brace if not part of the signature
                if (!methodSignature.trim().endsWith("{")) {
                    bodyStart = content.indexOf("{", bodyStart);
                    if (bodyStart >= 0) {
                        // Find the matching closing brace
                        int bodyEnd = findMatchingBrace(content, bodyStart);
                        if (bodyEnd > bodyStart) {
                            // Extract the method body including braces
                            body = content.substring(bodyStart, bodyEnd + 1);
                        }
                    }
                }
            }

            // Assuming you've built a proper MethodStructure builder elsewhere
            MethodStructure method =  MethodStructure.builder()
                    .accessModifier(accessModifier != null ? accessModifier : "")
                    .isStatic(staticModifier != null)
                    .returnType(returnType)
                    .methodName(methodName)
                    .parameters(parseParameters(parameters))
                    .body(body)
                    .javadoc(javadoc)
                    .comments(comments)
                    .build();

            methods.add(method);
        }

        return methods;
    }

    /**
     * Extract JavaDoc for a method.
     *
     * @param content The Java file content
     * @param methodPosition Position of method in content
     * @return JavadocStructure object or null if not found
     */
    public static JavadocStructure extractMethodJavadoc(String content, int methodPosition) {
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
     *
     * @param content The Java file content
     * @param methodPosition Position of method in content
     * @return List of comments
     */
    public static List<String> extractMethodComments(String content, int methodPosition) {
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
     *
     * @param javadocText The JavaDoc text to parse
     * @return JavadocStructure object
     */
    public static JavadocStructure parseJavadoc(String javadocText) {
        // Clean up the JavaDoc text by removing leading asterisks and extra whitespace
        String cleanJavadoc = javadocText.replaceAll("^\\s*\\*\\s*", "")
                .replaceAll("\\n\\s*\\*\\s*", "\n")
                .trim();

        // Split into main description and tags
        int firstTagIndex = cleanJavadoc.indexOf("@");
        String description = "";

        if (firstTagIndex == -1) {
            // No tags, whole text is description
            description = cleanJavadoc;
        } else {
            // Extract description (text before first @tag)
            description = cleanJavadoc.substring(0, firstTagIndex).trim();
        }

        // Extract tags
        List<JavadocTag> tags = new ArrayList<>();
        if (firstTagIndex != -1) {
            Matcher tagMatcher = JAVADOC_TAG_PATTERN.matcher(cleanJavadoc);

            while (tagMatcher.find()) {
                String tagName = tagMatcher.group(1);
                String tagValue = tagMatcher.group(2).trim();

                // Assuming you've built a proper JavadocTag builder elsewhere
                JavadocTag tag =  JavadocTag.builder()
                        .name(tagName)
                        .value(tagValue)
                        .build();

                tags.add(tag);
            }
        }

        // Assuming you've built a proper JavadocStructure builder elsewhere
        return  JavadocStructure.builder()
                .description(description)
                .tags(tags)
                .build();
    }

    /**
     * Find the position of the matching closing brace for an opening brace.
     *
     * @param content The text to search in
     * @param openingBracePos Position of the opening brace
     * @return Position of the matching closing brace or -1 if not found
     */
    public static int findMatchingBrace(String content, int openingBracePos) {
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
         * Parse a single parameter string into a Parameter object.
         *
         * @param parameterString The single parameter string (e.g., "String name")
         * @return Parameter object or null if parsing fails
         */
        public static Parameter parseParameter(String parameterString) {
            if (parameterString == null || parameterString.trim().isEmpty()) {
                return null;
            }

            String cleanParam = parameterString.trim();
            String[] parts = cleanParam.split("\\s+");

            if (parts.length >= 2) {
                return Parameter.builder()
                        .type(parts[0])
                        .name(parts[1].replace(")", ""))
                        .build();
            }

            return null;
        }

        /**
         * Parse method parameters into a list of Parameter objects.
         *
         * @param parameterString The parameter string from the method signature
         * @return List of Parameter objects
         */
        public static List<Parameter> parseParameters(String parameterString) {
            List<Parameter> parameters = new ArrayList<>();

            if (parameterString == null || parameterString.trim().isEmpty()) {
                return parameters;
            }

            String[] paramArray = parameterString.split(",");
            for (String param : paramArray) {
                Parameter parameter = parseParameter(param);
                if (parameter != null) {
                    parameters.add(parameter);
                }
            }

            return parameters;
        }

}