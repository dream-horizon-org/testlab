package com.ascend.testlab.dto.entity.experiment;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class representing overrides in the system. Overrides allow direct assignment of users to
 * specific variants.
 *
 * <p>JSON structure:
 *
 * <pre>
 * "overrides": {
 *   "control": ["user1", "user2"],
 *   "variant1": ["user3", "user4"]
 * }
 * </pre>
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Overrides {

  private Map<String, List<String>> overrideIds = new HashMap<>();

  /**
   * Sets a variant to user IDs mapping entry.
   *
   * @param variantName the variant name
   * @param userIds list of user IDs to assign to the variant
   */
  @JsonAnySetter
  public void setVariantUsers(String variantName, List<String> userIds) {
    this.overrideIds.put(variantName, userIds);
  }

  /**
   * Checks if the overrides map is empty.
   *
   * @return true if no overrides are defined
   */
  public boolean isEmpty() {
    return overrideIds == null || overrideIds.isEmpty();
  }
}
