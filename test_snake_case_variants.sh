#!/bin/bash

# Test script for snake_case JSON properties in variants
# Tests display_name and data_type fields

BASE_URL="http://localhost:8080/v1/experiment"
TENANT_ID="550e8400-e29b-41d4-a716-446655440000"
PROJECT_KEY="550e8400-e29b-41d4-a716-446655440001"

echo "=========================================="
echo "Testing Snake Case JSON Properties"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "=== TEST 1: Create experiment with snake_case properties (display_name, data_type) ==="
CREATE_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Snake Case Test",
    "description": "Testing snake_case JSON properties",
    "hypothesis": "Should accept display_name and data_type",
    "status": "DRAFT",
    "type": "A_B",
    "created_by": "test@example.com",
    "cohorts": ["test_users"],
    "variant_weights": {
      "type": "COHORT",
      "weights": {
        "control": 33.33,
        "variant1": 33.33,
        "variant2": 33.34
      }
    },
    "variants": {
      "control": {
        "display_name": "Control Group",
        "variables": [
          {"key": "button_color", "value": "blue", "data_type": "STRING"},
          {"key": "feature_enabled", "value": "false", "data_type": "BOOL"},
          {"key": "max_items", "value": "10", "data_type": "NUMBER"}
        ]
      },
      "variant1": {
        "display_name": "Test Variant 1",
        "variables": [
          {"key": "button_color", "value": "green", "data_type": "STRING"},
          {"key": "feature_enabled", "value": "true", "data_type": "BOOL"},
          {"key": "max_items", "value": "20", "data_type": "NUMBER"}
        ]
      },
      "variant2": {
        "display_name": "Test Variant 2",
        "variables": [
          {"key": "button_color", "value": "red", "data_type": "STRING"},
          {"key": "feature_enabled", "value": "true", "data_type": "BOOL"},
          {"key": "max_items", "value": "15", "data_type": "NUMBER"}
        ]
      }
    },
    "distribution_strategy": "RANDOM",
    "assignment_domain": "DEFAULT",
    "exposure": 100,
    "threshold": 1000,
    "start_time": 1700000000,
    "end_time": 1800000000,
    "tags": ["snake-case-test"],
    "owner": ["test@example.com"],
    "metrics": {
      "primary": ["conversion"],
      "secondary": ["engagement"]
    }
  }')

HTTP_CODE=$(echo "$CREATE_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$CREATE_RESPONSE" | sed '$d')

echo "HTTP Status: $HTTP_CODE"
echo "Response: $RESPONSE_BODY"
echo ""

if [ "$HTTP_CODE" -eq 200 ]; then
    echo -e "${GREEN}✓ PASS${NC}: Create with snake_case properties (display_name, data_type)"
    EXPERIMENT_ID=$(echo "$RESPONSE_BODY" | grep -o '"experiment_id":"[^"]*"' | cut -d'"' -f4)
    echo "Created experiment ID: $EXPERIMENT_ID"
else
    echo -e "${RED}✗ FAIL${NC}: Create with snake_case properties (HTTP $HTTP_CODE)"
    echo "Response: $RESPONSE_BODY"
    exit 1
fi

echo ""
echo "=== TEST 2: Update experiment with snake_case properties ==="

if [ -n "$EXPERIMENT_ID" ]; then
    UPDATE_RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/$EXPERIMENT_ID" \
      -H "Content-Type: application/json" \
      -H "x-tenant-id: $TENANT_ID" \
      -H "x-project-key: $PROJECT_KEY" \
      -d '{
        "description": "Updated with snake_case properties",
        "variants": {
          "control": {
            "display_name": "Updated Control Group",
            "variables": [
              {"key": "button_color", "value": "purple", "data_type": "STRING"},
              {"key": "feature_enabled", "value": "false", "data_type": "BOOL"},
              {"key": "max_items", "value": "12", "data_type": "NUMBER"},
              {"key": "new_feature", "value": "test", "data_type": "STRING"}
            ]
          },
          "variant1": {
            "display_name": "Updated Test Variant 1",
            "variables": [
              {"key": "button_color", "value": "yellow", "data_type": "STRING"},
              {"key": "feature_enabled", "value": "true", "data_type": "BOOL"},
              {"key": "max_items", "value": "25", "data_type": "NUMBER"}
            ]
          },
          "variant2": {
            "display_name": "Updated Test Variant 2",
            "variables": [
              {"key": "button_color", "value": "orange", "data_type": "STRING"},
              {"key": "feature_enabled", "value": "true", "data_type": "BOOL"},
              {"key": "max_items", "value": "18", "data_type": "NUMBER"}
            ]
          }
        }
      }')

    HTTP_CODE=$(echo "$UPDATE_RESPONSE" | tail -n1)
    RESPONSE_BODY=$(echo "$UPDATE_RESPONSE" | sed '$d')

    echo "HTTP Status: $HTTP_CODE"
    echo "Response: $RESPONSE_BODY"
    echo ""

    if [ "$HTTP_CODE" -eq 200 ]; then
        echo -e "${GREEN}✓ PASS${NC}: Update with snake_case properties"
    else
        echo -e "${RED}✗ FAIL${NC}: Update with snake_case properties (HTTP $HTTP_CODE)"
        echo "Response: $RESPONSE_BODY"
        exit 1
    fi
else
    echo -e "${RED}✗ FAIL${NC}: Update test skipped - no experiment ID"
    exit 1
fi

echo ""
echo "=== TEST 3: Create with camelCase (should also work for backward compatibility) ==="

CAMEL_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "CamelCase Test",
    "description": "Testing camelCase JSON properties",
    "hypothesis": "Should accept displayName and dataType",
    "status": "DRAFT",
    "type": "A_B",
    "created_by": "test@example.com",
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
        "displayName": "Control Group CamelCase",
        "variables": [
          {"key": "test", "value": "true", "dataType": "BOOL"}
        ]
      },
      "variant1": {
        "displayName": "Variant CamelCase",
        "variables": [
          {"key": "test", "value": "false", "dataType": "BOOL"}
        ]
      }
    },
    "distribution_strategy": "RANDOM",
    "assignment_domain": "DEFAULT",
    "exposure": 100,
    "threshold": 1000,
    "start_time": 1700000000,
    "end_time": 1800000000,
    "tags": ["camel-case-test"],
    "owner": ["test@example.com"],
    "metrics": {
      "primary": ["conversion"],
      "secondary": ["engagement"]
    }
  }')

HTTP_CODE=$(echo "$CAMEL_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$CAMEL_RESPONSE" | sed '$d')

echo "HTTP Status: $HTTP_CODE"
echo "Response: $RESPONSE_BODY"
echo ""

if [ "$HTTP_CODE" -eq 200 ]; then
    echo -e "${GREEN}✓ PASS${NC}: Create with camelCase properties (backward compatibility)"
else
    echo -e "${RED}✗ FAIL${NC}: Create with camelCase properties (HTTP $HTTP_CODE)"
    echo "Response: $RESPONSE_BODY"
fi

echo ""
echo "=========================================="
echo "All tests completed successfully!"
echo "=========================================="

