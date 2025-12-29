# USBD36 - Add Building to Facility

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
```sql
CREATE OR REPLACE FUNCTION fncAddBuildingToFacility(
    p_facility_id IN NUMBER,
    p_building_type_id IN NUMBER
)
RETURN NUMBER
IS
    v_building_id NUMBER;
    v_building_type_exists NUMBER;
    v_dummy NUMBER;
BEGIN
    IF p_facility_id IS NULL OR p_building_type_id IS NULL THEN
        RAISE_APPLICATION_ERROR(-20001, 'Facility ID and Building Type ID are required');
    END IF;

    BEGIN
        SELECT 1 INTO v_dummy
        FROM Endpoint
        WHERE endpointID = p_facility_id;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20002, 'Facility with ID ' || p_facility_id || ' does not exist');
    END;

    BEGIN
        SELECT COUNT(*) INTO v_building_type_exists
        FROM Building_Type
        WHERE id = p_building_type_id;

        IF v_building_type_exists = 0 THEN
            RAISE_APPLICATION_ERROR(-20003, 'Invalid building type ID: ' || p_building_type_id);
        END IF;
    END;

    SELECT NVL(MAX(buildingID), 0) + 1 INTO v_building_id FROM building;

    INSERT INTO building (buildingID, typeID)
    VALUES (v_building_id, p_building_type_id);

    INSERT INTO Endpoint_Building (EndpointendpointID, BuildingbuildingID)
    VALUES (p_facility_id, v_building_id);

    RETURN v_building_id;

EXCEPTION
    WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20004, 'Error adding building to facility: ' || SQLERRM);
END fncAddBuildingToFacility;
/
```

### Function Logic Explanation
1. Input Validation 
    - Validates that both required parameters (facility_id, building_type_id) are provided.
    - Raises an error if any required parameter is NULL.
2. Facility Validation
    - Checks if specified facility (endpoint) exists in the database.
    - Uses a SELECT INTO statement to verify existence.
    - Raises an error if the facility does not exist.
    - This ensures buildings are only added to valid facilities.
3. Building Type Validation
    - Verifies that the provided building type ID exists in the Building_Type table.
    - Raises an error if an invalid type ID is provided.
    - This maintains referential integrity.
4. Building Creation
    - Generates a new unique building ID by finding the maximum existing ID and adding 1.
    - Inserts the new building with the specified type.
    - The building table contains  only buildingID and typeID columns.
5. Facility-Building Relationship
    - Creates the association between the facility and the building
    - Uses the Endpoint_Building junction table to establish the many-to-many relationship.
    - This allows buildings to be shared across multiple facilities if needed.
6. Return Value
    - Returns the newly created building ID.
    - This allows the caller to reference the new building immediately.
7. Error Handling
    - All database operations are wrapped in exception handling.
    - Custom error messages are prrovided for different failure scenarios.
    - Any unexpected errors are caught and re-thrown with context.

---

## 2. Anonymous Block Testing

### Success Test Code
```sql
DECLARE
    v_new_building_id NUMBER;
    v_building_count_before NUMBER;
    v_building_count_after NUMBER;

    CURSOR c_buildings IS
        SELECT b.buildingID, b.typeID, bt."desc" AS building_type,
               e.endpointID, e.name AS facility_name
        FROM building b
        JOIN Building_Type bt ON b.typeID = bt.id
        LEFT JOIN Endpoint_Building eb ON b.buildingID = eb.BuildingbuildingID
        LEFT JOIN Endpoint e ON eb.EndpointendpointID = e.endpointID
        ORDER BY b.buildingID, e.endpointID;

BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('USBD36: Add Building to Facility Test');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('');

    SELECT COUNT(*) INTO v_building_count_before FROM building;
    DBMS_OUTPUT.PUT_LINE('Total buildings before: ' || v_building_count_before);
    DBMS_OUTPUT.PUT_LINE('');

    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('BUILDINGS BEFORE REGISTRATION');
    DBMS_OUTPUT.PUT_LINE('========================================');

    FOR rec IN c_buildings LOOP
        DBMS_OUTPUT.PUT_LINE('Building ID: ' || rec.buildingID || ' | Type ID: ' || rec.typeID);
        DBMS_OUTPUT.PUT_LINE('  Type: ' || rec.building_type);
        DBMS_OUTPUT.PUT_LINE('  Facility: ' || NVL(rec.facility_name, 'N/A') || ' (ID: ' || NVL(TO_CHAR(rec.endpointID), 'N/A') || ')');
        DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    END LOOP;

    v_new_building_id := fncAddBuildingToFacility(
        p_facility_id => 1,
        p_building_type_id => 1
    );

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('New building registered successfully!');
    DBMS_OUTPUT.PUT_LINE('New Building ID: ' || v_new_building_id);

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('BUILDINGS AFTER REGISTRATION');
    DBMS_OUTPUT.PUT_LINE('========================================');

    FOR rec IN c_buildings LOOP
        DBMS_OUTPUT.PUT_LINE('Building ID: ' || rec.buildingID || ' | Type ID: ' || rec.typeID);
        DBMS_OUTPUT.PUT_LINE('  Type: ' || rec.building_type);
        DBMS_OUTPUT.PUT_LINE('  Facility: ' || NVL(rec.facility_name, 'N/A') || ' (ID: ' || NVL(TO_CHAR(rec.endpointID), 'N/A') || ')');

        IF rec.buildingID = v_new_building_id THEN
            DBMS_OUTPUT.PUT_LINE('  >>> NEWLY REGISTERED <<<');
        END IF;

        DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    END LOOP;

    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('Test complete. Changes rolled back.');
END;
/
```

