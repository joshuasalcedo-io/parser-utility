package io.joshuasalcedo.model.markdown;

import lombok.Builder;
import lombok.Data;

/**
 * Represents a heading in a Markdown document.
 */
@Data
@Builder
public class MarkdownHeading {
    /**
     * The level of the heading (1-6).
     */
    private int level;
    
    /**
     * The text content of the heading.
     */
    private String text;
    
    /**
     * The generated ID for the heading (for anchor links).
     */
    private String id;
    
    /**
     * The position of the heading in the document.
     */
    private int position;
}