package com.ascend.testlab.dao;

import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.UUID;

public interface TagsDAO {
  Single<List<String>> fetchTags(UUID projectId);
}
