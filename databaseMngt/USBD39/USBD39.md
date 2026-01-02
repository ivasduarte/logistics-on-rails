# USBD33 - Prevent Excessive Train Length

### Index

1. [Trigger Implementation](#trigger-implementation)
    - [PL/SQL Code](#plsql-code)
    - [Explanation of the Logic](#explanation-of-the-logic)
2. [Success Test (Valid Scenario)](#success-test-valid-scenario)
    - [Test Code](#test-code)
    - [Explanation of the Flow](#explanation-of-the-flow)
3. [Error Testing](#3-error-scenario-testing)
    - [Test Code](#test-code-1)
    - [Explanation of the Flow](#explanation-of-the-flow-1)
4. [Database Schema Dependencies](#database-schema-dependencies)
5. [Error Management](#error-management)

### Trigger Implementation

## PL/SQL Code

```sql
CREATE OR REPLACE FUNCTION fncAssociateLocomotiveToTrain(
    p_locomotive_id IN NUMBER,
    p_train_id      IN NUMBER
)
    RETURN NUMBER
    IS
    v_dummy NUMBER;
BEGIN
    -- 1. Validações
    IF p_locomotive_id IS NULL OR p_train_id IS NULL THEN
        RAISE_APPLICATION_ERROR(-20010, 'Inputs required.');
END IF;

    -- Verificar existências
BEGIN
SELECT 1 INTO v_dummy FROM Locomotive WHERE id = p_locomotive_id;
EXCEPTION WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20011, 'Locomotive not found.');
END;

BEGIN
SELECT 1 INTO v_dummy FROM Train WHERE id = p_train_id;
EXCEPTION WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20012, 'Train not found.');
END;

    -- Verificar duplicados
SELECT COUNT(*) INTO v_dummy FROM Locomotive_Train
WHERE Locomotiveid = p_locomotive_id AND Trainid = p_train_id;

IF v_dummy > 0 THEN
        RAISE_APPLICATION_ERROR(-20013, 'Locomotive already associated.');
END IF;

    -- 2. Tentar Inserir
INSERT INTO Locomotive_Train (Locomotiveid, Trainid)
VALUES (p_locomotive_id, p_train_id);

COMMIT;
RETURN 1; -- Sucesso

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE; -- Passa o erro para o utilizador
END fncAssociateLocomotiveToTrain;
/
```

## Explanation of the Logic

**1. Input Validation**
- Checks if the locomotive and train parameters have been provided, triggering an error if any are null.

**2. Existence Check**
- Confirms that the specific locomotive and train exist in their respective timetables before proceeding.

**3. Duplicate Control**
- Ensures that the same locomotive is not associated more than once with the same trian in the connection table.

**4. Data Persistence**
- Performs the insertion into the table `Locomotive_Train` and executes the operation `COMMIT` to consolidate the transaction.

**5. Exception Handling**
- In the event of an unexpected failure, a process is carried out `ROLLBACK` to maintain data integrity.


---

## Success Test (Valid Scenario)

### Test Code

The anonymous test block associates the locomotive "Helena" with train 5437 and verifies its existence in the database.


```sql
DECLARE
v_resultado NUMBER;
    v_loco_id   NUMBER := 5630; -- Locomotiva 'Helena'
    v_train_id  NUMBER := 5437; -- Comboio
BEGIN
    DBMS_OUTPUT.PUT_LINE('=========================================================');
    DBMS_OUTPUT.PUT_LINE('USBD39: Associate a Locomotive to a Train Success Test');
    DBMS_OUTPUT.PUT_LINE('=========================================================');
    DBMS_OUTPUT.PUT_LINE('');

    -- 1. Tentar Associar 
    DBMS_OUTPUT.PUT_LINE('→ Calling association function...');
    v_resultado := fncAssociateLocomotiveToTrain(v_loco_id, v_train_id);

    IF v_resultado = 1 THEN
        DBMS_OUTPUT.PUT_LINE('   [SUCCESS] Association created in database.');

        -- Verificar se está lá mesmo
FOR r IN (SELECT * FROM Locomotive_Train WHERE Locomotiveid = v_loco_id AND Trainid = v_train_id) LOOP
                DBMS_OUTPUT.PUT_LINE('   [CHECKING] Record found in database.');
END LOOP;

        -- 2. Apagar o registo criado
        DBMS_OUTPUT.PUT_LINE('→ Cleaning test data...');
DELETE FROM Locomotive_Train
WHERE Locomotiveid = v_loco_id AND Trainid = v_train_id;

-- Confirmar a remoção
IF SQL%ROWCOUNT > 0 THEN
            DBMS_OUTPUT.PUT_LINE('   [CLEAN UP COMPLETE] Association removed successfully.');
COMMIT; -- Gravar a remoção
ELSE
            DBMS_OUTPUT.PUT_LINE('   [WARNING] It was not possible to remove the record.');
END IF;

END IF;

    DBMS_OUTPUT.PUT_LINE('=========================================================');

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('');
        DBMS_OUTPUT.PUT_LINE('=========================================================');
        DBMS_OUTPUT.PUT_LINE('ERROR DURING TEST');
        DBMS_OUTPUT.PUT_LINE('=========================================================');
        DBMS_OUTPUT.PUT_LINE('Error Code: ' || SQLCODE);
        DBMS_OUTPUT.PUT_LINE('Error Message: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE('Test Status: FAILED');
        DBMS_OUTPUT.PUT_LINE('=========================================================');

        -- Tenta limpar em caso de erro, se o registo tiver ficado lá
BEGIN
DELETE FROM Locomotive_Train WHERE Locomotiveid = v_loco_id AND Trainid = v_train_id;
COMMIT;
DBMS_OUTPUT.PUT_LINE('   (Emergency cleanup attempt performed)');
EXCEPTION WHEN OTHERS THEN NULL;
END;
END;
/
```

### Explanation of the Flow

**1. Execution**: The function is called with valid IDs.

**2. Verification**: The script confirms that the record was created through a match `SELECT` in the association table.

**3. Clean-up**: After success, the test log is removed to ensure the test environment remains clean.

**4. Expected Output**: The system displays success messages for creation, verification, and clean-up.

---

## Error Testing

### Test Code

The error test focuses on attempting to create a duplicate association.

```sql
DECLARE
v_resultado NUMBER;
    v_loco_id   NUMBER := 5621; -- Locomotiva 'Inês'
    v_train_id  NUMBER := 5421; -- Comboio existente
BEGIN
    DBMS_OUTPUT.PUT_LINE('=========================================================');
    DBMS_OUTPUT.PUT_LINE('USBD39: Associate a Locomotive to a Train Error Test');
    DBMS_OUTPUT.PUT_LINE('=========================================================');
    DBMS_OUTPUT.PUT_LINE('');

    DBMS_OUTPUT.PUT_LINE('Attempting to Create a Duplicate Association (' || v_loco_id || ' - ' || v_train_id || ')...');

    -- Chamada da função
    v_resultado := fncAssociateLocomotiveToTrain(v_loco_id, v_train_id);

    -- Se chegar aqui, o teste falhou
    DBMS_OUTPUT.PUT_LINE('    [TEST FAILURE] The function improperly allowed duplicates.');


EXCEPTION
    WHEN OTHERS THEN
        -- O erro esperado é o -20013 definido na função
        IF SQLCODE = -20013 THEN
            DBMS_OUTPUT.PUT_LINE('');
            DBMS_OUTPUT.PUT_LINE('[TEST SUCCESSFUL] The function blocked the duplicate correctly.');
            DBMS_OUTPUT.PUT_LINE('[Error Message] ' || SQLERRM);
ELSE
            DBMS_OUTPUT.PUT_LINE('[UNEXPECTED ERROR]' || SQLERRM);
END IF;
        DBMS_OUTPUT.PUT_LINE('=========================================================');
END;
/
```


### Explanation of the Flow

**1. Scenario**: Try to match locomotive 5621 to train 5421 for the second time.

**2. Result**: The function blocks the operation and triggers the error `-20013`.

**3. Message**: `[TEST SUCCESSFUL] The function blocked the duplicate correctly.`.

---

## Database Schema Dependencies

**Tables Involved:**

- `Locomotive`: Contains the master data for locomotives.

- `Train`: Contains data for planned trains.

- `Locomotive_Train`: Association table that links locomotives to trains.

---

## Error Management

| Error Code | Description               | Trigger Condition |
|------------|---------------------------|-------------------|
| -20010     | Required Inputs           | At least one of the IDs is NULL. |
| -20011     | Locomotive Not Found      | The locomotive ID does not exist in the table `Locomotive`. |
| -20012     | Train Not Found           | The train ID does not exist in the table `Train`. |
| -20013     | Locomotive Already Exists | The association already exists in the table `Locomotive_Train`. |

### Example of Use

```sql
DECLARE
    v_res NUMBER;
BEGIN
    v_res := fncAssociateLocomotiveToTrain(5630, 5437);
    DBMS_OUTPUT.PUT_LINE('Resultado: ' || v_res); -- 1 para sucesso
END;
```