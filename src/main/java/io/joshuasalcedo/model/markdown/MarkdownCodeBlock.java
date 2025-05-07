package io.joshuasalcedo.model.markdown;

import lombok.Builder;
import lombok.Data;

/**
 * Represents a code block in a Markdown document.
 */
@Data
@Builder
public class MarkdownCodeBlock {
    /**
     * The content of the code block.
     */
    private String content;
    
    /**
     * The language specified for the code block, if any.
     */
    private String language;
    
    /**
     * Whether this is a fenced code block or an indented code block.
     */
    private boolean fenced;
    
    /**
     * The position of the code block in the document.
     */
    private int position;
}