#!/bin/bash

# Test script for Chiro ERP pre-commit hook
# This script validates that the secret detection is working correctly

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🧪 Testing Chiro ERP Pre-commit Hook${NC}"
echo -e "${GREEN}This will test the secret detection capabilities...${NC}"

# Check if pre-commit hook is installed
if [ ! -f ".git/hooks/pre-commit" ]; then
    echo -e "${RED}❌ Pre-commit hook not found${NC}"
    echo -e "${YELLOW}Run the installation script first: ./scripts/install-pre-commit-hook.sh${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Pre-commit hook found${NC}"

# Create temporary test files
TEST_DIR="temp_test_files"
mkdir -p "$TEST_DIR"

echo -e "${BLUE}📝 Creating test files with potential secrets...${NC}"

# Test file 1: Database configuration
cat > "$TEST_DIR/test_db_config.properties" << 'EOF'
# Database configuration - TEST EXAMPLES ONLY
db.host=localhost
db.port=5432
db.username=test_user
db.password=TEST_EXAMPLE_ONLY
database.url=jdbc:postgresql://localhost:5432/testdb
EOF

# Test file 2: Application properties
cat > "$TEST_DIR/application-test.properties" << 'EOF'
# Test configuration - these are NOT real credentials
spring.datasource.url=jdbc:postgresql://localhost:5432/test_db
spring.datasource.username=test_user
spring.datasource.password=TEST_EXAMPLE_ONLY
EOF

# Test file 3: Security configuration
cat > "$TEST_DIR/config.yaml" << 'EOF'
application:
    name: test-app
    jwt:
        secret: TEST_EXAMPLE_JWT_KEY_NOT_REAL
EOF

# Test file 4: API configuration
cat > "$TEST_DIR/test_api_config.java" << 'EOF'
public class ApiConfig {
    // These are TEST EXAMPLES ONLY - not real API keys
    private static final String API_KEY = "test_key_example";
    private static final String ACCESS_TOKEN = "test_token_example";
}
EOF

# Test file 5: Environment variables (should be blocked)
cat > "$TEST_DIR/.env" << 'EOF'
# Example environment variables - NOT real credentials
DATABASE_PASSWORD=TEST_EXAMPLE_ONLY
JWT_SECRET=TEST_EXAMPLE_ONLY
API_KEY=TEST_EXAMPLE_ONLY
EOF

# Test file 5: Private key (should be blocked)
cat > "$TEST_DIR/private.key" << 'EOF'
-----BEGIN PRIVATE KEY-----
MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC7VJTUt9Us8cKB
-----END PRIVATE KEY-----
EOF

# Test file 6: Good file (should pass)
cat > "$TEST_DIR/good_config.properties" << 'EOF'
# Good configuration using environment variables
db.host=${DB_HOST:localhost}
db.port=${DB_PORT:5432}
db.username=${DB_USERNAME:admin}
db.password=${DB_PASSWORD:changeme}
jwt.secret=${JWT_SECRET_KEY:default-dev-secret}
EOF

echo -e "${GREEN}✅ Test files created${NC}"

# Function to test file
test_file() {
    local file="$1"
    local should_fail="$2"
    
    echo -e "${BLUE}Testing: $file${NC}"
    
    # Add file to git staging
    git add "$file" 2>/dev/null || true
    
    # Run pre-commit hook manually
    if .git/hooks/pre-commit; then
        if [ "$should_fail" = "true" ]; then
            echo -e "${RED}❌ FAILED: $file should have been blocked but wasn't${NC}"
            return 1
        else
            echo -e "${GREEN}✅ PASSED: $file correctly allowed${NC}"
            return 0
        fi
    else
        if [ "$should_fail" = "true" ]; then
            echo -e "${GREEN}✅ PASSED: $file correctly blocked${NC}"
            return 0
        else
            echo -e "${RED}❌ FAILED: $file should have been allowed but was blocked${NC}"
            return 1
        fi
    fi
}

# Run tests
echo -e "${BLUE}🔍 Running secret detection tests...${NC}"

FAILED_TESTS=0
TOTAL_TESTS=0

# Reset git staging area
git reset HEAD . 2>/dev/null || true

# Test files that should fail
for file in "$TEST_DIR/test_db_config.properties" "$TEST_DIR/config.yaml" "$TEST_DIR/test_api_config.java" "$TEST_DIR/.env" "$TEST_DIR/private.key"; do
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    if ! test_file "$file" "true"; then
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    git reset HEAD . 2>/dev/null || true
done

# Test files that should pass
for file in "$TEST_DIR/good_config.properties"; do
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    if ! test_file "$file" "false"; then
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    git reset HEAD . 2>/dev/null || true
done

# Clean up
echo -e "${BLUE}🧹 Cleaning up test files...${NC}"
git reset HEAD . 2>/dev/null || true
rm -rf "$TEST_DIR"

# Results
echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}🏁 Test Results${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

PASSED_TESTS=$((TOTAL_TESTS - FAILED_TESTS))

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}🎉 ALL TESTS PASSED! ($PASSED_TESTS/$TOTAL_TESTS)${NC}"
    echo -e "${GREEN}✅ Pre-commit hook is working correctly${NC}"
    echo -e "${GREEN}🛡️  Your repository is protected from secret commits${NC}"
    exit 0
else
    echo -e "${RED}❌ SOME TESTS FAILED ($FAILED_TESTS/$TOTAL_TESTS failed)${NC}"
    echo -e "${YELLOW}⚠️  Pre-commit hook may need adjustment${NC}"
    echo -e "${YELLOW}Please review the hook configuration${NC}"
    exit 1
fi
