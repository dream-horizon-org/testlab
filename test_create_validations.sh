#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080/v1/experiment"
TENANT_ID="550e8400-e29b-41d4-a716-446655440000"
PROJECT_KEY="550e8400-e29b-41d4-a716-446655440001"

echo -e "${BLUE}╔══════════════════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                   CREATE VALIDATION FLOW TESTS                               ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"
echo ""

# TEST 1: Valid Create Request
echo -e "${YELLOW}TEST 1: Valid Create Request${NC}"
echo "Expected: ✓ PASS"
echo ""

RESULT=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Valid Test Experiment",
    "description": "Testing valid create flow",
    "hypothesis": "All validations should pass",
    "status": "DRAFT",
    "type": "A_B",
    "guardrail_health_status": "PASSING",
    "cohorts": ["premium_users"],
    "variant_weights": {
      "type": "COHORT",
      "weights": {"control": 50, "variant1": 50}
    },
    "variants": {
      "control": {
        "displayName": "Control",
        "variables": [{"key": "color", "value": "blue", "data_type": "STRING"}]
      },
      "variant1": {
        "displayName": "Variant",
        "variables": [{"key": "color", "value": "green", "data_type": "STRING"}]
      }
    },
    "distribution_strategy": "RANDOM",
    "assignment_domain": "COHORT",
    "exposure": 100,
    "threshold": 50000,
    "start_time": 1700000000,
    "end_time": 1900000000,
    "created_by": "test@example.com",
    "tags": ["test"],
    "owner": ["test@example.com"],
    "metrics": {"primary": ["conversion"], "secondary": ["engagement"]}
  }')

if echo "$RESULT" | jq -e '.data.status == true' > /dev/null 2>&1; then
  echo -e "${GREEN}✓ PASS${NC}"
  echo "Response: $(echo "$RESULT" | jq -c .)"
else
  echo -e "${RED}✗ FAIL${NC}"
  echo "Response: $(echo "$RESULT" | jq .)"
fi
echo ""

# TEST 2: Missing Required Field (name)
echo -e "${YELLOW}TEST 2: Missing Required Field - name${NC}"
echo "Expected: ✗ FAIL - Should return validation error"
echo ""

RESULT=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "description": "Missing name field",
    "status": "DRAFT",
    "type": "A_B",
    "cohorts": ["premium_users"],
    "variant_weights": {"type": "COHORT", "weights": {"control": 50, "variant1": 50}},
    "variants": {
      "control": {"displayName": "Control", "variables": [{"key": "color", "value": "blue", "data_type": "STRING"}]},
      "variant1": {"displayName": "Variant", "variables": [{"key": "color", "value": "green", "data_type": "STRING"}]}
    },
    "distribution_strategy": "RANDOM",
    "assignment_domain": "COHORT",
    "exposure": 100,
    "threshold": 50000,
    "start_time": 1700000000,
    "end_time": 1900000000,
    "created_by": "test@example.com"
  }')

if echo "$RESULT" | jq -e '.error.code == "INVALID_REQUEST"' > /dev/null 2>&1 && echo "$RESULT" | jq -r '.error.message' | grep -q "name"; then
  echo -e "${GREEN}✓ PASS - Correctly blocked${NC}"
  echo "Error: $(echo "$RESULT" | jq -r '.error.message' | head -c 80)..."
else
  echo -e "${RED}✗ FAIL - Should have blocked${NC}"
  echo "Response: $(echo "$RESULT" | jq .)"
fi
echo ""

# TEST 3: Invalid Status (not DRAFT or LIVE)
echo -e "${YELLOW}TEST 3: Invalid Status - PAUSED not allowed on create${NC}"
echo "Expected: ✗ FAIL - Should return validation error"
echo ""

RESULT=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Invalid Status Test",
    "status": "PAUSED",
    "type": "A_B",
    "cohorts": ["premium_users"],
    "variant_weights": {"type": "COHORT", "weights": {"control": 50, "variant1": 50}},
    "variants": {
      "control": {"displayName": "Control", "variables": [{"key": "color", "value": "blue", "data_type": "STRING"}]},
      "variant1": {"displayName": "Variant", "variables": [{"key": "color", "value": "green", "data_type": "STRING"}]}
    },
    "distribution_strategy": "RANDOM",
    "assignment_domain": "COHORT",
    "exposure": 100,
    "threshold": 50000,
    "start_time": 1700000000,
    "end_time": 1900000000,
    "created_by": "test@example.com"
  }')

