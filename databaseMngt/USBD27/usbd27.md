# USBD27 – List Grain Wagons Used in a Given Period

## 1.Function

### PL/SQL Code

```
CREATE OR REPLACE FUNCTION fncGetGrainWagonsInTrains(p_start_date IN DATE,p_end_date   IN DATE)
RETURN SYS_REFCURSOR
IS
    v_cursor SYS_REFCURSOR;
BEGIN
    OPEN v_cursor FOR
        WITH Grain_Wagons AS (
            SELECT w.id AS wagon_id
            FROM Wagon w
            JOIN Wagon_Model wm ON wm.id = w.modelID
            JOIN Wagon_Hopper_Car whc ON whc.id = wm.id
            WHERE wm.typeID = 3
        ),
        Trains_With_Grain AS (
            SELECT DISTINCT f.trainID
            FROM Freight f
            JOIN Wagon_Freight wf ON wf.Freightid = f.id
            JOIN Grain_Wagons gw ON gw.wagon_id = wf.Wagonid
            WHERE f.data BETWEEN p_start_date AND p_end_date
              AND f.trainID IS NOT NULL
        ),
        Train_Count AS (
            SELECT COUNT(*) AS total_trains
            FROM Trains_With_Grain
        ),
        Grain_Wagon_Usage AS (
            SELECT
                wf.Wagonid AS wagon_id,
                COUNT(DISTINCT f.trainID) AS trains_used
            FROM Freight f
            JOIN Wagon_Freight wf ON wf.Freightid = f.id
            JOIN Grain_Wagons gw ON gw.wagon_id = wf.Wagonid
            JOIN Trains_With_Grain tw ON tw.trainID = f.trainID
            WHERE f.data BETWEEN p_start_date AND p_end_date
            GROUP BY wf.Wagonid
        )
        SELECT
            w.id AS wagon_id,
            wm.model AS wagon_model,
            whc.storedGrains AS grain_capacity
        FROM Grain_Wagon_Usage gu
        JOIN Train_Count tc ON 1 = 1
        JOIN Wagon w ON w.id = gu.wagon_id
        JOIN Wagon_Model wm ON wm.id = w.modelID
        JOIN Wagon_Hopper_Car whc ON whc.id = wm.id
        WHERE gu.trains_used = tc.total_trains;

    RETURN v_cursor;
END fncGetGrainWagonsInTrains;
```

### Explanation

### Purpose

The purpose of this function is to return a pointer (SYS_REFCURSOR) to a dataset containing all grain wagons (hopper cars) that were used in trains transporting freight within a given time period.
Each row corresponds to a wagon that carried grains in that interval.

**Function Parameters**

**Input**

- **p_start_date** – Start date (inclusive) of the period to analyse.
- **p_end_date** – End date (inclusive) of the period to analyse.

**Output**

**A SYS_REFCURSOR pointing to a result set with:**

- **wagon_id** – ID of the wagon.
- **wagon_model** – Model name of the wagon.
- **grain_capacity** – Grain storage capacity (from Wagon_Hopper_Car.storedGrains).

**Joins and Logic**

- **Freight f**:Representa cada transporte de mercadorias (freight), incluindo a data e o train associado.
- **Wagon_Freight wf**:Liga cada freight aos wagons que o transportaram.
- **Wagon w**:Identifica o wagon concreto usado no transporte.
- **Wagon_Model wm**:Dá informação do modelo do wagon (campo model).
- **Wagon_Hopper_Car whc**:Especialização de wagons de tipo hopper, com campos específicos para capacidade de grãos.
- **WHERE f.data BETWEEN p_start_date AND p_end_date**:Garante que apenas consideramos freights no período desejado.
- **DISTINCT**:Garante que cada wagon de grãos aparece pelo menos uma vez na lista, mesmo que tenha sido usado em vários freights dentro do período.


## Anonymous Block

## SQL Code


````

SET SERVEROUTPUT ON;
DECLARE
    v_cursor      SYS_REFCURSOR;
    v_wagon_id    Wagon.id%TYPE;
    v_model       Wagon_Model.model%TYPE;
    v_capacity    Wagon_Hopper_Car.storedGrains%TYPE;
    v_count       NUMBER := 0;
BEGIN
    DBMS_OUTPUT.PUT_LINE('==============================================');
    DBMS_OUTPUT.PUT_LINE(' USBD27 - Grain wagons used in all trains');
    DBMS_OUTPUT.PUT_LINE(' Period: 2025-10-01 to 2025-10-10');
    DBMS_OUTPUT.PUT_LINE('==============================================');
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('WAGON_ID | MODEL | GRAIN_CAPACITY');
    DBMS_OUTPUT.PUT_LINE('-------------------------------------');

    v_cursor := fncGetGrainWagonsUsedInAllTrains(p_start_date => DATE '2025-10-01',p_end_date   => DATE '2025-10-10');
    LOOP
        FETCH v_cursor INTO v_wagon_id, v_model, v_capacity;
        EXIT WHEN v_cursor%NOTFOUND;
        v_count := v_count + 1;
        DBMS_OUTPUT.PUT_LINE(v_wagon_id || ' | ' ||v_model    || ' | ' ||v_capacity);
    END LOOP;
    CLOSE v_cursor;
    DBMS_OUTPUT.PUT_LINE('');
    IF v_count = 0 THEN
        DBMS_OUTPUT.PUT_LINE('No grain wagons found for this period.');
    ELSE
        DBMS_OUTPUT.PUT_LINE('Total wagons: ' || v_count);
    END IF;
END;
````


**Explanation**
**Declarations:**
The anonymous block declares all variables needed to fetch and display the result set:

- **v_cursor** – SYS_REFCURSOR returned by the function fncGetGrainWagonsUsedInAllTrains;
- **v_wagon_id** – stores the wagon identifier;
- **v_model** – stores the wagon model description;
- **v_capacity** – stores the grain capacity from Wagon_Hopper_Car;
- **v_count** – counts how many wagons were found in the given period.

**Calling the function:**

- **v_cursor := fncGetGrainWagonsUsedInAllTrains(p_start_date => DATE '2025-10-01',p_end_date   => DATE '2025-10-10');**
- Opens the cursor for the period between 2025-10-01 and 2025-10-10.

**Loop**

- **FETCH:** retrieves each row from the cursor into the local variables;
- **EXIT WHEN:** v_cursor%NOTFOUND stops the loop when there are no more rows;
- **v_count** is incremented for each wagon found;


**Output**

- **DBMS_OUTPUT.PUT_LINE** prints a formatted line with wagon id, model and grain capacity.
- if `v_count = 0`, it prints “No grain wagons found for this period.”;

**Final summary:**

- After closing the cursor, the block checks v_count:
- **if v_count = 0**, it prints “No grain wagons found for this period.”;
- Otherwise, it prints the total number of wagons found in the given period.
