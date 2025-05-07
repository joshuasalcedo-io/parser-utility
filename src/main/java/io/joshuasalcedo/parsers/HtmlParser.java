package io.joshuasalcedo.parsers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for parsing HTML documents using JSoup.
 */
public final class HtmlParser {

    private HtmlParser() {
        // Utility class, prevent instantiation
    }

    /**
     * Parse HTML content from a string.
     *
     * @param html    The HTML content
     * @param baseUri The base URI for resolving relative links
     * @return JSoup Document object
     */
    public static Document parse(String html, String baseUri) {
        return Jsoup.parse(html, baseUri);
    }

    /**
     * Parse HTML content from a file.
     *
     * @param file    The HTML file
     * @param charset The character set of the file
     * @param baseUri The base URI for resolving relative links
     * @return JSoup Document object
     * @throws IOException If an IO error occurs
     */
    public static Document parse(File file, String charset, String baseUri) throws IOException {
        return Jsoup.parse(file, charset, baseUri);
    }

    /**
     * Parse HTML content from a Path.
     *
     * @param path    The path to the HTML file
     * @param baseUri The base URI for resolving relative links
     * @return JSoup Document object
     * @throws IOException If an IO error occurs
     */
    public static Document parse(Path path, String baseUri) throws IOException {
        String html = Files.readString(path, StandardCharsets.UTF_8);
        return parse(html, baseUri);
    }

    /**
     * Parse HTML content from an InputStream.
     *
     * @param inputStream The input stream containing HTML content
     * @param charset     The character set of the stream
     * @param baseUri     The base URI for resolving relative links
     * @return JSoup Document object
     * @throws IOException If an IO error occurs
     */
    public static Document parse(InputStream inputStream, String charset, String baseUri) throws IOException {
        return Jsoup.parse(inputStream, charset, baseUri);
    }

    /**
     * Connect to a URL and parse the returned HTML.
     *
     * @param url The URL to connect to
     * @return JSoup Document object
     * @throws IOException If an IO error occurs
     */
    public static Document parseUrl(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .get();
    }

    /**
     * Get all headings (h1-h6) from a document.
     *
     * @param document The JSoup Document
     * @return Elements containing all headings
     */
    public static Elements getHeadings(Document document) {
        return document.select("h1, h2, h3, h4, h5, h6");
    }

    /**
     * Get all links from a document.
     *
     * @param document The JSoup Document
     * @return Elements containing all links
     */
    public static Elements getLinks(Document document) {
        return document.select("a[href]");
    }

    /**
     * Get all images from a document.
     *
     * @param document The JSoup Document
     * @return Elements containing all images
     */
    public static Elements getImages(Document document) {
        return document.select("img");
    }

    /**
     * Get all tables from a document.
     *
     * @param document The JSoup Document
     * @return Elements containing all tables
     */
    public static Elements getTables(Document document) {
        return document.select("table");
    }

    /**
     * Get all forms from a document.
     *
     * @param document The JSoup Document
     * @return Elements containing all forms
     */
    public static Elements getForms(Document document) {
        return document.select("form");
    }

    /**
     * Get all list elements (ul, ol) from a document.
     *
     * @param document The JSoup Document
     * @return Elements containing all lists
     */
    public static Elements getLists(Document document) {
        return document.select("ul, ol");
    }

    /**
     * Get all elements with the specified class name.
     *
     * @param document  The JSoup Document
     * @param className The class name to search for
     * @return Elements containing all matching elements
     */
    public static Elements getElementsByClass(Document document, String className) {
        return document.getElementsByClass(className);
    }

    /**
     * Get the element with the specified ID.
     *
     * @param document The JSoup Document
     * @param id       The ID to search for
     * @return The element with the specified ID, or null if not found
     */
    public static Element getElementById(Document document, String id) {
        return document.getElementById(id);
    }

    /**
     * Get all elements matching the specified CSS selector.
     *
     * @param document    The JSoup Document
     * @param cssSelector The CSS selector to match
     * @return Elements containing all matching elements
     */
    public static Elements select(Document document, String cssSelector) {
        return document.select(cssSelector);
    }

    /**
     * Extract all metadata from the document head.
     *
     * @param document The JSoup Document
     * @return Map of metadata key-value pairs
     */
    public static Map<String, String> getMetadata(Document document) {
        Map<String, String> metadata = new HashMap<>();

        // Title
        metadata.put("title", document.title());

        // Meta tags
        Elements metaTags = document.select("meta");
        for (Element metaTag : metaTags) {
            if (metaTag.hasAttr("name") && metaTag.hasAttr("content")) {
                metadata.put(metaTag.attr("name"), metaTag.attr("content"));
            } else if (metaTag.hasAttr("property") && metaTag.hasAttr("content")) {
                metadata.put(metaTag.attr("property"), metaTag.attr("content"));
            }
        }

        return metadata;
    }

    /**
     * Convert an HTML table to a two-dimensional array of strings.
     *
     * @param table The JSoup table Element
     * @return 2D array of table cell contents
     */
    public static String[][] tableToArray(Element table) {
        Elements rows = table.select("tr");
        int rowCount = rows.size();

        // Find the maximum number of columns
        int maxColumns = 0;
        for (Element row : rows) {
            int cellCount = row.select("td, th").size();
            if (cellCount > maxColumns) {
                maxColumns = cellCount;
            }
        }

        String[][] tableArray = new String[rowCount][maxColumns];

        for (int i = 0; i < rowCount; i++) {
            Element row = rows.get(i);
            Elements cells = row.select("td, th");

            for (int j = 0; j < cells.size(); j++) {
                tableArray[i][j] = cells.get(j).text();
            }
        }

        return tableArray;
    }

