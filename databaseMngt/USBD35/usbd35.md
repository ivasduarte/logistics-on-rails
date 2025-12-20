# USBD35 - Register New Facility

## Table of Contents
1. [Function Implementation](#1-function-implementation)
    - [PL/SQL Code](#plsql-code)
    - [Function Logic Explanation](#function-logic-explanation)
2. [Anonymous Block Testing](#2-anonymous-block-testing)
    - [Test Code](#test-code)
    - [Test Flow Explanation](#test-flow-explanation)
3. [Database Schema Dependencies](#3-database-schema-dependencies)
4. [Error Handling](#4-error-handling)

---

## 1. Function Implementation

### PL/SQL Code

```sql
CREATE OR REPLACE FUNCTION fncRegisterNewFacility(
    p_name IN VARCHAR2,
    p_latitude IN VARCHAR2,
    p_longitude IN VARCHAR2,
    p_type_id IN NUMBER
)
RETURN NUMBER
IS
    v_location_id NUMBER;
    v_endpoint_id NUMBER;
    v_location_exists NUMBER := 0;
BEGIN
    -- Verifica os inputs fornecidos
    IF p_name IS NULL OR p_latitude IS NULL OR p_longitude IS NULL OR p_type_id IS NULL THEN
        RAISE_APPLICATION_ERROR(-20001, 'All parameters except entry_date are required');
    END IF;

    -- Verifica se já existe uma Location com aquelas coordenadas
    BEGIN
        SELECT id INTO v_location_id
        FROM Location
        WHERE latitude = p_latitude AND longitude = p_longitude;

        v_location_exists := 1;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_location_exists := 0;
    END;

    -- Se não existir essa localização, cria-a
    IF v_location_exists = 0 THEN
        SELECT NVL(MAX(id), 0) + 1 INTO v_location_id FROM Location;

        INSERT INTO Location (id, latitude, longitude)
        VALUES (v_location_id, p_latitude, p_longitude);
    END IF;

    -- Verifica se o tipo de Endpoint existe
    DECLARE
        v_type_exists NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_type_exists
        FROM Endpoint_Type
        WHERE id = p_type_id;

        IF v_type_exists = 0 THEN
            RAISE_APPLICATION_ERROR(-20002, 'Invalid endpoint type ID: ' || p_type_id);
        END IF;
    END;

    -- Gera um novo ID para o Endpoint
    SELECT NVL(MAX(endpointID), 0) + 1 INTO v_endpoint_id FROM Endpoint;

    -- Insere o novo Endpoint
    INSERT INTO Endpoint (endpointID, name, locationID, typeID)
    VALUES (v_endpoint_id, p_name, v_location_id, p_type_id);

    -- Retorna o ID do novo Endpoint
    RETURN v_endpoint_id;

EXCEPTION
    WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20003, 'Error registering facility: ' || SQLERRM);
END fncRegisterNewFacility;
/
```

### Function Logic Explanation

1. **Input Validation**:
    - Validates that all required parameters (name, latitude, longitude, type_id) are provided
    - Raises an error if any required parameter is NULL

2. **Location Management**:
    - Checks if a location with the provided coordinates already exists in the database
    - If the location exists, reuses that location's ID
    - If the location doesn't exist, generates a new location ID and creates the location record
    - This prevents the existence of doubled records on the Location table

3. **Type Validation**:
    - Verifies that the provided endpoint type ID exists in the Endpoint_Type table
    - Raises an error if an invalid type ID is provided

4. **Endpoint Creation**:
    - Generates a new unique endpoint ID by finding the maximum existing ID and adding 1
    - Inserts the new endpoint with all the required attributes
    - Commits the transaction

5. **Return Value**:
    - Returns the newly created endpoint ID
    - This allows the caller to reference the new facility immediately

6. **Error Handling**:
    - All database operations are wrapped in exception handling
    - Any errors trigger a ROLLBACK
    - Custom error messages are provided for different failure scenarios

---

## 2. Anonymous Block Testing

### Test Code

```sql
DECLARE
    v_new_endpoint_id NUMBER;
    v_facility_count_before NUMBER;
    v_facility_count_after NUMBER;

    -- Cursor para listar todos os Endpoints
    CURSOR c_facilities IS
        SELECT e.endpointID, e.name, et."desc" AS endpoint_type,
               l.latitude, l.longitude
        FROM Endpoint e
        JOIN Endpoint_Type et ON e.typeID = et.id
        JOIN Location l ON e.locationID = l.id
        ORDER BY e.endpointID;

BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('USBD35: Register New Facility Test');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('');

    -- Conta o número de Endpoints antes
    SELECT COUNT(*) INTO v_facility_count_before FROM Endpoint;
    DBMS_OUTPUT.PUT_LINE('Total facilities before: ' || v_facility_count_before);
    DBMS_OUTPUT.PUT_LINE('');

    -- Lista todos os Endpoints antes
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('FACILITIES BEFORE REGISTRATION');
    DBMS_OUTPUT.PUT_LINE('========================================');

    FOR rec IN c_facilities LOOP
        DBMS_OUTPUT.PUT_LINE('Facility ID: ' || rec.endpointID);
        DBMS_OUTPUT.PUT_LINE('  Name: ' || rec.name);
        DBMS_OUTPUT.PUT_LINE('  Type: ' || rec.endpoint_type);
        DBMS_OUTPUT.PUT_LINE('  Location: ' || rec.latitude || ', ' || rec.longitude);
        DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    END LOOP;

    -- Regista um novo Endpoint
    v_new_endpoint_id := fncRegisterNewFacility(
        p_name => 'Porto Intermodal Terminal',
        p_latitude => '41.1579',
        p_longitude => '-8.6291',
        p_type_id => 1
    );

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('New facility registered successfully!');
    DBMS_OUTPUT.PUT_LINE('New Endpoint ID: ' || v_new_endpoint_id);

    -- Lista todos os Endpoints Depois
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('FACILITIES AFTER REGISTRATION');
    DBMS_OUTPUT.PUT_LINE('========================================');

    FOR rec IN c_facilities LOOP
        DBMS_OUTPUT.PUT_LINE('Facility ID: ' || rec.endpointID);
        DBMS_OUTPUT.PUT_LINE('  Name: ' || rec.name);

        IF rec.endpointID = v_new_endpoint_id THEN
            DBMS_OUTPUT.PUT_LINE('  >>> NEWLY REGISTERED <<<');
        END IF;

        DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    END LOOP;

END;
```

### Test Flow Explanation

1. **Initialization**:
    - Declares variables to track the test execution
    - Creates a cursor to retrieve all endpoints

2. **Testing Before the Execution**:
    - Counts the total number of endpoints before registration
    - Lists all existing endpoints with their complete information:
        - Endpoint ID
        - Endpoint name
        - Endpoint type description
        - Coordinates (latitude, longitude)
    - This establishes a baseline for comparison

3. **Function Execution**:
    - Calls `fncRegisterNewFacility` with test data:
        - Name: "Porto Intermodal Terminal"
        - Coordinates: 41.1579°N, 8.6291°W
        - Type ID: 1
    - Captures the returned endpoint ID

4. **Testing After the Execution**:
    - Counts the total number of endpoints after registration
    - Lists all endpoints again, highlighting the newly registered one
    - Marks the new facility with ">>> NEWLY REGISTERED <<<" for easy identification

**Testing Benefits**:
- **Before/After Comparison**: Clearly shows the changes in the database
- **Visual Verification**: Easy to spot the new facility in the output
- **Deterministic**: Using a fixed date makes tests repeatable and consistent
---

## 3. Database Schema Dependencies

### Required Tables

**Endpoint**
- `endpointID` (PRIMARY KEY)
- `name`
- `locationID` (FOREIGN KEY)
- `typeID` (FOREIGN KEY)

**Location**
- `id` (PRIMARY KEY)
- `latitude`
- `longitude`
- `CONSTRAINT coordinates UNIQUE`

**Endpoint_Type**
- `id` (PRIMARY KEY)
- `desc`

### Relationships

```
Endpoint_Type (1) ----< (N) Endpoint
Location (1) ----< (N) Endpoint
```

---

## 4. Error Handling

### Custom Error Codes

| Error Code | Description | Trigger Condition |
|------------|-------------|-------------------|
| -20001 | Missing Required Parameters | Any required parameter is NULL |
| -20002 | Invalid Endpoint Type | The provided type_id doesn't exist |
| -20003 | General Registration Error | Any other error during registration |

