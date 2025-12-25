# USBD43 - Add New Electric Locomotive Model

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
CREATE OR REPLACE FUNCTION fncAddElectricLocomotiveModel(
    p_voltage IN NUMBER,
    p_frequency IN NUMBER
)
RETURN NUMBER
IS
    v_electric_id NUMBER;
BEGIN
    -- Valida se existem todos os inputs necessários
    IF p_voltage IS NULL OR p_frequency IS NULL THEN
        RAISE_APPLICATION_ERROR(-20001, 'All parameters are required for electric locomotive model');
    END IF;

    -- Valida se os valores numéricos são positivos
    IF p_voltage <= 0 OR p_frequency <= 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Voltage and frequency must be positive values');
    END IF;

    -- Gera um novo id para a locomotiva elétrica
    SELECT NVL(MAX(id), 0) + 1 INTO v_electric_id FROM Locomotive_Electric;

    -- Insere uma locomotiva elétrica com as especificações necessárias
    INSERT INTO Locomotive_Electric (id, voltage, frequency)
    VALUES (v_electric_id, p_voltage, p_frequency);

    COMMIT;

    -- Retorna o id da locomotiva elétrica
    RETURN v_electric_id;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20003, 'Error adding electric locomotive model: ' || SQLERRM);
END fncAddElectricLocomotiveModel;
/
```

### Function Logic Explanation

1. **Input Validation**:
    - Validates that all required parameters (voltage, frequency) are provided
    - Raises an error if any required parameter is NULL

2. **Business Logic Validation**:
    - Ensures voltage and frequency values are positive

3. **Electric Locomotive Record Creation**:
    - Generates a new unique ID for the Locomotive_Electric table
    - Inserts electric-specific specifications (voltage and frequency)
    - Commits the transaction

4. **Return Value**:
    - Returns the newly created electric locomotive ID
   
5. **Error Handling**:
    - All database operations are wrapped in exception handling
    - Any errors trigger a ROLLBACK to maintain data consistency
    - Custom error messages are provided for different failure scenarios

---

## 2. Anonymous Block Testing

### Test Code

```sql
DECLARE
    v_new_electric_id NUMBER;
    v_electric_count_before NUMBER;
    v_electric_count_after NUMBER;

    -- Cursor para listar todas as locomotivas elétricas
    CURSOR c_electric_locomotives IS
        SELECT le.id, le.voltage, le.frequency
        FROM Locomotive_Electric le
        ORDER BY le.id;

BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('USBD43: Add Electric Locomotive Model Test');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('');

    -- Conta o número de locomotivas elétricas antes
    SELECT COUNT(*) INTO v_electric_count_before FROM Locomotive_Electric;
    DBMS_OUTPUT.PUT_LINE('Total electric locomotives before: ' || v_electric_count_before);
    DBMS_OUTPUT.PUT_LINE('');

    -- Lista todas as locomotivas elétricas antes
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('ELECTRIC LOCOMOTIVES BEFORE REGISTRATION');
    DBMS_OUTPUT.PUT_LINE('========================================');

    FOR rec IN c_electric_locomotives LOOP
        DBMS_OUTPUT.PUT_LINE('Electric Locomotive ID: ' || rec.id);
        DBMS_OUTPUT.PUT_LINE('  Voltage: ' || rec.voltage || ' V');
        DBMS_OUTPUT.PUT_LINE('  Frequency: ' || rec.frequency || ' Hz');
        DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    END LOOP;

    IF v_electric_count_before = 0 THEN
        DBMS_OUTPUT.PUT_LINE('No electric locomotives found in database.');
        DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    END IF;

    -- Regista uma nova locomotiva elétrica
    v_new_electric_id := fncAddElectricLocomotiveModel(
        p_voltage => 25000,
        p_frequency => 50
    );

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('New electric locomotive registered successfully!');

    -- Conta o número de locomotivas elétricas depois
    SELECT COUNT(*) INTO v_electric_count_after FROM Locomotive_Electric;
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('Total electric locomotives after: ' || v_electric_count_after);

    -- Lista todas as locomotivas elétricas depois
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('ELECTRIC LOCOMOTIVES AFTER REGISTRATION');
    DBMS_OUTPUT.PUT_LINE('========================================');

    FOR rec IN c_electric_locomotives LOOP
        DBMS_OUTPUT.PUT_LINE('Electric Locomotive ID: ' || rec.id);
        DBMS_OUTPUT.PUT_LINE('  Voltage: ' || rec.voltage || ' V');
        DBMS_OUTPUT.PUT_LINE('  Frequency: ' || rec.frequency || ' Hz');

        IF rec.id = v_new_electric_id THEN
            DBMS_OUTPUT.PUT_LINE('  >>> NEWLY REGISTERED <<<');
        END IF;

        DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    END LOOP;


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
END;
/
```

### Test Flow Explanation

1. **Initialization**:
    - Declares variables to track the test execution
    - Creates a cursor to retrieve all electric locomotive models

2. **Testing Before the Execution**:
    - Counts the total number of electric locomotive models before registration
    - Lists all existing electric locomotive models with their complete information:
        - Electric Locomotive ID
        - Voltage (V)
        - Frequency (Hz)
    - This establishes a baseline for comparison

3. **Function Execution**:
    - Calls `fncAddElectricLocomotiveModel` with test data:
        - Voltage: 25000 V AC
        - Frequency: 50 Hz
    - Captures the returned electric locomotive ID

4. **Testing After the Execution**:
    - Counts the total number of electric locomotive models after registration
    - Lists all electric locomotive models again, highlighting the newly registered one
    - Marks the new model with ">>> NEWLY REGISTERED <<<" for easy identification

---

## 3. Database Schema Dependencies

### Required Tables

**Locomotive_Electric**
- `id` (PRIMARY KEY)
- `voltage`
- `frequency`

### Relationships
```
Locomotive_Electric (1) ----< (N) Locomotive_Model
```
---

## 4. Error Handling

### Custom Error Codes

| Error Code | Description | Trigger Condition |
|------------|-------------|-------------------|
| -20001 | Missing Required Parameters | Any required parameter is NULL |
| -20002 | Invalid Numeric Values | Voltage or frequency is zero or negative |
| -20003 | General Registration Error | Any other error during registration |