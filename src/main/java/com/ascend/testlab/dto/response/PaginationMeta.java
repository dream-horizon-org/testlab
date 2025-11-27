package com.ascend.testlab.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;

/**
 * Metadata class for pagination information used across multiple response DTOs.
 *
 * @param currentPage The current page number.
 * @param pageSize The number of items per page.
 * @param totalCount The total number of items.
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 */
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PaginationMeta(int currentPage, int pageSize, int totalCount, boolean hasNextPage) {}
