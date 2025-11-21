package com.ascend.testlab.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class representing a variant in an experiment.
 *
 * <p>Each variant contains a list of variables with their respective data types.
 *
 * @author anudeepreddy20
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Variant {
  @JsonProperty("display_name")
  private String displayName;

  @JsonProperty("variables")
  @NotEmpty(message = "Variant must have at least one variable")
  @Valid
  private List<Variables> variables;
}
