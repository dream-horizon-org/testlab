#!/bin/bash

BASE_URL="http://localhost:8080/v1/experiment"
TENANT_ID="550e8400-e29b-41d4-a716-446655440000"
PROJECT_KEY="550e8400-e29b-41d4-a716-446655440001"

echo "=========================================="
echo "EXPERIMENT API VALIDATION TESTS"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

test_count=0
pass_count=0
fail_count=0

run_test() {
    local test_name=$1
    local expected_result=$2
    local method=$3
    local endpoint=$4
    local payload=$5
    
    test_count=$((test_count + 1))
    echo -e "${YELLOW}Test $test_count: $test_name${NC}"
    
    response=$(curl -s -X $method "$BASE_URL$endpoint" \
        -H 'Content-Type: application/json' \
        -H "x-tenant-id: $TENANT_ID" \
        -H "x-project-key: $PROJECT_KEY" \
        -d "$payload")
    
    if [[ "$expected_result" == "SUCCESS" ]]; then
        if echo "$response" | grep -q '"status":true'; then
            echo -e "${GREEN}✓ PASS${NC}: $response"
            pass_count=$((pass_count + 1))
        else
            echo -e "${RED}✗ FAIL${NC}: Expected success but got: $response"
            fail_count=$((fail_count + 1))
        fi
    else
        if echo "$response" | grep -q '"error"'; then
            echo -e "${GREEN}✓ PASS${NC}: Validation error as expected: $(echo $response | grep -o '"message":"[^"]*"')"
            pass_count=$((pass_count + 1))
        else
            echo -e "${RED}✗ FAIL${NC}: Expected validation error but got: $response"
            fail_count=$((fail_count + 1))
        fi
    fi
    echo ""
}

echo "=========================================="
echo "CREATE FLOW - VALIDATION TESTS"
echo "=========================================="
echo ""

# Test 1: Valid create request
run_test "Valid Create Request" "SUCCESS" "POST" "" '{
  "name": "Valid Test 1",
  "description": "Valid",
  "hypothesis": "Valid",
  "status": "DRAFT",
  "type": "A_B",
  "cohorts": ["test"],
  "variant_weights": {"type": "COHORT", "weights": {"control": 100.0}},
  "variants": {"control": {"displayName": "C", "description": "C", "variables": [{"key": "t", "value": "true", "dataType": "BOOL"}]}},
  "distribution_strategy": "RANDOM",
  "assignment_domain": "COHORT",
  "exposure": 100,
  "threshold": 1000,
  "start_time": 1700000000,
  "end_time": 1800000000,
  "created_by": "test@example.com",
  "tags": ["test"],
  "owner": ["team@example.com"],
  "metrics": {"primary": ["c"], "secondary": ["e"]}
}'

# Test 2: Missing required field (name)
run_test "Missing Required Field (name)" "ERROR" "POST" "" '{
  "description": "Missing name",
  "status": "DRAFT",
  "type": "A_B",
  "cohorts": ["test"],
  "variant_weights": {"type": "COHORT", "weights": {"control": 100.0}},
  "variants": {"control": {"displayName": "C", "description": "C", "variables": [{"key": "t", "value": "true", "dataType": "BOOL"}]}},
  "distribution_strategy": "RANDOM",
  "assignment_domain": "COHORT",
  "exposure": 100,
  "threshold": 1000,
  "start_time": 1700000000,
  "end_time": 1800000000,
  "created_by": "test@example.com",
  "tags": ["test"],
  "owner": ["team@example.com"],
  "metrics": {"primary": ["c"], "secondary": ["e"]}
}'

# Test 3: Invalid status (only DRAFT or LIVE allowed)
run_test "Invalid Status (PAUSED not allowed on create)" "ERROR" "POST" "" '{
  "name": "Invalid Status Test",
  "description": "Test",
  "hypothesis": "Test",
  "status": "PAUSED",
  "type": "A_B",
  "cohorts": ["test"],
  "variant_weights": {"type": "COHORT", "weights": {"control": 100.0}},
  "variants": {"control": {"displayName": "C", "description": "C", "variables": [{"key": "t", "value": "true", "dataType": "BOOL"}]}},
  "distribution_strategy": "RANDOM",
  "assignment_domain": "COHORT",
  "exposure": 100,
  "threshold": 1000,
  "start_time": 1700000000,
  "end_time": 1800000000,
  "created_by": "test@example.com",
  "tags": ["test"],
  "owner": ["team@example.com"],
  "metrics": {"primary": ["c"], "secondary": ["e"]}
}'

