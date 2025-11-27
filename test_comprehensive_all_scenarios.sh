#!/bin/bash

# Comprehensive test script for all create and update scenarios
# Including MANUAL variant weights, snake_case properties, and all validations

BASE_URL="http://localhost:8080/v1/experiment"
TENANT_ID="550e8400-e29b-41d4-a716-446655440000"
PROJECT_KEY="550e8400-e29b-41d4-a716-446655440001"

echo "=========================================="
echo "COMPREHENSIVE TEST: All Create & Update Scenarios"
echo "=========================================="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

PASS=0
FAIL=0

print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ PASS${NC}: $2"
        ((PASS++))
    else
        echo -e "${RED}✗ FAIL${NC}: $2"
        ((FAIL++))
    fi
}

echo "=========================================="
echo "CREATE FLOW TESTS"
echo "=========================================="
echo ""

# Test 1: Valid COHORT variant weights
echo "TEST 1: Create with COHORT variant weights"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Cohort Weights Test",
    "description": "Testing COHORT variant weights",
    "hypothesis": "Should work with cohorts",
    "status": "DRAFT",
    "type": "A_B",
    "created_by": "test@example.com",
    "cohorts": ["premium_users"],
    "variant_weights": {
      "type": "COHORT",
      "weights": {
        "control": 50.0,
        "variant1": 50.0
      }
    },
    "variants": {
      "control": {
        "display_name": "Control",
        "variables": [{"key": "feature", "value": "false", "data_type": "BOOL"}]
      },
      "variant1": {
        "display_name": "Variant 1",
        "variables": [{"key": "feature", "value": "true", "data_type": "BOOL"}]
      }
    },
    "distribution_strategy": "RANDOM",
    "assignment_domain": "COHORT",
    "exposure": 100,
    "threshold": 1000,
    "start_time": 1700000000,
    "end_time": 1800000000,
    "tags": ["cohort-test"],
    "owner": ["test@example.com"],
    "metrics": {"primary": ["conversion"], "secondary": ["engagement"]}
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$RESPONSE" | sed '$d')
if [ "$HTTP_CODE" -eq 200 ]; then
    print_result 0 "Create with COHORT weights"
    COHORT_EXP_ID=$(echo "$RESPONSE_BODY" | grep -o '"experiment_id":"[^"]*"' | cut -d'"' -f4)
else
    print_result 1 "Create with COHORT weights (HTTP $HTTP_CODE)"
fi
echo ""

# Test 2: Valid MANUAL variant weights
echo "TEST 2: Create with MANUAL variant weights"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Manual Weights Test",
    "description": "Testing MANUAL variant weights",
    "hypothesis": "Should work with manual assignments",
    "status": "DRAFT",
    "type": "A_B",
    "created_by": "test@example.com",
    "cohorts": ["test_users"],
    "variant_weights": {
      "type": "MANUAL",
      "weights": {
        "control": ["user1@test.com", "user2@test.com"],
        "variant1": ["user3@test.com", "user4@test.com"]
      }
    },
    "variants": {
      "control": {
        "display_name": "Control",
        "variables": [{"key": "feature", "value": "false", "data_type": "BOOL"}]
      },
      "variant1": {
        "display_name": "Variant 1",
        "variables": [{"key": "feature", "value": "true", "data_type": "BOOL"}]
      }
    },
    "distribution_strategy": "RANDOM",
    "assignment_domain": "MANUAL",
    "exposure": 100,
    "threshold": 1000,
    "start_time": 1700000000,
    "end_time": 1800000000,
    "tags": ["manual-test"],
    "owner": ["test@example.com"],
    "metrics": {"primary": ["conversion"], "secondary": ["engagement"]}
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$RESPONSE" | sed '$d')
if [ "$HTTP_CODE" -eq 200 ]; then
    print_result 0 "Create with MANUAL weights"
    MANUAL_EXP_ID=$(echo "$RESPONSE_BODY" | grep -o '"experiment_id":"[^"]*"' | cut -d'"' -f4)
else
    print_result 1 "Create with MANUAL weights (HTTP $HTTP_CODE)"
fi
echo ""

