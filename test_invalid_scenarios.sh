#!/bin/bash

# Test script for invalid scenarios - POJO refactoring validation
# Tests validation for both create and update flows

BASE_URL="http://localhost:8080/v1/experiment"
TENANT_ID="550e8400-e29b-41d4-a716-446655440000"
PROJECT_KEY="550e8400-e29b-41d4-a716-446655440001"

echo "=========================================="
echo "Testing Invalid Scenarios (POJO Refactoring)"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counter
PASS=0
FAIL=0

# Function to print test result
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ PASS${NC}: $2"
        ((PASS++))
    else
        echo -e "${RED}✗ FAIL${NC}: $2"
        ((FAIL++))
    fi
}

# First, create a valid experiment for update tests
echo "=========================================="
echo "SETUP: Creating valid experiment for update tests"
echo "=========================================="

SETUP_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Valid Test Experiment",
    "description": "For testing invalid updates",
    "hypothesis": "Test hypothesis",
    "status": "DRAFT",
    "type": "A_B",
    "guardrail_health_status": "PASSING",
    "cohorts": ["test_users"],
    "variant_weights": {
      "type": "COHORT",
      "weights": {
        "control": 50.0,
        "variant1": 50.0
      }
    },
    "variants": {
      "control": {
        "displayName": "Control",
        "variables": [
          {"key": "feature", "value": "false", "dataType": "BOOL"}
        ]
      },
      "variant1": {
        "displayName": "Variant",
        "variables": [
          {"key": "feature", "value": "true", "dataType": "BOOL"}
        ]
      }
    },
    "distribution_strategy": "RANDOM",
    "assignment_domain": "DEFAULT",
    "exposure": 100,
    "threshold": 10000,
    "start_time": 1700000000,
    "end_time": 1800000000,
    "created_by": "test@example.com",
    "tags": ["test"],
    "owner": ["team@example.com"],
    "metrics": {
      "primary": ["conversion"],
      "secondary": ["engagement"]
    }
  }')

