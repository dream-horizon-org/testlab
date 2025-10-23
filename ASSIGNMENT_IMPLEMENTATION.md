# Assignment Flow Implementation

## Overview

This document describes the clean and modular implementation of the experiment assignment flow in the testlab project, inspired by the d11-experiment project's `userAssignments` API.

## Architecture

The implementation follows a layered architecture with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                     REST Layer                              │
│                 (Assignment.java)                           │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                   Service Layer                             │
│          (AssignmentService / AssignmentServiceImpl)        │
│                                                             │
│  • Main assignment orchestration                           │
│  • Filter application (exclusive, API, entity, cohort)     │
│  • Lock management                                         │
│  • Response building                                       │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                    DAO Layer                                │
│            (AssignmentDAO / AssignmentDAOImpl)              │
│                                                             │
│  • Database operations (MySQL, Aerospike)                  │
│  • Experiment fetching                                     │
│  • User assignment CRUD                                    │
│  • Lock acquisition/release                                │
│  • Threshold checking                                      │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                  Data Clients                               │
│    (MySQLReaderClient, MySQLWriterClient, AerospikeClient)  │
└─────────────────────────────────────────────────────────────┘
```

## Key Components

### 1. DTOs (Data Transfer Objects)

#### AssignmentRequest
```java
- apiPaths: List<String>
- apiPath: String
- entities: List<String>
- isStatic: Boolean
- attributes: Attributes
```

#### AssignmentResponse
```java
- apiAssignmentMap: List<UserExperimentMap>
- completeAssignmentMap: Map<String, List<UserExperimentMap>>
```

#### UserExperimentMap
```java
- experimentId: UUID
- experimentName: String
- variant: String
- status: String
- variables: Map<String, Object>
- isStatic: Boolean
- isExclusive: Boolean
- entities: List<String>
- apiPath: String
- assignedAt: Long
```

### 2. Entity Models

#### Experiment
Represents an A/B test experiment with:
- Basic info (id, name, description, status)
- API paths and variants configuration
- Distribution strategy (RANDOM, ROUND_ROBIN)
- Assignment domain (TRAIT, COHORT, BULK)
- Flags (isStatic, isExclusive)

#### Variant
Represents a variant within an experiment:
- Variant name
- Percentage allocation
- Variable configuration
- Current assignment count

### 3. DAO Layer

#### AssignmentDAO Interface
Key methods:
- `fetchActiveExperiments(UUID tenantId)` - Get all active experiments
- `getUserAssignments(String userId, UUID tenantId)` - Get user's current assignments
- `insertUserAssignments(...)` - Save new assignments to MySQL and Aerospike
- `incrementVariantCount(UUID experimentId, String variantName)` - Update variant count
- `checkThreshold(UUID experimentId)` - Check if experiment reached threshold
- `acquireUserLock(String userId, UUID tenantId)` - Acquire lock for assignment
- `releaseUserLock(String userId, UUID tenantId)` - Release lock
- `fetchConcludedExperiments(...)` - Get concluded experiments with winning variant
- `getGuestAssignments(...)` - Get guest user assignments for carryover

#### AssignmentDAOImpl
Implementation highlights:
- **Dual Storage**: Saves assignments to both MySQL (persistent) and Aerospike (fast retrieval)
- **Lock Mechanism**: Uses Aerospike with 10-second expiry for user-level locking
- **Fallback Strategy**: Reads from Aerospike first, falls back to MySQL if needed
- **Threshold Tracking**: Maintains variant counts in Aerospike for quick checks

### 4. Service Layer

#### AssignmentServiceImpl

Main flow in `assignExperiments()`:

```
1. Fetch active experiments and user's current assignments in parallel
   ↓
2. Identify unassigned experiments
   ↓
3. Apply filters:
   • Exclusive filter
   • API path filter
   • Entity filter
   • Cohort filter (if attributes provided)
   • Trait filter (if attributes provided)
   ↓
4. Acquire user lock for assignment
   ↓
5. Re-fetch assignments (ensure latest state)
   ↓
6. For each filtered experiment:
   • Check threshold
   • Select variant (using strategy)
   • Increment variant count
   • Create assignment
   ↓
