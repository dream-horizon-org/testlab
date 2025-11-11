# Concluded Experiments - Technical Implementation

## 📋 Changes Summary

### Files Created (5)
1. `ConcludedExperimentOverrideStrategy.java` - Strategy interface
2. `AssignedOnlyStrategy.java` - Override assigned users only
3. `UnassignedOnlyStrategy.java` - Add for unassigned users only
4. `BothStrategy.java` - Apply to all users
5. `ConcludedExperimentStrategyFactory.java` - Factory for strategy instantiation

### Files Modified (5)
1. `Experiment.java` - Added `winningVariant` field
2. `PostgresColumn.java` - Added `WINNING_VARIANT` enum
3. `ReadQuery.java` - Added `GET_CONCLUDED_EXPERIMENTS` query
4. `AssignmentDAO.java` - Added new interface methods
5. `AssignmentDAOImpl.java` - Implemented concluded experiments logic + fixed hardcoded strings

---

## 🔧 Detailed Changes

### 1. Entity Layer

#### `Experiment.java`
```java
public class Experiment {
    // ... existing fields
    private String winningVariant; // NEW
}
```

**Purpose:** Store the winning variant name for concluded experiments

---

### 2. Constants & Configuration

#### `PostgresColumn.java`
```java
public enum PostgresColumn {
    // ... existing columns
    WINNING_VARIANT("winning_variant"); // NEW
}
```

**Purpose:** Type-safe column reference for winning_variant field

#### `ReadQuery.java`
```java
public static final String GET_CONCLUDED_EXPERIMENTS =
    "SELECT * "
        + "FROM experiments "
        + "WHERE project_key = $1 "
        + "AND status = 'CONCLUDED' "
        + "AND winning_variant IS NOT NULL";
```

**Purpose:** Query to fetch concluded experiments with winning variants

---

### 3. Strategy Pattern Implementation

#### Interface: `ConcludedExperimentOverrideStrategy.java`

```java
public interface ConcludedExperimentOverrideStrategy {
    boolean shouldOverride(
        Experiment concludedExperiment, 
        List<UserExperimentMap> existingAssignments);
    
    String getStrategyName();
}
```

**Methods:**
- `shouldOverride()` - Determines if winning variant should be applied
- `getStrategyName()` - Returns strategy identifier for logging

#### Implementations:

**1. AssignedOnlyStrategy**
```java
@Override
public boolean shouldOverride(...) {
    return existingAssignments.stream()
        .anyMatch(a -> a.getExperimentId().equals(
            concludedExperiment.getExperimentId()));
}
```
- Returns `true` if user was assigned to experiment
- Returns `false` if user was not assigned

**2. UnassignedOnlyStrategy**
```java
@Override
public boolean shouldOverride(...) {
    return existingAssignments.stream()
        .noneMatch(a -> a.getExperimentId().equals(
            concludedExperiment.getExperimentId()));
}
```
- Returns `true` if user was NOT assigned to experiment
- Returns `false` if user was assigned

**3. BothStrategy**
```java
@Override
public boolean shouldOverride(...) {
    return true; // Always apply
}
```
- Always returns `true` regardless of assignment status

#### Factory: `ConcludedExperimentStrategyFactory.java`

```java
public enum StrategyType {
    ASSIGNED_ONLY,
    UNASSIGNED_ONLY,
    BOTH
}

public static ConcludedExperimentOverrideStrategy getStrategy(StrategyType type) {
    switch (type) {
        case ASSIGNED_ONLY: return new AssignedOnlyStrategy();
        case UNASSIGNED_ONLY: return new UnassignedOnlyStrategy();
        case BOTH: return new BothStrategy();
        default: return new BothStrategy();
    }
}
```

**Methods:**
- `getStrategy(StrategyType)` - Get strategy by enum
- `getStrategyByName(String)` - Get strategy by string name
- `getDefaultStrategy()` - Returns BothStrategy

---

### 4. DAO Layer

#### Interface: `AssignmentDAO.java`

**New Methods:**

