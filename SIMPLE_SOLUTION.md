# Simple Solution - Minimal Code Changes

## Problem
Repetitive conditional logic: `Objects.nonNull(userId) ? userId : stableId`

## Simple Solution
Add two small helper methods at the end of `AllocationServiceImpl` class.

---

## Option 1: Helper Methods (Simplest - No New Files)

Just add these two private methods to your existing `AllocationServiceImpl`:

```java
/**
 * Returns the effective identifier to use for operations.
 * Uses userId if present, otherwise falls back to stableId.
 */
private String getEffectiveIdentifier(String userId, String stableId) {
  return Objects.nonNull(userId) ? userId : stableId;
}

/**
 * Determines if guest carryover should be applied.
 * Only applies when userId is NOT null (user has logged in).
 */
private boolean shouldApplyGuestCarryover(String userId) {
  return Objects.nonNull(userId);
}
```

### Usage:
```java
// Instead of:
String effectiveUserId = Objects.nonNull(userId) ? userId : stableId;

// Use:
String effectiveUserId = getEffectiveIdentifier(userId, stableId);

// Instead of:
if (Objects.nonNull(userId)) {
  carryoverSingle = applyGuestCarryover(...);
}

// Use:
if (shouldApplyGuestCarryover(userId)) {
  carryoverSingle = applyGuestCarryover(...);
}
```

---

## Option 2: Tiny Utility Class (Slightly More Modular)

Create one small utility class:

```java
package com.ascend.testlab.allocation.util;

import java.util.Objects;
import lombok.experimental.UtilityClass;

/**
 * Utility for resolving user identifiers in allocation flow.
 */
@UtilityClass
public class IdentifierResolver {
  
  /**
   * Returns the effective identifier for allocation operations.
   * Prefers userId over stableId.
   */
  public static String resolve(String userId, String stableId) {
    return Objects.nonNull(userId) ? userId : stableId;
  }
  
  /**
   * Checks if this is a logged-in user (has userId).
   */
  public static boolean isLoggedIn(String userId) {
    return Objects.nonNull(userId);
  }
}
```

### Usage:
```java
// Import once
import com.ascend.testlab.allocation.util.IdentifierResolver;

// Use throughout
String effectiveId = IdentifierResolver.resolve(userId, stableId);
if (IdentifierResolver.isLoggedIn(userId)) {
  // apply guest carryover
}
```

---

## Comparison

| Approach | New Files | Lines Changed | Complexity |
|----------|-----------|---------------|------------|
| **Option 1** (Helper Methods) | 0 | ~5 places | Very Low |
| **Option 2** (Utility Class) | 1 small file | ~5 places + import | Low |
| Strategy Pattern (Previous) | 5+ files | 100+ lines | High |

---

## Recommendation: **Option 1** (Helper Methods)

Why?
- ✅ Zero new files
- ✅ Changes only in one existing file
- ✅ Easy to understand
- ✅ Self-documenting with method names
- ✅ Can be done in 5 minutes
- ✅ Easy to test (if needed, can test via existing tests)

---

## Implementation Steps

1. Add two helper methods at the end of `AllocationServiceImpl`
2. Replace 4-5 occurrences of the conditional logic
3. Done!

Would you like me to implement Option 1 (helper methods)?

