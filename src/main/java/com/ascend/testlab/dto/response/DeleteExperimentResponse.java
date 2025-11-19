package com.ascend.testlab.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteExperimentResponse {
  private boolean success;
  private String message;
}