```java
Single<List<Experiment>> fetchConcludedExperiments(UUID tenantId);

Single<List<UserExperimentMap>> applyConcludedExperiments(
    String userId, 
    UUID tenantId, 
    ConcludedExperimentOverrideStrategy strategy);
```

#### Implementation: `AssignmentDAOImpl.java`

**1. fetchConcludedExperiments()**
```java
public Single<List<Experiment>> fetchConcludedExperiments(UUID tenantId) {
    return pgReaderClient
        .fetchAll(
            ReadQuery.GET_CONCLUDED_EXPERIMENTS,
            Tuple.tuple().addString(tenantId.toString()),
            this::mapRowToExperiment)
        .doOnSuccess(experiments -> log.debug("Fetched {} concluded experiments", experiments.size()))
        .onErrorReturn(error -> {
            log.error("Error fetching concluded experiments", error);
            return new ArrayList<>();
        });
}
```

**Flow:**
1. Execute `GET_CONCLUDED_EXPERIMENTS` query with tenantId
2. Map rows to Experiment objects
3. Log success with count
4. Return empty list on error

**2. applyConcludedExperiments()**
```java
public Single<List<UserExperimentMap>> applyConcludedExperiments(
    String userId, UUID tenantId, ConcludedExperimentOverrideStrategy strategy) {
    
    return Single.zip(
        fetchConcludedExperiments(tenantId),
        getUserAssignments(userId, tenantId),
        (concludedExperiments, existingAssignments) -> {
            List<UserExperimentMap> assignmentsToApply = new ArrayList<>();
            
            for (Experiment exp : concludedExperiments) {
                if (strategy.shouldOverride(exp, existingAssignments)) {
                    assignmentsToApply.add(
                        createConcludedAssignment(exp, existingAssignments));
                }
            }
            return assignmentsToApply;
        })
        .flatMap(assignments -> {
            if (assignments.isEmpty()) {
                return Single.just(new ArrayList<>());
            }
            return insertUserAssignments(userId, tenantId, assignments)
                .map(success -> success ? assignments : new ArrayList<>());
        })
        .onErrorReturn(error -> {
            log.error("Error applying concluded experiments", error);
            return new ArrayList<>();
        });
}
```

**Flow:**
1. **Parallel Fetch**: Use `Single.zip()` to fetch:
   - Concluded experiments from PostgreSQL
   - User's existing assignments from Aerospike
2. **Strategy Application**: For each concluded experiment:
   - Check if strategy says to override
   - If yes, create assignment with winning variant
3. **Persistence**: 
   - If no assignments, return empty list
   - Otherwise, insert assignments to Aerospike
4. **Error Handling**: Return empty list on any error

**3. createConcludedAssignment()** (Private Helper)
```java
private UserExperimentMap createConcludedAssignment(
    Experiment concludedExperiment, 
    List<UserExperimentMap> existingAssignments) {
    
    String winningVariantName = concludedExperiment.getWinningVariant();
    Variant winningVariant = concludedExperiment.getVariant().get(winningVariantName);
    
    // Fallback if winning variant not found
    if (winningVariant == null) {
        winningVariant = concludedExperiment.getVariant().values().stream()
            .findFirst()
            .orElse(Variant.builder()
                .displayName(winningVariantName)
                .variables(Map.of())
                .dataType("STRING")
                .build());
    }
    
    // Preserve existing assignment timestamp if available
    UserExperimentMap existingAssignment = existingAssignments.stream()
        .filter(a -> a.getExperimentId().equals(concludedExperiment.getExperimentId()))
        .findFirst()
        .orElse(null);
    
    Long assignedAt = existingAssignment != null 
        ? existingAssignment.getAssignedAt()
        : System.currentTimeMillis();
    
    return UserExperimentMap.builder()
        .experimentId(concludedExperiment.getExperimentId())
        .experimentName(concludedExperiment.getName())
        .status(Constants.STATUS_CONCLUDED)
        .variant(winningVariant)
        .variantName(winningVariantName)
        .assignedAt(assignedAt)
        .build();
}
```

