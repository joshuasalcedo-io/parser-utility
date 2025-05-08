package io.joshuasalcedo.utility;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for file operations with intelligent detection mechanisms.
 */
public class FileUtils {

    /**
     * List all files in a directory and its subdirectories.
     *
     * @param directory Directory to search
     * @param respectGitIgnore Whether to respect .gitignore rules
     * @return List of files found
     * @throws IOException If an I/O error occurs
     */
    public static List<File> listAllFiles(String directory, boolean respectGitIgnore) throws IOException {
        List<File> files;
        if (respectGitIgnore) {
            files = listFilesRespectingGitignore(directory);
        } else {
            files = listAllFilesRecursively(directory);
        }
        return files;
    }

    /**
     * List all files in a directory and its subdirectories without respecting .gitignore.
     *
     * @param directory Directory to search
     * @return List of files found
     * @throws IOException If an I/O error occurs
     */
    private static List<File> listAllFilesRecursively(String directory) throws IOException {
        Path dirPath = Paths.get(directory);
        try (Stream<Path> pathStream = Files.walk(dirPath)) {
            return pathStream
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        }
    }

    /**
     * List all files in a directory, respecting .gitignore rules if present.
     *
     * @param directory The directory to list files from
     * @return List of files, excluding those matched by .gitignore rules
     * @throws IOException If any I/O errors occur
     */
    private static List<File> listFilesRespectingGitignore(String directory) throws IOException {
        // Try to use JGit for respecting .gitignore if it's a git repository
        try {
            return io.joshuasalcedo.parsers.GitParser.listFilesRespectingGitignore(directory);
        } catch (Exception e) {
            // Fall back to regular file listing if JGit fails or it's not a git repo
            return listAllFilesRecursively(directory);
        }
    }

