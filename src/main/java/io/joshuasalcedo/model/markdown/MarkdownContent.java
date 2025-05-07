package io.joshuasalcedo.model.markdown;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Represents the parsed structure of a Markdown document.
 */
@Data
@Builder
public class MarkdownContent {
    /**
     * Title extracted from the document (usually from the first heading).
     */
    private String title;
    
    /**
     * Full raw markdown content.
     */
    private String rawContent;
    
    /**
     * HTML rendered from the markdown.
     */
    private String htmlContent;
    
    /**
     * List of all headings in the document.
     */
    private List<MarkdownHeading> headings;
    
    /**
     * List of all links in the document.
     */
    private List<MarkdownLink> links;
    
    /**
     * List of all images in the document.
     */
    private List<MarkdownImage> images;
    
    /**
     * List of all code blocks in the document.
     */
    private List<MarkdownCodeBlock> codeBlocks;
    
    /**
     * Front matter metadata if present (for formats like Jekyll).
     */
    private Map<String, Object> frontMatter;
    
    /**
     * Word count of the document.
     */
    private int wordCount;
    
    /**
     * Estimated reading time in minutes.
     */
    private int readingTimeMinutes;
}