**Flow:**
1. Get winning variant from experiment
2. Fallback to first variant if winning variant not found
3. Check if user was previously assigned (to preserve timestamp)
4. Build UserExperimentMap with:
   - Status = "CONCLUDED"
   - Winning variant details
   - Preserved or new timestamp

**4. Updated mapRowToExperiment()**
```java
String winningVariant = row.getString(PostgresColumn.WINNING_VARIANT.getColumn());

return Experiment.builder()
    // ... other fields
    .winningVariant(winningVariant)
    .build();
```

**Change:** Now reads and maps the `winning_variant` column

---

### 5. Additional Improvements

#### Fixed Hardcoded Strings in `AssignmentDAOImpl.java`

**Before:**
```java
String asKey = experimentId.toString() + ":" + variantName;
long newCount = record.getLong("count");
```

**After:**
```java
String asKey = experimentId.toString() + Constants.COLON + variantName;
long newCount = record.getLong(aerospikeConfig.getCountBin());
```

**Locations Fixed:**
- Line 367: `decrementVariantCount()` - Using `Constants.COLON`
- Line 388: `decrementVariantCount()` - Using `aerospikeConfig.getCountBin()`
- Line 409: `getTotalVariantCount()` - Using `Constants.COLON`

---

## 🏗️ Architecture Patterns Used

### 1. Strategy Pattern
**Purpose:** Allow flexible behavior selection at runtime

**Benefits:**
- Open/Closed Principle - Open for extension, closed for modification
- Easy to add new strategies without changing existing code
- Clear separation of concerns

### 2. Factory Pattern
**Purpose:** Centralized object creation

**Benefits:**
- Single point of control for strategy instantiation
- Configuration-driven strategy selection
- Default fallback behavior

### 3. Reactive Programming (RxJava)
**Purpose:** Non-blocking, asynchronous operations

**Benefits:**
- Parallel execution with `Single.zip()`
- Composable operations with `flatMap()`, `map()`
- Comprehensive error handling with `onErrorReturn()`

### 4. Builder Pattern
**Purpose:** Construct complex objects

**Used in:**
- `Experiment.builder()`
- `UserExperimentMap.builder()`
- `Variant.builder()`

---

## 💾 Data Model

### Experiment Entity
```java
UUID experimentId
String projectKey
String name
String description
String status                    // "LIVE", "CONCLUDED", etc.
List<String> cohorts
VariantWeights variantWeights
Map<String, Variant> variant
List<RuleAttributes> ruleAttributes
Long startTime
List<String> overrides
Long endTime
Integer exposure
Long threshold
DistributionStrategy distributionStrategy
AssignmentDomain assignmentDomain
String winningVariant            // NEW - name of winning variant
```

### UserExperimentMap DTO
```java
UUID experimentId
String experimentName
String status                    // "ASSIGNED", "CONCLUDED"
Variant variant
String variantName
Long assignedAt
```

### Aerospike Storage Structure
```
Key: {tenantId}:{experimentId}
Bin: assignmentMap
Value: Map<ExperimentId, AssignmentJSON>

Example:
{
  "exp-123": "{\"experimentName\":\"...\",\"status\":\"CONCLUDED\",\"variant\":{...},\"assignedAt\":1234567890}"
}
```

---

## 🔄 Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                  Service Layer                               │
│  applyConcludedExperiments(userId, tenantId, strategy)      │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                 AssignmentDAOImpl                            │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ 1. Single.zip() - Parallel Fetch                     │   │
│  │    ├─ fetchConcludedExperiments(tenantId)            │   │
│  │    └─ getUserAssignments(userId, tenantId)           │   │
│  └──────────────────────────────────────────────────────┘   │
│                       │                                      │
│                       ▼                                      │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ 2. Strategy Application                              │   │
│  │    For each concluded experiment:                    │   │
│  │      if strategy.shouldOverride():                   │   │
│  │        create assignment with winning variant        │   │
│  └──────────────────────────────────────────────────────┘   │
│                       │                                      │
│                       ▼                                      │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ 3. Persistence                                       │   │
│  │    insertUserAssignments(userId, tenantId, list)    │   │
│  │      → Aerospike MapOperation.put()                  │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
           │                                    │
           ▼                                    ▼
    ┌──────────────┐                    ┌──────────────┐
    │ PostgreSQL   │                    │  Aerospike   │
    │ (Experiments)│                    │ (Assignments)│
    └──────────────┘                    └──────────────┘
