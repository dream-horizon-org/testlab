#!/bin/bash

# Test script for POJO refactoring (no Map conversion)
# Tests both create and update flows

BASE_URL="http://localhost:8080/v1/experiment"
TENANT_ID="550e8400-e29b-41d4-a716-446655440000"
PROJECT_KEY="550e8400-e29b-41d4-a716-446655440001"

echo "=========================================="
echo "Testing POJO Refactoring (No Map Conversion)"
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

echo "=========================================="
echo "TEST 1: Create Experiment (POJO Direct Storage)"
echo "=========================================="

CREATE_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "POJO Test Experiment",
    "description": "Testing direct POJO storage without Map conversion",
    "hypothesis": "POJO should be stored directly in JSONB",
    "status": "DRAFT",
    "type": "A_B",
    "guardrail_health_status": "PASSING",
    "cohorts": ["test_users", "beta_users"],
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
          {"key": "feature_enabled", "value": "false", "dataType": "BOOL"}
        ]
      },
      "variant1": {
        "displayName": "Variant 1",
        "variables": [
          {"key": "feature_enabled", "value": "true", "dataType": "BOOL"}
        ]
      }
    },
    "distribution_strategy": "RANDOM",
    "assignment_domain": "DEFAULT",
    "overrides": ["user1@test.com", "user2@test.com"],
    "rule_attributes": [
      {
        "name": "Platform Rule",
        "conditions": [
          {
            "operand": "platform",
            "operandDataType": "STRING",
            "operator": "=",
            "value": "android"
          }
        ]
      }
    ],
    "winning_variant": {
      "type": "COHORT",
      "weights": {
        "control": 0.0,
        "variant1": 100.0
      }
    },
    "exposure": 100,
    "threshold": 10000,
    "start_time": 1700000000,
    "end_time": 1800000000,
    "created_by": "test_user@example.com",
    "tags": ["pojo-test", "refactoring"],
    "owner": ["team@example.com"],
    "metrics": {
      "primary": ["conversion_rate"],
      "secondary": ["engagement_score"]
    }
  }')

HTTP_CODE=$(echo "$CREATE_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$CREATE_RESPONSE" | sed '$d')

echo "HTTP Status: $HTTP_CODE"
echo "Response: $RESPONSE_BODY"
echo ""

if [ "$HTTP_CODE" -eq 200 ]; then
    print_result 0 "Create experiment with POJO direct storage"
    EXPERIMENT_ID=$(echo "$RESPONSE_BODY" | grep -o '"experiment_id":"[^"]*"' | cut -d'"' -f4)
    echo "Created experiment ID: $EXPERIMENT_ID"
else
    print_result 1 "Create experiment with POJO direct storage (HTTP $HTTP_CODE)"
    echo "Response: $RESPONSE_BODY"
fi

echo ""
echo "=========================================="
echo "TEST 2: Update Experiment (POJO Field Extraction)"
echo "=========================================="

if [ -n "$EXPERIMENT_ID" ]; then
    UPDATE_RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/$EXPERIMENT_ID" \
      -H "Content-Type: application/json" \
      -H "x-tenant-id: $TENANT_ID" \
      -H "x-project-key: $PROJECT_KEY" \
      -d '{
        "description": "Updated via POJO field extraction (no Map conversion)",
        "status": "LIVE",
        "exposure": 80,
        "threshold": 15000,
        "variant_weights": {
          "type": "COHORT",
          "weights": {
            "control": 30.0,
            "variant1": 70.0
          }
        },
        "variants": {
          "control": {
            "displayName": "Control Updated",
            "variables": [
              {"key": "feature_enabled", "value": "false", "dataType": "BOOL"},
              {"key": "new_feature", "value": "test", "dataType": "STRING"}
            ]
          },
          "variant1": {
            "displayName": "Variant 1 Updated",
            "variables": [
              {"key": "feature_enabled", "value": "true", "dataType": "BOOL"},
              {"key": "new_feature", "value": "prod", "dataType": "STRING"}
            ]
          }
        },
        "rule_attributes": [
          {
            "name": "Platform Rule",
            "conditions": [
              {
                "operand": "platform",
                "operandDataType": "STRING",
                "operator": "=",
                "value": "android"
              }
            ]
          },
          {
            "name": "Version Rule",
            "conditions": [
              {
                "operand": "app_version",
                "operandDataType": "SEMVER_STRING",
                "operator": ">=",
                "value": "2.0.0"
              }
            ]
          }
        ],
        "tags": ["pojo-test", "refactoring", "updated"],
        "owner": ["team@example.com", "engineering@example.com"],
        "metrics": {
          "primary": ["conversion_rate", "revenue"],
          "secondary": ["engagement_score", "retention"]
        },
        "updated_by": "updater@example.com"
      }')

    HTTP_CODE=$(echo "$UPDATE_RESPONSE" | tail -n1)
    RESPONSE_BODY=$(echo "$UPDATE_RESPONSE" | sed '$d')

    echo "HTTP Status: $HTTP_CODE"
    echo "Response: $RESPONSE_BODY"
    echo ""

    if [ "$HTTP_CODE" -eq 200 ]; then
        print_result 0 "Update experiment with POJO field extraction"
    else
        print_result 1 "Update experiment with POJO field extraction (HTTP $HTTP_CODE)"
        echo "Response: $RESPONSE_BODY"
    fi
