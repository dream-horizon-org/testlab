package com.ascend.testlab.dto.response;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateExperimentResponse {
  private UUID experimentId;
  private boolean status;
  private String message;
}