# Test 3: Snake case properties
echo "TEST 3: Create with snake_case properties (display_name, data_type)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Snake Case Test",
    "description": "Testing snake_case",
    "hypothesis": "Should accept snake_case",
    "status": "DRAFT",
    "type": "A_B",
    "created_by": "test@example.com",
    "cohorts": ["test"],
    "variant_weights": {"type": "COHORT", "weights": {"control": 100.0}},
    "variants": {
      "control": {
        "display_name": "Control Group",
        "variables": [
          {"key": "color", "value": "blue", "data_type": "STRING"},
          {"key": "enabled", "value": "false", "data_type": "BOOL"}
        ]
      }
    },
    "distribution_strategy": "RANDOM",
    "assignment_domain": "COHORT",
    "exposure": 100,
    "threshold": 1000,
    "start_time": 1700000000,
    "end_time": 1800000000,
    "tags": ["snake-test"],
    "owner": ["test@example.com"],
    "metrics": {"primary": ["conversion"], "secondary": ["engagement"]}
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$RESPONSE" | sed '$d')
if [ "$HTTP_CODE" -eq 200 ]; then
    print_result 0 "Create with snake_case properties"
    SNAKE_EXP_ID=$(echo "$RESPONSE_BODY" | grep -o '"experiment_id":"[^"]*"' | cut -d'"' -f4)
else
    print_result 1 "Create with snake_case properties (HTTP $HTTP_CODE)"
fi
echo ""

# Test 4: Invalid - Empty MANUAL user list
echo "TEST 4: Reject MANUAL with empty user list"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Empty Manual List",
    "description": "Test",
    "hypothesis": "Test",
    "status": "DRAFT",
    "type": "A_B",
    "created_by": "test@example.com",
    "cohorts": ["test"],
    "variant_weights": {"type": "MANUAL", "weights": {"control": [], "variant1": ["user1"]}},
    "variants": {
      "control": {"display_name": "C", "variables": [{"key": "t", "value": "1", "data_type": "NUMBER"}]},
      "variant1": {"display_name": "V", "variables": [{"key": "t", "value": "2", "data_type": "NUMBER"}]}
    },
    "distribution_strategy": "RANDOM",
    "assignment_domain": "MANUAL",
    "exposure": 100,
    "threshold": 1000,
    "start_time": 1700000000,
    "end_time": 1800000000,
    "tags": ["test"],
    "owner": ["test@example.com"],
    "metrics": {"primary": ["c"], "secondary": ["e"]}
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject empty MANUAL user list"
else
    print_result 1 "Reject empty MANUAL user list (Expected 400, got $HTTP_CODE)"
fi
echo ""

# Test 5: Invalid - Duplicate user in MANUAL
echo "TEST 5: Reject duplicate user in MANUAL variants"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Duplicate Manual User",
    "description": "Test",
    "hypothesis": "Test",
    "status": "DRAFT",
    "type": "A_B",
    "created_by": "test@example.com",
    "cohorts": ["test"],
    "variant_weights": {"type": "MANUAL", "weights": {"control": ["user1", "user2"], "variant1": ["user2", "user3"]}},
    "variants": {
      "control": {"display_name": "C", "variables": [{"key": "t", "value": "1", "data_type": "NUMBER"}]},
      "variant1": {"display_name": "V", "variables": [{"key": "t", "value": "2", "data_type": "NUMBER"}]}
    },
    "distribution_strategy": "RANDOM",
    "assignment_domain": "MANUAL",
    "exposure": 100,
    "threshold": 1000,
    "start_time": 1700000000,
    "end_time": 1800000000,
    "tags": ["test"],
    "owner": ["test@example.com"],
    "metrics": {"primary": ["c"], "secondary": ["e"]}
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject duplicate user in MANUAL"
else
    print_result 1 "Reject duplicate user in MANUAL (Expected 400, got $HTTP_CODE)"
fi
echo ""

# Test 6: Invalid - Weight sum != 100
echo "TEST 6: Reject COHORT with weight sum != 100"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Invalid Weight Sum",
    "description": "Test",
    "hypothesis": "Test",
    "status": "DRAFT",
    "type": "A_B",
    "created_by": "test@example.com",
    "cohorts": ["test"],
    "variant_weights": {"type": "COHORT", "weights": {"control": 60.0, "variant1": 50.0}},
    "variants": {
      "control": {"display_name": "C", "variables": [{"key": "t", "value": "1", "data_type": "NUMBER"}]},
      "variant1": {"display_name": "V", "variables": [{"key": "t", "value": "2", "data_type": "NUMBER"}]}
    },
    "distribution_strategy": "RANDOM",
    "assignment_domain": "COHORT",
    "exposure": 100,
    "threshold": 1000,
    "start_time": 1700000000,
    "end_time": 1800000000,
    "tags": ["test"],
    "owner": ["test@example.com"],
    "metrics": {"primary": ["c"], "secondary": ["e"]}
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject weight sum != 100"
else
    print_result 1 "Reject weight sum != 100 (Expected 400, got $HTTP_CODE)"
fi
echo ""

echo "=========================================="
echo "UPDATE FLOW TESTS"
echo "=========================================="
echo ""

