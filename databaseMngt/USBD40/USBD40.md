# USBD40 - Resgiter a Freight in the Database

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
CREATE OR REPLACE FUNCTION fncRegisterFreight(
    p_origin      IN NUMBER,
    p_destination IN NUMBER,
    p_date        IN DATE,
    p_train_id    IN NUMBER,
    p_model_id    IN NUMBER,
    p_quantity    IN NUMBER
)
    RETURN NUMBER
    IS
    v_freight_id     NUMBER;
    v_origin_exists  NUMBER;
    v_dest_exists    NUMBER;

    -- Cursor para encontrar vagões disponíveis na origem
CURSOR c_available_wagons IS
SELECT w.id
FROM Wagon w
WHERE w.modelID = p_model_id
  AND (
    -- Caso 1: Nunca viajou e está na base inicial correta
    (w.startEndpoint = p_origin
        AND NOT EXISTS (SELECT 1 FROM Wagon_Freight wf WHERE wf.Wagonid = w.id))
        OR
        -- Caso 2: A última viagem terminou na origem atual
    (p_origin = (
        SELECT f.destination
        FROM Freight f
                 JOIN Wagon_Freight wf ON f.id = wf.Freightid
        WHERE wf.Wagonid = w.id
        ORDER BY f."date" DESC, f.id DESC
            FETCH FIRST 1 ROWS ONLY
    ))
    )
  -- Garante que o vagão não está numa carga futura ou a decorrer
  -- e limita à quantidade que precisamos
    FETCH FIRST p_quantity ROWS ONLY;

-- Coleção para guardar os IDs encontrados
TYPE t_wagon_ids IS TABLE OF NUMBER;
    v_wagons_found t_wagon_ids;

BEGIN
    -- Validações
    IF p_quantity <= 0 THEN
        RAISE_APPLICATION_ERROR(-20030, 'Quantity must be greater than 0.');
END IF;

    -- Verificar se Origem e Destino existem
SELECT COUNT(*) INTO v_origin_exists FROM Endpoint WHERE endpointID = p_origin;
SELECT COUNT(*) INTO v_dest_exists FROM Endpoint WHERE endpointID = p_destination;

IF v_origin_exists = 0 OR v_dest_exists = 0 THEN
        RAISE_APPLICATION_ERROR(-20031, 'Origin or Destination Endpoint not found.');
END IF;

    -- Procurar Vagões (Bulk Collect para memória)
OPEN c_available_wagons;
FETCH c_available_wagons BULK COLLECT INTO v_wagons_found;
CLOSE c_available_wagons;

-- Verificar se encontrámos suficientes
IF v_wagons_found.COUNT < p_quantity THEN
        RAISE_APPLICATION_ERROR(-20032,
                                'Not enough wagons available at Origin. Requested: ' || p_quantity ||
                                ', Available: ' || v_wagons_found.COUNT);
END IF;

    -- Criar a Carga (Freight)
    -- Gerar novo ID (Max + 1)
SELECT NVL(MAX(id), 0) + 1 INTO v_freight_id FROM Freight;

INSERT INTO Freight (id, "date", origin, destination, trainID)
VALUES (v_freight_id, p_date, p_origin, p_destination, p_train_id);

-- Associar os Vagões Encontrados à Carga
FOR i IN 1..v_wagons_found.COUNT LOOP
            INSERT INTO Wagon_Freight (Wagonid, Freightid)
            VALUES (v_wagons_found(i), v_freight_id);
END LOOP;

    -- 6. Confirmar
COMMIT;

RETURN v_freight_id;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END fncRegisterFreight;
/
```

## Explanation of the Logic

**1. Business Validation**
- Ensures that the requested number of train cars is greater than zero and that the origin and destination points exist in the database.

**2. Wagon Selection**
- Uses a cursor to identify available railcars at the origin point based on two criteria:
  1. Wagons that have never made trips and whose starting point (`startEndpoint`) coincides with the origin.
  2. Railcars whose last recorded journey ended at their current point of origin.

**3. Resource Allocation**
- Captures only the necessary amount (`p_quantity`) using `BULK COLLECT` memory efficiency.

**4. Load Creation**
- Generates a new wagon ID (MAX + 1) and inserts the data into the table `Freight`.

**5. Association Creation**
- Creates the entries in the table `Wagon_Freight` to link each found wagon to the new cargo.


---

## Success Test (Valid Scenario)

### Test Code

The script attempts to register a load from Origin 50 (Leixões) to Destination 11 (Valença), requesting 2 wagons of model 1212.


```sql
DECLARE
v_freight_id NUMBER;
    v_origin     NUMBER := 50;   -- Leixões
    v_dest       NUMBER := 11;   -- Valença
    v_model      NUMBER := 1212;
    v_qtd        NUMBER := 2;    -- Queremos 2 vagões
