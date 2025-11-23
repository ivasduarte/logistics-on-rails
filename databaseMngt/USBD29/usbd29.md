# USBD29 – Percentage of Electric Locomotives

## 1.Function

### PL/SQL Code

```
CREATE OR REPLACE FUNCTION fncGetElectricLocomotivePercentage(p_operator IN Operator.shortName%TYPE)
RETURN SYS_REFCURSOR
IS
    v_cursor SYS_REFCURSOR;
BEGIN
    OPEN v_cursor FOR
        SELECT
            t.operator                      AS operator_code,
            o.name                          AS operator_name,
            COUNT(DISTINCT l.id)            AS total_locomotives,
            COUNT(DISTINCT CASE WHEN lt.name = 'Electric' THEN l.id END)
                         AS electric_locomotives,
            CASE
                WHEN COUNT(DISTINCT l.id) = 0 THEN 0
                ELSE ROUND(COUNT(DISTINCT CASE WHEN lt.name = 'Electric' THEN l.id END) * 100/ COUNT(DISTINCT l.id),2)
            END AS percentage_electric
        FROM Train t
        JOIN Operator o
          ON o.shortName = t.operator
        JOIN Locomotive_Train lt_rel
          ON lt_rel.Trainid = t.id
        JOIN Locomotive l
          ON l.id = lt_rel.Locomotiveid
        JOIN Locomotive_Model lm
          ON lm.id = l.modelID
        JOIN Locomotive_Type lt
          ON lt.id = lm.typeID
        WHERE t.operator = p_operator
        GROUP BY t.operator, o.name;
    RETURN v_cursor;
END fncGetElectricLocomotivePercentage;
```

### Explanation

**Purpose:** This function calculates how many locomotives an operator uses and what percentage of them are electric.
It returns a dynamic result set (SYS_REFCURSOR) summarizing:

- Total locomotives
- Electric locomotives
- Percentage of electric locomotives
- Operator code and full name
- Function Parameters

**Input:**

- **p_operator** – Short code of the operator (e.g., 'Medway', 'Captrain')

**Output:**

- **SYS_REFCURSOR** with a single row containing the aggregated statistics.

### Logic

1. **Filter trains by operator**

- The function begins by selecting all trains for the given operator:

`FROM Train t WHERE t.operator = p_operator`

- This ensures all calculations are specific to that operator.

2. **Retrieve locomotives for those trains**

- The Locomotive_Train table links each train to its assigned locomotives:

`JOIN Locomotive_Train lt_rel ON lt_rel.Trainid = t.id`

3. **Get locomotive technical information**

- To determine whether a locomotive is electric or diesel:

```
JOIN Locomotive l
ON l.id = lt_rel.Locomotiveid
JOIN Locomotive_Model lm
ON lm.id = l.modelID
JOIN Locomotive_Type lt
ON lt.id = lm.typeID
```

- The field lt.name gives 'Electric' or 'Diesel'.

4. **Count total and electric locomotives**

- The function uses conditional aggregation:

`COUNT(DISTINCT l.id)           -- total COUNT(DISTINCT CASE WHEN lt.name = 'Electric' THEN l.id END)`

- Using DISTINCT prevents counting the same locomotive twice.

5. **Percentage calculation**

- If no locomotives exist, return 0% to avoid division by zero:

`CASE WHEN total = 0 THEN 0 ELSE ROUND((electric / total) * 100, 2) END`

# 2.Anonymous Block

## SQL Code (Using operator = Medway)

```
SET SERVEROUTPUT ON;
DECLARE
v_cursor        SYS_REFCURSOR;
v_op_code       Operator.shortName%TYPE;
v_op_name       Operator.name%TYPE;
v_total         NUMBER;
v_electric      NUMBER;
v_percentage    NUMBER;
v_count_rows    NUMBER := 0;
BEGIN
DBMS_OUTPUT.PUT_LINE('==============================================');
DBMS_OUTPUT.PUT_LINE('USBD29 - Percentage of electric locomotives');
DBMS_OUTPUT.PUT_LINE('Operator: Medway');
DBMS_OUTPUT.PUT_LINE('==============================================');
DBMS_OUTPUT.PUT_LINE('');
v_cursor := fncGetElectricLocomotivePercentage('Medway');
LOOP
FETCH v_cursor INTO v_op_code,
v_op_name,
v_total,
v_electric,
v_percentage;
EXIT WHEN v_cursor%NOTFOUND;
v_count_rows := v_count_rows + 1;
DBMS_OUTPUT.PUT_LINE('Operator code: ' || v_op_code);
DBMS_OUTPUT.PUT_LINE('Operator name: ' || v_op_name);
DBMS_OUTPUT.PUT_LINE('Total locomotives: ' || v_total);
DBMS_OUTPUT.PUT_LINE('Electric locomotives: ' || v_electric);
DBMS_OUTPUT.PUT_LINE('Percentage electric: ' || v_percentage || ' %');
DBMS_OUTPUT.PUT_LINE('----------------------------------------------');
END LOOP;
CLOSE v_cursor;
IF v_count_rows = 0 THEN
DBMS_OUTPUT.PUT_LINE('No locomotives found for this operator.');
END IF;
END;
```

### Explanation

- **Declarations**: Defines all variables needed to store the endpoint information, plus a counter for tracking results;
- **Calling the Function:**` v_cursor := fncGetElectricLocomotivePercentage('Medway');`This opens a cursor containing the summary for operator Medway.
- **Loop:**

  - **FETCH:** retrieves the summary row;
  - **EXIT WHEN:** v_cursor%NOTFOUND ensures the loop ends;
  - **DBMS_OUTPUT.PUT_LINE:** prints the results in a clean, readable format;
  - **Counter:** increments `v_count`for each electric locomotive found.
- **Output:**
- `DBMS_OUTPUT.PUT_LINE` prints a formatted line with operator identification,number of locomotives,number of electric locomotives and percentage electric.
- if `v_count_rows = 0`, it prints “No locomotives found for this operator.”;
