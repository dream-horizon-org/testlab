#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080"
PROJECT_KEY="550e8400-e29b-41d4-a716-446655440001"
TENANT_ID="550e8400-e29b-41d4-a716-446655440000"

# Counter for tests
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Function to print test result
print_result() {
    local test_name=$1
    local status=$2
    local message=$3
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    if [ "$status" = "PASS" ]; then
        echo -e "${GREEN}✓${NC} TEST $TOTAL_TESTS: $test_name - ${GREEN}PASSED${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}✗${NC} TEST $TOTAL_TESTS: $test_name - ${RED}FAILED${NC}"
        echo -e "  ${YELLOW}Reason: $message${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
}

# Function to create an experiment
create_experiment() {
    local name=$1
    local status=$2
    
    response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/v1/experiment" \
        -H "Content-Type: application/json" \
        -H "x-tenant-id: $TENANT_ID" \
        -H "x-project-key: $PROJECT_KEY" \
        -d '{
            "name": "'"$name"'",
            "description": "Test experiment for update scenarios",
            "hypothesis": "Testing state-based validation",
            "status": "'"$status"'",
            "type": "A_B",
            "guardrail_health_status": "PASSING",
            "cohorts": ["premium_users", "mobile_users"],
            "distribution_strategy": "RANDOM",
            "assignment_domain": "COHORT",
            "variant_weights": {
                "type": "COHORT",
                "weights": {
                    "control": 50,
                    "variant1": 50
                }
            },
            "variants": {
                "control": {
                    "displayName": "Control",
                    "variables": [
                        {
                            "key": "feature",
                            "value": "false",
                            "data_type": "BOOL"
                        }
                    ]
                },
                "variant1": {
                    "displayName": "Variant 1",
                    "variables": [
                        {
                            "key": "feature",
                            "value": "true",
                            "data_type": "BOOL"
                        }
                    ]
                }
            },
            "exposure": 100,
            "threshold": 50000,
            "start_time": 1700000000,
            "end_time": 1900000000,
            "created_by": "test@example.com",
            "tags": ["test", "automation"],
            "owner": ["test@example.com"],
            "metrics": {
                "primary": ["conversion"],
                "secondary": ["engagement"]
            }
        }')
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "201" ]; then
        experiment_id=$(echo "$body" | grep -o '"experimentId":"[^"]*"' | cut -d'"' -f4)
        echo "$experiment_id"
    else
        echo ""
    fi
}

# Function to update experiment
update_experiment() {
    local experiment_id=$1
    local payload=$2
    
    curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/v1/experiment/$experiment_id" \
        -H "Content-Type: application/json" \
        -H "x-tenant-id: $TENANT_ID" \
        -H "x-project-key: $PROJECT_KEY" \
        -d "$payload"
}

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  UPDATE FLOW TEST SCENARIOS${NC}"
echo -e "${BLUE}========================================${NC}\n"

# ============================================
# DRAFT STATE UPDATE TESTS
# ============================================
echo -e "${YELLOW}--- DRAFT STATE UPDATE TESTS ---${NC}\n"

# Test 1: Create DRAFT experiment and update description
echo -e "${BLUE}Creating DRAFT experiment for Test 1-5...${NC}"
DRAFT_EXP_ID=$(create_experiment "Draft Update Test v1" "DRAFT")

if [ -z "$DRAFT_EXP_ID" ]; then
    echo -e "${RED}Failed to create DRAFT experiment. Skipping DRAFT tests.${NC}\n"
