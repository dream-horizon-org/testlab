package com.ascend.testlab.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response DTO for the Tags API containing a list of experiment tags.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 * @param tags the list of distinct tag strings for the project
 */
public record TagsResponse(@JsonProperty("tags") List<String> tags) {}
