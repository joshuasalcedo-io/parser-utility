package io.joshuasalcedo.parsers;

import io.joshuasalcedo.model.markdown.*;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterVisitor;
import org.commonmark.ext.ins.InsExtension;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Utility class for parsing Markdown content using CommonMark.
 */
public final class MarkdownParser {
    
    // Prevent instantiation
    private MarkdownParser() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Parse markdown content into a structured MarkdownContent object.
     * 
     * @param markdown The markdown content to parse
     * @return MarkdownContent object with structured information
     */
    public static MarkdownContent parseMarkdown(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return MarkdownContent.builder().rawContent("").htmlContent("").build();
        }
        
        // Create parser with extensions
        List<org.commonmark.Extension> extensions = Arrays.asList(
                AutolinkExtension.create(),
                TablesExtension.create(),
                StrikethroughExtension.create(),
                InsExtension.create(),
                HeadingAnchorExtension.create(),
                YamlFrontMatterExtension.create()
        );
        
        Parser parser = Parser.builder()
                .extensions(extensions)
                .build();
        
        HtmlRenderer renderer = HtmlRenderer.builder()
                .extensions(extensions)
                .build();
        
        // Parse markdown
        Node document = parser.parse(markdown);
        
        // Process front matter
        YamlFrontMatterVisitor frontMatterVisitor = new YamlFrontMatterVisitor();
        document.accept(frontMatterVisitor);
        Map<String, Object> frontMatter = convertFrontMatter(frontMatterVisitor.getData());
        
        // Collect structural elements
        List<MarkdownHeading> headings = new ArrayList<>();
        List<MarkdownLink> links = new ArrayList<>();
        List<MarkdownImage> images = new ArrayList<>();
        List<MarkdownCodeBlock> codeBlocks = new ArrayList<>();
        
        // Extract structure by visiting nodes
        StructureCollector collector = new StructureCollector(headings, links, images, codeBlocks);
        document.accept(collector);
        
        // Extract title (first heading if available)
        String title = null;
        if (!headings.isEmpty()) {
            title = headings.get(0).getText();
        }
        
        // Count words and estimate reading time
        int wordCount = countWords(markdown);
        int readingTimeMinutes = calculateReadingTime(wordCount);
        
        // Render HTML
        String htmlContent = renderer.render(document);
        
