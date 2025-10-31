package com.ascend.testlab.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateExperimentResponse {
  private Long id;
  private boolean status;
  private String message;
}
