# USBD41 - I want to remove a freight from a planned train

## Table of Contents

1. [Function Implementation](#1-function-implementation)
   - [PL/SQL Code](#plsql-code)
   - [Function Logic Explanation](#function-logic-explanation)
2. [Anonymous Block Testing](#2-anonymous-block-testing)
   - [Success Test Code](#success-test-code)
   - [Error Test Code](#error-test-code)
   - [Test Flow Explanation](#test-flow-explanation)
3. [Error Handling](#3-error-handling)

---

## 1. Function Implementation

### PL/SQL Code

````

CREATE OR REPLACE FUNCTION fncRemoveFreightFromPlannedTrain (
    p_trainId   IN NUMBER,
    p_freightId IN NUMBER
)
RETURN NUMBER
IS
    v_train_count   NUMBER;
    v_freight_count NUMBER;
    v_assoc_count   NUMBER;
BEGIN
    -- 1. Validação de inputs
    IF p_trainId IS NULL OR p_freightId IS NULL THEN
        RAISE_APPLICATION_ERROR(-20020, 'Train ID and Freight ID are required.');
    END IF;

    -- 2. Verificar se o train existe
    SELECT COUNT(*)
    INTO v_train_count
    FROM TRAIN
    WHERE ID = p_trainId;

    IF v_train_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20021, 'Train ID does not exist.');
    END IF;

    -- 3. Verificar se o freight existe
    SELECT COUNT(*)
    INTO v_freight_count
    FROM FREIGHT
    WHERE ID = p_freightId;

    IF v_freight_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20022, 'Freight ID does not exist.');
    END IF;

    -- 4. Verificar se o freight está associado ao train (via wagons do operador)
    SELECT COUNT(*)
    INTO v_assoc_count
    FROM WAGON_FREIGHT wf
    JOIN OPERATOR_WAGON ow ON ow.WAGONID = wf.WAGONID
    JOIN TRAIN t ON UPPER(TRIM(t.OPERATOR)) = UPPER(TRIM(ow.OPERATORSHORTNAME))
    WHERE t.ID = p_trainId
      AND wf.FREIGHTID = p_freightId;

    IF v_assoc_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20023,'Freight is not associated with the given planned train.');
    END IF;

    -- 5. Remover todas as associações freight–wagon do train
    DELETE FROM WAGON_FREIGHT wf
    WHERE wf.FREIGHTID = p_freightId
      AND wf.WAGONID IN (
          SELECT ow.WAGONID
          FROM OPERATOR_WAGON ow
          JOIN TRAIN t
            ON UPPER(TRIM(t.OPERATOR)) = UPPER(TRIM(ow.OPERATORSHORTNAME))
          WHERE t.ID = p_trainId
      );

    -- 6. Retornar nº de associações removidas
    RETURN SQL%ROWCOUNT;

EXCEPTION
    WHEN OTHERS THEN
        RAISE;
END fncRemoveFreightFromPlannedTrain;
/

```
````

### Function Logic Explanation

1. Input Validation

   - If `p_trainId`or`p_freightId` is NULL, it raises -20020.
   - Ensures required parameters exist before any DB operations
2. Train Validation

   - Confirms `TRAIN.ID = p_trainId` exists.
   - If not, raises -20021.
3. Freight Validation

   - Confirms `FREIGHT.ID = p_freightId` exists.
   - If not, raises -20022.
4. Association Validation

   - Checks whether the specified freight is associated with the given train (through wagon ownership/operator mapping).
   - Uses a case-insensitive trimmed match between:
        - TRAIN.OPERATOR
        - OPERATOR_WAGON.OPERATORSHORTNAME
   - If no association exists, raises -20023.
5. Deletion Logic

   - Deletes all rows in `WAGON_FREIGHT` for the target freight and wagons belonging to the train’s operator.
   - This removes the freight from the planned train’s set of wagons.
6. Error Handling

   - Returns `SQL%ROWCOUNT`, representing how many associations were removed.

---

## 2. Anonymous Block Testing

### Success Test Code

````sql
SET SERVEROUTPUT ON;

DECLARE
    v_result NUMBER;

    v_trainId   NUMBER := 5421;
    v_freightId NUMBER := 2001;

    CURSOR c_all IS
        SELECT wf.WAGONID, wf.FREIGHTID
        FROM WAGON_FREIGHT wf
        JOIN OPERATOR_WAGON ow ON ow.WAGONID = wf.WAGONID
        JOIN TRAIN t
          ON UPPER(TRIM(t.OPERATOR)) = UPPER(TRIM(ow.OPERATORSHORTNAME))
        WHERE t.ID = v_trainId
        ORDER BY wf.WAGONID, wf.FREIGHTID;

    v_before NUMBER;
    v_after  NUMBER;

BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('USBD41: Remove Freight From Planned Train');
    DBMS_OUTPUT.PUT_LINE('SUCCESS CASE');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('');

    -- BEFORE COUNT (all freights)
    SELECT COUNT(*) INTO v_before
    FROM WAGON_FREIGHT wf
    JOIN OPERATOR_WAGON ow ON ow.WAGONID = wf.WAGONID
    JOIN TRAIN t
      ON UPPER(TRIM(t.OPERATOR)) = UPPER(TRIM(ow.OPERATORSHORTNAME))
    WHERE t.ID = v_trainId;

    DBMS_OUTPUT.PUT_LINE('Total freight associations BEFORE: ' || v_before);
    DBMS_OUTPUT.PUT_LINE('');

    DBMS_OUTPUT.PUT_LINE('--- BEFORE (all freights in the train) ---');
    FOR rec IN c_all LOOP
        DBMS_OUTPUT.PUT_LINE(
            'WagonID=' || rec.WAGONID ||
            ' | FreightID=' || rec.FREIGHTID
        );

        IF rec.FREIGHTID = v_freightId THEN
            DBMS_OUTPUT.PUT_LINE('   >>> TARGET TO REMOVE <<<');
        END IF;

        DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    END LOOP;

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('>> Executing fncRemoveFreightFromPlannedTrain');
    DBMS_OUTPUT.PUT_LINE('');

    v_result := fncRemoveFreightFromPlannedTrain(
        v_trainId,
        v_freightId
    );

    DBMS_OUTPUT.PUT_LINE('Removed associations: ' || v_result);
    DBMS_OUTPUT.PUT_LINE('');

    -- AFTER COUNT (all freights)
    SELECT COUNT(*) INTO v_after
    FROM WAGON_FREIGHT wf
    JOIN OPERATOR_WAGON ow ON ow.WAGONID = wf.WAGONID
    JOIN TRAIN t
      ON UPPER(TRIM(t.OPERATOR)) = UPPER(TRIM(ow.OPERATORSHORTNAME))
    WHERE t.ID = v_trainId;

    DBMS_OUTPUT.PUT_LINE('Total freight associations AFTER: ' || v_after);
    DBMS_OUTPUT.PUT_LINE('');

    DBMS_OUTPUT.PUT_LINE('--- AFTER (remaining freights in the train) ---');
    FOR rec IN c_all LOOP
        DBMS_OUTPUT.PUT_LINE(
            'WagonID=' || rec.WAGONID ||
            ' | FreightID=' || rec.FREIGHTID
        );

        IF rec.FREIGHTID = v_freightId THEN
            DBMS_OUTPUT.PUT_LINE('   ⚠ ERROR: freight still present');
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
    v_result NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('USBD41: Remove Freight From Planned Train');
    DBMS_OUTPUT.PUT_LINE('ERROR CASES');
    DBMS_OUTPUT.PUT_LINE('========================================');

    -- TESTE 1: Train ID NULL (Esperado -20020)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 1: Testing NULL Train ID');
        v_result := fncRemoveFreightFromPlannedTrain(
            p_trainId   => NULL,
            p_freightId => 2001
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- TESTE 2: Freight ID NULL (Esperado -20020)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 2: Testing NULL Freight ID');
        v_result := fncRemoveFreightFromPlannedTrain(
            p_trainId   => 5421,
            p_freightId => NULL
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- TESTE 3: Train inexistente (Esperado -20021)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 3: Testing Non-existent Train (99999)');
        v_result := fncRemoveFreightFromPlannedTrain(
            p_trainId   => 99999,
            p_freightId => 2001
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- TESTE 4: Freight inexistente (Esperado -20022)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 4: Testing Non-existent Freight (99999)');
        v_result := fncRemoveFreightFromPlannedTrain(
            p_trainId   => 5421,
            p_freightId => 99999
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- TESTE 5: Freight NÃO associado ao Train (Esperado -20023)
    -- Aqui escolhemos um freightId que NÃO apareça no train 5421.
    -- Tenta primeiro 99998 (se existir na tabela FREIGHT troca), ou usa um freight válido que não esteja associado.
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 5: Testing Freight not associated with Train (Train=5421, Freight=2008)');
        v_result := fncRemoveFreightFromPlannedTrain(
            p_trainId   => 5421,
            p_freightId => 2008
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
   - Sets `v_trainId` and `v_freightId` as test inputs.
   - Declares a cursor that lists all freight-wagon associations for the train (by operator mapping).
2. **Before-State Verification**:
   - Counts how many freight associations exist for the train before deletion.
   - Prints every wagon-freight association and highlights the target freight with:
   - `>>> TARGET TO REMOVE <<<`
3. **Function Execution**:
   - Calls `fncRemoveFreightFromPlannedTrain(v_trainId, v_freightId)`
   - Captures the returned value (number of deleted associations).
4. **After-State Verification**
   - Counts associations again to confirm the total decreased.
   - Prints remaining associations.
   - If the target freight is still present, prints:
      - `⚠ ERROR: freight still present`
5. **Cleanup**:
   - Executes `ROLLBACK` to ensure no test data persists in the database.

#### Error Test Flow

 - Scenario 1: NULL trainId → -20020
 
 - Scenario 2: NULL freightId → -20020
 
 - Scenario 3: Train does not exist → -20021

 - Scenario 4: Freight does not exist → -20022

 - Scenario 5: Freight not linked to train → -20023

 - Cleanup with ROLLBACK

- **Before/After Comparison**: Clearly shows the changes in the database.
- **Visual Verification**: Easy to spot the deleted freights.
- **Comprehensive Error Coverage**: Tests all validation points.
- **Safe Testing**: Rollback ensures no persistent changes to the database.

---

## 3. Error Handling

### Custom Error Codes


| Error Code | Description          | Trigger Condition       |
| ---------- | -------------------- | ----------------------- |
| -20020     | Missing Required Parameters | `p_trainId IS NULL OR p_freightId IS NULL` |
| -20021     | Train Does Not Exist  | No row in `TRAIN` with `ID = p_trainId`    |
| -20022     | Freight Does Not Exist     | No row in `FREIGHT` with `ID = p_freightId`   |
| -20023     | Freight Not Associated With Train | No association found via operator-wagon mapping|


### Error Handling Strategy

1. **Input validation** first to avoid unnecessary database operations.
2. **Business rule enforcement** through controlled exceptions.
3. **Safe testing** using rollback to avoid persistent data changes.
4. **Clear and informative messages** to simplify debugging and evaluation.
