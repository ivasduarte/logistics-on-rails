# USBD33 - Prevent Excessive Train Length

### Index

1. [Trigger Implementation](#trigger-implementation)
    - [PL/SQL Code](#plsql-code)
    - [Explanation of the Logic](#explanation-of-the-logic)
2. [Success Test (Valid Scenario)](#success-test-valid-scenario)
    - [Test Code](#test-code)
    - [Explanation of the Flow](#explanation-of-the-flow)
3. [Error Testing (Overload Scenario)](#3-error-scenario-testing)
    - [Test Code](#test-code-1)
    - [Explanation of the Flow](#explanation-of-the-flow-1)
4. [Database Schema Dependencies](#database-schema-dependencies)
5. [Error Management](#error-management)

### Trigger Implementation

## PL/SQL Code

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

## Explanation of the Logic

**1. Compoud Trigger** 
- Used to avoid the "Mutating Table" error, allowing the table to be read `Locomotive_Train` after insertion to calculate totals.

**2. ID Capture**
- In this phase `BEFORE EACH ROW`, the trigger records all train IDs that are being modified.

**3. Length Calculation**
- **Locomotives**: Sum of the length (`length`) of all locomotives associated with the train.

- **Wagons**: Sum the length of the wagons associated with the freight (`Freight`). The wagons are in millimeters (mm), so the value is divided by 1000 to convert to meters.

**4. Validation**
- If the total length exceeds 750 meters, a custom exception is thrown (`-20020`).

---

## Success Test (Valid Scenario)

### Test Code

The test attempts to add a locomotive (ID 5630) to train 5421, where the total accumulated length remains within legal limits.

```sql
DECLARE
    v_train_id      CONSTANT NUMBER := 5421;
    v_loco_id       CONSTANT NUMBER := 5630; -- Locomotiva Helena (Model 1: 19.2m)
BEGIN
    DBMS_OUTPUT.PUT_LINE('=========================================================');
    DBMS_OUTPUT.PUT_LINE('USBD33: Do Not Allow Train Excess Length Success Test');
    DBMS_OUTPUT.PUT_LINE('=========================================================');
    DBMS_OUTPUT.PUT_LINE('');

    -- Tentativa de inserir uma terceira locomotiva no comboio 5421
    -- Comprimento acumulado: (2 locos existentes * 19.2m) + (nova loco * 19.2m) + vagões < 750m
    INSERT INTO Locomotive_Train (Locomotiveid, Trainid)
    VALUES (v_loco_id, v_train_id);

    DBMS_OUTPUT.PUT_LINE('[SUCCESS] Locomotive ' || v_loco_id || ' added to Train ' || v_train_id);
    DBMS_OUTPUT.PUT_LINE('[INFO] Total length remains below the 750m threshold.');

EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20020 THEN
            DBMS_OUTPUT.PUT_LINE('[SUCCESS] The system blocked the insertion correctly.');
            DBMS_OUTPUT.PUT_LINE('[Error Message] ' || SQLERRM);
        ELSE
            DBMS_OUTPUT.PUT_LINE('[UNEXPECTED ERROR] ' || SQLERRM);
        END IF;

        DBMS_OUTPUT.PUT_LINE('');
        DBMS_OUTPUT.PUT_LINE('=========================================================');

        ROLLBACK;
END;
/
```

### Explanation of the Flow

**1. Insertion**: Executes an operation INSERTon the table Locomotive_Train.

**2. Verification**: The trigger calculates the sum of the existing locomotives, the new locomotive, and the wagons.

**3. Result**: Since the value is less than 750m, the operation is completed successfully.

---

## Error Testing (Overload Scenario)

### Test Code

This scenario deliberately creates an "ULTRA-LONG" locomotive that is 800 meters long and attempts to associate it with a train.

```sql
DECLARE
    v_mega_model_id   CONSTANT NUMBER := 8888;
    v_mega_loco_id    CONSTANT NUMBER := 8888;
    v_train_id        CONSTANT NUMBER := 5437;
BEGIN
    DBMS_OUTPUT.PUT_LINE('=========================================================');
    DBMS_OUTPUT.PUT_LINE('USBD33: Do Not Allow Train Excess Length Error Test');
    DBMS_OUTPUT.PUT_LINE('=========================================================');
    DBMS_OUTPUT.PUT_LINE('');

    DBMS_OUTPUT.PUT_LINE('→ Cleaning up test data...');

    -- Remove dados de execuções anteriores
    DELETE FROM Locomotive_Train WHERE Locomotiveid = v_mega_loco_id;
    DELETE FROM Locomotive WHERE id = v_mega_loco_id;
    DELETE FROM Locomotive_Model WHERE id = v_mega_model_id;

    -- 1. Criar modelo e locomotiva (800 metros)
    INSERT INTO Locomotive_Model (id, model, numberBogies, bogies, power, length, width, height, maxSpeed,
                                  operationalSpeed, traction, weight, dieselID, electricID)
    VALUES (v_mega_model_id, 'ULTRA-LONG', 2, 'Bo-Bo', 9000, 800, 3, 4, 100, 50, 400, 150, 1, 1);

    INSERT INTO Locomotive (id, name, make, modelID, entryDate, startEndpoint)
    VALUES (v_mega_loco_id, 'The-Long-One', 'TestBuild', v_mega_model_id, SYSDATE, 50);

    -- Tentar adicionar a locomotiva ao comboio para disparar o gatilho
    BEGIN
        INSERT INTO Locomotive_Train (Locomotiveid, Trainid)
        VALUES (v_mega_loco_id, v_train_id);

        DBMS_OUTPUT.PUT_LINE('[FAILURE] The system allowed a length > 750m!');
    EXCEPTION
        WHEN OTHERS THEN
            -- Captura o erro customizado do Gatilho
            IF SQLCODE = -20020 THEN
                DBMS_OUTPUT.PUT_LINE('[SUCCESS] Trigger blocked the insertion correctly.');
                DBMS_OUTPUT.PUT_LINE('[ERROR MESSAGE] ' || SQLERRM);
            ELSE
                DBMS_OUTPUT.PUT_LINE('[FAILURE] Unexpected error: ' || SQLERRM);
            END IF;
            DBMS_OUTPUT.PUT_LINE('=========================================================');

    END;

    ROLLBACK;
END;
/
```


### Explanation of the Flow

**1. Setup**: Creates a locomotive model and a physical unit measuring 800m.

**2. Error Trigger**: When attempting to insert into the table `Locomotive_Train`, the trigger detects that the length (800m + what already existed on the train) exceeds the limit.

**3. Confirmation**: The test block captures the error `-20020` and confirms it `[SUCCESS] Trigger blocked the insertion correctly`.

---

## Database Schema Dependencies

**Tables Involved:**

- `Locomotive_Train`: Association table where the trigger acts.

- `Locomotive` & `Locomotive_Model`: Provide the length of the locomotives.

- `Freight` & `Wagon_Freight`: They connect the train to its wagons.

- `Wagon` & `Wagon_Model`: Provide the length of the wagons (in mm).

---

## Error Management

| Error Code | Description           | Trigger Condition |
|------------|-----------------------|-------------------|
| -20020     | Length Limit Exceeded | The combined length of the locomotives and wagons exceeds 750m. |

### Error Recovery

Any violation of the limit results in an `ROLLBACK` automatic insertion/update transaction, ensuring that no train travels with an illegal length.