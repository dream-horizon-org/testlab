package com.ascend.testlab.dto.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request object for experiment assignment */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentRequest {
  private List<String> experiments; // List of experiment IDs to assign (optional)
  private List<String> entities; // Target entities (e.g., "mobile", "android", "web")
  private Attributes attributes; // User attributes for filtering
  private String guestId; // Guest ID for carryover to logged-in user
}
