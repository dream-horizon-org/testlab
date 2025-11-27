#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE}  STEP 1: CREATE EXPERIMENT${NC}"
echo -e "${BLUE}=========================================${NC}"
echo ""

# Create experiment
CREATE_RESPONSE=$(curl -s --location 'http://localhost:8080/v1/experiment' \
--header 'Content-Type: application/json' \
--header 'x-tenant-id: 550e8400-e29b-41d4-a716-446655440000' \
--header 'x-project-key: 550e8400-e29b-41d4-a716-446655440001' \
--data-raw '{
  "name": "Update Flow Test Experiment",
  "description": "Testing all possible attributes in create flow",
  "hypothesis": "All fields should be saved correctly with proper validation",
  "status": "DRAFT",
  "type": "A_B",
  "guardrail_health_status": "PASSING",
  "cohorts": [
    "premium_users",
    "mobile_users",
    "web_users"
  ],
  "variant_weights": {
    "type": "COHORT",
    "weights": {
      "control": 40,
      "variant1": 35,
      "variant2": 25
    }
  },
  "variants": {
    "control": {
      "displayName": "Control Group",
      "variables": [
        {
          "key": "button_color",
          "value": "blue",
          "data_type": "STRING"
        },
        {
          "key": "feature_enabled",
          "value": "false",
          "data_type": "BOOL"
        },
        {
          "key": "max_items",
          "value": "10",
          "data_type": "NUMBER"
        }
      ]
    },
    "variant1": {
      "displayName": "Test Variant 1",
      "variables": [
        {
          "key": "button_color",
          "value": "green",
          "data_type": "STRING"
        },
        {
          "key": "feature_enabled",
          "value": "true",
          "data_type": "BOOL"
        },
        {
          "key": "max_items",
          "value": "20",
          "data_type": "NUMBER"
        }
      ]
    },
    "variant2": {
      "displayName": "Test Variant 2",
      "variables": [
        {
          "key": "button_color",
          "value": "red",
          "data_type": "STRING"
        },
        {
          "key": "feature_enabled",
          "value": "true",
          "data_type": "BOOL"
        },
        {
          "key": "max_items",
          "value": "15",
          "data_type": "NUMBER"
        }
      ]
    }
  },
  "distribution_strategy": "RANDOM",
  "assignment_domain": "COHORT",
  "exposure": 100,
  "threshold": 50000,
  "start_time": 1700000000,
  "end_time": 1900000000,
  "created_by": "product_manager@example.com",
  "tags": [
    "feature-test",
    "all-attributes",
    "production-ready"
  ],
  "owner": [
    "product_team@example.com",
    "engineering_team@example.com",
    "data_team@example.com"
  ],
  "metrics": {
    "primary": [
      "conversion_rate"
    ],
    "secondary": [
      "engagement_score",
      "asasa"
    ]
  }
}')

EXP_ID=$(echo "$CREATE_RESPONSE" | jq -r '.data.experiment_id')

if [ "$EXP_ID" = "null" ] || [ -z "$EXP_ID" ]; then
  echo -e "${RED}✗ Failed to create experiment${NC}"
  echo "$CREATE_RESPONSE" | jq .
  exit 1
fi

echo -e "${GREEN}✓ Created experiment: $EXP_ID${NC}"
echo ""

# Verify created data
echo -e "${BLUE}Fetching created experiment data...${NC}"
GET_RESPONSE=$(curl -s -X GET "http://localhost:8080/v1/experiments/$EXP_ID" \
  -H "x-tenant-id: 550e8400-e29b-41d4-a716-446655440000" \
  -H "x-project-key: 550e8400-e29b-41d4-a716-446655440001")

echo -e "${YELLOW}Original Data:${NC}"
echo "  Description: $(echo "$GET_RESPONSE" | jq -r '.data.description')"
echo "  Status: $(echo "$GET_RESPONSE" | jq -r '.data.status')"
echo "  Exposure: $(echo "$GET_RESPONSE" | jq -r '.data.exposure')"
echo "  Control button_color: $(echo "$GET_RESPONSE" | jq -r '.data.variants.control.variables[0].value')"
echo "  Variant1 button_color: $(echo "$GET_RESPONSE" | jq -r '.data.variants.variant1.variables[0].value')"
echo "  Variant2 button_color: $(echo "$GET_RESPONSE" | jq -r '.data.variants.variant2.variables[0].value')"
echo ""

echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE}  DRAFT STATE UPDATE TESTS${NC}"
echo -e "${BLUE}=========================================${NC}"
echo ""

# TEST 1: Update description in DRAFT
echo -e "${YELLOW}TEST 1: Update description in DRAFT${NC}"
UPDATE_RESULT=$(curl -s -X PATCH "http://localhost:8080/v1/experiment/$EXP_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: 550e8400-e29b-41d4-a716-446655440000" \
  -H "x-project-key: 550e8400-e29b-41d4-a716-446655440001" \
  -d '{
    "description": "Updated description in DRAFT",
    "updated_by": "test@example.com"
  }')

if echo "$UPDATE_RESULT" | jq -e '.data.status == true' > /dev/null 2>&1; then
  echo -e "${GREEN}✓ PASS${NC}"
  GET_RESPONSE=$(curl -s -X GET "http://localhost:8080/v1/experiments/$EXP_ID" -H "x-tenant-id: 550e8400-e29b-41d4-a716-446655440000" -H "x-project-key: 550e8400-e29b-41d4-a716-446655440001")
  echo "  DB Value: $(echo "$GET_RESPONSE" | jq -r '.data.description')"
else
  echo -e "${RED}✗ FAIL${NC} - $(echo "$UPDATE_RESULT" | jq -r '.error.message')"
fi
echo ""

# TEST 2: Update variant VALUES only (allowed in DRAFT)
echo -e "${YELLOW}TEST 2: Update variant VALUES only in DRAFT${NC}"
UPDATE_RESULT=$(curl -s -X PATCH "http://localhost:8080/v1/experiment/$EXP_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: 550e8400-e29b-41d4-a716-446655440000" \
  -H "x-project-key: 550e8400-e29b-41d4-a716-446655440001" \
  -d '{
    "variants": {
      "control": {
        "displayName": "Control Group",
        "variables": [
          {"key": "button_color", "value": "purple", "data_type": "STRING"},
          {"key": "feature_enabled", "value": "false", "data_type": "BOOL"},
          {"key": "max_items", "value": "10", "data_type": "NUMBER"}
        ]
      },
      "variant1": {
        "displayName": "Test Variant 1",
        "variables": [
          {"key": "button_color", "value": "orange", "data_type": "STRING"},
          {"key": "feature_enabled", "value": "true", "data_type": "BOOL"},
          {"key": "max_items", "value": "20", "data_type": "NUMBER"}
        ]
      },
      "variant2": {
        "displayName": "Test Variant 2",
        "variables": [
          {"key": "button_color", "value": "yellow", "data_type": "STRING"},
          {"key": "feature_enabled", "value": "true", "data_type": "BOOL"},
          {"key": "max_items", "value": "15", "data_type": "NUMBER"}
        ]
      }
    },
    "updated_by": "test@example.com"
  }')

if echo "$UPDATE_RESULT" | jq -e '.data.status == true' > /dev/null 2>&1; then
  echo -e "${GREEN}✓ PASS${NC}"
  GET_RESPONSE=$(curl -s -X GET "http://localhost:8080/v1/experiments/$EXP_ID" -H "x-tenant-id: 550e8400-e29b-41d4-a716-446655440000" -H "x-project-key: 550e8400-e29b-41d4-a716-446655440001")
  echo "  Control button_color: $(echo "$GET_RESPONSE" | jq -r '.data.variants.control.variables[0].value')"
  echo "  Variant1 button_color: $(echo "$GET_RESPONSE" | jq -r '.data.variants.variant1.variables[0].value')"
  echo "  Variant2 button_color: $(echo "$GET_RESPONSE" | jq -r '.data.variants.variant2.variables[0].value')"
else
  echo -e "${RED}✗ FAIL${NC} - $(echo "$UPDATE_RESULT" | jq -r '.error.message')"
fi
echo ""

