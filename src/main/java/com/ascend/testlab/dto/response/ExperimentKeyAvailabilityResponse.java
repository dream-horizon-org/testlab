package com.ascend.testlab.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * Response DTO for the Experiment Key Availability API containing the availability status.
 *
 * @param isAvailable Indicates if the experiment key is available.
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ExperimentKeyAvailabilityResponse(Boolean isAvailable) {}
