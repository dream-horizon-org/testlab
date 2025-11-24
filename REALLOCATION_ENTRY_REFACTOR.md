# Better Ways to Create Reallocation Entry

## Current Approach (Not Ideal)

```java
Map<String, Object> entry = new HashMap<>();
entry.put("timestamp", System.currentTimeMillis());
entry.put("oldVariant", oldVariantName);
entry.put("newVariant", newVariantName);
entry.put("reason", reason != null ? reason : "No reason provided");
entry.put("changedBy", "system");
```

**Issues:**
- ❌ Magic strings prone to typos
- ❌ No type safety
- ❌ Easy to add wrong types
- ❌ Hard to refactor
- ❌ No IDE autocomplete support
- ❌ No validation

---

## Option 1: **Dedicated DTO Class** (RECOMMENDED ✅)

### Create ReallocationLogEntry Class

```java
package com.ascend.testlab.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a reallocation log entry in Aerospike.
 * Stored in reallocationLog set with composite key (userId_experimentId).
 *
 * @author NishantParmar0026
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReallocationLogEntry {

  @JsonProperty(value = "timestamp")
  private Long timestamp;

  @JsonProperty(value = "old_variant")
  private String oldVariant;

  @JsonProperty(value = "new_variant")
  private String newVariant;

  @JsonProperty(value = "reason")
  @Builder.Default
  private String reason = "No reason provided";

  @JsonProperty(value = "changed_by")
  @Builder.Default
  private String changedBy = "system";
}
```

### Usage in logReallocation()

```java
private Single<Boolean> logReallocation(
    String userId,
    String projectKey,
    String experimentId,
    String oldVariantName,
    String newVariantName,
    String reason) {

  try {
    String set = CommonUtil.getSetName(aerospikeConfig.getReallocationLogSet(), projectKey);
    String compositeKey = userId + Constants.UNDER_SCORE + experimentId;
    Key key = new Key(aerospikeConfig.getNamespace(), set, compositeKey);

    WritePolicy policy = new WritePolicy();
    policy.sendKey = true;
    policy.expiration = -1;

    // Create reallocation entry using builder
    ReallocationLogEntry entry = ReallocationLogEntry.builder()
        .timestamp(System.currentTimeMillis())
        .oldVariant(oldVariantName)
        .newVariant(newVariantName)
        .reason(reason)  // Defaults to "No reason provided" if null
        .changedBy("system")
        .build();

    String entryJson = objectMapper.writeValueAsString(entry);

    log.trace("Reallocation log entry created: {}", entryJson);

    return aerospikeClient
        .operate(
            policy,
            key,
            ListOperation.append(new ListPolicy(), "entries", Value.get(entryJson)))
        .map(result -> {
          log.info(
              "Successfully logged reallocation for user: {} experiment: {} (key: {})",
              userId,
              experimentId,
              compositeKey);
          return true;
        })
        .onErrorResumeNext(error -> {
          log.error(
              "Failed to log reallocation event for user: {} experiment: {}",
              userId,
              experimentId,
              error);
          return Single.just(false);
        });
  } catch (Exception e) {
    log.error("Error serializing reallocation log for user: {}", userId, e);
    return Single.just(false);
  }
}
```

**Pros:**
- ✅ Type-safe
- ✅ IDE autocomplete
- ✅ Validation support via annotations
- ✅ Easy to refactor
- ✅ Reusable for queries too
- ✅ Strongly typed in queries
- ✅ @Builder for fluent API
- ✅ Default values via @Builder.Default

**Cons:**
- Requires new class file
- Slight overhead (minimal)

---

## Option 2: **Java Record** (Modern, Java 16+)

### If you're on Java 16+

```java
package com.ascend.testlab.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Immutable record representing a reallocation log entry.
 *
 * @author NishantParmar0026
 */
public record ReallocationLogEntry(
    @JsonProperty(value = "timestamp") long timestamp,
    @JsonProperty(value = "old_variant") String oldVariant,
    @JsonProperty(value = "new_variant") String newVariant,
    @JsonProperty(value = "reason") String reason,
    @JsonProperty(value = "changed_by") String changedBy)
    implements Serializable {

  // Compact constructor with defaults
  public ReallocationLogEntry {
    if (reason == null) {
      reason = "No reason provided";
    }
    if (changedBy == null) {
      changedBy = "system";
    }
  }

  // Factory method for convenience
  public static ReallocationLogEntry of(
      String oldVariant, String newVariant, String reason) {
    return new ReallocationLogEntry(
        System.currentTimeMillis(), oldVariant, newVariant, reason, "system");
  }
}
```