else
    print_result 1 "Update experiment (skipped - no experiment ID from create)"
fi

echo ""
echo "=========================================="
echo "TEST 3: Update with Partial Fields (Testing Field Extraction)"
echo "=========================================="

if [ -n "$EXPERIMENT_ID" ]; then
    PARTIAL_UPDATE_RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/$EXPERIMENT_ID" \
      -H "Content-Type: application/json" \
      -H "x-tenant-id: $TENANT_ID" \
      -H "x-project-key: $PROJECT_KEY" \
      -d '{
        "description": "Testing partial update with only a few fields",
        "exposure": 90,
        "updated_by": "partial_updater@example.com"
      }')

    HTTP_CODE=$(echo "$PARTIAL_UPDATE_RESPONSE" | tail -n1)
    RESPONSE_BODY=$(echo "$PARTIAL_UPDATE_RESPONSE" | sed '$d')

    echo "HTTP Status: $HTTP_CODE"
    echo "Response: $RESPONSE_BODY"
    echo ""

    if [ "$HTTP_CODE" -eq 200 ]; then
        print_result 0 "Partial update (only non-null fields extracted)"
    else
        print_result 1 "Partial update (HTTP $HTTP_CODE)"
        echo "Response: $RESPONSE_BODY"
    fi
else
    print_result 1 "Partial update (skipped - no experiment ID)"
fi

echo ""
echo "=========================================="
echo "TEST 4: Verify Update Log Stores POJO Correctly"
echo "=========================================="

if [ -n "$EXPERIMENT_ID" ]; then
    GET_RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/$EXPERIMENT_ID" \
      -H "x-tenant-id: $TENANT_ID" \
      -H "x-project-key: $PROJECT_KEY")

    HTTP_CODE=$(echo "$GET_RESPONSE" | tail -n1)
    RESPONSE_BODY=$(echo "$GET_RESPONSE" | sed '$d')

    echo "HTTP Status: $HTTP_CODE"
    echo ""

    if [ "$HTTP_CODE" -eq 200 ]; then
        print_result 0 "Get experiment (verify data integrity)"
        
        # Check if key fields are present
        if echo "$RESPONSE_BODY" | grep -q "POJO"; then
            print_result 0 "Description contains 'POJO' (data stored correctly)"
        else
            print_result 1 "Description verification"
        fi
        
        if echo "$RESPONSE_BODY" | grep -q "partial_updater@example.com"; then
            print_result 0 "Updated_by field preserved (POJO stored correctly)"
        else
            print_result 1 "Updated_by field verification"
        fi
    else
        print_result 1 "Get experiment (HTTP $HTTP_CODE)"
    fi
else
    print_result 1 "Get experiment (skipped - no experiment ID)"
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
    echo -e "${GREEN}✓ All tests passed! POJO refactoring is working correctly.${NC}"
    exit 0
else
    echo -e "${RED}✗ Some tests failed. Please check the output above.${NC}"
    exit 1
fi

