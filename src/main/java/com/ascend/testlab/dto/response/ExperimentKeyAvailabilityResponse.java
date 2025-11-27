package com.ascend.testlab.dto.response;

/**
 * Response DTO for the Experiment Key Availability API containing the availability status.
 *
 * @param isAvailable Indicates if the experiment key is available.
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 */
public record ExperimentKeyAvailabilityResponse(Boolean isAvailable) {}