7. Save assignments to storage
   ↓
8. Release user lock
   ↓
9. Build response:
   • API assignment map (filtered by requested API path)
   • Complete assignment map (grouped by all API paths)
   • Merge concluded experiments
```

Key methods:
- `applyFilters()` - Orchestrates all filter applications
- `assignNewExperiments()` - Handles lock acquisition and assignment
- `assignVariantWithThreshold()` - Assigns variant with threshold check
- `buildResponse()` - Constructs API and complete assignment maps

### 5. Helper Utilities

#### AssignmentServiceHelper

Static utility methods organized by function:

**Filtering Methods:**
- `getUnassignedExperiments()` - Filter already assigned experiments
- `applyExclusiveFilter()` - Handle exclusive experiment logic
- `applyApiPathFilter()` - Filter by API path and static flag
- `applyEntityFilter()` - Filter by entity requirements
- `applyCohortFilter()` - Filter by user cohort membership
- `applyTraitFilter()` - Filter by user trait

**Variant Selection:**
- `selectVariant()` - Delegates to appropriate strategy (Random/RoundRobin)

**Mapping Methods:**
- `createUserExperimentMap()` - Create assignment from experiment and variant
- `filterByApiPath()` - Filter assignments for specific API path
- `groupByApiPath()` - Group assignments by API path

**Guest Carryover:**
- `applyGuestCarryover()` - Transfer guest assignments to logged-in user

### 6. Variant Assignment Strategies

#### VariantAssignmentStrategy Interface
```java
Variant selectVariant(List<Variant> variants, String userId)
```

#### RandomVariantAssignment
- Assigns variants randomly based on percentage distribution
- Uses cumulative percentage calculation for fair distribution

#### RoundRobinVariantAssignment
- Maintains balance based on current counts and target percentages
- Selects variant with lowest (actual/target) ratio
- Ensures proportional distribution over time

## Assignment Flow Example

### Request
```json
{
  "apiPath": "/api/v1/home",
  "entities": ["mobile", "android"],
  "isStatic": false,
  "attributes": {
    "cohorts": ["premium_users"],
    "trait": "power_user"
  }
}
```

### Flow Steps

1. **Fetch Data** (Parallel)
   - Active experiments: 10 found
   - User assignments: 3 found

2. **Filter Unassigned**
   - Remaining: 7 experiments

3. **Apply Filters**
   - Exclusive filter: 7 (user has no exclusive)
   - API path filter: 4 (match "/api/v1/home")
   - Entity filter: 3 (contain "mobile" and "android")
   - Cohort filter: 2 (user in "premium_users")
   - Trait filter: 2 (user has "power_user" trait)

4. **Acquire Lock**
   - Lock acquired: true

5. **Assign Experiments**
   - Experiment A: Threshold OK → Variant "treatment" selected
   - Experiment B: Threshold reached → Skipped

6. **Save Assignments**
   - 1 new assignment saved to MySQL and Aerospike

7. **Release Lock**
   - Lock released: true

8. **Build Response**
   - API assignments: 4 (3 existing + 1 new)
   - Complete map: All assignments grouped by API path

### Response
```json
{
  "apiAssignmentMap": [
    {
      "experimentId": "exp-123",
      "experimentName": "Homepage Layout Test",
      "variant": "treatment",
      "status": "ASSIGNED",
      "variables": {
        "layout": "grid",
        "columns": 3
      },
      "isStatic": false,
      "isExclusive": false,
      "entities": ["mobile", "android"],
      "apiPath": "/api/v1/home",
      "assignedAt": 1698765432000
    }
  ],
  "completeAssignmentMap": {
    "/api/v1/home": [...],
    "/api/v1/search": [...]
  }
}
```

## Key Features

### 1. Modularity
- Clear separation of concerns across layers
- Each component has a single responsibility
- Easy to test and maintain

### 2. Readability
- Descriptive method and variable names
- Comprehensive JavaDoc comments
- Logical flow with clear step-by-step progression

### 3. Crispness
- No code duplication
- Utility methods extracted to helper classes
- Strategy pattern for variant assignment

### 4. Performance
- Parallel data fetching (experiments + assignments)
- Aerospike for fast reads and locks
- Efficient filtering with streams

### 5. Reliability
- Lock mechanism prevents race conditions
- Re-fetch assignments after lock acquisition
- Threshold checks before assignment
- Fallback from Aerospike to MySQL

### 6. Extensibility
- Easy to add new filters
- Easy to add new assignment strategies
- Easy to add new storage backends

## Testing Considerations

### Unit Tests
- Service layer: Test filtering logic with mock DAO
- DAO layer: Test data mapping and query construction
- Helper utilities: Test each filter independently
- Strategies: Test variant selection algorithms

### Integration Tests
- End-to-end assignment flow
- Lock mechanism behavior
- Threshold enforcement
- Guest carryover functionality

### Performance Tests
- Concurrent assignment requests
- Large experiment sets
- High-frequency assignments

## Database Schema Requirements

### MySQL Tables

```sql
-- Experiments table
CREATE TABLE experiments (
  experiment_id VARCHAR(36) PRIMARY KEY,
  tenant_id VARCHAR(36) NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  status VARCHAR(50) NOT NULL,
  api_paths JSON NOT NULL,
  start_time BIGINT,
  end_time BIGINT,
  is_static BOOLEAN,
  is_exclusive BOOLEAN,
  entities VARCHAR(500),
  threshold BIGINT,
  distribution_strategy JSON,
  assignment_domain JSON,
  INDEX idx_tenant_status (tenant_id, status),
  INDEX idx_start_end_time (start_time, end_time)
);

