package com.ascend.testlab.dto.entity.experiment;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * Entity class representing an overrides in the system.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Overrides {

  Map<String, List<String>> overrideIds;
}
