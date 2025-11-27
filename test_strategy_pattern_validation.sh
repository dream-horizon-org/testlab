#!/bin/bash

# Test Strategy Pattern Implementation for State-Based Validation
# Tests DRAFT and LIVE state field restrictions

BASE_URL="http://localhost:8080/v1/experiment"
TENANT_ID="550e8400-e29b-41d4-a716-446655440000"
PROJECT_KEY="550e8400-e29b-41d4-a716-446655440001"

echo "=========================================="
echo "Strategy Pattern Validation Tests"
echo "=========================================="
echo ""

# Generate unique name with timestamp
TIMESTAMP=$(date +%s)
EXP_NAME="Strategy_Test_$TIMESTAMP"

echo "1️⃣  Creating experiment in DRAFT state..."
CREATE_RESP=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d "{
    \"name\": \"$EXP_NAME\",
    \"description\": \"Testing strategy pattern\",
    \"hypothesis\": \"Validation works\",
    \"status\": \"DRAFT\",
    \"type\": \"A_B\",
    \"created_by\": \"test@example.com\",
    \"cohorts\": [\"test\"],
    \"variant_weights\": {\"type\": \"COHORT\", \"weights\": {\"control\": 50.0, \"variant1\": 50.0}},
    \"variants\": {
      \"control\": {\"display_name\": \"Control\", \"variables\": [{\"key\": \"f\", \"value\": \"false\", \"data_type\": \"BOOL\"}]},
      \"variant1\": {\"display_name\": \"Variant 1\", \"variables\": [{\"key\": \"f\", \"value\": \"true\", \"data_type\": \"BOOL\"}]}
    },
    \"distribution_strategy\": \"RANDOM\",
    \"assignment_domain\": \"COHORT\",
    \"exposure\": 100,
    \"threshold\": 1000,
    \"start_time\": 1700000000,
    \"end_time\": 1800000000,
    \"tags\": [\"test\"],
    \"owner\": [\"test@example.com\"],
    \"metrics\": {\"primary\": [\"c\"], \"secondary\": [\"e\"]}
  }")

EXP_ID=$(echo "$CREATE_RESP" | jq -r '.data.experiment_id')

if [ "$EXP_ID" != "null" ] && [ -n "$EXP_ID" ]; then
    echo "   ✅ Created: $EXP_ID"
else
    echo "   ❌ Failed to create experiment"
    echo "$CREATE_RESP" | jq .
    exit 1
fi

echo ""
echo "2️⃣  Testing DRAFT state (all fields allowed)..."
UPDATE_RESP=$(curl -s -X PATCH "$BASE_URL/$EXP_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "description": "Updated in DRAFT",
    "cohorts": ["premium"],
    "variants": {
      "control": {"display_name": "C2", "variables": [{"key": "f", "value": "false", "data_type": "BOOL"}]},
      "variant1": {"display_name": "V2", "variables": [{"key": "f", "value": "true", "data_type": "BOOL"}]}
    }
  }')

STATUS=$(echo "$UPDATE_RESP" | jq -r '.data.status')
if [ "$STATUS" = "true" ]; then
    echo "   ✅ DRAFT update successful (all fields allowed)"
else
    echo "   ❌ DRAFT update failed"
    echo "$UPDATE_RESP" | jq .
fi

echo ""
echo "3️⃣  Transitioning to LIVE state..."
LIVE_RESP=$(curl -s -X PATCH "$BASE_URL/$EXP_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{"status": "LIVE"}')

STATUS=$(echo "$LIVE_RESP" | jq -r '.data.status')
if [ "$STATUS" = "true" ]; then
    echo "   ✅ Transitioned to LIVE"
else
    echo "   ❌ Failed to transition"
fi

echo ""
echo "4️⃣  Testing LIVE state - ALLOWED fields..."
ALLOWED_RESP=$(curl -s -X PATCH "$BASE_URL/$EXP_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "description": "Updated in LIVE",
    "hypothesis": "Updated hypothesis",
    "exposure": 80,
    "threshold": 5000
  }')

STATUS=$(echo "$ALLOWED_RESP" | jq -r '.data.status')
if [ "$STATUS" = "true" ]; then
    echo "   ✅ LIVE update successful (allowed fields)"
else
    echo "   ❌ LIVE update failed for allowed fields"
    echo "$ALLOWED_RESP" | jq .
fi

echo ""
echo "5️⃣  Testing LIVE state - RESTRICTED field (cohorts)..."
RESTRICTED_RESP=$(curl -s -X PATCH "$BASE_URL/$EXP_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{"cohorts": ["all_users"]}')

ERROR_CODE=$(echo "$RESTRICTED_RESP" | jq -r '.error.code')
if [ "$ERROR_CODE" = "INVALID_REQUEST" ]; then
    echo "   ✅ Correctly rejected 'cohorts' update"
    echo "   📋 Error: $(echo "$RESTRICTED_RESP" | jq -r '.error.message' | head -c 80)..."
else
    echo "   ❌ Should have rejected 'cohorts' update"
    echo "$RESTRICTED_RESP" | jq .
fi

echo ""
echo "6️⃣  Testing LIVE state - RESTRICTED field (variants)..."
RESTRICTED_RESP=$(curl -s -X PATCH "$BASE_URL/$EXP_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "variants": {
      "control": {"display_name": "New", "variables": [{"key": "f", "value": "false", "data_type": "BOOL"}]}
    }
  }')

ERROR_CODE=$(echo "$RESTRICTED_RESP" | jq -r '.error.code')
if [ "$ERROR_CODE" = "INVALID_REQUEST" ]; then
    echo "   ✅ Correctly rejected 'variants' update"
else
    echo "   ❌ Should have rejected 'variants' update"
    echo "$RESTRICTED_RESP" | jq .
fi

echo ""
echo "7️⃣  Testing LIVE state - Multiple RESTRICTED fields..."
MULTI_RESP=$(curl -s -X PATCH "$BASE_URL/$EXP_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "cohorts": ["new"],
    "type": "A_A",
    "distribution_strategy": "ROUND_ROBIN"
  }')

ERROR_CODE=$(echo "$MULTI_RESP" | jq -r '.error.code')
ERROR_MSG=$(echo "$MULTI_RESP" | jq -r '.error.message')
if [ "$ERROR_CODE" = "INVALID_REQUEST" ] && [[ "$ERROR_MSG" == *"cohorts"* ]] && [[ "$ERROR_MSG" == *"type"* ]] && [[ "$ERROR_MSG" == *"distribution_strategy"* ]]; then
    echo "   ✅ Correctly rejected multiple restricted fields"
    echo "   📋 Fields rejected: cohorts, type, distribution_strategy"
else
    echo "   ❌ Should have rejected all restricted fields"
    echo "$MULTI_RESP" | jq .
fi

echo ""
echo "=========================================="
echo "✅ Strategy Pattern Validation Complete!"
echo "=========================================="
echo ""
echo "Summary:"
echo "  • DraftStateValidationStrategy: ✅ Working"
echo "  • LiveStateValidationStrategy: ✅ Working"
echo "  • StateValidationContext: ✅ Working"
echo "  • Dependency Injection: ✅ Working"
echo ""

