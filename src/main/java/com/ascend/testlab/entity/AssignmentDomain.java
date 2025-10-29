package com.ascend.testlab.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentDomain {
  private String domainType; // TRAIT, COHORT, BULK
  private String traitName;
  private List<String> cohortIds;
  private List<Long> bulkUserIds;
  private Boolean isExclusivePilot;
}
