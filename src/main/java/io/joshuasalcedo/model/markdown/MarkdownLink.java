package io.joshuasalcedo.model.markdown;

import lombok.Builder;
import lombok.Data;

/**
 * Represents a link in a Markdown document.
 */
@Data
@Builder
public class MarkdownLink {
    /**
     * The text of the link.
     */
    private String text;
    
    /**
     * The URL that the link points to.
     */
    private String url;
    
    /**
     * The title attribute of the link, if any.
     */
    private String title;
    
    /**
     * Whether this is an internal link (within the document).
     */
    private boolean internal;
}