else
    echo -e "${GREEN}Created DRAFT experiment: $DRAFT_EXP_ID${NC}\n"
    
    # Test 1: Update description in DRAFT (should succeed)
    response=$(update_experiment "$DRAFT_EXP_ID" '{
        "description": "Updated description in DRAFT state",
        "updated_by": "test@example.com"
    }')
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "200" ]; then
        print_result "Update description in DRAFT state" "PASS" ""
    else
        print_result "Update description in DRAFT state" "FAIL" "Expected 200, got $http_code - $body"
    fi
    
    # Test 2: Update hypothesis in DRAFT (should succeed)
    response=$(update_experiment "$DRAFT_EXP_ID" '{
        "hypothesis": "Updated hypothesis in DRAFT state",
        "updated_by": "test@example.com"
    }')
    http_code=$(echo "$response" | tail -n1)
    
    if [ "$http_code" = "200" ]; then
        print_result "Update hypothesis in DRAFT state" "PASS" ""
    else
        print_result "Update hypothesis in DRAFT state" "FAIL" "Expected 200, got $http_code"
    fi
    
    # Test 3: Update variant_weights in DRAFT (should succeed)
    response=$(update_experiment "$DRAFT_EXP_ID" '{
        "variant_weights": {
            "type": "SEQUENTIAL",
            "weights": {
                "control": 60,
                "variant1": 40
            }
        },
        "updated_by": "test@example.com"
    }')
    http_code=$(echo "$response" | tail -n1)
    
    if [ "$http_code" = "200" ]; then
        print_result "Update variant_weights in DRAFT state" "PASS" ""
    else
        print_result "Update variant_weights in DRAFT state" "FAIL" "Expected 200, got $http_code"
    fi
    
    # Test 4: Update variants in DRAFT (should succeed)
    response=$(update_experiment "$DRAFT_EXP_ID" '{
        "variants": {
            "control": {
                "display_name": "Updated Control",
                "variables": [
                    {
                        "key": "feature",
                        "value": "false",
                        "data_type": "BOOL"
                    }
                ]
            },
            "variant1": {
                "display_name": "Updated Variant 1",
                "variables": [
                    {
                        "key": "feature",
                        "value": "true",
                        "data_type": "BOOL"
                    }
                ]
            }
        },
        "updated_by": "test@example.com"
    }')
    http_code=$(echo "$response" | tail -n1)
    
    if [ "$http_code" = "200" ]; then
        print_result "Update variants in DRAFT state" "PASS" ""
    else
        print_result "Update variants in DRAFT state" "FAIL" "Expected 200, got $http_code"
    fi
    
    # Test 5: Update exposure in DRAFT (should succeed)
    response=$(update_experiment "$DRAFT_EXP_ID" '{
        "exposure": 75,
        "updated_by": "test@example.com"
    }')
    http_code=$(echo "$response" | tail -n1)
    
    if [ "$http_code" = "200" ]; then
        print_result "Update exposure in DRAFT state" "PASS" ""
    else
        print_result "Update exposure in DRAFT state" "FAIL" "Expected 200, got $http_code"
    fi
fi

echo ""

# ============================================
# LIVE STATE UPDATE TESTS
# ============================================
echo -e "${YELLOW}--- LIVE STATE UPDATE TESTS ---${NC}\n"

# Test 6-15: Create LIVE experiment and test restricted fields
echo -e "${BLUE}Creating LIVE experiment for Test 6-15...${NC}"
LIVE_EXP_ID=$(create_experiment "Live Update Test v1" "LIVE")

if [ -z "$LIVE_EXP_ID" ]; then
    echo -e "${RED}Failed to create LIVE experiment. Skipping LIVE tests.${NC}\n"
