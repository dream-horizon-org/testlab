package com.ascend.testlab.dto.entity.variantweights;

import com.ascend.testlab.constants.enums.AssignmentDomain;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * Abstract base class for variant weights. Uses Jackson polymorphic deserialization to handle
 * different weight types based on assignment domain.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    defaultImpl = CohortVariantWeights.class)
@JsonSubTypes({
  @JsonSubTypes.Type(value = StratifiedVariantWeights.class, name = "MANUAL"),
  @JsonSubTypes.Type(value = CohortVariantWeights.class, name = "COHORT"),
})
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public abstract class VariantWeights {

  /**
   * Gets the type of variant weights
   *
   * @return the assignment domain type
   */
  public abstract AssignmentDomain getType();
}