HTTP_CODE=$(echo "$SETUP_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$SETUP_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    EXPERIMENT_ID=$(echo "$RESPONSE_BODY" | grep -o '"experiment_id":"[^"]*"' | cut -d'"' -f4)
    echo "✓ Created experiment ID: $EXPERIMENT_ID"
else
    echo "✗ Failed to create setup experiment (HTTP $HTTP_CODE)"
    echo "Response: $RESPONSE_BODY"
    exit 1
fi

echo ""
echo "=========================================="
echo "CREATE FLOW - INVALID SCENARIOS"
echo "=========================================="
echo ""

# Test 1: Missing required field (name)
echo "TEST 1: Create without required field 'name'"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "description": "Missing name field",
    "status": "DRAFT",
    "type": "A_B",
    "created_by": "test@example.com"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject create without 'name' (400 Bad Request)"
else
    print_result 1 "Reject create without 'name' (Expected 400, got $HTTP_CODE)"
fi
echo ""

# Test 2: Invalid enum value for status
echo "TEST 2: Create with invalid status enum"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Invalid Status Test",
    "status": "INVALID_STATUS",
    "type": "A_B",
    "created_by": "test@example.com"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject invalid status enum (400 Bad Request)"
else
    print_result 1 "Reject invalid status enum (Expected 400, got $HTTP_CODE)"
fi
echo ""

# Test 3: Invalid enum value for type
echo "TEST 3: Create with invalid type enum"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Invalid Type Test",
    "status": "DRAFT",
    "type": "INVALID_TYPE",
    "created_by": "test@example.com"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject invalid type enum (400 Bad Request)"
else
    print_result 1 "Reject invalid type enum (Expected 400, got $HTTP_CODE)"
fi
echo ""

# Test 4: Invalid distribution_strategy enum
echo "TEST 4: Create with invalid distribution_strategy"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Invalid Distribution Strategy",
    "status": "DRAFT",
    "type": "A_B",
    "distribution_strategy": "INVALID_STRATEGY",
    "created_by": "test@example.com"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject invalid distribution_strategy (400 Bad Request)"
else
    print_result 1 "Reject invalid distribution_strategy (Expected 400, got $HTTP_CODE)"
fi
echo ""

# Test 5: Invalid assignment_domain enum
echo "TEST 5: Create with invalid assignment_domain"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Invalid Assignment Domain",
    "status": "DRAFT",
    "type": "A_B",
    "assignment_domain": "INVALID_DOMAIN",
    "created_by": "test@example.com"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject invalid assignment_domain (400 Bad Request)"
else
    print_result 1 "Reject invalid assignment_domain (Expected 400, got $HTTP_CODE)"
fi
echo ""

# Test 6: Invalid variant key format
echo "TEST 6: Create with invalid variant key (not control/variant1/variant2)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Invalid Variant Keys",
    "status": "DRAFT",
    "type": "A_B",
    "variants": {
      "invalid_key": {
        "displayName": "Invalid",
        "variables": []
      }
    },
    "created_by": "test@example.com"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject invalid variant keys (400 Bad Request)"
else
    print_result 1 "Reject invalid variant keys (Expected 400, got $HTTP_CODE)"
fi
echo ""

# Test 7: Variant weights sum not equal to 100
echo "TEST 7: Create with variant weights sum != 100"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Invalid Weight Sum",
    "status": "DRAFT",
    "type": "A_B",
    "variant_weights": {
      "type": "COHORT",
      "weights": {
        "control": 60.0,
        "variant1": 50.0
      }
    },
    "variants": {
      "control": {"displayName": "Control", "variables": []},
      "variant1": {"displayName": "Variant", "variables": []}
    },
    "created_by": "test@example.com"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject weight sum != 100 (400 Bad Request)"
else
    print_result 1 "Reject weight sum != 100 (Expected 400, got $HTTP_CODE)"
fi
echo ""

# Test 8: Invalid status for create (not DRAFT or LIVE)
echo "TEST 8: Create with status CONCLUDED (only DRAFT/LIVE allowed)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Invalid Create Status",
    "status": "CONCLUDED",
    "type": "A_B",
    "created_by": "test@example.com"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject create with CONCLUDED status (400 Bad Request)"
else
    print_result 1 "Reject create with CONCLUDED status (Expected 400, got $HTTP_CODE)"
fi
echo ""

# Test 9: Invalid variable dataType
echo "TEST 9: Create with invalid variable dataType"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Invalid Variable DataType",
    "status": "DRAFT",
    "type": "A_B",
    "variants": {
      "control": {
        "displayName": "Control",
        "variables": [
          {"key": "test", "value": "123", "dataType": "INVALID_TYPE"}
        ]
      }
    },
    "created_by": "test@example.com"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject invalid variable dataType (400 Bad Request)"
else
    print_result 1 "Reject invalid variable dataType (Expected 400, got $HTTP_CODE)"
fi
echo ""

# Test 10: Invalid condition operand
echo "TEST 10: Create with invalid condition operand"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Invalid Operand",
    "status": "DRAFT",
    "type": "A_B",
    "rule_attributes": [
      {
        "name": "Test Rule",
        "conditions": [
          {
            "operand": "invalid_operand",
            "operandDataType": "STRING",
            "operator": "=",
            "value": "test"
          }
        ]
      }
    ],
    "created_by": "test@example.com"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject invalid operand (400 Bad Request)"
else
    print_result 1 "Reject invalid operand (Expected 400, got $HTTP_CODE)"
fi
echo ""

echo "=========================================="
echo "UPDATE FLOW - INVALID SCENARIOS"
echo "=========================================="
echo ""

# Test 11: Update non-updatable field (name)
echo "TEST 11: Update non-updatable field 'name'"
RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/$EXPERIMENT_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Trying to update name",
    "description": "Should fail"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject update of non-updatable field 'name' (400 Bad Request)"
else
    print_result 1 "Reject update of 'name' (Expected 400, got $HTTP_CODE)"
fi
echo ""

# Test 12: Update non-updatable field (experiment_key)
echo "TEST 12: Update non-updatable field 'experiment_key'"
RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/$EXPERIMENT_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "experiment_key": "new_key",
    "description": "Should fail"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject update of 'experiment_key' (400 Bad Request)"
else
    print_result 1 "Reject update of 'experiment_key' (Expected 400, got $HTTP_CODE)"
fi
echo ""

# Test 13: Update non-updatable field (created_by)
echo "TEST 13: Update non-updatable field 'created_by'"
RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/$EXPERIMENT_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "created_by": "hacker@example.com",
    "description": "Should fail"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject update of 'created_by' (400 Bad Request)"
else
    print_result 1 "Reject update of 'created_by' (Expected 400, got $HTTP_CODE)"
fi
echo ""

# Test 14: Update with invalid status enum
echo "TEST 14: Update with invalid status enum"
RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/$EXPERIMENT_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "status": "INVALID_STATUS"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject update with invalid status (400 Bad Request)"
else
    print_result 1 "Reject update with invalid status (Expected 400, got $HTTP_CODE)"
fi
echo ""

# Test 15: Update with invalid distribution_strategy
echo "TEST 15: Update with invalid distribution_strategy"
RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/$EXPERIMENT_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "distribution_strategy": "INVALID_STRATEGY"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject update with invalid distribution_strategy (400 Bad Request)"
else
    print_result 1 "Reject update with invalid distribution_strategy (Expected 400, got $HTTP_CODE)"
fi
echo ""

# Test 16: Update with invalid assignment_domain
echo "TEST 16: Update with invalid assignment_domain"
RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/$EXPERIMENT_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "assignment_domain": "INVALID_DOMAIN"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject update with invalid assignment_domain (400 Bad Request)"
else
    print_result 1 "Reject update with invalid assignment_domain (Expected 400, got $HTTP_CODE)"
fi
echo ""

# Test 17: Update variant weights with sum != 100
echo "TEST 17: Update with variant weights sum != 100"
RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/$EXPERIMENT_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "variant_weights": {
      "type": "COHORT",
      "weights": {
        "control": 70.0,
        "variant1": 50.0
      }
    }
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject update with weight sum != 100 (400 Bad Request)"
else
    print_result 1 "Reject update with weight sum != 100 (Expected 400, got $HTTP_CODE)"
fi
echo ""

# Test 18: Update with start_time > end_time
echo "TEST 18: Update with start_time > end_time"
RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/$EXPERIMENT_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "start_time": 1900000000,
    "end_time": 1800000000
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject update with start_time > end_time (400 Bad Request)"
else
    print_result 1 "Reject update with start_time > end_time (Expected 400, got $HTTP_CODE)"
fi
echo ""

# Test 19: Create experiment with TERMINATED status, then try to update
echo "TEST 19: Update experiment in TERMINATED state (terminal state)"

# First, create and set to TERMINATED
TERM_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Terminal State Test",
    "status": "DRAFT",
    "type": "A_B",
    "created_by": "test@example.com"
  }')

TERM_HTTP_CODE=$(echo "$TERM_RESPONSE" | tail -n1)
TERM_RESPONSE_BODY=$(echo "$TERM_RESPONSE" | sed '$d')

if [ "$TERM_HTTP_CODE" -eq 200 ]; then
    TERM_EXP_ID=$(echo "$TERM_RESPONSE_BODY" | grep -o '"experiment_id":"[^"]*"' | cut -d'"' -f4)
    
    # Update to TERMINATED
    curl -s -X PATCH "$BASE_URL/$TERM_EXP_ID" \
      -H "Content-Type: application/json" \
      -H "x-tenant-id: $TENANT_ID" \
      -H "x-project-key: $PROJECT_KEY" \
      -d '{"status": "TERMINATED"}' > /dev/null
    
    # Try to update TERMINATED experiment
    RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/$TERM_EXP_ID" \
      -H "Content-Type: application/json" \
      -H "x-tenant-id: $TENANT_ID" \
      -H "x-project-key: $PROJECT_KEY" \
      -d '{
        "description": "Should not update"
      }')
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    if [ "$HTTP_CODE" -eq 400 ]; then
        print_result 0 "Reject update of TERMINATED experiment (400 Bad Request)"
    else
        print_result 1 "Reject update of TERMINATED experiment (Expected 400, got $HTTP_CODE)"
    fi
else
    print_result 1 "Could not create experiment for terminal state test"
fi
echo ""

# Test 20: Invalid metrics structure (not primary/secondary keys)
echo "TEST 20: Create with invalid metrics structure"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Invalid Metrics Structure",
    "status": "DRAFT",
    "type": "A_B",
    "metrics": {
      "invalid_key": ["metric1"],
      "another_invalid": ["metric2"]
    },
    "created_by": "test@example.com"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject invalid metrics structure (400 Bad Request)"
else
    print_result 1 "Reject invalid metrics structure (Expected 400, got $HTTP_CODE)"
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
    echo -e "${GREEN}✓ All validation tests passed! POJO refactoring maintains proper validation.${NC}"
    exit 0
else
    echo -e "${YELLOW}⚠ Some validation tests failed. Review the output above.${NC}"
    exit 1
fi