-- User experiment assignments
CREATE TABLE user_experiment_assignments (
  user_id VARCHAR(255) NOT NULL,
  tenant_id VARCHAR(36) NOT NULL,
  experiment_id VARCHAR(36) NOT NULL,
  variant VARCHAR(100) NOT NULL,
  status VARCHAR(50) NOT NULL,
  variables JSON,
  api_path VARCHAR(255),
  assigned_at BIGINT,
  PRIMARY KEY (user_id, tenant_id, experiment_id),
  INDEX idx_experiment (experiment_id),
  INDEX idx_status (status)
);

-- Concluded experiments
CREATE TABLE concluded_experiments (
  experiment_id VARCHAR(36) NOT NULL,
  tenant_id VARCHAR(36) NOT NULL,
  api_path VARCHAR(255) NOT NULL,
  is_static BOOLEAN,
  entities VARCHAR(500),
  winning_variant VARCHAR(100),
  winning_variables JSON,
  PRIMARY KEY (experiment_id, api_path),
  INDEX idx_tenant_api (tenant_id, api_path)
);
```

### Aerospike Sets

- `user_experiments`: User assignment cache
- `experiment_counts`: Variant assignment counts
- `user_locks`: User assignment locks (10-second TTL)

## Configuration

### Application Properties
```properties
# MySQL Configuration
mysql.host=localhost
mysql.port=3306
mysql.database=testlab

# Aerospike Configuration
aerospike.host=localhost
aerospike.port=3000
aerospike.namespace=testlab

# Assignment Configuration
assignment.lock.expiry.seconds=10
assignment.threshold.check.enabled=true
```

## Future Enhancements

1. **Caching Layer**
   - Cache active experiments in-memory
   - Invalidate on experiment status changes

2. **Analytics Integration**
   - Track assignment events
   - Monitor experiment performance

3. **Advanced Strategies**
   - Contextual bandits
   - Thompson sampling
   - Multi-armed bandit algorithms

4. **A/B Test Analytics**
   - Statistical significance testing
   - Confidence intervals
   - Winner declaration automation

5. **Rollout Strategies**
   - Gradual rollout percentages
   - Geographic targeting
   - Time-based activation

## Conclusion

This implementation provides a clean, modular, and production-ready experiment assignment system. The code is:
- **Maintainable**: Clear structure and documentation
- **Scalable**: Efficient data storage and retrieval
- **Reliable**: Lock mechanism and threshold checks
- **Extensible**: Easy to add new features

The design follows best practices from the d11-experiment project while being adapted specifically for testlab's architecture and requirements.