        return MarkdownContent.builder()
                .title(title)
                .rawContent(markdown)
                .htmlContent(htmlContent)
                .headings(headings)
                .links(links)
                .images(images)
                .codeBlocks(codeBlocks)
                .frontMatter(frontMatter)
                .wordCount(wordCount)
                .readingTimeMinutes(readingTimeMinutes)
                .build();
    }
    
    /**
     * Parse markdown content from a file.
     * 
     * @param file The markdown file to parse
     * @return MarkdownContent object with structured information
     * @throws IOException If an IO error occurs
     */
    public static MarkdownContent parseMarkdownFile(File file) throws IOException {
        String content = Files.readString(file.toPath());
        return parseMarkdown(content);
    }
    
    /**
     * Parse markdown content from a Path.
     * 
     * @param path The path to the markdown file
     * @return MarkdownContent object with structured information
     * @throws IOException If an IO error occurs
     */
    public static MarkdownContent parseMarkdownFile(Path path) throws IOException {
        String content = Files.readString(path);
        return parseMarkdown(content);
    }
    
    /**
     * Parse markdown content from a Reader.
     * 
     * @param reader The reader containing markdown content
     * @return MarkdownContent object with structured information
     * @throws IOException If an IO error occurs
     */
    public static MarkdownContent parseMarkdownReader(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[4096];
        int n;
        while ((n = reader.read(buffer)) != -1) {
            sb.append(buffer, 0, n);
        }
        return parseMarkdown(sb.toString());
    }
    
    /**
     * Simply convert markdown to HTML.
     * 
     * @param markdown The markdown content to convert
     * @return HTML representation of the markdown
     */
    public static String markdownToHtml(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }
        
        List<org.commonmark.Extension> extensions = Arrays.asList(
                AutolinkExtension.create(),
                TablesExtension.create(),
                StrikethroughExtension.create(),
                InsExtension.create()
        );
        
        Parser parser = Parser.builder()
                .extensions(extensions)
                .build();
        
        HtmlRenderer renderer = HtmlRenderer.builder()
                .extensions(extensions)
                .build();
        
        Node document = parser.parse(markdown);
        return renderer.render(document);
    }
    
    /**
     * Extract all headings from markdown content.
     * 
     * @param markdown The markdown content to process
     * @return List of headings
     */
    public static List<MarkdownHeading> extractHeadings(String markdown) {
        MarkdownContent content = parseMarkdown(markdown);
        return content.getHeadings();
    }
    
    /**
     * Extract all links from markdown content.
     * 
     * @param markdown The markdown content to process
     * @return List of links
     */
    public static List<MarkdownLink> extractLinks(String markdown) {
        MarkdownContent content = parseMarkdown(markdown);
        return content.getLinks();
    }
    
    /**
     * Extract all images from markdown content.
     * 
     * @param markdown The markdown content to process
     * @return List of images
     */
    public static List<MarkdownImage> extractImages(String markdown) {
        MarkdownContent content = parseMarkdown(markdown);
        return content.getImages();
    }
    
    /**
     * Extract all code blocks from markdown content.
     * 
     * @param markdown The markdown content to process
     * @return List of code blocks
     */
    public static List<MarkdownCodeBlock> extractCodeBlocks(String markdown) {
        MarkdownContent content = parseMarkdown(markdown);
        return content.getCodeBlocks();
    }
    
    /**
     * Count the words in a markdown text.
     *
     * @param markdown The markdown content
     * @return The word count
     */
    private static int countWords(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return 0;
        }
        
        // Remove YAML front matter if present
        String content = markdown;
        if (content.startsWith("---")) {
            int end = content.indexOf("---", 3);
            if (end > 0) {
                content = content.substring(end + 3);
            }
        }
        
        // Remove code blocks
        content = content.replaceAll("```.*?```", " ");
        
        // Remove HTML tags
        content = content.replaceAll("<[^>]*>", " ");
        
        // Remove markdown symbols
        content = content.replaceAll("[#*_~`\\[\\](){}|]+", " ");
        
        // Split by whitespace and count non-empty words
        String[] words = content.trim().split("\\s+");
        int count = 0;
        for (String word : words) {
            if (!word.isEmpty()) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Calculate estimated reading time in minutes.
     *
     * @param wordCount The number of words
     * @return Estimated reading time in minutes
     */
    private static int calculateReadingTime(int wordCount) {
        // Average reading speed: 200-250 words per minute
        final int wordsPerMinute = 225;
        int minutes = wordCount / wordsPerMinute;
        if (wordCount % wordsPerMinute > 0) {
            minutes++;
        }
        return Math.max(1, minutes);
    }
    
    /**
     * Convert front matter data to a map of objects.
     *
     * @param frontMatterData The front matter data from the visitor
     * @return Converted front matter map
     */
    private static Map<String, Object> convertFrontMatter(Map<String, List<String>> frontMatterData) {
        Map<String, Object> result = new HashMap<>();
        
        for (Map.Entry<String, List<String>> entry : frontMatterData.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            
            if (values.size() == 1) {
                // Single value
                result.put(key, values.get(0));
            } else {
                // Multiple values
                result.put(key, values);
            }
        }
        
        return result;
    }
    
    /**
     * A node visitor that collects structural elements from a markdown document.
     */
    private static class StructureCollector extends AbstractVisitor {
        private final List<MarkdownHeading> headings;
        private final List<MarkdownLink> links;
        private final List<MarkdownImage> images;
        private final List<MarkdownCodeBlock> codeBlocks;
        private int nodePosition = 0;
        
        public StructureCollector(
                List<MarkdownHeading> headings,
                List<MarkdownLink> links,
                List<MarkdownImage> images,
                List<MarkdownCodeBlock> codeBlocks) {
            this.headings = headings;
            this.links = links;
            this.images = images;
            this.codeBlocks = codeBlocks;
        }
        
        @Override
        public void visit(Heading heading) {
            String id = "";
            // Get heading ID from attributes if available
            if (heading.getFirstChild() instanceof Text) {
                String text = ((Text) heading.getFirstChild()).getLiteral();
                // Generate simplified ID from heading text
                id = text.toLowerCase()
                        .replaceAll("[^\\w\\s-]", "")
                        .replaceAll("\\s+", "-");
            }
            
            StringBuilder textBuilder = new StringBuilder();
            Node child = heading.getFirstChild();
            while (child != null) {
                if (child instanceof Text) {
                    textBuilder.append(((Text) child).getLiteral());
                }
                child = child.getNext();
            }
            
            MarkdownHeading mdHeading = MarkdownHeading.builder()
                    .level(heading.getLevel())
                    .text(textBuilder.toString())
                    .id(id)
                    .position(nodePosition++)
                    .build();
            
            headings.add(mdHeading);
            visitChildren(heading);
        }
        
        @Override
        public void visit(Link link) {
            String text = "";
            if (link.getFirstChild() instanceof Text) {
                text = ((Text) link.getFirstChild()).getLiteral();
            }
            
            MarkdownLink mdLink = MarkdownLink.builder()
                    .text(text)
                    .url(link.getDestination())
                    .title(link.getTitle())
                    .internal(link.getDestination().startsWith("#"))
                    .build();
            
            links.add(mdLink);
            visitChildren(link);
        }
        
        @Override
        public void visit(Image image) {
            MarkdownImage mdImage = MarkdownImage.builder()
                    .altText(image.getTitle())
                    .url(image.getDestination())
                    .title(image.getTitle())
                    .local(!image.getDestination().startsWith("http"))
                    .build();
            
            images.add(mdImage);
            visitChildren(image);
        }
        
        @Override
        public void visit(FencedCodeBlock codeBlock) {
            MarkdownCodeBlock mdCodeBlock = MarkdownCodeBlock.builder()
                    .content(codeBlock.getLiteral())
                    .language(codeBlock.getInfo())
                    .fenced(true)
                    .position(nodePosition++)
                    .build();
            
            codeBlocks.add(mdCodeBlock);
            visitChildren(codeBlock);
        }
        
        @Override
        public void visit(IndentedCodeBlock codeBlock) {
            MarkdownCodeBlock mdCodeBlock = MarkdownCodeBlock.builder()
                    .content(codeBlock.getLiteral())
                    .language(null)
                    .fenced(false)
                    .position(nodePosition++)
                    .build();
            
            codeBlocks.add(mdCodeBlock);
            visitChildren(codeBlock);
        }
    }
}