package com.ascend.testlab.service;

import com.ascend.testlab.dto.response.TagsResponse;
import io.reactivex.rxjava3.core.Single;
import java.util.UUID;

public interface TagsService {
  Single<TagsResponse> getTags(UUID projectId);
}
