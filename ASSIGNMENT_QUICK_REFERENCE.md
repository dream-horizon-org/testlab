# Assignment API Quick Reference

## API Endpoint

```
POST /v1/assign
```

## Request Headers

```
x-user-id: <userId>
x-guest-id: <guestId> (optional)
x-tenant-id: <tenantId>
```

## Request Body

```json
{
  "apiPath": "/api/v1/home",
  "apiPaths": ["/api/v1/home", "/api/v1/search"],
  "entities": ["mobile", "android"],
  "isStatic": false,
  "attributes": {
    "trait": "power_user",
    "cohorts": ["premium_users", "early_adopters"],
    "customAttributes": {
      "location": "US",
      "age_group": "18-24"
    }
  }
}
```

## Response

```json
{
  "data": {
    "apiAssignmentMap": [
      {
        "experimentId": "550e8400-e29b-41d4-a716-446655440000",
        "experimentName": "Homepage Layout Test",
        "variant": "treatment",
        "status": "ASSIGNED",
        "variables": {
          "layout": "grid",
          "columns": 3,
          "showBanner": true
        },
        "isStatic": false,
        "isExclusive": false,
        "entities": ["mobile", "android"],
        "apiPath": "/api/v1/home",
        "assignedAt": 1698765432000
      }
    ],
    "completeAssignmentMap": {
      "/api/v1/home": [
        {
          "experimentId": "550e8400-e29b-41d4-a716-446655440000",
          "variant": "treatment",
          ...
        }
      ],
      "/api/v1/search": [
        {
          "experimentId": "660e8400-e29b-41d4-a716-446655440001",
          "variant": "control",
          ...
        }
      ]
    }
  }
}
```

## Key Classes and Methods

### Service Layer

#### AssignmentService
```java
Single<AssignmentResponse> assignExperiments(
    UUID tenantId, 
    String userId, 
    AssignmentRequest request
)
```

### DAO Layer

#### AssignmentDAO
```java
// Fetch active experiments
Single<List<Experiment>> fetchActiveExperiments(UUID tenantId)

// Get user assignments
Single<List<UserExperimentMap>> getUserAssignments(String userId, UUID tenantId)

// Save assignments
Single<Boolean> insertUserAssignments(
    String userId, 
    UUID tenantId, 
    List<UserExperimentMap> assignments
)

// Threshold check
Single<Boolean> checkThreshold(UUID experimentId)

// Lock management
Single<Boolean> acquireUserLock(String userId, UUID tenantId)
Single<Boolean> releaseUserLock(String userId, UUID tenantId)

// Variant count
Single<Long> incrementVariantCount(UUID experimentId, String variantName)

// Concluded experiments
Single<List<UserExperimentMap>> fetchConcludedExperiments(
    UUID tenantId, 
    String apiPath, 
    List<String> entities
)
```

### Helper Utilities

#### AssignmentServiceHelper

**Filtering**
```java
// Unassigned experiments
List<Experiment> getUnassignedExperiments(
    List<Experiment> activeExperiments, 
    List<UserExperimentMap> userAssignments
)

// Exclusive filter
List<Experiment> applyExclusiveFilter(
    List<Experiment> experiments, 
    List<UserExperimentMap> userAssignments
)

// API path filter
List<Experiment> applyApiPathFilter(
    List<Experiment> experiments, 
    AssignmentRequest request
)

// Entity filter
List<Experiment> applyEntityFilter(
    List<Experiment> experiments, 
    List<String> requestEntities
)

// Cohort filter
List<Experiment> applyCohortFilter(
    List<Experiment> experiments, 
    List<String> userCohorts
)

// Trait filter
List<Experiment> applyTraitFilter(
    List<Experiment> experiments, 
    String userTrait
)
```

**Variant Selection**
```java
Variant selectVariant(Experiment experiment, String userId)
```

**Mapping**
```java
UserExperimentMap createUserExperimentMap(
    Experiment experiment, 
    Variant variant, 
    String apiPath
)

List<UserExperimentMap> filterByApiPath(
    List<UserExperimentMap> assignments, 
    String apiPath, 
    List<String> entities
)

Map<String, List<UserExperimentMap>> groupByApiPath(
    List<UserExperimentMap> assignments
)
```

**Guest Carryover**
```java
List<UserExperimentMap> applyGuestCarryover(
    List<UserExperimentMap> guestAssignments,
    List<Experiment> activeExperiments,
    List<UserExperimentMap> currentUserAssignments
)
```

## Variant Assignment Strategies

### RandomVariantAssignment
```java
VariantAssignmentStrategy strategy = new RandomVariantAssignment();
Variant selected = strategy.selectVariant(variants, userId);
```
- Uses random selection based on percentage distribution
- Fair distribution over time

### RoundRobinVariantAssignment
```java
VariantAssignmentStrategy strategy = new RoundRobinVariantAssignment();
Variant selected = strategy.selectVariant(variants, userId);
```
- Balances assignments based on current counts
- Maintains target percentage ratios

## Common Use Cases

### 1. Basic Assignment
```java
AssignmentRequest request = AssignmentRequest.builder()
    .apiPath("/api/v1/home")
    .entities(Arrays.asList("mobile"))
    .isStatic(false)
    .build();

assignmentService.assignExperiments(tenantId, userId, request)
    .subscribe(response -> {
        // Handle response
    });
```