# Test 4: Invalid enum value
run_test "Invalid Enum Value (type)" "ERROR" "POST" "" '{
  "name": "Invalid Enum Test",
  "description": "Test",
  "hypothesis": "Test",
  "status": "DRAFT",
  "type": "INVALID_TYPE",
  "cohorts": ["test"],
  "variant_weights": {"type": "COHORT", "weights": {"control": 100.0}},
  "variants": {"control": {"displayName": "C", "description": "C", "variables": [{"key": "t", "value": "true", "dataType": "BOOL"}]}},
  "distribution_strategy": "RANDOM",
  "assignment_domain": "COHORT",
  "exposure": 100,
  "threshold": 1000,
  "start_time": 1700000000,
  "end_time": 1800000000,
  "created_by": "test@example.com",
  "tags": ["test"],
  "owner": ["team@example.com"],
  "metrics": {"primary": ["c"], "secondary": ["e"]}
}'

# Test 5: Missing cohorts and rule_attributes (at least one required)
run_test "Missing Both Cohorts and Rule Attributes" "ERROR" "POST" "" '{
  "name": "Missing Targeting Test",
  "description": "Test",
  "hypothesis": "Test",
  "status": "DRAFT",
  "type": "A_B",
  "variant_weights": {"type": "COHORT", "weights": {"control": 100.0}},
  "variants": {"control": {"displayName": "C", "description": "C", "variables": [{"key": "t", "value": "true", "dataType": "BOOL"}]}},
  "distribution_strategy": "RANDOM",
  "assignment_domain": "COHORT",
  "exposure": 100,
  "threshold": 1000,
  "start_time": 1700000000,
  "end_time": 1800000000,
  "created_by": "test@example.com",
  "tags": ["test"],
  "owner": ["team@example.com"],
  "metrics": {"primary": ["c"], "secondary": ["e"]}
}'

# Test 6: Invalid variant keys (not sequential)
run_test "Invalid Variant Keys (not sequential)" "ERROR" "POST" "" '{
  "name": "Invalid Variant Keys Test",
  "description": "Test",
  "hypothesis": "Test",
  "status": "DRAFT",
  "type": "A_B",
  "cohorts": ["test"],
  "variant_weights": {"type": "COHORT", "weights": {"control": 50.0, "variant3": 50.0}},
  "variants": {
    "control": {"displayName": "C", "description": "C", "variables": [{"key": "t", "value": "true", "dataType": "BOOL"}]},
    "variant3": {"displayName": "V3", "description": "V3", "variables": [{"key": "t", "value": "false", "dataType": "BOOL"}]}
  },
  "distribution_strategy": "RANDOM",
  "assignment_domain": "COHORT",
  "exposure": 100,
  "threshold": 1000,
  "start_time": 1700000000,
  "end_time": 1800000000,
  "created_by": "test@example.com",
  "tags": ["test"],
  "owner": ["team@example.com"],
  "metrics": {"primary": ["c"], "secondary": ["e"]}
}'

# Test 7: Weight sum not equal to 100
run_test "Variant Weights Sum Not 100" "ERROR" "POST" "" '{
  "name": "Invalid Weight Sum Test",
  "description": "Test",
  "hypothesis": "Test",
  "status": "DRAFT",
  "type": "A_B",
  "cohorts": ["test"],
  "variant_weights": {"type": "COHORT", "weights": {"control": 50.0, "variant1": 30.0}},
  "variants": {
    "control": {"displayName": "C", "description": "C", "variables": [{"key": "t", "value": "true", "dataType": "BOOL"}]},
    "variant1": {"displayName": "V1", "description": "V1", "variables": [{"key": "t", "value": "false", "dataType": "BOOL"}]}
  },
  "distribution_strategy": "RANDOM",
  "assignment_domain": "COHORT",
  "exposure": 100,
  "threshold": 1000,
  "start_time": 1700000000,
  "end_time": 1800000000,
  "created_by": "test@example.com",
  "tags": ["test"],
  "owner": ["team@example.com"],
  "metrics": {"primary": ["c"], "secondary": ["e"]}
}'

# Test 8: Invalid metrics structure
run_test "Invalid Metrics Structure" "ERROR" "POST" "" '{
  "name": "Invalid Metrics Test",
  "description": "Test",
  "hypothesis": "Test",
  "status": "DRAFT",
  "type": "A_B",
  "cohorts": ["test"],
  "variant_weights": {"type": "COHORT", "weights": {"control": 100.0}},
  "variants": {"control": {"displayName": "C", "description": "C", "variables": [{"key": "t", "value": "true", "dataType": "BOOL"}]}},
  "distribution_strategy": "RANDOM",
  "assignment_domain": "COHORT",
  "exposure": 100,
  "threshold": 1000,
  "start_time": 1700000000,
  "end_time": 1800000000,
  "created_by": "test@example.com",
  "tags": ["test"],
  "owner": ["team@example.com"],
  "metrics": {"primary": ["c"]}
}'