if echo "$RESULT" | jq -e '.error' > /dev/null 2>&1; then
  echo -e "${GREEN}✓ PASS - Correctly blocked${NC}"
  echo "Error: $(echo "$RESULT" | jq -r '.error.message' | head -c 80)..."
else
  echo -e "${RED}✗ FAIL - Should have blocked${NC}"
  echo "Response: $(echo "$RESULT" | jq .)"
fi
echo ""

# TEST 4: Invalid Variant Weights Sum (not 100)
echo -e "${YELLOW}TEST 4: Invalid Variant Weights Sum${NC}"
echo "Expected: ✗ FAIL - Weights must sum to 100"
echo ""

RESULT=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Invalid Weights Test",
    "status": "DRAFT",
    "type": "A_B",
    "cohorts": ["premium_users"],
    "variant_weights": {"type": "COHORT", "weights": {"control": 40, "variant1": 50}},
    "variants": {
      "control": {"displayName": "Control", "variables": [{"key": "color", "value": "blue", "data_type": "STRING"}]},
      "variant1": {"displayName": "Variant", "variables": [{"key": "color", "value": "green", "data_type": "STRING"}]}
    },
    "distribution_strategy": "RANDOM",
    "assignment_domain": "COHORT",
    "exposure": 100,
    "threshold": 50000,
    "start_time": 1700000000,
    "end_time": 1900000000,
    "created_by": "test@example.com"
  }')

if echo "$RESULT" | jq -e '.error' > /dev/null 2>&1 && echo "$RESULT" | jq -r '.error.message' | grep -qi "weight"; then
  echo -e "${GREEN}✓ PASS - Correctly blocked${NC}"
  echo "Error: $(echo "$RESULT" | jq -r '.error.message' | head -c 80)..."
else
  echo -e "${RED}✗ FAIL - Should have blocked${NC}"
  echo "Response: $(echo "$RESULT" | jq .)"
fi
echo ""

# TEST 5: Variant Keys Mismatch with Weights
echo -e "${YELLOW}TEST 5: Variant Keys Mismatch with Weights${NC}"
echo "Expected: ✗ FAIL - Variant keys must match weight keys"
echo ""

RESULT=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Variant Key Mismatch Test",
    "status": "DRAFT",
    "type": "A_B",
    "cohorts": ["premium_users"],
    "variant_weights": {"type": "COHORT", "weights": {"control": 50, "variant1": 50}},
    "variants": {
      "control": {"displayName": "Control", "variables": [{"key": "color", "value": "blue", "data_type": "STRING"}]},
      "variant2": {"displayName": "Variant", "variables": [{"key": "color", "value": "green", "data_type": "STRING"}]}
    },
    "distribution_strategy": "RANDOM",
    "assignment_domain": "COHORT",
    "exposure": 100,
    "threshold": 50000,
    "start_time": 1700000000,
    "end_time": 1900000000,
    "created_by": "test@example.com"
  }')

if echo "$RESULT" | jq -e '.error' > /dev/null 2>&1; then
  echo -e "${GREEN}✓ PASS - Correctly blocked${NC}"
  echo "Error: $(echo "$RESULT" | jq -r '.error.message' | head -c 80)..."
else
  echo -e "${RED}✗ FAIL - Should have blocked${NC}"
  echo "Response: $(echo "$RESULT" | jq .)"
fi
echo ""

# TEST 6: Invalid Variable Data Type
echo -e "${YELLOW}TEST 6: Invalid Variable Data Type${NC}"
echo "Expected: ✗ FAIL - data_type must be STRING, NUMBER, or BOOL"
echo ""

RESULT=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Invalid Data Type Test",
    "status": "DRAFT",
    "type": "A_B",
    "cohorts": ["premium_users"],
    "variant_weights": {"type": "COHORT", "weights": {"control": 50, "variant1": 50}},
    "variants": {
      "control": {"displayName": "Control", "variables": [{"key": "color", "value": "blue", "data_type": "INVALID"}]},
      "variant1": {"displayName": "Variant", "variables": [{"key": "color", "value": "green", "data_type": "STRING"}]}
    },
    "distribution_strategy": "RANDOM",
    "assignment_domain": "COHORT",
    "exposure": 100,
    "threshold": 50000,
    "start_time": 1700000000,
    "end_time": 1900000000,
    "created_by": "test@example.com"
  }')

if echo "$RESULT" | jq -e '.error' > /dev/null 2>&1; then
  echo -e "${GREEN}✓ PASS - Correctly blocked${NC}"
  echo "Error: $(echo "$RESULT" | jq -r '.error.message' | head -c 80)..."