# TEST 3: Try to change variable KEY (should be blocked)
echo -e "${YELLOW}TEST 3: Try to change variable KEY (should block)${NC}"
UPDATE_RESULT=$(curl -s -X PATCH "http://localhost:8080/v1/experiment/$EXP_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: 550e8400-e29b-41d4-a716-446655440000" \
  -H "x-project-key: 550e8400-e29b-41d4-a716-446655440001" \
  -d '{
    "variants": {
      "control": {
        "displayName": "Control Group",
        "variables": [
          {"key": "btn_color", "value": "purple", "data_type": "STRING"},
          {"key": "feature_enabled", "value": "false", "data_type": "BOOL"},
          {"key": "max_items", "value": "10", "data_type": "NUMBER"}
        ]
      },
      "variant1": {
        "displayName": "Test Variant 1",
        "variables": [
          {"key": "button_color", "value": "orange", "data_type": "STRING"},
          {"key": "feature_enabled", "value": "true", "data_type": "BOOL"},
          {"key": "max_items", "value": "20", "data_type": "NUMBER"}
        ]
      },
      "variant2": {
        "displayName": "Test Variant 2",
        "variables": [
          {"key": "button_color", "value": "yellow", "data_type": "STRING"},
          {"key": "feature_enabled", "value": "true", "data_type": "BOOL"},
          {"key": "max_items", "value": "15", "data_type": "NUMBER"}
        ]
      }
    },
    "updated_by": "test@example.com"
  }')

if echo "$UPDATE_RESULT" | jq -e '.error.code == "INVALID_REQUEST"' > /dev/null 2>&1 && echo "$UPDATE_RESULT" | jq -r '.error.message' | grep -q "Variable keys cannot be changed"; then
  echo -e "${GREEN}✓ PASS - Blocked correctly${NC}"
  echo "  Error: $(echo "$UPDATE_RESULT" | jq -r '.error.message' | head -c 100)..."
else
  echo -e "${RED}✗ FAIL - Should have blocked${NC}"
  echo "  Response: $(echo "$UPDATE_RESULT" | jq -r '.error.message')"
fi
echo ""

# TEST 4: Try to change variable DATA_TYPE (should be blocked)
echo -e "${YELLOW}TEST 4: Try to change variable DATA_TYPE (should block)${NC}"
UPDATE_RESULT=$(curl -s -X PATCH "http://localhost:8080/v1/experiment/$EXP_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: 550e8400-e29b-41d4-a716-446655440000" \
  -H "x-project-key: 550e8400-e29b-41d4-a716-446655440001" \
  -d '{
    "variants": {
      "control": {
        "displayName": "Control Group",
        "variables": [
          {"key": "button_color", "value": "purple", "data_type": "NUMBER"},
          {"key": "feature_enabled", "value": "false", "data_type": "BOOL"},
          {"key": "max_items", "value": "10", "data_type": "NUMBER"}
        ]
      },
      "variant1": {
        "displayName": "Test Variant 1",
        "variables": [
          {"key": "button_color", "value": "orange", "data_type": "STRING"},
          {"key": "feature_enabled", "value": "true", "data_type": "BOOL"},
          {"key": "max_items", "value": "20", "data_type": "NUMBER"}
        ]
      },
      "variant2": {
        "displayName": "Test Variant 2",
        "variables": [
          {"key": "button_color", "value": "yellow", "data_type": "STRING"},
          {"key": "feature_enabled", "value": "true", "data_type": "BOOL"},
          {"key": "max_items", "value": "15", "data_type": "NUMBER"}
        ]
      }
    },
    "updated_by": "test@example.com"
  }')

if echo "$UPDATE_RESULT" | jq -e '.error.code == "INVALID_REQUEST"' > /dev/null 2>&1 && echo "$UPDATE_RESULT" | jq -r '.error.message' | grep -q "data_type cannot be changed"; then
  echo -e "${GREEN}✓ PASS - Blocked correctly${NC}"
  echo "  Error: $(echo "$UPDATE_RESULT" | jq -r '.error.message' | head -c 100)..."
else
  echo -e "${RED}✗ FAIL - Should have blocked${NC}"
  echo "  Response: $(echo "$UPDATE_RESULT" | jq -r '.error.message')"
fi
echo ""

# TEST 5: Try to add new variable (should be blocked - changes structure)
echo -e "${YELLOW}TEST 5: Try to add new variable (should block)${NC}"
UPDATE_RESULT=$(curl -s -X PATCH "http://localhost:8080/v1/experiment/$EXP_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: 550e8400-e29b-41d4-a716-446655440000" \
  -H "x-project-key: 550e8400-e29b-41d4-a716-446655440001" \
  -d '{
    "variants": {
      "control": {
        "displayName": "Control Group",
        "variables": [
          {"key": "button_color", "value": "purple", "data_type": "STRING"},
          {"key": "feature_enabled", "value": "false", "data_type": "BOOL"},
          {"key": "max_items", "value": "10", "data_type": "NUMBER"},
          {"key": "new_field", "value": "test", "data_type": "STRING"}
        ]
      },
      "variant1": {
        "displayName": "Test Variant 1",
        "variables": [
          {"key": "button_color", "value": "orange", "data_type": "STRING"},
          {"key": "feature_enabled", "value": "true", "data_type": "BOOL"},
          {"key": "max_items", "value": "20", "data_type": "NUMBER"}
        ]
      },
      "variant2": {
        "displayName": "Test Variant 2",
        "variables": [
          {"key": "button_color", "value": "yellow", "data_type": "STRING"},
          {"key": "feature_enabled", "value": "true", "data_type": "BOOL"},
          {"key": "max_items", "value": "15", "data_type": "NUMBER"}
        ]
      }
    },
    "updated_by": "test@example.com"
  }')

