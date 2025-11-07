# Manual Testing Guide for Name Availability API

## Prerequisites

1. **Java 17** installed
2. **Maven** installed
3. **PostgreSQL** running and accessible
4. **Aerospike** running and accessible (if required by your setup)

## Step 1: Build the Application

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
mvn clean package
```

This will create a fat JAR in `target/testlab/testlab-1.0-fat.jar`

## Step 2: Start the Application

Navigate to the jar directory:
```bash
cd target/testlab
```

Run the application:
```bash
PG_USER=<your-postgres-username> \
PG_PASSWORD=<your-postgres-password> \
java -Dapp.environment=local \
     -Dlogback.configurationFile=./resources/logback/logback.xml \
     -jar testlab-1.0-fat.jar
```

**Note:** Make sure your PostgreSQL database has the schema created. You can run:
```bash
psql -U <username> -d <database> -f src/main/resources/db/postgresql/schema.sql
```

The application should start on **port 8080** (default).

## Step 3: Test the API Endpoint

### Endpoint Details
- **URL:** `http://localhost:8080/v1/experiments/name-availability`
- **Method:** `GET`
- **Header:** `x-project-key: <project-key>`
- **Query Parameter:** `name=<experiment-name>`

### Test Cases

#### 1. Test with Available Name (Name doesn't exist)
```bash
curl -X GET "http://localhost:8080/v1/experiments/name-availability?name=new-experiment-name" \
  -H "x-project-key: test-project" \
  -H "Content-Type: application/json"
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "isAvailable": true,
    "message": "Experiment name 'new-experiment-name' is available in project 'test-project'"
  }
}
```

#### 2. Test with Existing Name (Name already exists)
First, make sure you have an experiment with this name in your database, then:
```bash
curl -X GET "http://localhost:8080/v1/experiments/name-availability?name=existing-experiment" \
  -H "x-project-key: test-project" \
  -H "Content-Type: application/json"
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "isAvailable": false,
    "message": "Experiment name 'existing-experiment' already exists in project 'test-project'"
  }
}
```

#### 3. Test Missing Project Key Header (Should return 400)
```bash
curl -X GET "http://localhost:8080/v1/experiments/name-availability?name=test-name" \
  -H "Content-Type: application/json"
```

**Expected Response (400 Bad Request):**
```json
{
  "error": {
    "code": "INVALID_REQUEST",
    "message": "x-project-key header is missing",
    "cause": "..."
  }
}
```

#### 4. Test Missing Name Query Parameter (Should return 400)
```bash
curl -X GET "http://localhost:8080/v1/experiments/name-availability" \
  -H "x-project-key: test-project" \
  -H "Content-Type: application/json"
```

**Expected Response (400 Bad Request):**
```json
{
  "error": {
    "code": "INVALID_REQUEST",
    "message": "name query parameter is missing",
    "cause": "..."
  }
}
```

#### 5. Test Blank Project Key Header (Should return 400)
```bash
curl -X GET "http://localhost:8080/v1/experiments/name-availability?name=test-name" \
  -H "x-project-key: " \
  -H "Content-Type: application/json"
```

**Expected Response (400 Bad Request)**

#### 6. Test Blank Name Query Parameter (Should return 400)
```bash
curl -X GET "http://localhost:8080/v1/experiments/name-availability?name=" \
  -H "x-project-key: test-project" \
  -H "Content-Type: application/json"
```

**Expected Response (400 Bad Request)**

#### 7. Test Name Too Long (Should return 400)
```bash
# Create a name longer than 255 characters
LONG_NAME=$(python3 -c "print('a' * 256)")
curl -X GET "http://localhost:8080/v1/experiments/name-availability?name=${LONG_NAME}" \
  -H "x-project-key: test-project" \
  -H "Content-Type: application/json"
```

**Expected Response (400 Bad Request):**
```json
{
  "error": {
    "code": "testlab_EXPERIMENT_NAME_TOO_LONG",
    "message": "experiment name is too long (max 255 characters)",
    "cause": "..."
  }
}
```

## Step 4: Verify Database Query

You can verify the database query directly:

```sql
-- Check if a name exists
SELECT EXISTS (
  SELECT 1 
  FROM experiment.experiments 
  WHERE project_key = 'test-project' 
  AND name = 'test-experiment-name'
);
```

- Returns `true` if name exists (not available)
- Returns `false` if name doesn't exist (available)

## Troubleshooting

### Application won't start
- Check PostgreSQL is running: `pg_isready`
- Check database connection credentials
- Verify schema is created in the database
- Check application logs for errors

### Getting 500 errors
- Check application logs for detailed error messages
- Verify database connection is working
- Check if the `experiment.experiments` table exists
- Verify the query is correct in the logs

### Getting connection refused
- Verify application started successfully
- Check if port 8080 is available
- Check firewall settings

## Using Postman or Similar Tools

1. **Method:** GET
2. **URL:** `http://localhost:8080/v1/experiments/name-availability`
3. **Headers:**
   - `x-project-key`: `test-project`
   - `Content-Type`: `application/json`
4. **Query Parameters:**
   - `name`: `your-experiment-name`

## Quick Test Script

Save this as `test-api.sh`:

```bash
#!/bin/bash

BASE_URL="http://localhost:8080"
PROJECT_KEY="test-project"

echo "Testing Name Availability API..."
echo ""

# Test 1: Available name
echo "Test 1: Available name"
curl -s -X GET "${BASE_URL}/v1/experiments/name-availability?name=new-experiment" \
  -H "x-project-key: ${PROJECT_KEY}" | jq .
echo ""

# Test 2: Missing header
echo "Test 2: Missing project key header"
curl -s -X GET "${BASE_URL}/v1/experiments/name-availability?name=test" | jq .
echo ""

# Test 3: Missing query param
echo "Test 3: Missing name query parameter"
curl -s -X GET "${BASE_URL}/v1/experiments/name-availability" \
  -H "x-project-key: ${PROJECT_KEY}" | jq .
echo ""
```

Make it executable and run:
```bash
chmod +x test-api.sh
./test-api.sh
```