### Usage

```java
ReallocationLogEntry entry = ReallocationLogEntry.of(
    oldVariantName,
    newVariantName,
    reason);

String entryJson = objectMapper.writeValueAsString(entry);
```

**Pros:**
- ✅ Immutable by default
- ✅ Concise syntax
- ✅ Automatic equals/hashCode/toString
- ✅ No boilerplate
- ✅ Factory methods supported

**Cons:**
- Requires Java 16+
- Less flexible than class

---

## Option 3: **Builder Pattern** (Fluent & Clean)

```java
package com.ascend.testlab.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Mutable builder for reallocation log entries with fluent API.
 *
 * @author NishantParmar0026
 */
public class ReallocationLogEntryBuilder {

  private Long timestamp = System.currentTimeMillis();
  private String oldVariant;
  private String newVariant;
  private String reason = "No reason provided";
  private String changedBy = "system";

  public ReallocationLogEntryBuilder timestamp(long timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  public ReallocationLogEntryBuilder oldVariant(String oldVariant) {
    this.oldVariant = oldVariant;
    return this;
  }

  public ReallocationLogEntryBuilder newVariant(String newVariant) {
    this.newVariant = newVariant;
    return this;
  }

  public ReallocationLogEntryBuilder reason(String reason) {
    if (reason != null && !reason.isEmpty()) {
      this.reason = reason;
    }
    return this;
  }

  public ReallocationLogEntryBuilder changedBy(String changedBy) {
    this.changedBy = changedBy;
    return this;
  }

  public ReallocationLogEntry build() {
    return new ReallocationLogEntry(timestamp, oldVariant, newVariant, reason, changedBy);
  }
}

// Usage
Map<String, Object> entry = new ReallocationLogEntryBuilder()
    .oldVariant(oldVariantName)
    .newVariant(newVariantName)
    .reason(reason)
    .build()
    .toMap();

// Or with Lombok @Builder (Option 1 is better)
```

**Pros:**
- ✅ Fluent API
- ✅ Clear intent
- ✅ Chainable methods
- ✅ Flexible

**Cons:**
- Verbose
- Requires builder class

---

## Option 4: **Factory Method + Map** (Simple Improvement)

```java
/**
 * Factory method to create reallocation entry map.
 */
private Map<String, Object> createReallocationEntry(
    String oldVariantName,
    String newVariantName,
    String reason) {
  
  Map<String, Object> entry = new LinkedHashMap<>();  // Maintains order
  entry.put("timestamp", System.currentTimeMillis());
  entry.put("oldVariant", oldVariantName);
  entry.put("newVariant", newVariantName);
  entry.put("reason", reason != null ? reason : "No reason provided");
  entry.put("changedBy", "system");
  
  return entry;
}

// Usage
Map<String, Object> entry = createReallocationEntry(
    oldVariantName,
    newVariantName,
    reason);
```

**Pros:**
- ✅ Simple
- ✅ Keeps logic together
- ✅ LinkedHashMap maintains order
- ✅ No new class needed

**Cons:**
- ⚠️ Still uses magic strings
- ⚠️ No type safety
- ⚠️ Still a map

---

## Option 5: **Inline with Named Map Constants**

```java
private static final class ReallocationEntryKeys {
  static final String TIMESTAMP = "timestamp";
  static final String OLD_VARIANT = "oldVariant";
  static final String NEW_VARIANT = "newVariant";
  static final String REASON = "reason";
  static final String CHANGED_BY = "changedBy";
  static final String DEFAULT_REASON = "No reason provided";
  static final String DEFAULT_CHANGED_BY = "system";
}

// Usage
Map<String, Object> entry = new LinkedHashMap<>();
entry.put(ReallocationEntryKeys.TIMESTAMP, System.currentTimeMillis());
entry.put(ReallocationEntryKeys.OLD_VARIANT, oldVariantName);
entry.put(ReallocationEntryKeys.NEW_VARIANT, newVariantName);
entry.put(
    ReallocationEntryKeys.REASON,
    reason != null ? reason : ReallocationEntryKeys.DEFAULT_REASON);
entry.put(ReallocationEntryKeys.CHANGED_BY, ReallocationEntryKeys.DEFAULT_CHANGED_BY);
```