# Test 9: End time less than start time
run_test "End Time Less Than Start Time" "ERROR" "POST" "" '{
  "name": "Invalid Time Range Test",
  "description": "Test",
  "hypothesis": "Test",
  "status": "DRAFT",
  "type": "A_B",
  "cohorts": ["test"],
  "variant_weights": {"type": "COHORT", "weights": {"control": 100.0}},
  "variants": {"control": {"displayName": "C", "description": "C", "variables": [{"key": "t", "value": "true", "dataType": "BOOL"}]}},
  "distribution_strategy": "RANDOM",
  "assignment_domain": "COHORT",
  "exposure": 100,
  "threshold": 1000,
  "start_time": 1800000000,
  "end_time": 1700000000,
  "created_by": "test@example.com",
  "tags": ["test"],
  "owner": ["team@example.com"],
  "metrics": {"primary": ["c"], "secondary": ["e"]}
}'

# Test 10: Invalid variable dataType
run_test "Invalid Variable DataType" "ERROR" "POST" "" '{
  "name": "Invalid DataType Test",
  "description": "Test",
  "hypothesis": "Test",
  "status": "DRAFT",
  "type": "A_B",
  "cohorts": ["test"],
  "variant_weights": {"type": "COHORT", "weights": {"control": 100.0}},
  "variants": {"control": {"displayName": "C", "description": "C", "variables": [{"key": "t", "value": "not_a_number", "dataType": "NUMBER"}]}},
  "distribution_strategy": "RANDOM",
  "assignment_domain": "COHORT",
  "exposure": 100,
  "threshold": 1000,
  "start_time": 1700000000,
  "end_time": 1800000000,
  "created_by": "test@example.com",
  "tags": ["test"],
  "owner": ["team@example.com"],
  "metrics": {"primary": ["c"], "secondary": ["e"]}
}'

echo "=========================================="
echo "UPDATE FLOW - VALIDATION TESTS"
echo "=========================================="
echo ""

# First create an experiment to update
EXPERIMENT_ID=$(curl -s -X POST "$BASE_URL" \
    -H 'Content-Type: application/json' \
    -H "x-tenant-id: $TENANT_ID" \
    -H "x-project-key: $PROJECT_KEY" \
    -d '{
      "name": "Update Test Experiment",
      "description": "For update tests",
      "hypothesis": "Test",
      "status": "DRAFT",
      "type": "A_B",
      "cohorts": ["test"],
      "variant_weights": {"type": "COHORT", "weights": {"control": 100.0}},
      "variants": {"control": {"displayName": "C", "description": "C", "variables": [{"key": "t", "value": "true", "dataType": "BOOL"}]}},
      "distribution_strategy": "RANDOM",
      "assignment_domain": "COHORT",
      "exposure": 100,
      "threshold": 1000,
      "start_time": 1700000000,
      "end_time": 1800000000,
      "created_by": "test@example.com",
      "tags": ["test"],
      "owner": ["team@example.com"],
      "metrics": {"primary": ["c"], "secondary": ["e"]}
    }' | grep -o '"experiment_id":"[^"]*"' | cut -d'"' -f4)

echo "Created test experiment: $EXPERIMENT_ID"
echo ""

# Test 11: Valid update request
run_test "Valid Update Request" "SUCCESS" "PATCH" "/$EXPERIMENT_ID" '{
  "description": "Updated description",
  "threshold": 5000
}'

# Test 12: Try to update non-updatable field (name)
run_test "Try to Update Non-Updatable Field (name)" "ERROR" "PATCH" "/$EXPERIMENT_ID" '{
  "name": "Trying to change name"
}'

# Test 13: Try to update non-updatable field (experiment_key)
run_test "Try to Update Non-Updatable Field (experiment_key)" "ERROR" "PATCH" "/$EXPERIMENT_ID" '{
  "experiment_key": "trying_to_change_key"
}'

# Test 14: Try to update non-updatable field (created_by)
run_test "Try to Update Non-Updatable Field (created_by)" "ERROR" "PATCH" "/$EXPERIMENT_ID" '{
  "created_by": "hacker@example.com"
}'

