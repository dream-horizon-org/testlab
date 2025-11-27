# Update Flow Test Summary

**Experiment ID:** `610548f5-1a65-4c68-97be-ffeba9f5b655`  
**Test Date:** 2025-11-26  
**Total Tests:** 11  
**Passed:** 11/11 (100%)

---

## Database Verification Queries

Run these queries in your PostgreSQL client to verify all changes:

```sql
-- Set the experiment ID
\set exp_id '610548f5-1a65-4c68-97be-ffeba9f5b655'

-- View complete experiment data
SELECT 
  experiment_id,
  name,
  description,
  hypothesis,
  status,
  exposure,
  cohorts,
  tags,
  variants,
  created_at,
  updated_at
FROM experiments 
WHERE experiment_id = :'exp_id'::uuid 
  AND project_key = '550e8400-e29b-41d4-a716-446655440001';

-- Verify variant values (should be 'red' and 'yellow' from Test 2)
SELECT 
  variants->'control'->'variables'->0->>'value' as control_button_color,
  variants->'variant1'->'variables'->0->>'value' as variant1_button_color,
  variants->'control'->'variables'->0->>'key' as control_key,
  variants->'control'->'variables'->0->>'data_type' as control_data_type
FROM experiments 
WHERE experiment_id = :'exp_id'::uuid;

-- Expected results:
-- control_button_color: 'red'
-- variant1_button_color: 'yellow'
-- control_key: 'button_color'
-- control_data_type: 'STRING'

-- Verify final state
SELECT 
  status,           -- Should be: PAUSED
  description,      -- Should be: 'UPDATED in LIVE state'
  exposure,         -- Should be: 75
  tags              -- Should be: ["live", "updated"]
FROM experiments 
WHERE experiment_id = :'exp_id'::uuid;
```

---

## Test Results

### ✅ DRAFT State Tests

#### TEST 1: Update Description in DRAFT
- **Status:** ✅ PASS
- **Action:** Updated description to "UPDATED description in DRAFT"
- **Result:** Successfully updated
- **Verification:** 
  ```sql
  SELECT description FROM experiments WHERE experiment_id = '610548f5-1a65-4c68-97be-ffeba9f5b655'::uuid;
  -- Expected: 'UPDATED description in DRAFT'
  ```

#### TEST 2: Update Variant VALUES Only
- **Status:** ✅ PASS
- **Action:** Changed button_color values: blue→red, green→yellow
- **Result:** Successfully updated (keys and data_types unchanged)
- **Verification:**
  ```sql
  SELECT 
    variants->'control'->'variables'->0->>'value' as control_color,
    variants->'variant1'->'variables'->0->>'value' as variant1_color
  FROM experiments WHERE experiment_id = '610548f5-1a65-4c68-97be-ffeba9f5b655'::uuid;
  -- Expected: control_color='red', variant1_color='yellow'
  ```

#### TEST 3: Try to Change Variable KEY
- **Status:** ✅ PASS (Correctly Blocked)
- **Action:** Attempted to change 'button_color' to 'btn_color'
- **Result:** Blocked with error
- **Error Message:** 
  ```
  Variable keys cannot be changed for variant 'control'. 
  Expected keys: [button_color, feature_enabled, max_items], 
  but got: [feature_enabled, btn_color, max_items]. 
  Only variable values can be updated.
  ```
- **Verification:**
  ```sql
  SELECT variants->'control'->'variables'->0->>'key' as control_key
  FROM experiments WHERE experiment_id = '610548f5-1a65-4c68-97be-ffeba9f5b655'::uuid;
  -- Expected: 'button_color' (unchanged)
  ```

#### TEST 4: Try to Change Variable DATA_TYPE
- **Status:** ✅ PASS (Correctly Blocked)
- **Action:** Attempted to change button_color from STRING to NUMBER
- **Result:** Blocked with error
- **Error Message:**
  ```
  Variable data_type cannot be changed for variant 'control', variable 'button_color'. 
  Expected data_type: 'STRING', but got: 'NUMBER'. 
  Only variable values can be updated.
  ```
- **Verification:**
  ```sql
  SELECT variants->'control'->'variables'->0->>'data_type' as control_data_type
  FROM experiments WHERE experiment_id = '610548f5-1a65-4c68-97be-ffeba9f5b655'::uuid;
  -- Expected: 'STRING' (unchanged)
  ```

#### TEST 5: Try to Add New Variable
- **Status:** ✅ PASS (Correctly Blocked)
- **Action:** Attempted to add 'new_field' to control variant
- **Result:** Blocked with error
- **Error Message:**
  ```
  Variable count mismatch for variant 'control'. 
  Expected 3 variables, but got 4
  ```

---

### ✅ State Transition Tests

#### TEST 6: Transition DRAFT → LIVE
- **Status:** ✅ PASS
- **Action:** Changed status from DRAFT to LIVE
- **Result:** Successfully transitioned
- **Verification:**
  ```sql
  SELECT status FROM experiments WHERE experiment_id = '610548f5-1a65-4c68-97be-ffeba9f5b655'::uuid;
  -- Expected: 'LIVE'
  ```

---

### ✅ LIVE State Tests

#### TEST 7: Try to Update Variants in LIVE
- **Status:** ✅ PASS (Correctly Blocked)
- **Action:** Attempted to change variant values in LIVE state
- **Result:** Blocked with error
- **Error Message:**
  ```
  Cannot update the following fields in LIVE state: variants. 
  Only these fields can be updated in LIVE state: 
  owner, updated_by, threshold, description, hypothesis, winning_variant, 
  end_time, status, tags, metrics, guardrail_health_status, exposure
  ```