if echo "$UPDATE_RESULT" | jq -e '.error.code == "INVALID_REQUEST"' > /dev/null 2>&1 && echo "$UPDATE_RESULT" | jq -r '.error.message' | grep -q "Variable count mismatch"; then
  echo -e "${GREEN}✓ PASS - Blocked correctly${NC}"
  echo "  Error: $(echo "$UPDATE_RESULT" | jq -r '.error.message' | head -c 100)..."
else
  echo -e "${RED}✗ FAIL - Should have blocked${NC}"
  echo "  Response: $(echo "$UPDATE_RESULT" | jq -r '.error.message')"
fi
echo ""

# TEST 6: Update tags in DRAFT
echo -e "${YELLOW}TEST 6: Update tags in DRAFT${NC}"
UPDATE_RESULT=$(curl -s -X PATCH "http://localhost:8080/v1/experiment/$EXP_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: 550e8400-e29b-41d4-a716-446655440000" \
  -H "x-project-key: 550e8400-e29b-41d4-a716-446655440001" \
  -d '{
    "tags": ["updated", "draft-test"],
    "updated_by": "test@example.com"
  }')

if echo "$UPDATE_RESULT" | jq -e '.data.status == true' > /dev/null 2>&1; then
  echo -e "${GREEN}✓ PASS${NC}"
else
  echo -e "${RED}✗ FAIL${NC} - $(echo "$UPDATE_RESULT" | jq -r '.error.message')"
fi
echo ""

echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE}  TRANSITION TO LIVE STATE${NC}"
echo -e "${BLUE}=========================================${NC}"
echo ""

# TEST 7: Transition to LIVE
echo -e "${YELLOW}TEST 7: Transition DRAFT → LIVE${NC}"
UPDATE_RESULT=$(curl -s -X PATCH "http://localhost:8080/v1/experiment/$EXP_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: 550e8400-e29b-41d4-a716-446655440000" \
  -H "x-project-key: 550e8400-e29b-41d4-a716-446655440001" \
  -d '{
    "status": "LIVE",
    "updated_by": "test@example.com"
  }')

if echo "$UPDATE_RESULT" | jq -e '.data.status == true' > /dev/null 2>&1; then
  echo -e "${GREEN}✓ PASS${NC}"
  GET_RESPONSE=$(curl -s -X GET "http://localhost:8080/v1/experiments/$EXP_ID" -H "x-tenant-id: 550e8400-e29b-41d4-a716-446655440000" -H "x-project-key: 550e8400-e29b-41d4-a716-446655440001")
  echo "  DB Status: $(echo "$GET_RESPONSE" | jq -r '.data.status')"
else
  echo -e "${RED}✗ FAIL${NC} - $(echo "$UPDATE_RESULT" | jq -r '.error.message')"
fi
echo ""

echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE}  LIVE STATE UPDATE TESTS${NC}"
echo -e "${BLUE}=========================================${NC}"
echo ""

# TEST 8: Update allowed fields in LIVE
echo -e "${YELLOW}TEST 8: Update allowed fields in LIVE (description, exposure)${NC}"
UPDATE_RESULT=$(curl -s -X PATCH "http://localhost:8080/v1/experiment/$EXP_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: 550e8400-e29b-41d4-a716-446655440000" \
  -H "x-project-key: 550e8400-e29b-41d4-a716-446655440001" \
  -d '{
    "description": "Updated in LIVE state",
    "exposure": 90,
    "updated_by": "test@example.com"
  }')