BEGIN
    DBMS_OUTPUT.PUT_LINE('=========================================================');
    DBMS_OUTPUT.PUT_LINE('USBD40: Register a Freight In The Database Success Test');
    DBMS_OUTPUT.PUT_LINE('=========================================================');
    DBMS_OUTPUT.PUT_LINE('');

    DBMS_OUTPUT.PUT_LINE('Attempting to register cargo (Origin 50 -> Destination 11)...');

    v_freight_id := fncRegisterFreight(
            p_origin      => v_origin,
            p_destination => v_dest,
            p_date        => SYSDATE,
            p_train_id    => NULL,   -- Ainda sem comboio
            p_model_id    => v_model,
            p_quantity    => v_qtd
                    );

    DBMS_OUTPUT.PUT_LINE('   [SUCCESS] Cargo created with ID ' || v_freight_id);

    -- Verificar associações
FOR r IN (SELECT Wagonid FROM Wagon_Freight WHERE Freightid = v_freight_id) LOOP
            DBMS_OUTPUT.PUT_LINE('   -> Associated Wagon: ' || r.Wagonid);
END LOOP;

    -- Limpeza
    DBMS_OUTPUT.PUT_LINE('→ Cleaning test data...');
DELETE FROM Wagon_Freight WHERE Freightid = v_freight_id;
DELETE FROM Freight WHERE id = v_freight_id;
COMMIT;
DBMS_OUTPUT.PUT_LINE('   [CLEAN UP COMPLETE] Test data cleaned.');

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
END;
/
```

### Explanation of the Flow

**1. Function Call**: Invokes `fncRegisterFreight` with valid parameters where available train cars exist.

**2. Result**: The system creates the wagon with a new ID (e.g., 2052) and lists the IDs of the associated wagons (e.g., 3330001, 3330002).

**3. Clean-up**: Removes associations `Wagon_Freight` and records `Freight` to maintain test integrity.

**4. Confirmation**: Displays the message `[SUCCESS] Cargo created`.

---

## Error Testing

### Test Code

This test attempts to register a load from a location (Origin 1 - São Romão) where there are no available wagons.

```sql
DECLARE
v_freight_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('=========================================================');
    DBMS_OUTPUT.PUT_LINE('USBD40: Register a Freight In The Database Error Test');
    DBMS_OUTPUT.PUT_LINE('=========================================================');
    DBMS_OUTPUT.PUT_LINE('');

    DBMS_OUTPUT.PUT_LINE('Attempting to register cargo from a location WITHOUT railcars (Origin 1)...');
    DBMS_OUTPUT.PUT_LINE(' ');

    v_freight_id := fncRegisterFreight(
            p_origin      => 1,      -- São Romão
            p_destination => 50,
            p_date        => SYSDATE,
            p_train_id    => NULL,
            p_model_id    => 1212,
            p_quantity    => 1
                    );

    DBMS_OUTPUT.PUT_LINE('[ERROR] The system made it possible to create cargo without train cars.');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20032 THEN
            DBMS_OUTPUT.PUT_LINE('[TEST SUCCESSFUL] The system was blocked due to a lack of train cars.');
            DBMS_OUTPUT.PUT_LINE('[Error Message] ' || SQLERRM);
ELSE
            DBMS_OUTPUT.PUT_LINE('[UNEXPECTED ERROR] ' || SQLERRM);
END IF;

        DBMS_OUTPUT.PUT_LINE('=========================================================');

END;
/
```


### Explanation of the Flow

**1. Execution**: Attempts to request 1 train car at a station with no stock.

**2. Failure Detection**: The available wagons cursor returns a count lower than requested.

**3. Result**: The function throws an error `-20032`.

**4. Error Message**: `Not enough wagons available at Origin. Requested: 1, Available: 0`.

---

## Database Schema Dependencies

**Tables Involved:**

- `Freight`: Stores cargo information (ID, date, origin, destination, train).

- `Wagon`: Contains the fleet of wagons and their starting point.

- `Wagon_Freight`: A junction table that associates specific wagons with a load.

- `Endpoint`: Validates the existence of the origin and destination locations.

---

## Error Management

| Error Code | Description                     | Trigger Condition                                                 |
|------------|---------------------------------|-------------------------------------------------------------------|
| -20030     | Quantity Must Be Greater Than 0 | The parameter `p_quantity` is zero or negative.                   |
| -20031     | Origin/Destination Not Found    | The ID `Endpoint` does not exist in the database.                 |
| -20032     | Not Enough Wagons Available     | The number of train cars available at the origin is insufficient. |