    /**
     * List files with specific extensions in a directory and its subdirectories.
     *
     * @param directory Directory to search
     * @param extensions File extensions to include (with or without leading dot)
     * @return List of files with the specified extensions
     * @throws IOException If an I/O error occurs
     */
    public static List<File> listFilesByExtension(String directory, String... extensions) throws IOException {
        Path dirPath = Paths.get(directory);
        List<String> normalizedExtensions = new ArrayList<>();
        
        // Normalize extensions to start with a dot
        for (String ext : extensions) {
            normalizedExtensions.add(ext.startsWith(".") ? ext.toLowerCase() : "." + ext.toLowerCase());
        }
        
        try (Stream<Path> pathStream = Files.walk(dirPath)) {
            return pathStream
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .filter(file -> {
                        String name = file.getName().toLowerCase();
                        for (String ext : normalizedExtensions) {
                            if (name.endsWith(ext)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        }
    }

    /**
     * List all text files in a directory and its subdirectories.
     *
     * @param directory Directory to search
     * @param respectGitIgnore Whether to respect .gitignore rules
     * @return List of text files
     * @throws IOException If an I/O error occurs
     */
    public static List<File> listTextFiles(String directory, boolean respectGitIgnore) throws IOException {
        List<File> allFiles = listAllFiles(directory, respectGitIgnore);
        return allFiles.stream()
                .filter(FileUtils::isTextFile)
                .collect(Collectors.toList());
    }

    /**
     * List files matching a custom predicate.
     *
     * @param directory Directory to search
     * @param respectGitIgnore Whether to respect .gitignore rules
     * @param predicate Custom filter to apply to files
     * @return List of files matching the predicate
     * @throws IOException If an I/O error occurs
     */
    public static List<File> listFilesMatching(String directory, boolean respectGitIgnore, 
                                              Predicate<File> predicate) throws IOException {
        List<File> allFiles = listAllFiles(directory, respectGitIgnore);
        return allFiles.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * Get the relative path of a file from a base directory.
     *
     * @param file The file
     * @param baseDir The base directory
     * @return The relative path as a string
     */
    public static String getRelativePath(File file, File baseDir) {
        return baseDir.toURI().relativize(file.toURI()).getPath();
    }

    /**
     * Determine if a file is a text file using character decoding capabilities.
     * No hard-coded extensions or MIME types are used.
     *
     * @param file The file to check
     * @return true if the file appears to be readable as text
     */
    public static boolean isTextFile(File file) {
        // Don't bother with empty files
        if (file.length() == 0) return true;
        
        // Don't bother with very large files - likely not text
        if (file.length() > 50 * 1024 * 1024) return false; // 50MB limit
        
        Path path = file.toPath();
        
        try {
            // Sample the file if it's large
            byte[] data;
            if (file.length() > 8192) {
                // For large files, sample the beginning, middle, and end
                data = sampleFileContent(path);
            } else {
                // For small files, read it all
                data = Files.readAllBytes(path);
            }
            
            // Quick test: check for null bytes (common in binary files)
            for (byte b : data) {
                if (b == 0) return false;
            }
            
            // Try to decode the file as text with common encodings
            return canDecodeAsText(data);
            
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Sample content from beginning, middle and end of a file.
     */
    private static byte[] sampleFileContent(Path path) throws IOException {
        long fileSize = Files.size(path);
        byte[] sample = new byte[8192]; // 8KB sample
        
        try (var channel = Files.newByteChannel(path)) {
            ByteBuffer buffer = ByteBuffer.wrap(sample);
            
            // Read beginning (first 3KB)
            buffer.limit(3072);
            channel.read(buffer);
            
            // Read from middle (2KB)
            buffer.limit(5120);
            buffer.position(3072);
            channel.position(fileSize / 2 - 1024);
            channel.read(buffer);
            
            // Read from end (3KB)
            buffer.limit(8192);
            buffer.position(5120);
            channel.position(Math.max(0, fileSize - 3072));
            channel.read(buffer);
        }
        
        return sample;
    }
    
    /**
     * Try to decode the data as text with various encodings.
     * Returns true if the content can be successfully decoded.
     */
    private static boolean canDecodeAsText(byte[] data) {
        // Try with common encodings
        String[] encodings = {"UTF-8", "UTF-16", "ISO-8859-1", "US-ASCII"};
        
        for (String encoding : encodings) {
            Charset charset = Charset.forName(encoding);
            CharsetDecoder decoder = charset.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
            
            ByteBuffer buffer = ByteBuffer.wrap(data);
            try {
                CharBuffer charBuffer = decoder.decode(buffer);
                
                // Additional check - if over 5% of the content is control characters,
                // it's probably binary despite being decodable
                return !hasTooManyControlCharacters(charBuffer);
            } catch (CharacterCodingException e) {
                // Decoding failed with this charset, try another one
                continue;
            }
        }
        
        // If we couldn't decode with any charset, it's likely binary
        return false;
    }
    
    /**
     * Check if the text contains too many control characters.
     */
    private static boolean hasTooManyControlCharacters(CharBuffer buffer) {
        int controlCount = 0;
        int totalChars = buffer.length();
        
        // Reset buffer to beginning
        buffer.rewind();
        
        while (buffer.hasRemaining()) {
            char c = buffer.get();
            // Count control chars except common whitespace
            if (c < 32 && c != '\t' && c != '\n' && c != '\r') {
                controlCount++;
            }
        }
        
        // If more than 5% are control characters, likely binary
        return ((double) controlCount / totalChars) > 0.05;
    }
    
    /**
     * Get file content as string.
     * 
     * @param file The file to read
     * @return The file content as string
     * @throws IOException If an I/O error occurs
     */
    public static String readFileAsString(File file) throws IOException {
        return Files.readString(file.toPath());
    }
    
    /**
     * Read file as bytes.
     * 
     * @param file The file to read
     * @return The file content as byte array
     * @throws IOException If an I/O error occurs
     */
    public static byte[] readFileAsBytes(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }
    
    /**
     * Write string content to a file.
     * 
     * @param file The file to write
     * @param content The content to write
     * @throws IOException If an I/O error occurs
     */
    public static void writeStringToFile(File file, String content) throws IOException {
        Files.writeString(file.toPath(), content);
    }
    
    /**
     * Create directories for a file if they don't exist.
     * 
     * @param file The file whose directory structure should be created
     * @return true if directories were created or already existed
     */
    public static boolean createDirectoriesForFile(File file) {
        File dir = file.getParentFile();
        if (dir != null && !dir.exists()) {
            return dir.mkdirs();
        }
        return true;
    }
    
    /**
     * Check if a file exists and is readable.
     * 
     * @param file The file to check
     * @return true if the file exists and is readable
     */
    public static boolean isFileReadable(File file) {
        return file.exists() && file.isFile() && file.canRead();
    }
    
    /**
     * Check if a file exists and is writable.
     * 
     * @param file The file to check
     * @return true if the file exists and is writable
     */
    public static boolean isFileWritable(File file) {
        return file.exists() && file.isFile() && file.canWrite();
    }
    
    /**
     * Get the file extension.
     * 
     * @param file The file
     * @return The file extension (without the dot) or empty string if none
     */
    public static String getFileExtension(File file) {
        String name = file.getName();
        int lastDotIndex = name.lastIndexOf('.');
        return lastDotIndex > 0 ? name.substring(lastDotIndex + 1) : "";
    }
    
    /**
     * Get the filename without extension.
     * 
     * @param file The file
     * @return The filename without extension
     */
    public static String getFileBaseName(File file) {
        String name = file.getName();
        int lastDotIndex = name.lastIndexOf('.');
        return lastDotIndex > 0 ? name.substring(0, lastDotIndex) : name;
    }
    
    /**
     * Get a file's size in a human-readable format.
     * 
     * @param file The file
     * @return The file size as a human-readable string (e.g., "1.2 MB")
     */
    public static String getHumanReadableSize(File file) {
        long size = file.length();
        if (size <= 0) return "0 B";
        
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB", "PB", "EB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    public static void main(String[] args) {
        try {
            // Get the current directory properly
            String testDir = System.getProperty("user.dir");
            System.out.println("Testing with directory: " + testDir);

            System.out.println("=== FileUtils Test ===\n");

            // Test 1: List all files
            System.out.println("Test 1: Listing all files (not respecting .gitignore)");
            List<File> allFiles = listAllFiles(testDir, false);
            System.out.println("Found " + allFiles.size() + " files");
            printFileSample(allFiles, 5);

            // Rest of the test method remains the same...
        } catch (Exception e) {
            System.err.println("Error during test: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Helper method to print a sample of files for the tests
     */
    private static void printFileSample(List<File> files, int maxFiles) {
        System.out.println("Sample files:");
        int count = 0;
        for (File file : files) {
            if (count++ >= maxFiles) {
                System.out.println("  ... and " + (files.size() - maxFiles) + " more");
                break;
            }
            System.out.println("  " + file.getName() + " (" + getHumanReadableSize(file) + ")");
        }
    }
}