if echo "$UPDATE_RESULT" | jq -e '.data.status == true' > /dev/null 2>&1; then
  echo -e "${GREEN}✓ PASS${NC}"
  GET_RESPONSE=$(curl -s -X GET "http://localhost:8080/v1/experiments/$EXP_ID" -H "x-tenant-id: 550e8400-e29b-41d4-a716-446655440000" -H "x-project-key: 550e8400-e29b-41d4-a716-446655440001")
  echo "  DB Description: $(echo "$GET_RESPONSE" | jq -r '.data.description')"
  echo "  DB Exposure: $(echo "$GET_RESPONSE" | jq -r '.data.exposure')"
else
  echo -e "${RED}✗ FAIL${NC} - $(echo "$UPDATE_RESULT" | jq -r '.error.message')"
fi
echo ""

# TEST 9: Try to update variants in LIVE (should be blocked)
echo -e "${YELLOW}TEST 9: Try to update variants in LIVE (should block)${NC}"
UPDATE_RESULT=$(curl -s -X PATCH "http://localhost:8080/v1/experiment/$EXP_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: 550e8400-e29b-41d4-a716-446655440000" \
  -H "x-project-key: 550e8400-e29b-41d4-a716-446655440001" \
  -d '{
    "variants": {
      "control": {
        "displayName": "Control Group",
        "variables": [
          {"key": "button_color", "value": "black", "data_type": "STRING"},
          {"key": "feature_enabled", "value": "false", "data_type": "BOOL"},
          {"key": "max_items", "value": "10", "data_type": "NUMBER"}
        ]
      },
      "variant1": {
        "displayName": "Test Variant 1",
        "variables": [
          {"key": "button_color", "value": "white", "data_type": "STRING"},
          {"key": "feature_enabled", "value": "true", "data_type": "BOOL"},
          {"key": "max_items", "value": "20", "data_type": "NUMBER"}
        ]
      },
      "variant2": {
        "displayName": "Test Variant 2",
        "variables": [
          {"key": "button_color", "value": "gray", "data_type": "STRING"},
          {"key": "feature_enabled", "value": "true", "data_type": "BOOL"},
          {"key": "max_items", "value": "15", "data_type": "NUMBER"}
        ]
      }
    },
    "updated_by": "test@example.com"
  }')

if echo "$UPDATE_RESULT" | jq -e '.error.code == "INVALID_REQUEST"' > /dev/null 2>&1; then
  echo -e "${GREEN}✓ PASS - Blocked correctly${NC}"
  echo "  Error: $(echo "$UPDATE_RESULT" | jq -r '.error.message' | head -c 100)..."
  
  # Verify values didn't change in DB
  GET_RESPONSE=$(curl -s -X GET "http://localhost:8080/v1/experiments/$EXP_ID" -H "x-tenant-id: 550e8400-e29b-41d4-a716-446655440000" -H "x-project-key: 550e8400-e29b-41d4-a716-446655440001")
  CONTROL_COLOR=$(echo "$GET_RESPONSE" | jq -r '.data.variants.control.variables[0].value')
  echo "  DB Control button_color (unchanged): $CONTROL_COLOR"
else
  echo -e "${RED}✗ FAIL - Should have blocked${NC}"
  echo "  Response: $(echo "$UPDATE_RESULT" | jq -r '.error.message')"
fi
echo ""

# TEST 10: Try to update cohorts in LIVE (should be blocked)
echo -e "${YELLOW}TEST 10: Try to update cohorts in LIVE (should block)${NC}"
UPDATE_RESULT=$(curl -s -X PATCH "http://localhost:8080/v1/experiment/$EXP_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: 550e8400-e29b-41d4-a716-446655440000" \
  -H "x-project-key: 550e8400-e29b-41d4-a716-446655440001" \
  -d '{
    "cohorts": ["new_cohort"],
    "updated_by": "test@example.com"
  }')

if echo "$UPDATE_RESULT" | jq -e '.error.code == "INVALID_REQUEST"' > /dev/null 2>&1; then
  echo -e "${GREEN}✓ PASS - Blocked correctly${NC}"
  echo "  Error: $(echo "$UPDATE_RESULT" | jq -r '.error.message' | head -c 100)..."
else
  echo -e "${RED}✗ FAIL - Should have blocked${NC}"
fi
echo ""

# TEST 11: Try to update variant_weights in LIVE (should be blocked)
echo -e "${YELLOW}TEST 11: Try to update variant_weights in LIVE (should block)${NC}"
UPDATE_RESULT=$(curl -s -X PATCH "http://localhost:8080/v1/experiment/$EXP_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: 550e8400-e29b-41d4-a716-446655440000" \
  -H "x-project-key: 550e8400-e29b-41d4-a716-446655440001" \
  -d '{
    "variant_weights": {
      "type": "COHORT",
      "weights": {"control": 50, "variant1": 30, "variant2": 20}
    },
    "updated_by": "test@example.com"
  }')