# Test 7: Update COHORT experiment
if [ -n "$COHORT_EXP_ID" ]; then
    echo "TEST 7: Update COHORT experiment"
    RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/$COHORT_EXP_ID" \
      -H "Content-Type: application/json" \
      -H "x-tenant-id: $TENANT_ID" \
      -H "x-project-key: $PROJECT_KEY" \
      -d '{
        "description": "Updated COHORT experiment",
        "variant_weights": {"type": "COHORT", "weights": {"control": 30.0, "variant1": 70.0}},
        "exposure": 80
      }')
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    if [ "$HTTP_CODE" -eq 200 ]; then
        print_result 0 "Update COHORT experiment"
    else
        print_result 1 "Update COHORT experiment (HTTP $HTTP_CODE)"
    fi
else
    print_result 1 "Update COHORT experiment (skipped - no ID)"
fi
echo ""

# Test 8: Update MANUAL experiment
if [ -n "$MANUAL_EXP_ID" ]; then
    echo "TEST 8: Update MANUAL experiment with new users"
    RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/$MANUAL_EXP_ID" \
      -H "Content-Type: application/json" \
      -H "x-tenant-id: $TENANT_ID" \
      -H "x-project-key: $PROJECT_KEY" \
      -d '{
        "description": "Updated MANUAL experiment",
        "variant_weights": {"type": "MANUAL", "weights": {"control": ["user5", "user6"], "variant1": ["user7", "user8"]}},
        "exposure": 90
      }')
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    if [ "$HTTP_CODE" -eq 200 ]; then
        print_result 0 "Update MANUAL experiment"
    else
        print_result 1 "Update MANUAL experiment (HTTP $HTTP_CODE)"
    fi
else
    print_result 1 "Update MANUAL experiment (skipped - no ID)"
fi
echo ""

# Test 9: Update with snake_case
if [ -n "$SNAKE_EXP_ID" ]; then
    echo "TEST 9: Update with snake_case properties"
    RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/$SNAKE_EXP_ID" \
      -H "Content-Type: application/json" \
      -H "x-tenant-id: $TENANT_ID" \
      -H "x-project-key: $PROJECT_KEY" \
      -d '{
        "variants": {
          "control": {
            "display_name": "Updated Control",
            "variables": [
              {"key": "color", "value": "red", "data_type": "STRING"},
              {"key": "enabled", "value": "true", "data_type": "BOOL"},
              {"key": "count", "value": "5", "data_type": "NUMBER"}
            ]
          }
        }
      }')
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    if [ "$HTTP_CODE" -eq 200 ]; then
        print_result 0 "Update with snake_case"
    else
        print_result 1 "Update with snake_case (HTTP $HTTP_CODE)"
    fi
else
    print_result 1 "Update with snake_case (skipped - no ID)"
fi
echo ""

# Test 10: Invalid update - start_time > end_time
if [ -n "$COHORT_EXP_ID" ]; then
    echo "TEST 10: Reject update with start_time > end_time"
    RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/$COHORT_EXP_ID" \
      -H "Content-Type: application/json" \
      -H "x-tenant-id: $TENANT_ID" \
      -H "x-project-key: $PROJECT_KEY" \
      -d '{"start_time": 1900000000, "end_time": 1800000000}')
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    if [ "$HTTP_CODE" -eq 400 ]; then
        print_result 0 "Reject invalid time range"
    else
        print_result 1 "Reject invalid time range (Expected 400, got $HTTP_CODE)"
    fi
else
    print_result 1 "Reject invalid time range (skipped - no ID)"
fi
echo ""

# Test 11: Invalid update - non-updatable field
if [ -n "$COHORT_EXP_ID" ]; then
    echo "TEST 11: Reject update of non-updatable field (name)"
    RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/$COHORT_EXP_ID" \
      -H "Content-Type: application/json" \
      -H "x-tenant-id: $TENANT_ID" \
      -H "x-project-key: $PROJECT_KEY" \
      -d '{"name": "New Name", "description": "Test"}')
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    if [ "$HTTP_CODE" -eq 400 ]; then
        print_result 0 "Reject non-updatable field"
    else
        print_result 1 "Reject non-updatable field (Expected 400, got $HTTP_CODE)"
    fi
else
    print_result 1 "Reject non-updatable field (skipped - no ID)"
fi
echo ""

echo "=========================================="
echo "TEST SUMMARY"
echo "=========================================="
echo -e "${GREEN}Passed: $PASS${NC}"
echo -e "${RED}Failed: $FAIL${NC}"
echo "Total: $((PASS + FAIL))"
echo ""

if [ $FAIL -eq 0 ]; then
    echo -e "${GREEN}✓ All comprehensive tests passed!${NC}"
    exit 0
else
    echo -e "${YELLOW}⚠ Some tests failed. Review the output above.${NC}"
    exit 1
fi

