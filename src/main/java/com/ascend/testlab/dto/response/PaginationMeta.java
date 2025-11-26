package com.ascend.testlab.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * Metadata class for pagination information used across multiple response DTOs.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 */
@Builder
public record PaginationMeta(
    @JsonProperty("current_page") int currentPage,
    @JsonProperty("page_size") int pageSize,
    @JsonProperty("total_count") int totalCount,
    @JsonProperty("has_next_page") boolean hasNextPage) {}
