package io.joshuasalcedo.parsers;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HtmlParserTest {

    private File testHtmlFile;
    private File outputFile;

    @BeforeEach
    void setUp() {
        // Set up the test HTML file location
        testHtmlFile = new File("src/test/resources/test/html/test.html");
        assertTrue(testHtmlFile.exists(), "Test HTML file does not exist");

        // Set up output file
        outputFile = new File("src/test/resources/test/html/test-output.txt");
    }

    @Test
    void testParseAndExtractElements() throws IOException {
        // Create output file writer
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            // Parse the HTML file
            Document document = HtmlParser.parse(testHtmlFile, "UTF-8", "");

            // Test basic HTML parsing
            assertNotNull(document, "Document should not be null");
            String title = document.title();
            assertEquals("Test HTML Document", title, "Document title should match");

            // Print document title
            System.out.println("Document Title: " + title);
            writer.println("Document Title: " + title);

            // Test extracting headings
            Elements headings = HtmlParser.getHeadings(document);
            assertFalse(headings.isEmpty(), "There should be at least one heading");

            // Print headings
            System.out.println("\n=== HEADINGS ===");
            writer.println("\n=== HEADINGS ===");
            for (Element heading : headings) {
                System.out.println(heading.tagName() + ": " + heading.text());
                writer.println(heading.tagName() + ": " + heading.text());
            }

            // Test extracting links
            Elements links = HtmlParser.getLinks(document);
            assertFalse(links.isEmpty(), "There should be at least one link");

            // Print links
            System.out.println("\n=== LINKS ===");
            writer.println("\n=== LINKS ===");
            for (Element link : links) {
                System.out.println("Link: " + link.text() + " -> " + link.attr("href"));
                writer.println("Link: " + link.text() + " -> " + link.attr("href"));
            }

            // Test metadata extraction
            Map<String, String> metadata = HtmlParser.getMetadata(document);
            assertNotNull(metadata, "Metadata should not be null");
            assertTrue(metadata.containsKey("title"), "Metadata should contain title");

            // Print metadata
            System.out.println("\n=== METADATA ===");
            writer.println("\n=== METADATA ===");
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
                writer.println(entry.getKey() + ": " + entry.getValue());
            }

            // Test tables
            Elements tables = HtmlParser.getTables(document);

            // Print tables
            System.out.println("\n=== TABLES ===");
            writer.println("\n=== TABLES ===");
            for (Element table : tables) {
                System.out.println("Table found with " + table.select("tr").size() + " rows");
                writer.println("Table found with " + table.select("tr").size() + " rows");

                // Extract table data as array
                String[][] tableData = HtmlParser.tableToArray(table);
                for (String[] row : tableData) {
                    for (String cell : row) {
                        System.out.print(cell + "\t| ");
                        writer.print(cell + "\t| ");
                    }
                    System.out.println();
                    writer.println();
                }

                // Extract table as maps
                Map<String, String>[] tableMaps = HtmlParser.tableToMaps(table);
                System.out.println("\nTable as Maps:");
                writer.println("\nTable as Maps:");
                for (Map<String, String> row : tableMaps) {
                    System.out.println(row);
                    writer.println(row);
                }
            }

            // Test images
            Elements images = HtmlParser.getImages(document);

            // Print images
            System.out.println("\n=== IMAGES ===");
            writer.println("\n=== IMAGES ===");
            for (Element image : images) {
                System.out.println("Image: " + image.attr("src") + " (Alt: " + image.attr("alt") + ")");
                writer.println("Image: " + image.attr("src") + " (Alt: " + image.attr("alt") + ")");
            }

            // Test lists
            Elements lists = HtmlParser.getLists(document);

            // Print lists
            System.out.println("\n=== LISTS ===");
            writer.println("\n=== LISTS ===");
            for (Element list : lists) {
                System.out.println(list.tagName() + " with " + list.select("li").size() + " items:");
                writer.println(list.tagName() + " with " + list.select("li").size() + " items:");
                for (Element item : list.select("li")) {
                    System.out.println("- " + item.text());
                    writer.println("- " + item.text());
                }
            }

            System.out.println("\nHTML parsing test completed. Results written to: " + outputFile.getAbsolutePath());
        }
    }

    @Test
    void testExtractingTextContent() throws IOException {
        // Parse the HTML file
        Document document = HtmlParser.parse(testHtmlFile, "UTF-8", "");

        // Test extracting text content
        String textContent = HtmlParser.getTextContent(document);
        assertNotNull(textContent, "Text content should not be null");
        assertFalse(textContent.isEmpty(), "Text content should not be empty");

        // Print text content
        System.out.println("\n=== FULL TEXT CONTENT ===");
        System.out.println(textContent);

        // Write to file
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile, true))) {
            writer.println("\n=== FULL TEXT CONTENT ===");
            writer.println(textContent);
        }
    }

    @Test
    void testCssSelectors() throws IOException {
        // Parse the HTML file
        Document document = HtmlParser.parse(testHtmlFile, "UTF-8", "");

        // Write to file
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile, true))) {
            // Test using CSS selectors
            Elements paragraphs = HtmlParser.select(document, "p");
            assertFalse(paragraphs.isEmpty(), "There should be at least one paragraph");

            // Print paragraphs
            System.out.println("\n=== PARAGRAPHS ===");
            writer.println("\n=== PARAGRAPHS ===");
            for (Element p : paragraphs) {
                System.out.println(p.text());
                writer.println(p.text());
            }

            // Test custom selectors
            Elements containerDiv = HtmlParser.select(document, "div.container");

            // Print container info
            System.out.println("\n=== CONTAINER DIV ===");
            writer.println("\n=== CONTAINER DIV ===");
            if (!containerDiv.isEmpty()) {
                System.out.println("Container found with " + containerDiv.first().children().size() + " child elements");
                writer.println("Container found with " + containerDiv.first().children().size() + " child elements");
            }

            // Test checking if elements exist
            boolean hasDivs = HtmlParser.exists(document, "div");
            boolean hasForm = HtmlParser.exists(document, "form");

            System.out.println("\n=== ELEMENT EXISTS ===");
            writer.println("\n=== ELEMENT EXISTS ===");
            System.out.println("Has divs: " + hasDivs);
            writer.println("Has divs: " + hasDivs);
            System.out.println("Has forms: " + hasForm);
            writer.println("Has forms: " + hasForm);
        }
    }
}