**Pros:**
- ✅ Eliminates magic strings
- ✅ IDE autocomplete
- ✅ Single source of truth
- ✅ No new class

**Cons:**
- ⚠️ Still verbose
- ⚠️ Still no type safety

---

## Comparison Matrix

| Feature | DTO Class | Record | Builder | Factory Method | Constants |
|---------|-----------|--------|---------|-----------------|-----------|
| **Type Safety** | ✅✅ | ✅✅ | ✅ | ⚠️ | ⚠️ |
| **IDE Support** | ✅✅ | ✅✅ | ✅ | ⚠️ | ✅ |
| **Easy Refactor** | ✅✅ | ✅✅ | ✅ | ⚠️ | ✅ |
| **Validation** | ✅✅ | ✅ | ⚠️ | ❌ | ❌ |
| **Immutable** | ✅ (via builder) | ✅✅ | ❌ | ❌ | ❌ |
| **Reusable** | ✅✅ | ✅✅ | ⚠️ | ❌ | ✅ |
| **Simplicity** | ✅ | ✅✅ | ⚠️ | ✅ | ⚠️ |
| **No New Class** | ❌ | ❌ | ⚠️ | ✅ | ✅ |
| **Query Support** | ✅✅ | ✅✅ | ⚠️ | ❌ | ❌ |

---

## FINAL RECOMMENDATION ⭐

### **Option 1: Dedicated DTO Class with Lombok @Builder**

```java
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReallocationLogEntry {
  private Long timestamp;
  private String oldVariant;
  private String newVariant;
  
  @Builder.Default
  private String reason = "No reason provided";
  
  @Builder.Default
  private String changedBy = "system";
}

// Usage
ReallocationLogEntry entry = ReallocationLogEntry.builder()
    .timestamp(System.currentTimeMillis())
    .oldVariant(oldVariantName)
    .newVariant(newVariantName)
    .reason(reason)
    .build();

String entryJson = objectMapper.writeValueAsString(entry);
```

**Why This Wins:**
- ✅ Type-safe
- ✅ Fluent builder API
- ✅ Reusable for retrieval queries
- ✅ Validation via annotations
- ✅ Easy serialization
- ✅ IDE autocomplete
- ✅ One source of truth
- ✅ Already using Lombok in codebase
- ✅ Immutable with builder
- ✅ Self-documenting

---

## Implementation Steps

1. **Create ReallocationLogEntry DTO** (or Record if Java 16+)
2. **Replace HashMap creation with builder**
3. **Use throughout DAO and service**
4. **Update retrieval methods to use typed DTO**
5. **Add validation annotations if needed**

---

## Example: Complete Integration

```java
private Single<Boolean> logReallocation(
    String userId,
    String projectKey,
    String experimentId,
    String oldVariantName,
    String newVariantName,
    String reason) {

  try {
    String set = CommonUtil.getSetName(aerospikeConfig.getReallocationLogSet(), projectKey);
    String compositeKey = userId + Constants.UNDER_SCORE + experimentId;
    Key key = new Key(aerospikeConfig.getNamespace(), set, compositeKey);

    WritePolicy policy = new WritePolicy();
    policy.sendKey = true;
    policy.expiration = -1;

    // Type-safe entry creation
    ReallocationLogEntry entry = ReallocationLogEntry.builder()
        .timestamp(System.currentTimeMillis())
        .oldVariant(oldVariantName)
        .newVariant(newVariantName)
        .reason(reason)  // Auto-defaults if null via @Builder.Default
        .build();

    String entryJson = objectMapper.writeValueAsString(entry);

    return aerospikeClient
        .operate(
            policy,
            key,
            ListOperation.append(new ListPolicy(), "entries", Value.get(entryJson)))
        .map(result -> {
          log.info("Successfully logged reallocation for user: {}", userId);
          return true;
        })
        .onErrorResumeNext(error -> {
          log.error("Failed to log reallocation event", error);
          return Single.just(false);
        });
  } catch (Exception e) {
    log.error("Error serializing reallocation log for user: {}", userId, e);
    return Single.just(false);
  }
}
```

**Result:**
- ✅ Clean, readable code
- ✅ Type-safe
- ✅ Self-documenting
- ✅ Easy to maintain
- ✅ IDE support
- ✅ No magic strings

**Ready to implement!** ✅

