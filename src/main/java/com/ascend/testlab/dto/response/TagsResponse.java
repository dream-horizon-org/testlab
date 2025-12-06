package com.ascend.testlab.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;

/**
 * Response DTO for the Tags API containing a list of experiment tags.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 * @param tags the list of distinct tag strings for the project
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record TagsResponse(List<String> tags) {}
