# USBD38 - As a Planner, I want to add a new gauge

## Table of Contents

1. [Function Implementation](#1-function-implementation)
   - [PL/SQL Code](#plsql-code)
   - [Function Logic Explanation](#function-logic-explanation)
2. [Anonymous Block Testing](#2-anonymous-block-testing)
   - [Success Test Code](#success-test-code)
   - [Error Test Code](#error-test-code)
   - [Test Flow Explanation](#test-flow-explanation)
3. [Database Schema Dependencies](#3-database-schema-dependencies)
4. [Error Handling](#4-error-handling)

---

## 1. Function Implementation

### PL/SQL Code

````

CREATE OR REPLACE FUNCTION fncAddGauge (
    p_gaugeNumber IN NUMBER
)
RETURN NUMBER
IS
    v_count NUMBER;
BEGIN
    IF p_gaugeNumber IS NULL THEN
        RAISE_APPLICATION_ERROR(-20010,'Gauge number is required');
    END IF;
   
    IF p_gaugeNumber <= 0 THEN
        RAISE_APPLICATION_ERROR(-20012,'Gauge number must be greater than 0');
    END IF;

SELECT COUNT(*);
INTO v_count;
FROM GAUGE
WHERE GAUGENUMBER = p_gaugeNumber;

IF v_count > 0 THEN
    RAISE_APPLICATION_ERRROR(-20011,'Gauge already exists');
END IF;

INSERT INTO GAUGE(GAUGENUMBER)
VALUES(p_gaugeNumber);

RETURN p_gaugeNumber;

EXCEPTION
   WHEN OTHERS THEN
      RAISE;
END fncAddGauge;
/
```
````

### Function Logic Explanation

1. Input Validation

   - Validates that the p_gaugeNumber parameter is provided.
   - Raises an error if any required parameter is NULL.
2. Value Validation

   - Ensures that the gauge number is a positive value.
   - If p_gaugeNumber is less than or equal to zero, error -20012 is raised.
   - This enforces domain rules for valid gauge values.
3. Duplicate Validation

   - Checks if a gauge with the same number already exists in the GAUGE table.
   - Uses a COUNT(*) query to detect duplicates.
   - If a duplicate is found, error -20011 is raised.
   - This guarantees gauge uniqueness at the application level.
4. Insert operation

   - Inserts the new gauge number into the GAUGE table once all validations pass.
5. Return Value

   - Returns the inserted gauge number.
   - This allows the caller to immediately identify and confirm the created record.
6. Error Handling

   - Any unexpected exception is re-raised.
   - This preserves the original Oracle error and ensures transparency during failures.

---

## 2. Anonymous Block Testing

### Success Test Code

````sql
SET SERVEROUTPUT ON;

DECLARE
    v_new_gauge NUMBER;
    v_count_before NUMBER;
    v_count_after  NUMBER;

    CURSOR c_gauges IS
        SELECT GAUGENUMBER
        FROM GAUGE
        ORDER BY GAUGENUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('USBD38: ADD GAUGE TEST');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('');

    SELECT COUNT(*) INTO v_count_before FROM GAUGE;
    DBMS_OUTPUT.PUT_LINE('Total gauges before: ' || v_count_before);
    DBMS_OUTPUT.PUT_LINE('');

    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('GAUGES BEFORE INSERTION');
    DBMS_OUTPUT.PUT_LINE('========================================');
    FOR rec IN c_gauges LOOP
        DBMS_OUTPUT.PUT_LINE('Gauge: ' || rec.GAUGENUMBER);
        DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    END LOOP;

    v_new_gauge := fncAddGauge(1000);

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('New gauge inserted successfully!');
    DBMS_OUTPUT.PUT_LINE('New Gauge Number: ' || v_new_gauge);

    SELECT COUNT(*) INTO v_count_after FROM GAUGE;
    DBMS_OUTPUT.PUT_LINE('Total gauges after: ' || v_count_after);
    DBMS_OUTPUT.PUT_LINE('');

    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('GAUGES AFTER INSERTION');
    DBMS_OUTPUT.PUT_LINE('========================================');

    FOR rec IN c_gauges LOOP
        DBMS_OUTPUT.PUT_LINE('Gauge: ' || rec.GAUGENUMBER);

        IF rec.GAUGENUMBER = v_new_gauge THEN
            DBMS_OUTPUT.PUT_LINE('  >>> NEWLY INSERTED <<<');
        END IF;

        DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    END LOOP;

    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('Test complete. Changes rolled back.');
END;
/

```
````

### Error Test Code

```sql
SET SERVEROUTPUT ON;

DECLARE
    v_result_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('USBD38: Testing Error Scenarios');
    DBMS_OUTPUT.PUT_LINE('========================================');

    -- TEST 1: NULL Gauge (Expected error -20010)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 1: Testing NULL Gauge');
        v_result_id := fncAddGauge(
            p_gaugeNumber => NULL
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- TEST 2: Invalid Gauge (<= 0) (Expected error -20012)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 2: Testing Invalid Gauge (0)');
        v_result_id := fncAddGauge(
            p_gaugeNumber => 0
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- TEST 3: Existing Gauge (Expected error -20011)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 3: Testing Existing Gauge (1668)');
        v_result_id := fncAddGauge(
            p_gaugeNumber => 1668
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    DBMS_OUTPUT.PUT_LINE('Error testing complete.');
END;
/
```

### Test Flow Explanation

#### Success Test Flow

1. **Initialization**:
   - Declares variables to track gauge counts before and after execution.
   - Defines a cursor to list all gauges ordered by gauge number.
2. **Testing Before the Execution**:
   - Counts total gauges before insertion.
   - Lists all existing gauges to establish a baseline.
3. **Function Execution**:
   - Calls `fncAddGauge(1000)`.
   - Stores the returned gauge number.
4. **Testing After the Execution**
   - Counts gauges after insertion.
   - Lists all gauges again and highlights the newly inserted one.
   - Marks the new gauge with ">>> NEWLY REGISTERED <<<" for easy identification.
5. **Cleanup**:
   - Executes ROLLBACK to ensure no test data persists in the database.

#### Error Test Flow

1. **Scenario 1 - NULL Gauge**:

   - Tests mandatory parameter validation.
   - Expected: Error -20010.
2. **Scenario 2 - Invalid Gauge (0)**:

   - Tests domain validation.
   - Expected: Error -20012.
3. **Scenario 3 -Duplicate Gauge**:

   - Tests uniqueness validation.
   - Expected: Error -20011.
4. **Cleanup**:

   - Rolls back any partial changes.
   - Ensures database remains clean after error tests.

#### Testing Benefits

- **Before/After Comparison**: Clearly shows the changes in the database.
- **Visual Verification**: Easy to spot the new gague.
- **Comprehensive Error Coverage**: Tests all validation points.
- **Safe Testing**: Rollback ensures no persistent changes to the database.

---

## 3. Database Schema Dependencies

### Required Tables

**GAUGE:**

* `GAUGENUMBER` (Primary Key)

---

## 4. Error Handling

### Custom Error Codes


| Error Code | Description          | Trigger Condition       |
| ---------- | -------------------- | ----------------------- |
| -20010     | Missing Gauge Number | `p_gaugeNumber is NULL` |
| -20012     | Invalid Gauge Value  | `p_gaugeNumber <= 0`    |
| -20011     | Duplicated Gauge     | Gauge already exists    |
|            |                      |                         |

### Error Handling Strategy

1. **Input validation** first to avoid unnecessary database operations.
2. **Business rule enforcement** through controlled exceptions.
3. **Safe testing** using rollback to avoid persistent data changes.
4. **Clear and informative messages** to simplify debugging and evaluation.
