package com.ascend.testlab.service;

import com.ascend.testlab.dto.response.TagsResponse;
import io.reactivex.rxjava3.core.Single;
import java.util.UUID;

/**
 * Service interface for managing experiment tags operations.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 * @see TagsServiceImpl
 * @see TagsResponse
 */
public interface TagsService {

  /**
   * Retrieves all distinct tags for experiments within the specified project.
   *
   * @param projectId the UUID of the project to fetch tags for
   * @return a Single containing TagsResponse with the list of distinct tags
   * @throws com.dream11.rest.exception.RestException if the operation fails
   */
  Single<TagsResponse> getTags(UUID projectId);
}
