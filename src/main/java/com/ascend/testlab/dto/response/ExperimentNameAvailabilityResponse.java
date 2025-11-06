package com.ascend.testlab.dto.response;

/**
 * Record encapsulating the response for checking if an experiment name is available.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 */
public record ExperimentNameAvailabilityResponse(Boolean isAvailable, String message) {}
