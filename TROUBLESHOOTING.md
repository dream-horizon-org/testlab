# Troubleshooting Guide for Name Availability API

## Error: "2 exceptions occurred" / "UNKNOWN_EXCEPTION"

This error typically indicates a database connection or query execution issue. Follow these steps:

### Step 1: Check Application Logs

Look for detailed error messages in your application logs. The error should show the root cause.

### Step 2: Verify Database Connection

1. **Check if PostgreSQL is running:**
   ```bash
   pg_isready
   # or
   psql -U <username> -d <database> -c "SELECT 1;"
   ```

2. **Verify database credentials:**
   - Make sure `PG_USER` and `PG_PASSWORD` environment variables are set correctly
   - Check if the user has proper permissions

### Step 3: Verify Database Schema

The API requires the `experiment.experiments` table to exist. Check if it exists:

```sql
-- Connect to your database
psql -U <username> -d <database>

-- Check if schema exists
\dn experiment

-- Check if table exists
\dt experiment.experiments

-- If table doesn't exist, create it by running:
-- source src/main/resources/db/postgresql/schema.sql
```

### Step 4: Test the Query Directly

Test the query that the API uses:

```sql
-- This is the query the API executes
SELECT EXISTS (
  SELECT 1 
  FROM experiment.experiments 
  WHERE project_key = 'test-project' 
  AND name = 'test-name'
);
```

If this query fails, you'll see the exact error message.

### Step 5: Common Issues and Solutions

#### Issue 1: Table doesn't exist
**Error:** `relation "experiment.experiments" does not exist`

**Solution:**
```bash
# Run the schema file
psql -U <username> -d <database> -f src/main/resources/db/postgresql/schema.sql
```

#### Issue 2: Schema doesn't exist
**Error:** `schema "experiment" does not exist`

**Solution:**
```sql
CREATE SCHEMA IF NOT EXISTS experiment;
```

#### Issue 3: Connection refused
**Error:** `Connection refused` or `Connection timeout`

**Solution:**
- Verify PostgreSQL is running: `pg_isready`
- Check PostgreSQL is listening on the correct port (default: 5432)
- Verify firewall settings
- Check PostgreSQL configuration (`postgresql.conf` and `pg_hba.conf`)

#### Issue 4: Authentication failed
**Error:** `password authentication failed`

**Solution:**
- Verify `PG_USER` and `PG_PASSWORD` are correct
- Check PostgreSQL user permissions
- Verify `pg_hba.conf` allows connections from your host

#### Issue 5: Partition doesn't exist
**Error:** `relation "experiment.experiments" does not exist` (but table exists)

**Solution:**
The `experiments` table is partitioned. You may need to create a partition for your project key:

```sql
-- Create partition for your project key
CREATE TABLE IF NOT EXISTS experiment.experiments_p_test 
PARTITION OF experiment.experiments 
FOR VALUES IN ('test-project');
```

### Step 6: Enable Detailed Logging

Add more logging to see what's happening. Check your `logback.xml` configuration and ensure error logs are enabled.

### Step 7: Test with a Simple Query First

Before testing the API, verify the database connection works with a simple query:

```sql
-- Test basic connection
SELECT 1;

-- Test schema access
SELECT * FROM experiment.experiments LIMIT 1;
```

### Step 8: Check Application Configuration

Verify your application is configured correctly:

1. **Check environment variables:**
   ```bash
   echo $PG_USER
   echo $PG_PASSWORD
   ```

2. **Check application config files:**
   - Look for PostgreSQL configuration in `src/main/resources/config/`
   - Verify connection settings match your database

### Step 9: Manual Database Setup

If the table doesn't exist, set it up manually:

```bash
# 1. Connect to PostgreSQL
psql -U <username> -d <database>

# 2. Create schema (if needed)
CREATE SCHEMA IF NOT EXISTS experiment;

# 3. Create types (if needed)
CREATE TYPE experiment_status AS ENUM ('LIVE','PAUSED','DRAFT','CONCLUDED','TERMINATED');
CREATE TYPE experiment_type AS ENUM ('A/B');
CREATE TYPE experiment_health AS ENUM ('WARNING','PASSING','NO_CHECKS_AVAILABLE','FAILED');
CREATE TYPE experiment_strategy AS ENUM ('RANDOM', 'ROUND_ROBIN');

# 4. Create table
CREATE TABLE IF NOT EXISTS experiment.experiments (
    project_key          VARCHAR(255) NOT NULL,
    experiment_id       VARCHAR(36) NOT NULL,
    name                VARCHAR(64) NOT NULL,
    description         VARCHAR(255),
    hypothesis          TEXT,
    status              experiment_status NOT NULL,
    type                experiment_type,
    guardrail_health_status experiment_health,
    cohorts             VARCHAR(255) ARRAY,
    variant_weights     JSONB,
    assignment_strategy experiment_strategy,
    overrides           JSONB,
    rule_attributes     JSONB,
    winning_variant     JSONB,
    exposure            INTEGER,
    threshold           bigint,
    start_time          bigint,
    end_time            bigint,
    created_by          VARCHAR(255),
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    name_tsvector       TSVECTOR,
    PRIMARY KEY ( project_key, experiment_id),
    CONSTRAINT name_unique_check UNIQUE (project_key, name)
) PARTITION BY LIST (project_key);

# 5. Create a test partition
CREATE TABLE IF NOT EXISTS experiment.experiments_p_test 
PARTITION OF experiment.experiments 
FOR VALUES IN ('test-project');
```

### Step 10: Test the Fixed API

After fixing the error handling, rebuild and test:

```bash
# Rebuild
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
mvn clean package

# Restart application
cd target/testlab
PG_USER=<username> \
PG_PASSWORD=<password> \
java -Dapp.environment=local \
     -Dlogback.configurationFile=./resources/logback/logback.xml \
     -jar testlab-1.0-fat.jar

# Test in another terminal
curl -X GET "http://localhost:8080/v1/experiments/name-availability?name=test" \
  -H "x-project-key: test-project"
```

## Getting More Information

If you're still getting errors, check:

1. **Application logs** - Look for stack traces
2. **Database logs** - Check PostgreSQL logs for connection/query errors
3. **Network connectivity** - Verify you can reach the database
4. **Database permissions** - Ensure the user can SELECT from the table

## Expected Behavior After Fix

After the error handling fix, you should see:
- **Proper error messages** instead of "UNKNOWN_EXCEPTION"
- **Detailed error information** in application logs
- **Correct HTTP status codes** (500 for server errors, 400 for client errors)

