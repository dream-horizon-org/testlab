package com.ascend.testlab.dto.response;

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
public record PaginationMeta(int currentPage, int pageSize, int totalCount) {}