### Error Test Code
```sql
DECLARE
    v_result_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('USBD36: Testing Error Scenarios');
    DBMS_OUTPUT.PUT_LINE('========================================');

    -- TESTE 1: Parâmetro Facility ID Nulo (Esperado o erro -20001)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 1: Testing NULL Facility ID');
        v_result_id := fncAddBuildingToFacility(
            p_facility_id => NULL,  -- Esta linha causa erro
            p_building_type_id => 1
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- TESTE 2: Parâmetro Building Type ID Nulo (Esperado o erro -20001)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 2: Testing NULL Building Type ID');
        v_result_id := fncAddBuildingToFacility(
            p_facility_id => 1,
            p_building_type_id => NULL  -- Esta linha causa erro
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- TESTE 3: Facility Inexistente (Esperado o erro -20002)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 3: Testing Non-existent Facility (ID 99999)');
        v_result_id := fncAddBuildingToFacility(
            p_facility_id => 99999, -- Este ID não existe
            p_building_type_id => 1
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- TESTE 4: Tipo de Building Inexistente (Esperado o erro -20003)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 4: Testing Invalid Building Type ID (9999)');
        v_result_id := fncAddBuildingToFacility(
            p_facility_id => 1, -- Assumindo que existe
            p_building_type_id => 9999 -- Este ID não existe na tabela Building_Type
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
    - Declares variables to track the test execution.
    - Creates a cursor to retrieve all buildings with their associated facilities.
2. **Testing Before the Execution**:
    - Counts the total number of buildings before the operation.
    - Lists all existing buildings with their complete information:
        - Building ID and Type ID
        - Building Type Description
        - Associated Facility Name and ID
    - This establishes a baseline for comparison.
3. **Function Execution**:
    - Calls `fncAddBuildingToFacility` with test data:
        - Facility ID: 1
        - Building Type ID: 1 (Warehouse)
    - Captures the returned building ID.
4. **Testing After the Execution**:
    - Lists all buildings again, highlighting the newly registered one.
    - Marks the new building with ">>> NEWLY REGISTERED <<<" for easy identification.
    - Shows the facility association for the new building.
5. **Cleanup**:
    - Rolls back all changes to maintain database integrity.
    - Ensures test data doesn't persist in the database.

#### Error Test Flow
1. **Scenario 1 - NULL Facility ID**:
    - Tests input validation by passing NULL for the facility ID.
    - Expected: Error -20001.
2. **Scenario 2 - NULL Building Type ID**:
    - Tests input validation by passing NULL for the building type ID.
    - Expected: Error -20001.
3. **Scenario 3 - Non-Existing Facility**:
    - Tests facility existence validation with ID 99999.
    - Expected: Error -20002.
4. **Scenario 4 - Invalid Building Type**:
    - Tests building type validation with non-existent type ID 9999.
    - Expected: Error -20003.
5. **Cleanup**:
    - Rolls back any partial changes.
    - Ensures database remains clean after error tests.

#### Testing Benefits
- **Before/After Comparison**: Clearly shows the changes in the database.
- **Visual Verification**: Easy to spot the new building and its facility association.
- **Comprehensive Error Coverage**: Tests all validation points.
- **Safe Testing**: Rollback ensures no persistent changes to the database.

---

## 3. Database Schema Dependencies

### Required Tables
**Building**:
- `buildingID` (PRIMARY KEY)
- `typeID` (FOREIGN KEY)

**Building_Type**:
- `id` (PRIMARY KEY)
- `desc`

**Endpoint**:
- `endpointID` (PRIMARY KEY)
- `name`
- `locationID` (FOREIGN KEY)
- `typeID` (FOREIGN KEY)

**Endpoint_Building** (Junction Table):
- `EndpointendpointID` (FOREIGN KEY, PRIMARY KEY)
- `BuildingbuildingID` (FOREIGN KEY, PRIMARY KEY)

### Relationships
```
Building_Type (1) ----< (N) Building
Endpoint (N) ----< (M) Building [through Endpoint_Building]
```

**Note**: The `Endpoint_Building` junction table implemens a many-to-many relationship, allowing:
- A building to be associated with multiple facilities.
- A facility to have multiple buildings.

---

## 4. Error Handling

### Custom Error Codes
| Error Code | Description                     | Trigger Condition                                                  |
|------------|---------------------------------|--------------------------------------------------------------------|
| -20001     | Missing Required Parameters     | Any required parameter is NULL                                     |
| -20002     | Facility Does Not Exist         | The provided facility_id doesn't exist in Endpoint table           |
| -20003     | Invalid Building Type           | The provided building_type_id doesn't exist in Building_Type table |
| -20004     | General Building Creation Error | Any other error during building creation or association            | 

### Error Handling Strategy
1. **Input Validation**: First line of defense - validates all parameters before any database operations.
2. **Referential Integrity**: Validates foreign key references exist before attempting inserts.
3. **Transaction Safety**: Uses exception handling to ensure database consistency.
4. **Informative Messages**: Provides clear error messages with context (IDs, operation details).
5. **Catch-All Handlers**: Final EXCEPTION WHEN OTHERS block catches unexpected errors.