#!/bin/bash

# Test script for LIVE state field restrictions
# Tests that only allowed fields can be updated in LIVE state

BASE_URL="http://localhost:8080/v1/experiment"
TENANT_ID="550e8400-e29b-41d4-a716-446655440000"
PROJECT_KEY="550e8400-e29b-41d4-a716-446655440001"

echo "=========================================="
echo "Testing LIVE State Field Restrictions"
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

echo "=== SETUP: Create experiment in DRAFT state ==="
CREATE_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Live State Test Experiment",
    "description": "Testing LIVE state restrictions",
    "hypothesis": "Only certain fields can be updated in LIVE",
    "status": "DRAFT",
    "type": "A_B",
    "created_by": "test@example.com",
    "cohorts": ["premium_users"],
    "variant_weights": {"type": "COHORT", "weights": {"control": 50.0, "variant1": 50.0}},
    "variants": {
      "control": {"display_name": "Control", "variables": [{"key": "feature", "value": "false", "data_type": "BOOL"}]},
      "variant1": {"display_name": "Variant 1", "variables": [{"key": "feature", "value": "true", "data_type": "BOOL"}]}
    },
    "distribution_strategy": "RANDOM",
    "assignment_domain": "COHORT",
    "exposure": 100,
    "threshold": 1000,
    "start_time": 1700000000,
    "end_time": 1800000000,
    "tags": ["live-test"],
    "owner": ["test@example.com"],
    "metrics": {"primary": ["conversion"], "secondary": ["engagement"]}
  }')

HTTP_CODE=$(echo "$CREATE_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$CREATE_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    EXPERIMENT_ID=$(echo "$RESPONSE_BODY" | grep -o '"experiment_id":"[^"]*"' | cut -d'"' -f4)
    echo "✓ Created experiment: $EXPERIMENT_ID"
else
    echo "✗ Failed to create experiment (HTTP $HTTP_CODE)"
    exit 1
fi

echo ""
echo "=== TEST 1: Update allowed fields in DRAFT state (should succeed) ==="
RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/$EXPERIMENT_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "description": "Updated in DRAFT",
    "cohorts": ["all_users"],
    "variants": {
      "control": {"display_name": "Control Updated", "variables": [{"key": "feature", "value": "false", "data_type": "BOOL"}]},
      "variant1": {"display_name": "Variant Updated", "variables": [{"key": "feature", "value": "true", "data_type": "BOOL"}]}
    }
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 200 ]; then
    print_result 0 "Update restricted fields in DRAFT state"
else
    print_result 1 "Update restricted fields in DRAFT state (HTTP $HTTP_CODE)"
fi
echo ""

echo "=== SETUP: Transition to LIVE state ==="
RESPONSE=$(curl -s -X PATCH "$BASE_URL/$EXPERIMENT_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{"status": "LIVE"}')
echo "✓ Transitioned to LIVE state"
echo ""

echo "=== TEST 2: Update allowed fields in LIVE state (should succeed) ==="
RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/$EXPERIMENT_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "description": "Updated description in LIVE",
    "hypothesis": "Updated hypothesis in LIVE",
    "exposure": 80,
    "threshold": 5000,
    "end_time": 1900000000
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 200 ]; then
    print_result 0 "Update allowed fields in LIVE state"
else
    print_result 1 "Update allowed fields in LIVE state (HTTP $HTTP_CODE)"
fi
echo ""

echo "=== TEST 3: Reject updating 'cohorts' in LIVE state ==="
RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/$EXPERIMENT_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{"cohorts": ["new_cohort"]}')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject 'cohorts' update in LIVE state"
else
    print_result 1 "Reject 'cohorts' update in LIVE state (Expected 400, got $HTTP_CODE)"
fi
echo ""

echo "=== TEST 4: Reject updating 'variants' in LIVE state ==="
RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/$EXPERIMENT_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "variants": {
      "control": {"display_name": "New Control", "variables": [{"key": "feature", "value": "false", "data_type": "BOOL"}]}
    }
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject 'variants' update in LIVE state"
else
    print_result 1 "Reject 'variants' update in LIVE state (Expected 400, got $HTTP_CODE)"
fi
echo ""

echo "=== TEST 5: Reject updating 'variant_weights' in LIVE state ==="
RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/$EXPERIMENT_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{"variant_weights": {"type": "COHORT", "weights": {"control": 30.0, "variant1": 70.0}}}')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject 'variant_weights' update in LIVE state"
else
    print_result 1 "Reject 'variant_weights' update in LIVE state (Expected 400, got $HTTP_CODE)"
fi
echo ""

echo "=== TEST 6: Reject updating 'distribution_strategy' in LIVE state ==="
RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/$EXPERIMENT_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{"distribution_strategy": "ROUND_ROBIN"}')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject 'distribution_strategy' update in LIVE state"
else
    print_result 1 "Reject 'distribution_strategy' update in LIVE state (Expected 400, got $HTTP_CODE)"
fi
echo ""

echo "=== TEST 7: Reject updating 'rule_attributes' in LIVE state ==="
RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/$EXPERIMENT_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "rule_attributes": [
      {
        "name": "New Rule",
        "conditions": [
          {"operand": "platform", "operandDataType": "STRING", "operator": "=", "value": "ios"}
        ]
      }
    ]
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject 'rule_attributes' update in LIVE state"
else
    print_result 1 "Reject 'rule_attributes' update in LIVE state (Expected 400, got $HTTP_CODE)"
fi
echo ""

echo "=== TEST 8: Reject updating 'start_time' in LIVE state ==="
RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/$EXPERIMENT_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{"start_time": 1750000000}')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject 'start_time' update in LIVE state"
else
    print_result 1 "Reject 'start_time' update in LIVE state (Expected 400, got $HTTP_CODE)"
fi
echo ""

echo "=== TEST 9: Reject multiple restricted fields in LIVE state ==="
RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/$EXPERIMENT_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "cohorts": ["new_cohort"],
    "type": "A_A",
    "distribution_strategy": "ROUND_ROBIN"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Reject multiple restricted fields in LIVE state"
else
    print_result 1 "Reject multiple restricted fields in LIVE state (Expected 400, got $HTTP_CODE)"
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
    echo -e "${GREEN}✓ All LIVE state restriction tests passed!${NC}"
    exit 0
else
    echo -e "${YELLOW}⚠ Some tests failed. Review the output above.${NC}"
    exit 1
fi

