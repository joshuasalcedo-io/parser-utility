package io.joshuasalcedo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.joshuasalcedo.parsers.Parser;
import io.joshuasalcedo.utility.console.ConsoleFormatterFactory;
import io.joshuasalcedo.utility.console.MessageType;

import java.io.File;
import java.util.Map;

public class ParserRunner {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println(ConsoleFormatterFactory.createMessage("No arguments provided. Use 'help' for usage information.", MessageType.WARNING));
            help();
            return;
        }

        String command = args[0];

        switch (command) {
            case "help":
                help();
                break;
            case "all":
                if (args.length < 2) {
                    System.out.println(ConsoleFormatterFactory.createMessage("Please provide a directory path.", MessageType.ERROR));
                    return;
                }
                parseAll(args[1]);
                break;
            case "java":
                if (args.length < 2) {
                    System.out.println(ConsoleFormatterFactory.createMessage("Please provide a directory path.", MessageType.ERROR));
                    return;
                }
                parseJava(args[1]);
                break;
            case "git":
                if (args.length < 2) {
                    System.out.println(ConsoleFormatterFactory.createMessage("Please provide a directory path.", MessageType.ERROR));
                    return;
                }
                parseGit(args[1]);
                break;
            case "pom":
                if (args.length < 2) {
                    System.out.println(ConsoleFormatterFactory.createMessage("Please provide a directory path.", MessageType.ERROR));
                    return;
                }
                parsePom(args[1]);
                break;
            case "html":
                if (args.length < 2) {
                    System.out.println(ConsoleFormatterFactory.createMessage("Please provide a directory path.", MessageType.ERROR));
                    return;
                }
                parseHtml(args[1]);
                break;
            case "version":
                showVersion();
                break;
            default:
                // Assume it's a file path and try to parse it based on extension
                parseFile(command);
                break;
        }
    }

    private static void parseAll(String directory) {
        System.out.println(ConsoleFormatterFactory.createDivider("PARSING ALL"));
        System.out.println(ConsoleFormatterFactory.createMessage("Parsing directory: " + directory, MessageType.INFO));

        try {
            File dir = new File(directory);
            if (!dir.exists() || !dir.isDirectory()) {
                System.out.println(ConsoleFormatterFactory.createMessage("Directory does not exist: " + directory, MessageType.ERROR));
                return;
            }

            Map<String, Object> result = Parser.parse.all(directory);
            outputResult(result);

        } catch (Exception e) {
            System.out.println(ConsoleFormatterFactory.createMessage("Error parsing directory: " + e.getMessage(), MessageType.ERROR));
            e.printStackTrace();
        }
    }

    private static void parseJava(String directory) {
        System.out.println(ConsoleFormatterFactory.createDivider("PARSING JAVA FILES"));
        System.out.println(ConsoleFormatterFactory.createMessage("Parsing Java files in: " + directory, MessageType.INFO));

        try {
            Map<String, Object> result = Parser.parse.java(directory);
            outputResult(result);
        } catch (Exception e) {
            System.out.println(ConsoleFormatterFactory.createMessage("Error parsing Java files: " + e.getMessage(), MessageType.ERROR));
            e.printStackTrace();
        }
    }

    private static void parseGit(String directory) {
        System.out.println(ConsoleFormatterFactory.createDivider("PARSING GIT REPOSITORY"));
        System.out.println(ConsoleFormatterFactory.createMessage("Parsing Git repository in: " + directory, MessageType.INFO));

        try {
            Map<String, Object> result = Parser.parse.git(directory);
            outputResult(result);
        } catch (Exception e) {
            System.out.println(ConsoleFormatterFactory.createMessage("Error parsing Git repository: " + e.getMessage(), MessageType.ERROR));
            e.printStackTrace();
        }
    }

    private static void parsePom(String directory) {
        System.out.println(ConsoleFormatterFactory.createDivider("PARSING POM FILES"));
        System.out.println(ConsoleFormatterFactory.createMessage("Parsing POM files in: " + directory, MessageType.INFO));

        try {
            Map<String, Object> result = Parser.parse.pom(directory);
            outputResult(result);
        } catch (Exception e) {
            System.out.println(ConsoleFormatterFactory.createMessage("Error parsing POM files: " + e.getMessage(), MessageType.ERROR));
            e.printStackTrace();
        }
    }

    private static void parseHtml(String directory) {
        System.out.println(ConsoleFormatterFactory.createDivider("PARSING HTML FILES"));
        System.out.println(ConsoleFormatterFactory.createMessage("Parsing HTML files in: " + directory, MessageType.INFO));

        try {
            Map<String, Object> result = Parser.parse.html(directory);
            outputResult(result);
        } catch (Exception e) {
            System.out.println(ConsoleFormatterFactory.createMessage("Error parsing HTML files: " + e.getMessage(), MessageType.ERROR));
            e.printStackTrace();
        }
    }

    private static void parseFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println(ConsoleFormatterFactory.createMessage("File does not exist: " + filePath, MessageType.ERROR));
            return;
        }

        String fileName = file.getName().toLowerCase();

        try {
            if (fileName.endsWith(".java")) {
                System.out.println(ConsoleFormatterFactory.createDivider("PARSING JAVA FILE"));
                // For single file parsing, you might need to add a method in your Parser class
                System.out.println(ConsoleFormatterFactory.createMessage("Java file parsing not implemented for single files yet.", MessageType.WARNING));
            } else if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
                System.out.println(ConsoleFormatterFactory.createDivider("PARSING HTML FILE"));
                // For single file parsing, you might need to add a method in your Parser class
                System.out.println(ConsoleFormatterFactory.createMessage("HTML file parsing not implemented for single files yet.", MessageType.WARNING));
            } else if (fileName.equals("pom.xml")) {
                System.out.println(ConsoleFormatterFactory.createDivider("PARSING POM FILE"));
                // For single file parsing, you might need to add a method in your Parser class
                System.out.println(ConsoleFormatterFactory.createMessage("POM file parsing not implemented for single files yet.", MessageType.WARNING));
            } else {
                System.out.println(ConsoleFormatterFactory.createMessage("Unsupported file type: " + fileName, MessageType.ERROR));
            }
        } catch (Exception e) {
            System.out.println(ConsoleFormatterFactory.createMessage("Error parsing file: " + e.getMessage(), MessageType.ERROR));
            e.printStackTrace();
        }
    }

    private static void outputResult(Map<String, Object> result) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(result);
        System.out.println(ConsoleFormatterFactory.createMessage("Results:", MessageType.SUCCESS));
        System.out.println(ConsoleFormatterFactory.createMessage(json, MessageType.DOC_CODE));
    }

    private static void showVersion() {
        System.out.println(ConsoleFormatterFactory.createBox("Java Structure Parser v1.0", MessageType.UI_HEADER));
        System.out.println(ConsoleFormatterFactory.createMessage("Copyright © 2024 Joshua Salcedo", MessageType.UI_SUBHEADER));
    }

    public static void help() {
        System.out.println(ConsoleFormatterFactory.createBox("JAVA STRUCTURE PARSER", MessageType.UI_HEADER));

        System.out.println(ConsoleFormatterFactory.createDivider("USAGE"));
        System.out.println(ConsoleFormatterFactory.createMessage("java -jar parser-utility.jar [command] [arguments]", MessageType.DOC_CODE));

        System.out.println(ConsoleFormatterFactory.createDivider("COMMANDS"));
        System.out.println(ConsoleFormatterFactory.createMessage("help", MessageType.UI_LABEL) + "      - Show this help message");
        System.out.println(ConsoleFormatterFactory.createMessage("version", MessageType.UI_LABEL) + "   - Show version information");
        System.out.println(ConsoleFormatterFactory.createMessage("all", MessageType.UI_LABEL) + " [dir] - Parse all supported files in the directory");
        System.out.println(ConsoleFormatterFactory.createMessage("java", MessageType.UI_LABEL) + " [dir] - Parse Java files in the directory");
        System.out.println(ConsoleFormatterFactory.createMessage("git", MessageType.UI_LABEL) + " [dir]  - Parse Git repository in the directory");
        System.out.println(ConsoleFormatterFactory.createMessage("pom", MessageType.UI_LABEL) + " [dir]  - Parse POM files in the directory");
        System.out.println(ConsoleFormatterFactory.createMessage("html", MessageType.UI_LABEL) + " [dir] - Parse HTML files in the directory");
        System.out.println(ConsoleFormatterFactory.createMessage("[file]", MessageType.UI_LABEL) + "     - Parse the specified file based on its extension");

        System.out.println(ConsoleFormatterFactory.createDivider("EXAMPLES"));
        System.out.println(ConsoleFormatterFactory.createMessage("java -jar parser-utility.jar help", MessageType.DOC_CODE));
        System.out.println(ConsoleFormatterFactory.createMessage("java -jar parser-utility.jar all /path/to/project", MessageType.DOC_CODE));
        System.out.println(ConsoleFormatterFactory.createMessage("java -jar parser-utility.jar java /path/to/project", MessageType.DOC_CODE));
        System.out.println(ConsoleFormatterFactory.createMessage("java -jar parser-utility.jar pom.xml", MessageType.DOC_CODE));

        System.out.println(ConsoleFormatterFactory.createDivider("NOTES"));
        System.out.println(ConsoleFormatterFactory.createMessage("The parser outputs results in JSON format for easy parsing by other tools.", MessageType.DOC_NOTE));

        System.out.println(ConsoleFormatterFactory.createDivider("LICENSE"));
        System.out.println(ConsoleFormatterFactory.createMessage("Copyright © 2024 Joshua Salcedo", MessageType.SUCCESS));
    }
}