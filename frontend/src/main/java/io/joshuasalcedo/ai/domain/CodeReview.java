package io.joshuasalcedo.ai.domain;

import java.util.List;

/**
 * Record representing a structured code review.
 */
public record CodeReview(
    String overallAssessment,
    List<String> bugs,
    List<String> performanceSuggestions,
    List<String> readabilityImprovements,
    List<String> securityConcerns
) {}