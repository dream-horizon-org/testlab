package com.ascend.testlab.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TagsResponse(@JsonProperty("tags") List<String> tags) {}
