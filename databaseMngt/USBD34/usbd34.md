# USBD34 - Associate Freight with Train

## Table of Contents
1. [Function Implementation](#1-function-implementation)
    - [PL/SQL Code](#plsql-code)
    - [Function Logic Explanation](#function-logic-explanation)
2. [Anonymous Block Testing](#2-anonymous-block-testing)
    - [Test Code](#test-code)
    - [Test Flow Explanation](#test-flow-explanation)
3. [Error Scenario Testing](#3-error-scenario-testing)
4. [Database Schema Dependencies](#4-database-schema-dependencies)
5. [Error Handling](#5-error-handling)

---

## 1. Function Implementation

### PL/SQL Code

```sql
CREATE OR REPLACE FUNCTION fncAssociateFreightWithTrain(
    p_freight_id IN NUMBER,
    p_train_id IN NUMBER
)
    RETURN NUMBER
    IS
    v_freight_count NUMBER;
    v_train_count NUMBER;
    v_wagons_associated NUMBER := 0;
    v_current_train_id NUMBER;
BEGIN
    -- Valida se existem todos os inputs necessários
    IF p_freight_id IS NULL OR p_train_id IS NULL THEN
        RAISE_APPLICATION_ERROR(-20001, 'All parameters are required for freight-train association');
    END IF;

    -- Valida se o freight existe
    SELECT COUNT(*) INTO v_freight_count
    FROM Freight
    WHERE id = p_freight_id;

    IF v_freight_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Freight ID does not exist');
    END IF;

    -- Valida se o train existe
    SELECT COUNT(*) INTO v_train_count
    FROM Train
    WHERE id = p_train_id;

    IF v_train_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20003, 'Train ID does not exist');
    END IF;

    -- Verifica se o freight já está associado a outro train
    SELECT trainID INTO v_current_train_id
    FROM Freight
    WHERE id = p_freight_id;

    IF v_current_train_id IS NOT NULL AND v_current_train_id != p_train_id THEN
        RAISE_APPLICATION_ERROR(-20004, 'Freight is already associated with another train (Train ID: ' || v_current_train_id || ')');
    END IF;

    -- Se já está associado ao mesmo train, apenas retorna a contagem de wagons
    IF v_current_train_id = p_train_id THEN
        SELECT COUNT(*) INTO v_wagons_associated
        FROM Wagon_Freight
        WHERE Freightid = p_freight_id;

        RETURN v_wagons_associated;
    END IF;

    -- Associa o freight ao train (apenas se ainda não estiver associado)
    UPDATE Freight
    SET trainID = p_train_id
    WHERE id = p_freight_id;

    -- Conta quantos wagons estão no freight
    SELECT COUNT(*) INTO v_wagons_associated
    FROM Wagon_Freight
    WHERE Freightid = p_freight_id;

    COMMIT;

    -- Retorna o número de wagons associados
    RETURN v_wagons_associated;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END fncAssociateFreightWithTrain;
```

### Function Logic Explanation

1. **Input Validation**:
    - Validates that all required parameters (freight_id, train_id) are provided
    - Raises an error if any required parameter is NULL

2. **Entity Existence Validation**:
    - Verifies that the specified freight exists in the database
    - Verifies that the specified train exists in the database
    - Raises specific errors if either entity doesn't exist

3. **Business Logic Validation**:
    - Checks if the freight is already associated with a different train
    - Prevents reassignment to a different train (maintains data integrity)
    - Allows re-association to the same train (idempotent operation)

4. **Association Process**:
    - Updates the Freight table to link the freight with the train
    - Counts the number of wagons associated with this freight
    - **Important**: Wagons are implicitly associated through the Freight-Train relationship:
        - `Wagon_Freight` links wagons to freight
        - `Freight.trainID` links freight to train
        - Therefore, wagons in a freight are automatically associated with the train

5. **Return Value**:
    - Returns the number of wagons that are now associated with the train through this freight

6. **Error Handling**:
    - All database operations are wrapped in exception handling
    - Any errors trigger a ROLLBACK to maintain data consistency
    - Custom error messages are provided for different failure scenarios

---

## 2. Anonymous Block Testing

### Test Code

The test anonymous block is provided in the artifact above. It demonstrates:
- Displaying freight details before association
- Listing all wagons in the freight
- Calling the function to create the association
- Displaying freight details after association
- Confirming the wagons are now associated with the train

### Test Flow Explanation

1. **Initialization**:
    - Declares variables to track the test execution
    - Creates cursors to retrieve freight details and wagon information

2. **Pre-Association State**:
    - Displays current freight details including:
        - Freight ID
        - Date
        - Origin and Destination endpoints
        - Current train assignment (if any)
        - Number of wagons
    - Lists all wagons in the freight with their specifications

3. **Function Execution**:
    - Calls `fncAssociateFreightWithTrain` with test data:
        - Freight ID: 1
        - Train ID: 1
    - Captures the returned wagon count

4. **Post-Association State**:
    - Displays updated freight details
    - Confirms the train association
    - Lists all wagons now associated with the train
    - Marks the successful association with ">>> SUCCESSFULLY ASSOCIATED <<<"

---

## 3. Error Scenario Testing

The error scenario test validates the following cases:

1. **NULL Freight ID**: Ensures the function rejects null freight IDs
2. **NULL Train ID**: Ensures the function rejects null train IDs
3. **Non-existent Freight ID**: Tests behavior with invalid freight ID
4. **Non-existent Train ID**: Tests behavior with invalid train ID
5. **Already Associated Freight**: Tests prevention of reassigning a freight to a different train

All tests should catch expected errors and display appropriate error messages.

---

## 4. Database Schema Dependencies

### Required Tables

**Freight**
- `id` (PRIMARY KEY)
- `date`
- `origin` (FK to Endpoint)
- `destination` (FK to Endpoint)
- `trainID` (FK to Train) - **This field is updated by the function**

**Train**
- `id` (PRIMARY KEY)
- `date`
- `pathID` (FK to Path)

**Wagon_Freight** (Association Table)
- `Wagonid` (FK to Wagon)
- `Freightid` (FK to Freight)

**Wagon**
- `id` (PRIMARY KEY)
- `modelID` (FK to Wagon_Model)
- `entryDate`
- `gauge`
- `startEndpoint` (FK to Endpoint)

### Relationships

```
Train (1) ----< (N) Freight
Freight (N) ----< (N) Wagon [through Wagon_Freight]

Association Chain:
Wagon → Wagon_Freight → Freight → Train
```

### Key Points

- A freight can only be associated with one train at a time
- A train can have multiple freights
- Each freight can contain multiple wagons
- Wagons are associated with trains indirectly through the freight

---

## 5. Error Handling

### Custom Error Codes

| Error Code | Description | Trigger Condition |
|------------|-------------|-------------------|
| -20001 | Missing Required Parameters | Freight ID or Train ID is NULL |
| -20002 | Freight Does Not Exist | Specified freight ID not found in database |
| -20003 | Train Does Not Exist | Specified train ID not found in database |
| -20004 | Freight Already Associated | Freight is already linked to a different train |
| -20005 | General Association Error | Any other error during the association process |

### Error Recovery

All errors trigger an automatic ROLLBACK to ensure:
- No partial updates are committed
- Database remains in a consistent state
- Transaction integrity is maintained

---

## Usage Example

```sql
-- Associate freight 1 with train 1
DECLARE
    v_wagon_count NUMBER;
BEGIN
    v_wagon_count := fncAssociateFreightWithTrain(
        p_freight_id => 1,
        p_train_id => 1
    );
    
    DBMS_OUTPUT.PUT_LINE('Successfully associated freight with train.');
    DBMS_OUTPUT.PUT_LINE('Number of wagons: ' || v_wagon_count);
END;
/
```

---

## Notes

- This function implements an **idempotent operation**: calling it multiple times with the same freight and train IDs will succeed without error
- The function prevents **reassignment** of a freight to a different train to maintain data integrity
- Wagons are **implicitly associated** with the train through the freight relationship
- All changes are **committed** upon successful completion
- Any errors result in a complete **rollback** of the transaction