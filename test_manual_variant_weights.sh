#!/bin/bash

# Test script for MANUAL variant weights validation
# Tests user assignment validation

BASE_URL="http://localhost:8080/v1/experiment"
TENANT_ID="550e8400-e29b-41d4-a716-446655440000"
PROJECT_KEY="550e8400-e29b-41d4-a716-446655440001"

echo "=========================================="
echo "Testing MANUAL Variant Weights Validation"
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

echo "=== VALID SCENARIOS ==="
echo ""

# Test 1: Valid MANUAL variant weights
echo "TEST 1: Valid MANUAL variant weights with user assignments"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Manual Weights Valid Test",
    "description": "Testing valid manual variant weights",
    "hypothesis": "Should accept valid user assignments",
    "status": "DRAFT",
    "type": "A_B",
    "created_by": "test@example.com",
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
        "variables": [{"key": "test", "value": "false", "data_type": "BOOL"}]
      },
      "variant1": {
        "display_name": "Variant 1",
        "variables": [{"key": "test", "value": "true", "data_type": "BOOL"}]
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
if [ "$HTTP_CODE" -eq 200 ]; then
    print_result 0 "Valid MANUAL variant weights (HTTP 200)"
else
    print_result 1 "Valid MANUAL variant weights (Expected 200, got $HTTP_CODE)"
fi
echo ""

echo "=== INVALID SCENARIOS ==="
echo ""

# Test 2: Empty user list
echo "TEST 2: MANUAL variant with empty user list"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Manual Empty List Test",
    "description": "Testing empty user list",
    "hypothesis": "Should reject empty list",
    "status": "DRAFT",
    "type": "A_B",
    "created_by": "test@example.com",
    "variant_weights": {
      "type": "MANUAL",
      "weights": {
        "control": [],
        "variant1": ["user1@test.com"]
      }
    },
    "variants": {
      "control": {"display_name": "Control", "variables": [{"key": "test", "value": "false", "data_type": "BOOL"}]},
      "variant1": {"display_name": "Variant 1", "variables": [{"key": "test", "value": "true", "data_type": "BOOL"}]}
    },
    "distribution_strategy": "RANDOM",
    "assignment_domain": "MANUAL",
    "exposure": 100,
    "threshold": 1000,
    "start_time": 1700000000,
    "end_time": 1800000000,
    "tags": ["test"],
    "owner": ["test@example.com"],
    "metrics": {"primary": ["conversion"], "secondary": ["engagement"]}
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject empty user list (HTTP 400)"
else
    print_result 1 "Reject empty user list (Expected 400, got $HTTP_CODE)"
fi
echo ""

# Test 3: Blank user ID
echo "TEST 3: MANUAL variant with blank user ID"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Manual Blank User Test",
    "description": "Testing blank user ID",
    "hypothesis": "Should reject blank user ID",
    "status": "DRAFT",
    "type": "A_B",
    "created_by": "test@example.com",
    "variant_weights": {
      "type": "MANUAL",
      "weights": {
        "control": ["user1@test.com", ""],
        "variant1": ["user2@test.com"]
      }
    },
    "variants": {
      "control": {"display_name": "Control", "variables": [{"key": "test", "value": "false", "data_type": "BOOL"}]},
      "variant1": {"display_name": "Variant 1", "variables": [{"key": "test", "value": "true", "data_type": "BOOL"}]}
    },
    "distribution_strategy": "RANDOM",
    "assignment_domain": "MANUAL",
    "exposure": 100,
    "threshold": 1000,
    "start_time": 1700000000,
    "end_time": 1800000000,
    "tags": ["test"],
    "owner": ["test@example.com"],
    "metrics": {"primary": ["conversion"], "secondary": ["engagement"]}
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject blank user ID (HTTP 400)"
else
    print_result 1 "Reject blank user ID (Expected 400, got $HTTP_CODE)"
fi
echo ""

# Test 4: Duplicate user across variants
echo "TEST 4: MANUAL variant with duplicate user across variants"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Manual Duplicate User Test",
    "description": "Testing duplicate user",
    "hypothesis": "Should reject duplicate user",
    "status": "DRAFT",
    "type": "A_B",
    "created_by": "test@example.com",
    "variant_weights": {
      "type": "MANUAL",
      "weights": {
        "control": ["user1@test.com", "user2@test.com"],
        "variant1": ["user2@test.com", "user3@test.com"]
      }
    },
    "variants": {
      "control": {"display_name": "Control", "variables": [{"key": "test", "value": "false", "data_type": "BOOL"}]},
      "variant1": {"display_name": "Variant 1", "variables": [{"key": "test", "value": "true", "data_type": "BOOL"}]}
    },
    "distribution_strategy": "RANDOM",
    "assignment_domain": "MANUAL",
    "exposure": 100,
    "threshold": 1000,
    "start_time": 1700000000,
    "end_time": 1800000000,
    "tags": ["test"],
    "owner": ["test@example.com"],
    "metrics": {"primary": ["conversion"], "secondary": ["engagement"]}
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject duplicate user across variants (HTTP 400)"
else
    print_result 1 "Reject duplicate user across variants (Expected 400, got $HTTP_CODE)"
fi
echo ""

# Test 5: Missing control key
echo "TEST 5: MANUAL variant without 'control' key"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Manual No Control Test",
    "description": "Testing missing control",
    "hypothesis": "Should reject missing control",
    "status": "DRAFT",
    "type": "A_B",
    "created_by": "test@example.com",
    "variant_weights": {
      "type": "MANUAL",
      "weights": {
        "variant1": ["user1@test.com"],
        "variant2": ["user2@test.com"]
      }
    },
    "variants": {
      "variant1": {"display_name": "Variant 1", "variables": [{"key": "test", "value": "true", "data_type": "BOOL"}]},
      "variant2": {"display_name": "Variant 2", "variables": [{"key": "test", "value": "false", "data_type": "BOOL"}]}
    },
    "distribution_strategy": "RANDOM",
    "assignment_domain": "MANUAL",
    "exposure": 100,
    "threshold": 1000,
    "start_time": 1700000000,
    "end_time": 1800000000,
    "tags": ["test"],
    "owner": ["test@example.com"],
    "metrics": {"primary": ["conversion"], "secondary": ["engagement"]}
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject missing 'control' key (HTTP 400)"
else
    print_result 1 "Reject missing 'control' key (Expected 400, got $HTTP_CODE)"
fi
echo ""

# Test 6: Non-sequential variant keys
echo "TEST 6: MANUAL variant with non-sequential keys"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Manual Non-Sequential Test",
    "description": "Testing non-sequential keys",
    "hypothesis": "Should reject non-sequential",
    "status": "DRAFT",
    "type": "A_B",
    "created_by": "test@example.com",
    "variant_weights": {
      "type": "MANUAL",
      "weights": {
        "control": ["user1@test.com"],
        "variant3": ["user2@test.com"]
      }
    },
    "variants": {
      "control": {"display_name": "Control", "variables": [{"key": "test", "value": "false", "data_type": "BOOL"}]},
      "variant3": {"display_name": "Variant 3", "variables": [{"key": "test", "value": "true", "data_type": "BOOL"}]}
    },
    "distribution_strategy": "RANDOM",
    "assignment_domain": "MANUAL",
    "exposure": 100,
    "threshold": 1000,
    "start_time": 1700000000,
    "end_time": 1800000000,
    "tags": ["test"],
    "owner": ["test@example.com"],
    "metrics": {"primary": ["conversion"], "secondary": ["engagement"]}
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject non-sequential variant keys (HTTP 400)"
else
    print_result 1 "Reject non-sequential variant keys (Expected 400, got $HTTP_CODE)"
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
    echo -e "${GREEN}✓ All MANUAL variant weights validation tests passed!${NC}"
    exit 0
else
    echo -e "${YELLOW}⚠ Some tests failed. Review the output above.${NC}"
    exit 1
fi