# Test 15: Valid status transition (DRAFT -> LIVE)
run_test "Valid Status Transition (DRAFT -> LIVE)" "SUCCESS" "PATCH" "/$EXPERIMENT_ID" '{
  "status": "LIVE"
}'

# Test 16: Valid status transition (LIVE -> PAUSED)
run_test "Valid Status Transition (LIVE -> PAUSED)" "SUCCESS" "PATCH" "/$EXPERIMENT_ID" '{
  "status": "PAUSED"
}'

# Test 17: Valid status transition (PAUSED -> LIVE)
run_test "Valid Status Transition (PAUSED -> LIVE)" "SUCCESS" "PATCH" "/$EXPERIMENT_ID" '{
  "status": "LIVE"
}'

# Test 18: Valid status transition (LIVE -> TERMINATED)
run_test "Valid Status Transition (LIVE -> TERMINATED)" "SUCCESS" "PATCH" "/$EXPERIMENT_ID" '{
  "status": "TERMINATED"
}'

# Test 19: Try to update TERMINATED experiment
run_test "Try to Update TERMINATED Experiment" "ERROR" "PATCH" "/$EXPERIMENT_ID" '{
  "description": "Trying to update terminated"
}'

# Test 20: Update metrics (should work now)
EXPERIMENT_ID2=$(curl -s -X POST "$BASE_URL" \
    -H 'Content-Type: application/json' \
    -H "x-tenant-id: $TENANT_ID" \
    -H "x-project-key: $PROJECT_KEY" \
    -d '{
      "name": "Metrics Update Test",
      "description": "Test",
      "hypothesis": "Test",
      "status": "DRAFT",
      "type": "A_B",
      "cohorts": ["test"],
      "variant_weights": {"type": "COHORT", "weights": {"control": 100.0}},
      "variants": {"control": {"displayName": "C", "description": "C", "variables": [{"key": "t", "value": "true", "dataType": "BOOL"}]}},
      "distribution_strategy": "RANDOM",
      "assignment_domain": "COHORT",
      "exposure": 100,
      "threshold": 1000,
      "start_time": 1700000000,
      "end_time": 1800000000,
      "created_by": "test@example.com",
      "tags": ["test"],
      "owner": ["team@example.com"],
      "metrics": {"primary": ["c"], "secondary": ["e"]}
    }' | grep -o '"experiment_id":"[^"]*"' | cut -d'"' -f4)

run_test "Update Metrics (Updatable Field)" "SUCCESS" "PATCH" "/$EXPERIMENT_ID2" '{
  "metrics": {"primary": ["new_metric1", "new_metric2"], "secondary": ["new_metric3"]}
}'

# Test 21: Update tags (should work now)
run_test "Update Tags (Updatable Field)" "SUCCESS" "PATCH" "/$EXPERIMENT_ID2" '{
  "tags": ["new_tag1", "new_tag2"]
}'

# Test 22: Update owner (should work now)
run_test "Update Owner (Updatable Field)" "SUCCESS" "PATCH" "/$EXPERIMENT_ID2" '{
  "owner": ["new_owner@example.com"]
}'

# Test 23: Add new variant
run_test "Add New Variant" "SUCCESS" "PATCH" "/$EXPERIMENT_ID2" '{
  "variants": {
    "control": {"displayName": "C", "description": "C", "variables": [{"key": "t", "value": "true", "dataType": "BOOL"}]},
    "variant1": {"displayName": "V1", "description": "New", "variables": [{"key": "t", "value": "false", "dataType": "BOOL"}]}
  },
  "variant_weights": {"type": "COHORT", "weights": {"control": 50.0, "variant1": 50.0}}
}'

# Test 24: Update rule_attributes
run_test "Update Rule Attributes" "SUCCESS" "PATCH" "/$EXPERIMENT_ID2" '{
  "rule_attributes": [
    {
      "name": "New Rule",
      "conditions": [
        {
          "operand": "platform",
          "operandDataType": "STRING",
          "operator": "=",
          "value": "ios"
        }
      ]
    }
  ]
}'

echo "=========================================="
echo "TEST SUMMARY"
echo "=========================================="
echo -e "Total Tests: $test_count"
echo -e "${GREEN}Passed: $pass_count${NC}"
echo -e "${RED}Failed: $fail_count${NC}"
echo ""

if [ $fail_count -eq 0 ]; then
    echo -e "${GREEN}✓ ALL TESTS PASSED!${NC}"
    exit 0
else
    echo -e "${RED}✗ SOME TESTS FAILED${NC}"
    exit 1
fi