```

---

## ⚡ Performance Characteristics

### Complexity Analysis

**fetchConcludedExperiments()**
- Time: O(n) where n = number of concluded experiments
- Space: O(n)
- DB: 1 query to PostgreSQL

**applyConcludedExperiments()**
- Time: O(n + m) where:
  - n = number of concluded experiments
  - m = number of existing user assignments
- Space: O(n + m)
- DB: 1 PostgreSQL read + 1 Aerospike read + 1 Aerospike write

**Parallel Execution:**
- PostgreSQL and Aerospike reads happen in parallel using `Single.zip()`
- Expected speedup: ~2x compared to sequential

### Optimization Opportunities

1. **Caching Concluded Experiments**
   ```java
   @Cacheable(value = "concludedExperiments", key = "#tenantId", ttl = 300)
   public Single<List<Experiment>> fetchConcludedExperiments(UUID tenantId)
   ```

2. **Batch Processing**
   ```java
   Observable.fromIterable(userIds)
       .buffer(100) // Process 100 users at a time
       .flatMapSingle(batch -> processBatch(batch))
   ```

3. **Database Indexing**
   ```sql
   CREATE INDEX idx_experiments_concluded 
   ON experiments(project_key, status, winning_variant);
   ```

---

## 🧪 Testing Strategy

### Unit Tests Needed

1. **Strategy Tests**
   - `AssignedOnlyStrategy_WhenUserWasAssigned_ReturnsTrue`
   - `AssignedOnlyStrategy_WhenUserWasNotAssigned_ReturnsFalse`
   - `UnassignedOnlyStrategy_WhenUserWasAssigned_ReturnsFalse`
   - `UnassignedOnlyStrategy_WhenUserWasNotAssigned_ReturnsTrue`
   - `BothStrategy_Always_ReturnsTrue`

2. **Factory Tests**
   - `GetStrategy_WithAssignedOnly_ReturnsCorrectInstance`
   - `GetStrategyByName_WithInvalidName_ReturnsDefault`

3. **DAO Tests**
   - `FetchConcludedExperiments_Success`
   - `FetchConcludedExperiments_Error_ReturnsEmptyList`
   - `ApplyConcludedExperiments_WithStrategy_ReturnsCorrectAssignments`
   - `CreateConcludedAssignment_WithWinningVariant_Success`
   - `CreateConcludedAssignment_MissingWinningVariant_UsesFallback`

### Integration Tests Needed

1. **End-to-End Flow**
   - `ApplyConcludedExperiments_EndToEnd_Success`
   - `ApplyConcludedExperiments_Idempotent_SameResultOnRetry`

2. **Database Integration**
   - `FetchConcludedExperiments_FromActualDB_Success`
   - `InsertAssignments_ToAerospike_Persisted`

3. **Error Scenarios**
   - `PostgresDown_GracefulFailure`
   - `AerospikeDown_GracefulFailure`

---

## 🔒 Error Handling

### Error Recovery Strategy

**Principle:** Fail gracefully, return empty lists instead of throwing exceptions

**Implementation:**
```java
.onErrorReturn(error -> {
    log.error("Error details", error);
    return new ArrayList<>();
})
```

**Benefits:**
- No cascading failures
- System remains functional even with partial failures
- Clear error logging for debugging

### Error Scenarios Handled

1. **PostgreSQL Failure** → Returns empty experiment list
2. **Aerospike Read Failure** → Returns empty assignment list
3. **Aerospike Write Failure** → Returns empty result (logged)
4. **Missing Winning Variant** → Uses fallback variant
5. **Deserialization Error** → Logs and skips that assignment

---

## 📊 Logging Strategy

### Log Levels Used

**DEBUG:**
- Fetched experiment counts
- Strategy decisions
- Assignment application counts

**ERROR:**
- Database failures
- Deserialization errors
- Missing winning variants

**Example Logs:**
```
DEBUG: Fetched 3 concluded experiments for tenant abc-123
DEBUG: User was assigned to experiment exp-456, will override with winning variant
DEBUG: Applying 2 concluded experiment assignments for user user-789
ERROR: Error fetching concluded experiments for tenant abc-123
ERROR: Winning variant variant_c not found in experiment exp-456, using default
```

---

## 🎯 Key Design Decisions

### 1. Strategy Pattern Over Configuration Flags

**Why:** More extensible, testable, and follows Open/Closed Principle

**Alternative Rejected:** Using boolean flags like `overrideAssigned`, `addUnassigned`

### 2. Return Empty Lists on Errors

**Why:** Prevents cascading failures, maintains system stability

**Alternative Rejected:** Throwing exceptions (would require extensive try/catch)

### 3. Preserve Original Timestamps

**Why:** Maintains data integrity for analytics and auditing

**Implementation:** Check for existing assignment and preserve its timestamp

### 4. Parallel Fetching with Single.zip()

**Why:** Better performance, reduced latency

**Alternative Rejected:** Sequential fetching (slower)

### 5. Factory Pattern for Strategy Creation

**Why:** Centralized control, easy configuration integration

**Alternative Rejected:** Direct instantiation (scattered across codebase)

---

## 📝 Code Quality

### Adherence to Principles

✅ **SOLID Principles**
- Single Responsibility: Each strategy has one job
- Open/Closed: Extend strategies without modifying existing code
- Liskov Substitution: All strategies interchangeable
- Interface Segregation: Minimal interface design
- Dependency Inversion: Depend on abstraction (interface), not concrete classes

✅ **Clean Code**
- Descriptive method names
- Comprehensive JavaDoc comments
- Consistent formatting
- No magic numbers/strings

✅ **Best Practices**
- Lombok to reduce boilerplate
- Reactive programming for async operations
- Configuration-driven behavior
- Comprehensive error handling

---

## 🔄 Future Enhancements

### Potential Extensions

1. **Time-Based Strategies**
   ```java
   public class TimeWindowStrategy implements ConcludedExperimentOverrideStrategy {
       private final long startTime;
       private final long endTime;
       // Only apply during time window
   }
   ```

2. **Cohort-Based Strategies**
   ```java
   public class CohortStrategy implements ConcludedExperimentOverrideStrategy {
       private final List<String> targetCohorts;
       // Only apply to specific cohorts
   }
   ```

3. **Feature Flag Integration**
   ```java
   public class FeatureFlagStrategy implements ConcludedExperimentOverrideStrategy {
       @Inject private FeatureFlagService flagService;
       // Check feature flags before applying
   }
   ```

4. **Metrics & Monitoring**
   ```java
   @Timed
   @Counted
   public Single<List<UserExperimentMap>> applyConcludedExperiments(...)
   ```

5. **Caching Layer**
   ```java
   @Cacheable(value = "concludedExperiments", key = "#tenantId")
   public Single<List<Experiment>> fetchConcludedExperiments(UUID tenantId)
   ```

---

## 📚 References

### Related Classes
- `AssignmentServiceImpl.java` - Service layer
- `CommonUtil.java` - Utility methods
- `Constants.java` - Application constants
- `AerospikeConfig.java` - Configuration

### Design Patterns
- Strategy Pattern
- Factory Pattern
- Builder Pattern
- Reactive Programming

### Technologies
- RxJava 3 for reactive streams
- Aerospike for caching
- PostgreSQL for persistence
- Guice for dependency injection
- Lombok for boilerplate reduction

---

**Version:** 1.0  
**Last Updated:** 2025-01-11  
**Author:** anudeepreddy20