- **Verification:**
  ```sql
  SELECT variants->'control'->'variables'->0->>'value' as control_color
  FROM experiments WHERE experiment_id = '610548f5-1a65-4c68-97be-ffeba9f5b655'::uuid;
  -- Expected: 'red' (unchanged from Test 2)
  ```

#### TEST 8: Update Allowed Fields in LIVE
- **Status:** ✅ PASS
- **Action:** Updated description, exposure, and tags
- **Result:** Successfully updated
- **Verification:**
  ```sql
  SELECT description, exposure, tags 
  FROM experiments WHERE experiment_id = '610548f5-1a65-4c68-97be-ffeba9f5b655'::uuid;
  -- Expected: 
  --   description='UPDATED in LIVE state'
  --   exposure=75
  --   tags=["live", "updated"]
  ```

#### TEST 9: Try to Update Cohorts in LIVE
- **Status:** ✅ PASS (Correctly Blocked)
- **Action:** Attempted to update cohorts
- **Result:** Blocked with error
- **Error Message:**
  ```
  Cannot update the following fields in LIVE state: cohorts. 
  Only these fields can be updated in LIVE state: ...
  ```
- **Verification:**
  ```sql
  SELECT cohorts FROM experiments WHERE experiment_id = '610548f5-1a65-4c68-97be-ffeba9f5b655'::uuid;
  -- Expected: ["premium_users", "mobile_users"] (unchanged)
  ```

---

### ✅ PAUSED State Tests

#### TEST 10: Transition LIVE → PAUSED
- **Status:** ✅ PASS
- **Action:** Changed status from LIVE to PAUSED
- **Result:** Successfully transitioned
- **Verification:**
  ```sql
  SELECT status FROM experiments WHERE experiment_id = '610548f5-1a65-4c68-97be-ffeba9f5b655'::uuid;
  -- Expected: 'PAUSED'
  ```

#### TEST 11: Try to Update Description in PAUSED
- **Status:** ✅ PASS (Correctly Blocked)
- **Action:** Attempted to update description
- **Result:** Blocked with error
- **Error Message:**
  ```
  Cannot update experiment in PAUSED state. 
  Only status transitions are allowed. 
  Attempted to update: description
  ```
- **Verification:**
  ```sql
  SELECT description FROM experiments WHERE experiment_id = '610548f5-1a65-4c68-97be-ffeba9f5b655'::uuid;
  -- Expected: 'UPDATED in LIVE state' (unchanged from Test 8)
  ```

---

## Implementation Summary

### 1. State-Based Validation (Strategy Pattern)

**Files:**
- `StateValidationStrategy.java` - Interface
- `DraftStateValidationStrategy.java` - Allows all updates
- `LiveStateValidationStrategy.java` - Restricts to specific fields
- `PausedStateValidationStrategy.java` - Blocks all updates except status
- `StateValidationContext.java` - Context manager

**Key Features:**
- ✅ Dependency injection for all strategies
- ✅ Clear error messages listing allowed fields
- ✅ Extensible for new states (CONCLUDED, TERMINATED, etc.)

### 2. Variant Structure Validation (Annotation-Based)

**Files:**
- `@ValidVariantStructure` - Marker annotation
- `VariantStructureValidator.java` - Service-layer validator
- `VariantStructureConstraintValidator.java` - Hibernate validator

**Key Features:**
- ✅ Validates variant keys cannot change
- ✅ Validates variable keys cannot change
- ✅ Validates variable data_types cannot change
- ✅ Validates variable count remains same
- ✅ Allows only variable values to change
- ✅ Handles both String and Map types from database

### 3. Field Restriction Validation

**Files:**
- `@ValidUpdateRequest` - Annotation for non-updatable fields
- `UpdateRequestValidator.java` - Validator

**Key Features:**
- ✅ Blocks updates to: name, experimentKey, createdBy
- ✅ Clear error messages

---

## Allowed Fields by State

### DRAFT State
**All fields can be updated** ✅

### LIVE State
**Only these fields can be updated:**
- description
- hypothesis
- status
- guardrail_health_status
- winning_variant
- exposure
- threshold
- end_time
- tags
- owner
- metrics
- updated_by

**Blocked fields:**
- name
- experiment_key
- project_key
- created_by
- created_at
- type
- cohorts
- **variants** ⚠️
- variant_weights
- distribution_strategy
- assignment_domain
- overrides
- rule_attributes
- start_time

### PAUSED State
**Only status transitions are allowed** ⚠️  
All other updates are blocked.

---

## Error Messages

All error messages are:
- ✅ Clear and actionable
- ✅ Specify what was attempted
- ✅ Specify what is allowed
- ✅ Use consistent format
- ✅ Include relevant context (variant name, field names, etc.)

---

## Next Steps (Optional Enhancements)

1. **Add CONCLUDED and TERMINATED state strategies**
2. **Add audit logging for all state transitions**
3. **Add webhook notifications for state changes**
4. **Add variant structure validation for MANUAL variant weights**
5. **Add integration tests for all state transitions**

---

## Conclusion

✅ **All 11 tests passed successfully!**

The update flow implementation is **complete and production-ready** with:
- Robust state-based validation using Strategy Pattern
- Comprehensive variant structure validation
- Clear, actionable error messages
- Proper dependency injection
- Extensible architecture for future enhancements

