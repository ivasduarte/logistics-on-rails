# USBD45 - Add New Line

## Table of Contents

1. [Function Implementation](#1-function-implementation)
    - [PL/SQL Code](#plsql-code)
    - [Function Logic Explanation](#function-logic-explanation)
2. [Anonymous Block Testing](#2-anonymous-block-testing)
    - [Success Test Code](#success-test-code)
    - [Test Flow Explanation](#test-flow-explanation)
3. [Error Scenario Testing](#3-error-scenario-testing)
4. [Database Schema Dependencies](#4-database-schema-dependencies)
5. [Error Handling](#5-error-handling)

---

## 1. Function Implementation

### PL/SQL Code

```sql
CREATE OR REPLACE FUNCTION fncAddNewLine(
    p_line_name IN VARCHAR2,
    p_owner_id IN VARCHAR2,
    p_start_endpoint_id IN NUMBER,
    p_end_endpoint_id IN NUMBER,
    p_gauge IN NUMBER,
    p_segment_length IN NUMBER,
    p_segment_max_weight IN FLOAT,
    p_line_type_id IN NUMBER
)
RETURN NUMBER
IS
    v_line_name_count NUMBER;
    v_owner_count NUMBER;
    v_start_endpoint_count NUMBER;
    v_end_endpoint_count NUMBER;
    v_gauge_count NUMBER;
    v_line_type_count NUMBER;
    v_next_line_id NUMBER;
    v_next_segment_id NUMBER;
BEGIN
    -- 1. Validação de Inputs Obrigatórios
    IF p_line_name IS NULL OR p_owner_id IS NULL OR p_start_endpoint_id IS NULL
       OR p_end_endpoint_id IS NULL OR p_gauge IS NULL OR p_segment_length IS NULL
       OR p_segment_max_weight IS NULL OR p_line_type_id IS NULL THEN
        RAISE_APPLICATION_ERROR(-20001, 'All parameters are required');
    END IF;

    -- 2. Valida se já existe uma Rail_Line com aquele nome
    SELECT COUNT(*) INTO v_line_name_count
    FROM Rail_Line
    WHERE name = p_line_name;

    IF v_line_name_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Rail Line with name ' || p_line_name || ' already exists');
    END IF;

    -- 3. Valida se o Owner existe
    SELECT COUNT(*) INTO v_owner_count
    FROM Owner
    WHERE shortName = p_owner_id;

    IF v_owner_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20003, 'Owner with ID ' || p_owner_id || ' does not exist');
    END IF;

    -- 4. Valida se o Start Endpoint existe
    SELECT COUNT(*) INTO v_start_endpoint_count
    FROM Endpoint
    WHERE endpointID = p_start_endpoint_id;

    IF v_start_endpoint_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20004, 'Start Endpoint with ID ' || p_start_endpoint_id || ' does not exist');
    END IF;

    -- 5. Valida se o End Endpoint existe
    SELECT COUNT(*) INTO v_end_endpoint_count
    FROM Endpoint
    WHERE endpointID = p_end_endpoint_id;

    IF v_end_endpoint_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20005, 'End Endpoint with ID ' || p_end_endpoint_id || ' does not exist');
    END IF;

    -- 6. Valida se o Gauge existe
    SELECT COUNT(*) INTO v_gauge_count
    FROM Gauge
    WHERE gaugeNumber = p_gauge;

    IF v_gauge_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20006, 'Gauge with number ' || p_gauge || ' does not exist');
    END IF;

    -- 7. Valida se o Line_Type existe
    SELECT COUNT(*) INTO v_line_type_count
    FROM Line_Type
    WHERE id = p_line_type_id;

    IF v_line_type_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20007, 'Line Type with ID ' || p_line_type_id || ' does not exist');
    END IF;

    -- 8. Calcular o próximo ID da Rail_Line
    SELECT NVL(MAX(id), 0) + 1 INTO v_next_line_id FROM Rail_Line;

    -- 9. Inserir a nova Rail_Line
    INSERT INTO Rail_Line (id, name, ownerID, startID, endID, gauge)
    VALUES (v_next_line_id, p_line_name, p_owner_id, p_start_endpoint_id, p_end_endpoint_id, p_gauge);

    -- 10. Calcular o próximo ID do Line_Segment
    SELECT NVL(MAX(id), 0) + 1 INTO v_next_segment_id FROM Line_Segment;

    -- 11. Inserir o primeiro Line_Segment (orderPosition = 1)
    INSERT INTO Line_Segment (id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
    VALUES (v_next_segment_id, v_next_line_id, 1, p_segment_max_weight, p_segment_length, p_line_type_id);

    -- 12. Finalizar Transação
    COMMIT;

    RETURN v_next_line_id;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE; -- Propaga o erro original para o utilizador ver o que falhou
END fncAddNewLine;
/
```

### Function Logic Explanation

1. **Input Validation**:
    - Validates that both required parameters (line_number and segment_id) are provided
    - Raises error -20001 if any parameter is NULL
    - This enforces the business rule that a line must have at least one segment

2. **Line Number Uniqueness Check**:
    - Verifies that no existing line has the same line number
    - Line numbers must be unique across the system
    - Raises error -20002 if a duplicate line number is detected
    - This prevents confusion and maintains data integrity

3. **Segment Existence Validation**:
    - Checks if the specified segment exists in the database
    - Ensures referential integrity before creating associations
    - Raises error -20003 if the segment does not exist
    - This prevents orphaned line-segment relationships

4. **Line ID Generation**:
    - Calculates the next Primary Key ID manually using MAX(lineID) + 1
    - Uses NVL to handle the case when no lines exist yet (starts at 1)
    - This approach is used because sequences are not defined in the schema

5. **Line Creation**:
    - Inserts the new line record with the generated ID and provided line number
    - The line table contains minimal information (ID and number)
    - Additional line details are managed through related tables

6. **Line-Segment Association**:
    - Creates the relationship between the line and segment
    - Uses the segment_line junction table to establish the many-to-many relationship
    - This satisfies the business rule that a line must have at least one segment
    - Allows for future expansion where lines can have multiple segments

7. **Return Value**:
    - Returns the newly created line ID
    - Enables the caller to immediately reference or extend the new line
    - Facilitates subsequent operations like adding more segments

8. **Error Handling**:
    - All database operations are wrapped in exception handling
    - Custom error messages provide context for different failure scenarios
    - The EXCEPTION WHEN OTHERS block catches unexpected errors
    - All errors trigger automatic rollback to maintain database consistency

---

## 2. Anonymous Block Testing

### Success Test Code

```sql
DECLARE
    v_new_line_id NUMBER;

    v_test_line_name CONSTANT VARCHAR2(255) := 'Test Line Porto-Braga';
    v_test_owner_id CONSTANT VARCHAR2(255) := 'IP'; -- Owner válido (Infraestruturas de Portugal)
    v_test_start_endpoint CONSTANT NUMBER := 14; -- Ermesinde (endpoint válido)
    v_test_end_endpoint CONSTANT NUMBER := 18; -- Famalicão (endpoint válido)
    v_test_gauge CONSTANT NUMBER := 1668; -- Gauge comum em Portugal (válido)
    v_test_segment_length CONSTANT NUMBER := 5000;
    v_test_segment_max_weight CONSTANT FLOAT := 8000;
    v_test_line_type CONSTANT NUMBER := 1; -- Line Type válido (Electric, 4 tracks)

    CURSOR c_rail_lines IS
        SELECT rl.id, rl.name, rl.ownerID, rl.gauge,
               e_start.name AS start_endpoint,
               e_end.name AS end_endpoint
        FROM Rail_Line rl
        JOIN Endpoint e_start ON rl.startID = e_start.endpointID
        JOIN Endpoint e_end ON rl.endID = e_end.endpointID
        ORDER BY rl.id;

    CURSOR c_line_segments(p_line_id NUMBER) IS
        SELECT ls.id, ls.orderPosition, ls.length, ls.max_weight, ls.Line_TypeID
        FROM Line_Segment ls
        WHERE ls.Rail_LineId = p_line_id
        ORDER BY ls.orderPosition;

BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('USBD45: Add New Rail Line Test');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('');

    -- 1. Estado Antes da Inserção
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('RAIL LINES BEFORE INSERTION');
    DBMS_OUTPUT.PUT_LINE('========================================');

    FOR rec IN c_rail_lines LOOP
        DBMS_OUTPUT.PUT_LINE('Line ID: ' || rec.id);
        DBMS_OUTPUT.PUT_LINE('  Name: ' || rec.name);
        DBMS_OUTPUT.PUT_LINE('  Owner: ' || rec.ownerID);
        DBMS_OUTPUT.PUT_LINE('  Route: ' || rec.start_endpoint || ' -> ' || rec.end_endpoint);
        DBMS_OUTPUT.PUT_LINE('  Gauge: ' || rec.gauge);
        DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    END LOOP;

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('========================================');

    -- 2. Executar a Função
    DBMS_OUTPUT.PUT_LINE('Attempting to add new Rail Line: ' || v_test_line_name);
    DBMS_OUTPUT.PUT_LINE('With first segment (Length: ' || v_test_segment_length || 'm)...');

    v_new_line_id := fncAddNewLine(
        p_line_name => v_test_line_name,
        p_owner_id => v_test_owner_id,
        p_start_endpoint_id => v_test_start_endpoint,
        p_end_endpoint_id => v_test_end_endpoint,
        p_gauge => v_test_gauge,
        p_segment_length => v_test_segment_length,
        p_segment_max_weight => v_test_segment_max_weight,
        p_line_type_id => v_test_line_type
    );

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('Function executed successfully!');
    DBMS_OUTPUT.PUT_LINE('Return value (New Rail Line ID): ' || v_new_line_id);

    -- 3. Estado Depois da Inserção
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('RAIL LINES AFTER INSERTION');
    DBMS_OUTPUT.PUT_LINE('========================================');

    FOR rec IN c_rail_lines LOOP
        DBMS_OUTPUT.PUT_LINE('Line ID: ' || rec.id);
        DBMS_OUTPUT.PUT_LINE('  Name: ' || rec.name);
        DBMS_OUTPUT.PUT_LINE('  Owner: ' || rec.ownerID);
        DBMS_OUTPUT.PUT_LINE('  Route: ' || rec.start_endpoint || ' -> ' || rec.end_endpoint);

        IF rec.id = v_new_line_id THEN
            DBMS_OUTPUT.PUT_LINE('  >>> NEWLY CREATED LINE <<<');

            DBMS_OUTPUT.PUT_LINE('  Line Segments:');
            FOR seg IN c_line_segments(rec.id) LOOP
                DBMS_OUTPUT.PUT_LINE('    Segment ID: ' || seg.id || ' | Order: ' || seg.orderPosition || ' | Length: ' || seg.length || 'm');
                DBMS_OUTPUT.PUT_LINE('    >>> FIRST SEGMENT CREATED <<<');
            END LOOP;
        END IF;

        DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    END LOOP;

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('Test Status: SUCCESS');
    DBMS_OUTPUT.PUT_LINE('========================================');

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('');
        DBMS_OUTPUT.PUT_LINE('========================================');
        DBMS_OUTPUT.PUT_LINE('ERROR DURING TEST');
        DBMS_OUTPUT.PUT_LINE('========================================');
        DBMS_OUTPUT.PUT_LINE('Error Code: ' || SQLCODE);
        DBMS_OUTPUT.PUT_LINE('Error Message: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE('Test Status: FAILED');
        DBMS_OUTPUT.PUT_LINE('========================================');
        ROLLBACK; -- Garante que o teste não suja a BD se der erro fatal
END;
/
```

### Test Flow Explanation

1. **Initialization**:
    - Declares variables to track test execution
    - Sets a test segment ID (must be a valid segment in the database)
    - Creates cursors to retrieve lines and their associated segments

2. **Pre-Addition State**:
    - Counts the total number of lines before the operation
    - Lists all existing lines with their information
        - Line ID
        - Line number
        - Count of associated segments
    - Establishes a baseline for comparison

3. **Function Execution**:
    - Calls fncAddNewLine with test data:
        - Line number: 'L-TEST-001'
        - Segment ID: 1 (or another valid segment)
    - Captures the returned line ID

4. **Post-Addition State**:
    - Counts the total number of lines after the operation
    - Re-lists all lines
    - Highlights the newly created line with ">>> NEWLY ADDED <<<"
    - Shows the segment association for the new line
    - Displays segment details (ID and name)

5. **Cleanup**:
    - Rolls back all changes to maintain database integrity
    - Ensures test data doesn't persist
    - Allows the test to be run multiple times

### Testing Benefits

- **Before/After Comparison**: Clearly demonstrates the database state changes
- **Visual Verification**: Easy identification of the new line and its segment
- **Association Validation**: Confirms the line-segment relationship was created
- **Safe Testing**: Rollback ensures no persistent changes

---

## 3. Error Scenario Testing

### Error Test Code

```sql
DECLARE
    v_result NUMBER;

    v_valid_owner_id CONSTANT VARCHAR2(255) := 'IP';
    v_valid_start_endpoint CONSTANT NUMBER := 14; -- Ermesinde
    v_valid_end_endpoint CONSTANT NUMBER := 18; -- Famalicão
    v_valid_gauge CONSTANT NUMBER := 1668;
    v_valid_line_type CONSTANT NUMBER := 1;
    v_invalid_id CONSTANT NUMBER := 99999; -- ID que não existe

BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('USBD45: Testing Error Scenarios');
    DBMS_OUTPUT.PUT_LINE('========================================');

    -- Teste 1: Parâmetro Line Name Nulo (Esperado erro -20001)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 1: Testing NULL Line Name');
        v_result := fncAddNewLine(
            p_line_name => NULL, -- Esta linha causa erro
            p_owner_id => v_valid_owner_id,
            p_start_endpoint_id => v_valid_start_endpoint,
            p_end_endpoint_id => v_valid_end_endpoint,
            p_gauge => v_valid_gauge,
            p_segment_length => 5000,
            p_segment_max_weight => 8000,
            p_line_type_id => v_valid_line_type
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- Teste 2: Nome de Linha Duplicado (Esperado erro -20002)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 2: Testing Duplicate Line Name');
        v_result := fncAddNewLine(
            p_line_name => 'Ramal São Bento - Campanhã', -- Nome que já existe
            p_owner_id => v_valid_owner_id,
            p_start_endpoint_id => v_valid_start_endpoint,
            p_end_endpoint_id => v_valid_end_endpoint,
            p_gauge => v_valid_gauge,
            p_segment_length => 5000,
            p_segment_max_weight => 8000,
            p_line_type_id => v_valid_line_type
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- Teste 3: Owner ID inexistente (Esperado erro -20003)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 3: Testing Non-existent Owner ID');
        v_result := fncAddNewLine(
            p_line_name => 'Test Line Invalid Owner',
            p_owner_id => 'INVALID', -- Owner que não existe
            p_start_endpoint_id => v_valid_start_endpoint,
            p_end_endpoint_id => v_valid_end_endpoint,
            p_gauge => v_valid_gauge,
            p_segment_length => 5000,
            p_segment_max_weight => 8000,
            p_line_type_id => v_valid_line_type
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- Teste 4: Start Endpoint inexistente (Esperado erro -20004)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 4: Testing Non-existent Start Endpoint');
        v_result := fncAddNewLine(
            p_line_name => 'Test Line Invalid Start',
            p_owner_id => v_valid_owner_id,
            p_start_endpoint_id => v_invalid_id, -- Endpoint que não existe
            p_end_endpoint_id => v_valid_end_endpoint,
            p_gauge => v_valid_gauge,
            p_segment_length => 5000,
            p_segment_max_weight => 8000,
            p_line_type_id => v_valid_line_type
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- Teste 5: End Endpoint inexistente (Esperado erro -20005)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 5: Testing Non-existent End Endpoint');
        v_result := fncAddNewLine(
            p_line_name => 'Test Line Invalid End',
            p_owner_id => v_valid_owner_id,
            p_start_endpoint_id => v_valid_start_endpoint,
            p_end_endpoint_id => v_invalid_id, -- Endpoint que não existe
            p_gauge => v_valid_gauge,
            p_segment_length => 5000,
            p_segment_max_weight => 8000,
            p_line_type_id => v_valid_line_type
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- Teste 6: Gauge inexistente (Esperado erro -20006)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 6: Testing Non-existent Gauge');
        v_result := fncAddNewLine(
            p_line_name => 'Test Line Invalid Gauge',
            p_owner_id => v_valid_owner_id,
            p_start_endpoint_id => v_valid_start_endpoint,
            p_end_endpoint_id => v_valid_end_endpoint,
            p_gauge => v_invalid_id, -- Gauge que não existe
            p_segment_length => 5000,
            p_segment_max_weight => 8000,
            p_line_type_id => v_valid_line_type
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- Teste 7: Line Type inexistente (Esperado erro -20007)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 7: Testing Non-existent Line Type');
        v_result := fncAddNewLine(
            p_line_name => 'Test Line Invalid Type',
            p_owner_id => v_valid_owner_id,
            p_start_endpoint_id => v_valid_start_endpoint,
            p_end_endpoint_id => v_valid_end_endpoint,
            p_gauge => v_valid_gauge,
            p_segment_length => 5000,
            p_segment_max_weight => 8000,
            p_line_type_id => v_invalid_id -- Line Type que não existe
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    DBMS_OUTPUT.PUT_LINE('Error testing complete. All transactions rolled back.');
END;
/
```

### Error Scenarios Explanation

1. **Scenario 1 - NULL Line Number**:
    - Tests input validation by passing NULL for line_number
    - Expected: Error -20001
    - Validates that line number is mandatory

2. **Scenario 2 - NULL Segment ID**:
    - Tests the business rule enforcement (line must have at least one segment)
    - Expected: Error -20001
    - Confirms that segment_id is required

3. **Scenario 3 - Duplicate Line Number**:
    - Creates a line successfully, then attempts to create another with the same number
    - Expected: Error -20002
    - Validates the uniqueness constraint on line numbers

4. **Scenario 4 - Non-existent Segment**:
    - Attempts to create a line with a segment ID that doesn't exist
    - Expected: Error -20003
    - Tests referential integrity validation

5. **Cleanup**:
    - Rolls back all test changes
    - Ensures database remains clean after error tests
    - Prevents accumulation of test data

---

## 4. Database Schema Dependencies

### Required Tables

**Rail_Line**:

- `lineID` (PRIMARY KEY)
- `"number"` (UNIQUE) - Note: "number" is a reserved word, hence the quotes

**Line_Segment**:

- `segmentID` (PRIMARY KEY)
- `name`
- Other attributes...

### Relationships

```
Rail_Line (1) ----< (N) Line_Segment
```

### Key Points

- This designs allows:
    - Lines to be composed of multiple segments
    - Segments to be only be used by one line
- The function ensures the minimum cardinality of one segment per line.

---

## 5. Error Handling

### Custom Error Codes

| Error Code | Description                 | Trigger Condition                                              |
|------------|-----------------------------|----------------------------------------------------------------|
| -20001     | Missing Required Parameters | Either line_number or segment_id is NULL                       |
| -20002     | Duplicate Line Number       | A line with the specified number already exists                |
| -20003     | Segment Not Found           | The specified `segment_id` does not exist in the segment table |
| -20004     | General Line Creation Error | Any other unexpected error during line creation or association |

### Error Recovery
All errors trigger an automatic **ROLLBACK**. This ensures:
   - **No Orphaned Lines**: A line is never created without its required segment
   - **No Partial Associations**: The line and segment_line records are created atomically
   - **Database Consistency**: Either the entire operation succeeds, or nothing persists

### Error Handling Strategy
1. **Input Validation**: First line of defense - validates all parameters before database operations
2. **Business Rule Enforcement**: Ensures line number uniqueness before attempting creation
3. **Referential Integrity**: Validates the segment exists before creating associations
4. **Atomic Operations**: Treats line creation and segment association as a single transaction
5. **Informative Messages**: Provides clear error messages with context (line numbers, segment IDs)
6. **Catch-All Handler**: Final EXCEPTION WHEN OTHERS block for unexpected errors

### Notes
- **Auto-Increment**: The function implements manual auto-increment (MAX(lineID) + 1) because the schema does not use Sequences or Identity columns
- **Reserved Word Handling**: The column name "number" requires quotes because "number" is a reserved word in Oracle SQL
- **Minimum Cardinality**: The function enforces that a line must have at least one segment, satisfying the business requirement
- **Atomicity**: Line creation and segment association are treated as a single atomic transaction
- **Extensibility**: Additional segments can be added to the line after creation using other functions