### 2. Assignment with Cohort Filter
```java
Attributes attributes = Attributes.builder()
    .cohorts(Arrays.asList("premium_users", "beta_testers"))
    .build();

AssignmentRequest request = AssignmentRequest.builder()
    .apiPath("/api/v1/premium")
    .attributes(attributes)
    .build();
```

### 3. Assignment with Trait Filter
```java
Attributes attributes = Attributes.builder()
    .trait("power_user")
    .build();

AssignmentRequest request = AssignmentRequest.builder()
    .apiPath("/api/v1/advanced")
    .attributes(attributes)
    .build();
```

### 4. Multiple API Paths
```java
AssignmentRequest request = AssignmentRequest.builder()
    .apiPaths(Arrays.asList("/api/v1/home", "/api/v1/search"))
    .entities(Arrays.asList("web"))
    .build();
```

## Experiment Configuration

### Experiment Entity
```java
Experiment experiment = Experiment.builder()
    .experimentId(UUID.randomUUID())
    .tenantId(tenantId)
    .name("Homepage Layout Test")
    .description("Testing grid vs list layout")
    .status("ACTIVE")
    .startTime(System.currentTimeMillis())
    .endTime(System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000L)
    .isStatic(false)
    .isExclusive(false)
    .entities(Arrays.asList("mobile", "android"))
    .threshold(10000L)
    .distributionStrategy(
        DistributionStrategy.builder()
            .strategyType("RANDOM")
            .build()
    )
    .apiPaths(Map.of(
        "/api/v1/home",
        ApiPathVariants.builder()
            .apiPath("/api/v1/home")
            .variants(Arrays.asList(
                Variant.builder()
                    .variantName("control")
                    .percentage(50)
                    .variables(Map.of("layout", "list"))
                    .build(),
                Variant.builder()
                    .variantName("treatment")
                    .percentage(50)
                    .variables(Map.of("layout", "grid", "columns", 3))
                    .build()
            ))
            .build()
    ))
    .build();
```

### Assignment Domain Types

#### COHORT
```java
AssignmentDomain domain = AssignmentDomain.builder()
    .domainType("COHORT")
    .cohortIds(Arrays.asList("premium_users", "beta_testers"))
    .build();
```

#### TRAIT
```java
AssignmentDomain domain = AssignmentDomain.builder()
    .domainType("TRAIT")
    .traitName("power_user")
    .build();
```

#### BULK
```java
AssignmentDomain domain = AssignmentDomain.builder()
    .domainType("BULK")
    .bulkUserIds(Arrays.asList(1001L, 1002L, 1003L))
    .build();
```

## Error Handling

### Common Errors
```java
// Lock acquisition failure
if (!lockAcquired) {
    log.warn("Failed to acquire lock, returning current assignments");
    // Returns existing assignments without new ones
}

// Threshold reached
if (!underThreshold) {
    log.debug("Experiment reached threshold, skipping assignment");
    // Skips this experiment
}

// No variant selected
if (selectedVariant == null) {
    log.warn("No variant selected, skipping experiment");
    // Skips this experiment
}
```

## Performance Tips

1. **Parallel Operations**: Fetch experiments and user assignments in parallel
2. **Caching**: Consider caching active experiments at application level
3. **Aerospike First**: Always check Aerospike before MySQL for better performance
4. **Batch Operations**: Use bulk insert for multiple assignments
5. **Lock Timeout**: Keep lock expiry short (10 seconds) to prevent deadlocks

## Monitoring and Logging

### Key Log Messages
```
INFO  - Assignment request for user: {}, tenant: {}, apiPath: {}
DEBUG - Fetched {} active experiments and {} user assignments
DEBUG - After {filter} filter: {} experiments
INFO  - Lock acquired for user {}
INFO  - Saved {} new assignments for user {}
DEBUG - Released lock for user {}: {}
INFO  - Assignment completed for user: {}, assigned: {}
```

### Metrics to Track
- Assignment request rate
- Lock acquisition success rate
- Average assignment latency
- Threshold breach rate per experiment
- Variant distribution per experiment

## Testing

### Unit Test Example
```java
@Test
public void testExclusiveFilter() {
    List<Experiment> experiments = createMockExperiments();
    List<UserExperimentMap> userAssignments = createMockAssignments();
    
    List<Experiment> filtered = 
        AssignmentServiceHelper.applyExclusiveFilter(experiments, userAssignments);
    
    assertThat(filtered).isEmpty(); // User has exclusive experiment
}
```

### Integration Test Example
```java
@Test
public void testAssignmentFlow() {
    AssignmentRequest request = createMockRequest();
    
    Single<AssignmentResponse> response = 
        assignmentService.assignExperiments(tenantId, userId, request);
    
    response.test()
        .assertNoErrors()
        .assertValue(resp -> !resp.getApiAssignmentMap().isEmpty());
}
```

## Troubleshooting

### Issue: No assignments returned
**Check:**
- Are there active experiments for the tenant?
- Do experiments match the API path?
- Have all filters been passed?
- Has the experiment reached threshold?

### Issue: Lock timeout
**Check:**
- Is Aerospike running and accessible?
- Are there too many concurrent requests for same user?
- Consider increasing lock expiry if assignments take longer

### Issue: Variant count mismatch
**Check:**
- Aerospike count sync
- MySQL count sync
- Race conditions during assignment

### Issue: Guest carryover not working
**Check:**
- Guest ID provided in request?
- Guest assignments exist?
- Experiments still active?

