package io.joshuasalcedo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.joshuasalcedo.parsers.Parser;
import io.joshuasalcedo.utility.console.ConsoleFormatterFactory;
import io.joshuasalcedo.utility.console.MessageType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Main entry point for the Parser utility.
 * Processes command-line arguments and executes the appropriate parsing functions.
 */
public class ParserRunner {

    // Map command names to their titles for consistent display formatting
    private static final Map<String, String> COMMAND_TITLES = new HashMap<>();

    static {
        COMMAND_TITLES.put("all", "PARSING ALL");
        COMMAND_TITLES.put("java", "PARSING JAVA FILES");
        COMMAND_TITLES.put("git", "PARSING GIT REPOSITORY");
        COMMAND_TITLES.put("pom", "PARSING POM FILES");
        COMMAND_TITLES.put("html", "PARSING HTML FILES");
        COMMAND_TITLES.put("markdown", "PARSING MARKDOWN FILES");
    }

    // Map command names to their corresponding parser functions
    private static final Map<String, Function<String, Map<String, Object>>> PARSER_FUNCTIONS = new HashMap<>();

    static {
        PARSER_FUNCTIONS.put("all", Parser.parse::all);
        PARSER_FUNCTIONS.put("java", Parser.parse::java);
        PARSER_FUNCTIONS.put("git", Parser.parse::git);
        PARSER_FUNCTIONS.put("pom", Parser.parse::pom);
        PARSER_FUNCTIONS.put("html", Parser.parse::html);
        PARSER_FUNCTIONS.put("markdown", Parser.parse::markdown);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            printWarning("No arguments provided. Use 'help' for usage information.");
            help();
            return;
        }

        String command = args[0];