    /**
     * Extract text content from a document, removing all HTML tags.
     *
     * @param document The JSoup Document
     * @return Plain text content
     */
    public static String getTextContent(Document document) {
        return document.text();
    }

    /**
     * Extract structured data from elements matching a CSS selector.
     * Useful for scraping specific content patterns from a website.
     *
     * @param document   The JSoup Document
     * @param selector   CSS selector to find elements
     * @param attributes List of attributes to extract from each element
     * @return 2D array of extracted data, with each row representing one element and columns for attributes
     */
    public static String[][] extractData(Document document, String selector, String... attributes) {
        Elements elements = document.select(selector);
        String[][] data = new String[elements.size()][attributes.length];

        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            for (int j = 0; j < attributes.length; j++) {
                String attribute = attributes[j];
                if (attribute.equals("text")) {
                    data[i][j] = element.text();
                } else if (attribute.equals("html")) {
                    data[i][j] = element.html();
                } else if (attribute.equals("outerHtml")) {
                    data[i][j] = element.outerHtml();
                } else {
                    data[i][j] = element.attr(attribute);
                }
            }
        }

        return data;
    }

    /**
     * Extract data with nested elements. Useful for scraping structured content
     * like product listings with multiple data points.
     *
     * @param document          The JSoup Document
     * @param containerSelector CSS selector for the container elements
     * @param fieldSelectors    Map of field names to relative CSS selectors within each container
     * @return Map of field names to extracted values for each container
     */
    public static Map<String, String>[] extractStructuredData(Document document, String containerSelector, Map<String, String> fieldSelectors) {
        Elements containers = document.select(containerSelector);
        @SuppressWarnings("unchecked")
        Map<String, String>[] result = new HashMap[containers.size()];

        for (int i = 0; i < containers.size(); i++) {
            Element container = containers.get(i);
            Map<String, String> item = new HashMap<>();

            for (Map.Entry<String, String> field : fieldSelectors.entrySet()) {
                String fieldName = field.getKey();
                String fieldSelector = field.getValue();

                Element fieldElement = container.select(fieldSelector).first();
                if (fieldElement != null) {
                    item.put(fieldName, fieldElement.text());
                } else {
                    item.put(fieldName, "");
                }
            }

            result[i] = item;
        }

        return result;
    }

    /**
     * Scrape a paginated website by following the next page links.
     *
     * @param startUrl         The URL of the first page
     * @param nextPageSelector CSS selector for the next page link
     * @param maxPages         Maximum number of pages to scrape
     * @return Array of Documents, one for each page
     * @throws IOException If an IO error occurs
     */
    public static Document[] scrapePaginated(String startUrl, String nextPageSelector, int maxPages) throws IOException {
        Document[] pages = new Document[maxPages];
        String currentUrl = startUrl;

        for (int i = 0; i < maxPages; i++) {
            Document page = parseUrl(currentUrl);
            pages[i] = page;

            Element nextPageLink = page.select(nextPageSelector).first();
            if (nextPageLink == null || !nextPageLink.hasAttr("href")) {
                break;
            }

            String nextUrl = nextPageLink.absUrl("href");
            if (nextUrl.isEmpty() || nextUrl.equals(currentUrl)) {
                break;
            }

            currentUrl = nextUrl;
        }

        return pages;
    }

    /**
     * Extract data from a table into a list of maps, where each map represents a row
     * with column headers as keys.
     *
     * @param table The JSoup table Element
     * @return Array of maps, each representing a row with column names as keys
     */
    public static Map<String, String>[] tableToMaps(Element table) {
        Elements rows = table.select("tr");
        if (rows.isEmpty()) {
            return new HashMap[0];
        }

        // Extract headers
        Elements headerCells = rows.first().select("th");
        if (headerCells.isEmpty()) {
            headerCells = rows.first().select("td");
        }

        String[] headers = new String[headerCells.size()];
        for (int i = 0; i < headerCells.size(); i++) {
            headers[i] = headerCells.get(i).text();
        }

        // Extract data rows
        @SuppressWarnings("unchecked")
        Map<String, String>[] result = new HashMap[rows.size() - 1];

        for (int i = 1; i < rows.size(); i++) {
            Element row = rows.get(i);
            Elements cells = row.select("td");

            Map<String, String> rowMap = new HashMap<>();
            for (int j = 0; j < Math.min(headers.length, cells.size()); j++) {
                rowMap.put(headers[j], cells.get(j).text());
            }

            result[i - 1] = rowMap;
        }

        return result;
    }

    /**
     * Check if elements matching a selector exist in the document.
     *
     * @param document The JSoup Document
     * @param selector CSS selector to check
     * @return true if at least one matching element exists
     */
    public static boolean exists(Document document, String selector) {
        return !document.select(selector).isEmpty();
    }

    /**
     * Get text content of an element, or empty string if the selector doesn't match any elements.
     *
     * @param document The JSoup Document
     * @param selector CSS selector to find element
     * @return Text content of the first matching element, or empty string if none match
     */
    public static String getText(Document document, String selector) {
        Element element = document.select(selector).first();
        return element != null ? element.text() : "";
    }

    /**
     * Get attribute value of an element, or empty string if the selector doesn't match any elements.
     *
     * @param document  The JSoup Document
     * @param selector  CSS selector to find element
     * @param attribute Attribute name to get
     * @return Attribute value of the first matching element, or empty string if none match
     */
    public static String getAttr(Document document, String selector, String attribute) {
        Element element = document.select(selector).first();
        return element != null ? element.attr(attribute) : "";
    }

}