else
    echo -e "${GREEN}Created LIVE experiment: $LIVE_EXP_ID${NC}\n"
    
    # Test 6: Update description in LIVE (should succeed - allowed field)
    response=$(update_experiment "$LIVE_EXP_ID" '{
        "description": "Updated description in LIVE state",
        "updated_by": "test@example.com"
    }')
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "200" ]; then
        print_result "Update description in LIVE state (allowed)" "PASS" ""
    else
        print_result "Update description in LIVE state (allowed)" "FAIL" "Expected 200, got $http_code - $body"
    fi
    
    # Test 7: Update hypothesis in LIVE (should succeed - allowed field)
    response=$(update_experiment "$LIVE_EXP_ID" '{
        "hypothesis": "Updated hypothesis in LIVE state",
        "updated_by": "test@example.com"
    }')
    http_code=$(echo "$response" | tail -n1)
    
    if [ "$http_code" = "200" ]; then
        print_result "Update hypothesis in LIVE state (allowed)" "PASS" ""
    else
        print_result "Update hypothesis in LIVE state (allowed)" "FAIL" "Expected 200, got $http_code"
    fi
    
    # Test 8: Update tags in LIVE (should succeed - allowed field)
    response=$(update_experiment "$LIVE_EXP_ID" '{
        "tags": ["live-test", "updated"],
        "updated_by": "test@example.com"
    }')
    http_code=$(echo "$response" | tail -n1)
    
    if [ "$http_code" = "200" ]; then
        print_result "Update tags in LIVE state (allowed)" "PASS" ""
    else
        print_result "Update tags in LIVE state (allowed)" "FAIL" "Expected 200, got $http_code"
    fi
    
    # Test 9: Update owner in LIVE (should succeed - allowed field)
    response=$(update_experiment "$LIVE_EXP_ID" '{
        "owner": ["newowner@example.com"],
        "updated_by": "test@example.com"
    }')
    http_code=$(echo "$response" | tail -n1)
    
    if [ "$http_code" = "200" ]; then
        print_result "Update owner in LIVE state (allowed)" "PASS" ""
    else
        print_result "Update owner in LIVE state (allowed)" "FAIL" "Expected 200, got $http_code"
    fi
    
    # Test 10: Update metrics in LIVE (should succeed - allowed field)
    response=$(update_experiment "$LIVE_EXP_ID" '{
        "metrics": {
            "primary": ["conversion"],
            "secondary": ["engagement"]
        },
        "updated_by": "test@example.com"
    }')
    http_code=$(echo "$response" | tail -n1)
    
    if [ "$http_code" = "200" ]; then
        print_result "Update metrics in LIVE state (allowed)" "PASS" ""
    else
        print_result "Update metrics in LIVE state (allowed)" "FAIL" "Expected 200, got $http_code"
    fi
    
    # Test 11: Update variant_weights in LIVE (should FAIL - restricted field)
    response=$(update_experiment "$LIVE_EXP_ID" '{
        "variant_weights": {
            "type": "SEQUENTIAL",
            "weights": {
                "control": 70,
                "variant1": 30
            }
        },
        "updated_by": "test@example.com"
    }')
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "400" ] && echo "$body" | grep -q "Cannot update.*LIVE"; then
        print_result "Update variant_weights in LIVE state (restricted)" "PASS" ""
    else
        print_result "Update variant_weights in LIVE state (restricted)" "FAIL" "Expected 400 with restriction message, got $http_code - $body"
    fi
    
    # Test 12: Update variants in LIVE (should FAIL - restricted field)
    response=$(update_experiment "$LIVE_EXP_ID" '{
        "variants": {
            "control": {
                "display_name": "Should Not Update",
                "variables": [
                    {
                        "key": "feature",
                        "value": "false",
                        "data_type": "BOOL"
                    }
                ]
            },
            "variant1": {
                "display_name": "Should Not Update",
                "variables": [
                    {
                        "key": "feature",
                        "value": "true",
                        "data_type": "BOOL"
                    }
                ]
            }
        },
        "updated_by": "test@example.com"
    }')
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "400" ] && echo "$body" | grep -q "Cannot update.*LIVE"; then
        print_result "Update variants in LIVE state (restricted)" "PASS" ""
    else
        print_result "Update variants in LIVE state (restricted)" "FAIL" "Expected 400 with restriction message, got $http_code - $body"
    fi
    
    # Test 13: Update exposure in LIVE (should FAIL - restricted field)
    response=$(update_experiment "$LIVE_EXP_ID" '{
        "exposure": 50,
        "updated_by": "test@example.com"
    }')
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "400" ] && echo "$body" | grep -q "Cannot update.*LIVE"; then
        print_result "Update exposure in LIVE state (restricted)" "PASS" ""
    else
        print_result "Update exposure in LIVE state (restricted)" "FAIL" "Expected 400 with restriction message, got $http_code - $body"
    fi
    
    # Test 14: Update type in LIVE (should FAIL - restricted field)
    response=$(update_experiment "$LIVE_EXP_ID" '{
        "type": "MULTIVARIATE",
        "updated_by": "test@example.com"
    }')
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "400" ] && echo "$body" | grep -q "Cannot update.*LIVE"; then
        print_result "Update type in LIVE state (restricted)" "PASS" ""
    else
        print_result "Update type in LIVE state (restricted)" "FAIL" "Expected 400 with restriction message, got $http_code - $body"
    fi
    
    # Test 15: Update cohorts in LIVE (should FAIL - restricted field)
    response=$(update_experiment "$LIVE_EXP_ID" '{
        "cohorts": ["new_cohort"],
        "updated_by": "test@example.com"
    }')
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "400" ] && echo "$body" | grep -q "Cannot update.*LIVE"; then
        print_result "Update cohorts in LIVE state (restricted)" "PASS" ""
    else
        print_result "Update cohorts in LIVE state (restricted)" "FAIL" "Expected 400 with restriction message, got $http_code - $body"
    fi
fi

echo ""

# ============================================
# NON-UPDATABLE FIELDS TESTS
# ============================================
echo -e "${YELLOW}--- NON-UPDATABLE FIELDS TESTS (Any State) ---${NC}\n"

