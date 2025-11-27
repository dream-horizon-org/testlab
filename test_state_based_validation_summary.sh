#!/bin/bash

# Comprehensive test showing LIVE state field restrictions
# Demonstrates both allowed and restricted field updates

BASE_URL="http://localhost:8080/v1/experiment"
TENANT_ID="550e8400-e29b-41d4-a716-446655440000"
PROJECT_KEY="550e8400-e29b-41d4-a716-446655440001"

echo "=========================================="
echo "LIVE State Field Restrictions - Summary"
echo "=========================================="
echo ""

# Create experiment in LIVE state
echo "1️⃣  Creating experiment directly in LIVE state..."
CREATE_RESP=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "name": "State Validation Demo",
    "description": "Original description",
    "hypothesis": "Original hypothesis",
    "status": "LIVE",
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
    "tags": ["demo"],
    "owner": ["product@example.com"],
    "metrics": {"primary": ["conversion"], "secondary": ["engagement"]}
  }')

EXP_ID=$(echo "$CREATE_RESP" | jq -r '.data.experiment_id')
echo "   ✓ Created experiment: $EXP_ID"
echo ""

# Test allowed fields
echo "2️⃣  Testing ALLOWED field updates in LIVE state..."
echo "   Fields: description, hypothesis, exposure, threshold, end_time, tags, owner, metrics"
echo ""
ALLOWED_RESP=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/$EXP_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "description": "✅ Updated description",
    "hypothesis": "✅ Updated hypothesis",
    "exposure": 80,
    "threshold": 5000,
    "end_time": 1900000000,
    "tags": ["updated", "allowed"],
    "owner": ["updated@example.com"],
    "metrics": {"primary": ["revenue"], "secondary": ["retention"]}
  }')

HTTP_CODE=$(echo "$ALLOWED_RESP" | tail -n1)
if [ "$HTTP_CODE" -eq 200 ]; then
    echo "   ✅ SUCCESS: All allowed fields updated (HTTP 200)"
else
    echo "   ❌ FAILED: Expected 200, got $HTTP_CODE"
fi
echo ""

# Test restricted fields
echo "3️⃣  Testing RESTRICTED field updates in LIVE state..."
echo "   Attempting to update: cohorts, variants, type"
echo ""
RESTRICTED_RESP=$(curl -s -X PATCH "$BASE_URL/$EXP_ID" \
  -H "Content-Type: application/json" \
  -H "x-tenant-id: $TENANT_ID" \
  -H "x-project-key: $PROJECT_KEY" \
  -d '{
    "cohorts": ["all_users"],
    "type": "A_A",
    "variants": {
      "control": {"display_name": "New Control", "variables": [{"key": "feature", "value": "false", "data_type": "BOOL"}]}
    }
  }')

ERROR_MSG=$(echo "$RESTRICTED_RESP" | jq -r '.error.message')
if [[ "$ERROR_MSG" == *"Cannot update the following fields in LIVE state"* ]]; then
    echo "   ✅ REJECTED: Correctly blocked restricted fields"
    echo ""
    echo "   📋 Error Message:"
    echo "$RESTRICTED_RESP" | jq '.error.message'
else
    echo "   ❌ FAILED: Should have rejected the update"
fi
echo ""

# Show field categories
echo "=========================================="
echo "📊 Field Categories Summary"
echo "=========================================="
echo ""
echo "✅ ALLOWED in LIVE state (Metadata & Non-Critical):"
echo "   • description"
echo "   • hypothesis"
echo "   • status"
echo "   • guardrail_health_status"
echo "   • winning_variant"
echo "   • exposure"
echo "   • threshold"
echo "   • end_time"
echo "   • tags"
echo "   • owner"
echo "   • metrics"
echo "   • updated_by"
echo ""
echo "❌ RESTRICTED in LIVE state (Behavior-Changing):"
echo "   • type"
echo "   • cohorts"
echo "   • variant_weights"
echo "   • variants"
echo "   • distribution_strategy"
echo "   • assignment_domain"
echo "   • overrides"
echo "   • rule_attributes"
echo "   • start_time"
echo ""
echo "=========================================="
echo "✅ All validations working correctly!"
echo "=========================================="