if echo "$UPDATE_RESULT" | jq -e '.error.code == "INVALID_REQUEST"' > /dev/null 2>&1; then
  echo -e "${GREEN}✓ PASS - Blocked correctly${NC}"
  echo "  Error: $(echo "$UPDATE_RESULT" | jq -r '.error.message' | head -c 100)..."
else
  echo -e "${RED}✗ FAIL - Should have blocked${NC}"
fi
echo ""

# TEST 12: Transition to PAUSED
echo -e "${YELLOW}TEST 12: Transition LIVE → PAUSED${NC}"
UPDATE_RESULT=$(curl -s -X PATCH "http://localhost:8080/v1/experiment/$EXP_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: 550e8400-e29b-41d4-a716-446655440000" \
  -H "x-project-key: 550e8400-e29b-41d4-a716-446655440001" \
  -d '{
    "status": "PAUSED",
    "updated_by": "test@example.com"
  }')

if echo "$UPDATE_RESULT" | jq -e '.data.status == true' > /dev/null 2>&1; then
  echo -e "${GREEN}✓ PASS${NC}"
  GET_RESPONSE=$(curl -s -X GET "http://localhost:8080/v1/experiments/$EXP_ID" -H "x-tenant-id: 550e8400-e29b-41d4-a716-446655440000" -H "x-project-key: 550e8400-e29b-41d4-a716-446655440001")
  echo "  DB Status: $(echo "$GET_RESPONSE" | jq -r '.data.status')"
else
  echo -e "${RED}✗ FAIL${NC} - $(echo "$UPDATE_RESULT" | jq -r '.error.message')"
fi
echo ""

echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE}  PAUSED STATE UPDATE TESTS${NC}"
echo -e "${BLUE}=========================================${NC}"
echo ""

# TEST 13: Try to update description in PAUSED (should be blocked)
echo -e "${YELLOW}TEST 13: Try to update description in PAUSED (should block)${NC}"
UPDATE_RESULT=$(curl -s -X PATCH "http://localhost:8080/v1/experiment/$EXP_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: 550e8400-e29b-41d4-a716-446655440000" \
  -H "x-project-key: 550e8400-e29b-41d4-a716-446655440001" \
  -d '{
    "description": "Should not update in PAUSED",
    "updated_by": "test@example.com"
  }')

if echo "$UPDATE_RESULT" | jq -e '.error.code == "INVALID_REQUEST"' > /dev/null 2>&1 && echo "$UPDATE_RESULT" | jq -r '.error.message' | grep -q "PAUSED"; then
  echo -e "${GREEN}✓ PASS - Blocked correctly${NC}"
  echo "  Error: $(echo "$UPDATE_RESULT" | jq -r '.error.message' | head -c 100)..."
else
  echo -e "${RED}✗ FAIL - Should have blocked${NC}"
  echo "  Response: $(echo "$UPDATE_RESULT" | jq -r '.error.message')"
fi
echo ""

echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE}  FINAL VERIFICATION${NC}"
echo -e "${BLUE}=========================================${NC}"
echo ""

GET_RESPONSE=$(curl -s -X GET "http://localhost:8080/v1/experiments/$EXP_ID" \
  -H "x-tenant-id: 550e8400-e29b-41d4-a716-446655440000" \
  -H "x-project-key: 550e8400-e29b-41d4-a716-446655440001")

echo -e "${YELLOW}Final Experiment State:${NC}"
echo "  ID: $EXP_ID"
echo "  Status: $(echo "$GET_RESPONSE" | jq -r '.data.status')"
echo "  Description: $(echo "$GET_RESPONSE" | jq -r '.data.description')"
echo "  Exposure: $(echo "$GET_RESPONSE" | jq -r '.data.exposure')"
echo "  Control button_color: $(echo "$GET_RESPONSE" | jq -r '.data.variants.control.variables[0].value')"
echo "  Variant1 button_color: $(echo "$GET_RESPONSE" | jq -r '.data.variants.variant1.variables[0].value')"
echo "  Variant2 button_color: $(echo "$GET_RESPONSE" | jq -r '.data.variants.variant2.variables[0].value')"
echo ""

echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}  ✓ ALL UPDATE FLOW TESTS COMPLETE${NC}"
echo -e "${GREEN}=========================================${NC}"