# Test 16-18: Try to update non-updatable fields
if [ ! -z "$DRAFT_EXP_ID" ]; then
    # Test 16: Try to update name (should FAIL - non-updatable)
    response=$(update_experiment "$DRAFT_EXP_ID" '{
        "name": "Should Not Update Name",
        "updated_by": "test@example.com"
    }')
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "400" ] && echo "$body" | grep -q "cannot be updated"; then
        print_result "Update name field (non-updatable)" "PASS" ""
    else
        print_result "Update name field (non-updatable)" "FAIL" "Expected 400 with non-updatable message, got $http_code - $body"
    fi
    
    # Test 17: Try to update created_by (should FAIL - non-updatable)
    response=$(update_experiment "$DRAFT_EXP_ID" '{
        "created_by": "hacker@example.com",
        "updated_by": "test@example.com"
    }')
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "400" ] && echo "$body" | grep -q "cannot be updated"; then
        print_result "Update created_by field (non-updatable)" "PASS" ""
    else
        print_result "Update created_by field (non-updatable)" "FAIL" "Expected 400 with non-updatable message, got $http_code - $body"
    fi
    
    # Test 18: Try to update experiment_key (should FAIL - non-updatable)
    response=$(update_experiment "$DRAFT_EXP_ID" '{
        "experiment_key": "new_key",
        "updated_by": "test@example.com"
    }')
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "400" ] && echo "$body" | grep -q "cannot be updated"; then
        print_result "Update experiment_key field (non-updatable)" "PASS" ""
    else
        print_result "Update experiment_key field (non-updatable)" "FAIL" "Expected 400 with non-updatable message, got $http_code - $body"
    fi
fi

echo ""

# ============================================
# TERMINAL STATE UPDATE TESTS
# ============================================
echo -e "${YELLOW}--- TERMINAL STATE UPDATE TESTS ---${NC}\n"

# Test 19: Create CONCLUDED experiment and try to update
echo -e "${BLUE}Creating CONCLUDED experiment for Test 19...${NC}"
CONCLUDED_EXP_ID=$(create_experiment "Concluded Update Test v1" "CONCLUDED")

if [ ! -z "$CONCLUDED_EXP_ID" ]; then
    echo -e "${GREEN}Created CONCLUDED experiment: $CONCLUDED_EXP_ID${NC}\n"
    
    response=$(update_experiment "$CONCLUDED_EXP_ID" '{
        "description": "Should not update in CONCLUDED state",
        "updated_by": "test@example.com"
    }')
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "400" ] && echo "$body" | grep -q "terminal state"; then
        print_result "Update experiment in CONCLUDED state (terminal)" "PASS" ""
    else
        print_result "Update experiment in CONCLUDED state (terminal)" "FAIL" "Expected 400 with terminal state message, got $http_code - $body"
    fi
fi

# Test 20: Create TERMINATED experiment and try to update
echo -e "${BLUE}Creating TERMINATED experiment for Test 20...${NC}"
TERMINATED_EXP_ID=$(create_experiment "Terminated Update Test v1" "TERMINATED")

if [ ! -z "$TERMINATED_EXP_ID" ]; then
    echo -e "${GREEN}Created TERMINATED experiment: $TERMINATED_EXP_ID${NC}\n"
    
    response=$(update_experiment "$TERMINATED_EXP_ID" '{
        "description": "Should not update in TERMINATED state",
        "updated_by": "test@example.com"
    }')
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "400" ] && echo "$body" | grep -q "terminal state"; then
        print_result "Update experiment in TERMINATED state (terminal)" "PASS" ""
    else
        print_result "Update experiment in TERMINATED state (terminal)" "FAIL" "Expected 400 with terminal state message, got $http_code - $body"
    fi
fi

echo ""

# ============================================
# VALIDATION TESTS
# ============================================
echo -e "${YELLOW}--- VALIDATION TESTS ---${NC}\n"

if [ ! -z "$DRAFT_EXP_ID" ]; then
    # Test 21: Invalid time range (start_time > end_time)
    response=$(update_experiment "$DRAFT_EXP_ID" '{
        "start_time": 1900000000,
        "end_time": 1700000000,
        "updated_by": "test@example.com"
    }')
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "400" ] && echo "$body" | grep -qi "time"; then
        print_result "Invalid time range validation (start > end)" "PASS" ""
    else
        print_result "Invalid time range validation (start > end)" "FAIL" "Expected 400 with time validation error, got $http_code - $body"
    fi
    
    # Test 22: Empty update request
    response=$(update_experiment "$DRAFT_EXP_ID" '{
        "updated_by": "test@example.com"
    }')
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "200" ]; then
        print_result "Empty update request handling" "PASS" ""
    else
        print_result "Empty update request handling" "FAIL" "Expected 200 (no updates), got $http_code - $body"
    fi
fi

echo ""

# ============================================
# SUMMARY
# ============================================
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  TEST SUMMARY${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "Total Tests: ${BLUE}$TOTAL_TESTS${NC}"
echo -e "Passed: ${GREEN}$PASSED_TESTS${NC}"
echo -e "Failed: ${RED}$FAILED_TESTS${NC}"

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "\n${GREEN}✓ All tests passed!${NC}\n"
    exit 0
else
    echo -e "\n${RED}✗ Some tests failed!${NC}\n"
    exit 1
fi