else
  echo -e "${RED}✗ FAIL - Should have blocked${NC}"
  echo "Response: $(echo "$RESULT" | jq .)"
fi
echo ""

# TEST 7: Invalid Variable Value for BOOL Type
echo -e "${YELLOW}TEST 7: Invalid Variable Value for BOOL Type${NC}"
echo "Expected: ✗ FAIL - BOOL must be 'true' or 'false'"
echo ""

RESULT=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Invalid Bool Value Test",
    "status": "DRAFT",
    "type": "A_B",
    "cohorts": ["premium_users"],
    "variant_weights": {"type": "COHORT", "weights": {"control": 50, "variant1": 50}},
    "variants": {
      "control": {"displayName": "Control", "variables": [{"key": "enabled", "value": "yes", "data_type": "BOOL"}]},
      "variant1": {"displayName": "Variant", "variables": [{"key": "enabled", "value": "true", "data_type": "BOOL"}]}
    },
    "distribution_strategy": "RANDOM",
    "assignment_domain": "COHORT",
    "exposure": 100,
    "threshold": 50000,
    "start_time": 1700000000,
    "end_time": 1900000000,
    "created_by": "test@example.com"
  }')

if echo "$RESULT" | jq -e '.error' > /dev/null 2>&1 && echo "$RESULT" | jq -r '.error.message' | grep -qi "bool"; then
  echo -e "${GREEN}✓ PASS - Correctly blocked${NC}"
  echo "Error: $(echo "$RESULT" | jq -r '.error.message' | head -c 80)..."
else
  echo -e "${RED}✗ FAIL - Should have blocked${NC}"
  echo "Response: $(echo "$RESULT" | jq .)"
fi
echo ""

# TEST 8: Invalid Variable Value for NUMBER Type
echo -e "${YELLOW}TEST 8: Invalid Variable Value for NUMBER Type${NC}"
echo "Expected: ✗ FAIL - NUMBER must be a valid integer"
echo ""

RESULT=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Invalid Number Value Test",
    "status": "DRAFT",
    "type": "A_B",
    "cohorts": ["premium_users"],
    "variant_weights": {"type": "COHORT", "weights": {"control": 50, "variant1": 50}},
    "variants": {
      "control": {"displayName": "Control", "variables": [{"key": "count", "value": "abc", "data_type": "NUMBER"}]},
      "variant1": {"displayName": "Variant", "variables": [{"key": "count", "value": "10", "data_type": "NUMBER"}]}
    },
    "distribution_strategy": "RANDOM",
    "assignment_domain": "COHORT",
    "exposure": 100,
    "threshold": 50000,
    "start_time": 1700000000,
    "end_time": 1900000000,
    "created_by": "test@example.com"
  }')

if echo "$RESULT" | jq -e '.error' > /dev/null 2>&1 && echo "$RESULT" | jq -r '.error.message' | grep -qi "number\|integer"; then
  echo -e "${GREEN}✓ PASS - Correctly blocked${NC}"
  echo "Error: $(echo "$RESULT" | jq -r '.error.message' | head -c 80)..."
else
  echo -e "${RED}✗ FAIL - Should have blocked${NC}"
  echo "Response: $(echo "$RESULT" | jq .)"
fi
echo ""

# TEST 9: start_time >= end_time
echo -e "${YELLOW}TEST 9: Invalid Time Range (start_time >= end_time)${NC}"
echo "Expected: ✗ FAIL - start_time must be less than end_time"
echo ""

RESULT=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Invalid Time Range Test",
    "status": "DRAFT",
    "type": "A_B",
    "cohorts": ["premium_users"],
    "variant_weights": {"type": "COHORT", "weights": {"control": 50, "variant1": 50}},
    "variants": {
      "control": {"displayName": "Control", "variables": [{"key": "color", "value": "blue", "data_type": "STRING"}]},
      "variant1": {"displayName": "Variant", "variables": [{"key": "color", "value": "green", "data_type": "STRING"}]}
    },
    "distribution_strategy": "RANDOM",
    "assignment_domain": "COHORT",
    "exposure": 100,
    "threshold": 50000,
    "start_time": 1900000000,
    "end_time": 1700000000,
    "created_by": "test@example.com"
  }')

if echo "$RESULT" | jq -e '.error' > /dev/null 2>&1 && echo "$RESULT" | jq -r '.error.message' | grep -qi "time"; then
  echo -e "${GREEN}✓ PASS - Correctly blocked${NC}"
  echo "Error: $(echo "$RESULT" | jq -r '.error.message' | head -c 80)..."
