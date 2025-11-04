package com.ascend.testlab.service.impl;

import com.ascend.testlab.dao.TagsDAO;
import com.ascend.testlab.dto.response.TagsResponse;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.service.TagsService;
import com.dream11.rest.exception.RestException;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the TagsService interface for managing experiment tags operations.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 * @see TagsService
 * @see TagsDAO
 */
@Slf4j
public class TagsServiceImpl implements TagsService {

  /** The tags DAO. */
  private final TagsDAO tagsDAO;

  /**
   * Constructor for the TagsServiceImpl.
   *
   * @param tagsDAO the tags DAO
   */
  @Inject
  public TagsServiceImpl(TagsDAO tagsDAO) {
    this.tagsDAO = tagsDAO;
  }

  /** {@inheritDoc} */
  @Override
  public Single<TagsResponse> getTags(String projectKey) {
    return tagsDAO
        .fetchTags(projectKey)
        .map(TagsResponse::new)
        .onErrorResumeNext(
            err -> {
              log.error("Error in list tags for project {}: {}", projectKey, err.getMessage());
              return Single.error(
                  ErrorEnum.handleException(
                      err, new RestException(ErrorEnum.REST_FETCH_TAGS_FAILED, err)));
            });
  }
}