        switch (command) {
            case "help":
                help();
                break;
            case "version":
                showVersion();
                break;
            case "all":
            case "java":
            case "git":
            case "pom":
            case "html":
            case "markdown":
                handleParserCommand(command, args);
                break;
            default:
                // Assume it's a file path and try to parse it based on extension
                parseFile(command);
                break;
        }
    }

    /**
     * Handles parser commands that require a directory path
     *
     * @param command The command to execute
     * @param args Command-line arguments
     */
    private static void handleParserCommand(String command, String[] args) {
        if (args.length < 2) {
            printError("Please provide a directory path.");
            return;
        }

        String directory = args[1];
        executeParser(command, directory);
    }

    /**
     * Execute a parser based on the command name and directory
     *
     * @param command The command name
     * @param directory The directory to parse
     */
    private static void executeParser(String command, String directory) {
        String title = COMMAND_TITLES.getOrDefault(command, "PARSING " + command.toUpperCase());
        System.out.println(ConsoleFormatterFactory.createDivider(title));

        printInfo("Parsing " + (command.equals("git") ? "Git repository" : command + " files") + " in: " + directory);

        try {
            File dir = new File(directory);
            if (!validateDirectory(dir)) {
                return;
            }

            // Get the parser function from the map and execute it
            Function<String, Map<String, Object>> parserFunction = PARSER_FUNCTIONS.get(command);
            if (parserFunction == null) {
                printError("Unknown parser command: " + command);
                return;
            }

            Map<String, Object> result = parserFunction.apply(directory);
            String outputPath = saveResultToJsonFile(result, directory, command);
            outputResult(result, outputPath);

        } catch (Exception e) {
            printError("Error executing parser: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Validates if a directory exists and is a directory
     *
     * @param directory The directory to validate
     * @return true if directory is valid, false otherwise
     */
    private static boolean validateDirectory(File directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            printError("Directory does not exist: " + directory);
            return false;
        }
        return true;
    }

    /**
     * Parse a single file based on its extension
     *
     * @param filePath Path to the file
     */
    private static void parseFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            printError("File does not exist: " + filePath);
            return;
        }

        String fileName = file.getName().toLowerCase();
        String parserType = determineParserType(fileName);

        if (parserType == null) {
            printError("Unsupported file type: " + fileName);
            return;
        }

        System.out.println(ConsoleFormatterFactory.createDivider("PARSING " + parserType.toUpperCase() + " FILE"));
        printWarning(parserType + " file parsing not implemented for single files yet.");
    }

    /**
     * Determine the parser type based on file name
     *
     * @param fileName The file name
     * @return The parser type or null if unsupported
     */
    private static String determineParserType(String fileName) {
        if (fileName.endsWith(".java")) {
            return "java";
        } else if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
            return "html";
        } else if (fileName.equals("pom.xml")) {
            return "pom";
        } else if (fileName.endsWith(".md") || fileName.endsWith(".markdown")) {
            return "markdown";
        }
        return null;
    }

    /**
     * Save parsing results to a JSON file in the .parsed directory
     *
     * @param result The parsing result map
     * @param directory The directory that was parsed
     * @param parserType The type of parser used (all, java, git, etc.)
     * @return The path to the saved JSON file
     */
    private static String saveResultToJsonFile(Map<String, Object> result, String directory, String parserType) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(result);

        // Create output filename
        String filename = String.format("parser_%s.json", parserType);

        // Create .parsed directory where the command is executed
        File parsedDir = createOutputDirectory();

        // Create the output file in the .parsed directory
        return writeJsonToFile(json, parsedDir, filename);
    }

    /**
     * Creates the output directory for storing results
     *
     * @return The created directory or current directory as fallback
     */
    private static File createOutputDirectory() {
        File currentDirectory = new File(".");
        File parsedDir = new File(currentDirectory, ".parsed");

        // Create the directory if it doesn't exist
        if (!parsedDir.exists()) {
            if (!parsedDir.mkdir()) {
                printWarning("Failed to create .parsed directory. Using current directory instead.");
                parsedDir = currentDirectory;
            }
        }

        return parsedDir;
    }

    /**
     * Write JSON content to a file
     *
     * @param json The JSON content to write
     * @param directory The directory to write to
     * @param filename The filename to use
     * @return The path to the saved file or null if failed
     */
    private static String writeJsonToFile(String json, File directory, String filename) {
        File outputFile = new File(directory, filename);

        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(json);
            return outputFile.getAbsolutePath();
        } catch (IOException e) {
            printError("Error saving JSON to file: " + e.getMessage());

            // Fallback to saving in the current directory
            try {
                outputFile = new File(filename);
                try (FileWriter writer = new FileWriter(outputFile)) {
                    writer.write(json);
                    return outputFile.getAbsolutePath();
                }
            } catch (IOException ex) {
                printError("Failed to save JSON file in fallback location: " + ex.getMessage());
                return null;
            }
        }
    }

    /**
     * Output the parsing results to console and show file save location
     *
     * @param result The parsing results
     * @param outputPath The path where results were saved
     */
    private static void outputResult(Map<String, Object> result, String outputPath) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(result);

        // Print the results to console
        printSuccess("Results:");
        System.out.println(ConsoleFormatterFactory.createMessage(json, MessageType.DOC_CODE));

        // Print information about saved file
        if (outputPath != null) {
            System.out.println(ConsoleFormatterFactory.createDivider("OUTPUT FILE"));
            printSuccess("Results saved to: " + outputPath);
        }
    }

    /**
     * Print version information
     */
    private static void showVersion() {
        System.out.println(ConsoleFormatterFactory.createBox("Java Structure Parser v1.0", MessageType.UI_HEADER));
        System.out.println(ConsoleFormatterFactory.createMessage("Copyright © 2024 Joshua Salcedo", MessageType.UI_SUBHEADER));
    }

    /**
     * Print help documentation
     */
    public static void help() {
        System.out.println(ConsoleFormatterFactory.createBox("JAVA STRUCTURE PARSER", MessageType.UI_HEADER));

        System.out.println(ConsoleFormatterFactory.createDivider("USAGE"));
        System.out.println(ConsoleFormatterFactory.createMessage("java -jar parser-utility.jar [command] [arguments]", MessageType.DOC_CODE));

        System.out.println(ConsoleFormatterFactory.createDivider("COMMANDS"));
        System.out.println(ConsoleFormatterFactory.createMessage("help", MessageType.UI_LABEL) + "        - Show this help message");
        System.out.println(ConsoleFormatterFactory.createMessage("version", MessageType.UI_LABEL) + "     - Show version information");
        System.out.println(ConsoleFormatterFactory.createMessage("all", MessageType.UI_LABEL) + " [dir]   - Parse all supported files in the directory");
        System.out.println(ConsoleFormatterFactory.createMessage("java", MessageType.UI_LABEL) + " [dir]  - Parse Java files in the directory");
        System.out.println(ConsoleFormatterFactory.createMessage("git", MessageType.UI_LABEL) + " [dir]   - Parse Git repository in the directory");
        System.out.println(ConsoleFormatterFactory.createMessage("pom", MessageType.UI_LABEL) + " [dir]   - Parse POM files in the directory");
        System.out.println(ConsoleFormatterFactory.createMessage("html", MessageType.UI_LABEL) + " [dir]  - Parse HTML files in the directory");
        System.out.println(ConsoleFormatterFactory.createMessage("markdown", MessageType.UI_LABEL) + " [dir] - Parse Markdown files in the directory");
        System.out.println(ConsoleFormatterFactory.createMessage("[file]", MessageType.UI_LABEL) + "      - Parse the specified file based on its extension");

        System.out.println(ConsoleFormatterFactory.createDivider("EXAMPLES"));
        System.out.println(ConsoleFormatterFactory.createMessage("java -jar parser-utility.jar help", MessageType.DOC_CODE));
        System.out.println(ConsoleFormatterFactory.createMessage("java -jar parser-utility.jar all /path/to/project", MessageType.DOC_CODE));
        System.out.println(ConsoleFormatterFactory.createMessage("java -jar parser-utility.jar java /path/to/project", MessageType.DOC_CODE));
        System.out.println(ConsoleFormatterFactory.createMessage("java -jar parser-utility.jar pom.xml", MessageType.DOC_CODE));

        System.out.println(ConsoleFormatterFactory.createDivider("NOTES"));
        System.out.println(ConsoleFormatterFactory.createMessage("The parser outputs results as JSON both in the console and saves to a file in the parsed directory.", MessageType.DOC_NOTE));
        System.out.println(ConsoleFormatterFactory.createMessage("Output files are named 'parser_[type].json'", MessageType.DOC_NOTE));

        System.out.println(ConsoleFormatterFactory.createDivider("LICENSE"));
        System.out.println(ConsoleFormatterFactory.createMessage("Copyright © 2024 Joshua Salcedo", MessageType.SUCCESS));
    }

    // Utility methods for consistent message formatting

    private static void printInfo(String message) {
        System.out.println(ConsoleFormatterFactory.createMessage(message, MessageType.INFO));
    }

    private static void printError(String message) {
        System.out.println(ConsoleFormatterFactory.createMessage(message, MessageType.ERROR));
    }

    private static void printWarning(String message) {
        System.out.println(ConsoleFormatterFactory.createMessage(message, MessageType.WARNING));
    }

    private static void printSuccess(String message) {
        System.out.println(ConsoleFormatterFactory.createMessage(message, MessageType.SUCCESS));
    }
}