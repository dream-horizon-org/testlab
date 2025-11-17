package com.ascend.testlab.variantWeights;

import com.ascend.testlab.entity.AssignmentDomain;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Abstract base class for variant weights. Uses Jackson polymorphic deserialization to handle
 * different weight types based on assignment domain.
 *
 * @author anudeepreddy20
 * @version 1.0
 * @since 1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(
      value = com.ascend.testlab.variantWeights.ManualVariantWeights.class,
      name = "MANUAL"),
  @JsonSubTypes.Type(
      value = com.ascend.testlab.variantWeights.CohortVariantWeights.class,
      name = "COHORT"),
})
public abstract class VariantWeights {

  /**
   * Gets the type of variant weights
   *
   * @return the assignment domain type
   */
  public abstract AssignmentDomain getType();
}