else
  echo -e "${RED}✗ FAIL - Should have blocked${NC}"
  echo "Response: $(echo "$RESULT" | jq .)"
fi
echo ""

# TEST 10: Empty Variants
echo -e "${YELLOW}TEST 10: Empty Variants${NC}"
echo "Expected: ✗ FAIL - At least 2 variants required"
echo ""

RESULT=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Empty Variants Test",
    "status": "DRAFT",
    "type": "A_B",
    "cohorts": ["premium_users"],
    "variant_weights": {"type": "COHORT", "weights": {}},
    "variants": {},
    "distribution_strategy": "RANDOM",
    "assignment_domain": "COHORT",
    "exposure": 100,
    "threshold": 50000,
    "start_time": 1700000000,
    "end_time": 1900000000,
    "created_by": "test@example.com"
  }')

if echo "$RESULT" | jq -e '.error' > /dev/null 2>&1; then
  echo -e "${GREEN}✓ PASS - Correctly blocked${NC}"
  echo "Error: $(echo "$RESULT" | jq -r '.error.message' | head -c 80)..."
else
  echo -e "${RED}✗ FAIL - Should have blocked${NC}"
  echo "Response: $(echo "$RESULT" | jq .)"
fi
echo ""

# TEST 11: Blank Variable Key
echo -e "${YELLOW}TEST 11: Blank Variable Key${NC}"
echo "Expected: ✗ FAIL - Variable key cannot be blank"
echo ""

RESULT=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Blank Variable Key Test",
    "status": "DRAFT",
    "type": "A_B",
    "cohorts": ["premium_users"],
    "variant_weights": {"type": "COHORT", "weights": {"control": 50, "variant1": 50}},
    "variants": {
      "control": {"displayName": "Control", "variables": [{"key": "", "value": "blue", "data_type": "STRING"}]},
      "variant1": {"displayName": "Variant", "variables": [{"key": "color", "value": "green", "data_type": "STRING"}]}
    },
    "distribution_strategy": "RANDOM",
    "assignment_domain": "COHORT",
    "exposure": 100,
    "threshold": 50000,
    "start_time": 1700000000,
    "end_time": 1900000000,
    "created_by": "test@example.com"
  }')

if echo "$RESULT" | jq -e '.error' > /dev/null 2>&1 && echo "$RESULT" | jq -r '.error.message' | grep -qi "key.*blank"; then
  echo -e "${GREEN}✓ PASS - Correctly blocked${NC}"
  echo "Error: $(echo "$RESULT" | jq -r '.error.message' | head -c 80)..."
else
  echo -e "${RED}✗ FAIL - Should have blocked${NC}"
  echo "Response: $(echo "$RESULT" | jq .)"
fi
echo ""

# TEST 12: Invalid Exposure (out of range)
echo -e "${YELLOW}TEST 12: Invalid Exposure (> 100)${NC}"
echo "Expected: ✗ FAIL - Exposure must be between 0 and 100"
echo ""

RESULT=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "Invalid Exposure Test",
    "status": "DRAFT",
    "type": "A_B",
    "cohorts": ["premium_users"],
    "variant_weights": {"type": "COHORT", "weights": {"control": 50, "variant1": 50}},
    "variants": {
      "control": {"displayName": "Control", "variables": [{"key": "color", "value": "blue", "data_type": "STRING"}]},
      "variant1": {"displayName": "Variant", "variables": [{"key": "color", "value": "green", "data_type": "STRING"}]}
    },
    "distribution_strategy": "RANDOM",
    "assignment_domain": "COHORT",
    "exposure": 150,
    "threshold": 50000,
    "start_time": 1700000000,
    "end_time": 1900000000,
    "created_by": "test@example.com"
  }')

if echo "$RESULT" | jq -e '.error' > /dev/null 2>&1 && echo "$RESULT" | jq -r '.error.message' | grep -qi "exposure"; then
  echo -e "${GREEN}✓ PASS - Correctly blocked${NC}"
  echo "Error: $(echo "$RESULT" | jq -r '.error.message' | head -c 80)..."
else
  echo -e "${RED}✗ FAIL - Should have blocked${NC}"
  echo "Response: $(echo "$RESULT" | jq .)"
fi
echo ""

echo -e "${GREEN}╔══════════════════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║                   ✓ CREATE VALIDATION TESTS COMPLETE                         ║${NC}"
echo -e "${GREEN}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"

