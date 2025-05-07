package io.joshuasalcedo.model.markdown;

import lombok.Builder;
import lombok.Data;

/**
 * Represents an image in a Markdown document.
 */
@Data
@Builder
public class MarkdownImage {
    /**
     * The alt text of the image.
     */
    private String altText;
    
    /**
     * The URL of the image.
     */
    private String url;
    
    /**
     * The title of the image, if any.
     */
    private String title;
    
    /**
     * Whether this is a local image (file path) or an external image (URL).
     */
    private boolean local;
}