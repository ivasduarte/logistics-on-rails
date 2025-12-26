# USBD44 - Add Segment to Line (with Siding)

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
CREATE OR REPLACE FUNCTION fncAddSegmentToLine(
    p_rail_line_id   IN NUMBER,
    p_line_type_id   IN NUMBER,
    p_length         IN NUMBER,
    p_max_weight     IN NUMBER,
    p_has_siding     IN NUMBER, -- 1 for Yes, 0 for No
    p_siding_len     IN NUMBER DEFAULT NULL,
    p_siding_pos     IN NUMBER DEFAULT NULL
)
    RETURN NUMBER
IS
    v_rail_line_count NUMBER;
    v_line_type_count NUMBER;
    v_next_segment_id NUMBER;
    v_next_order_pos  NUMBER;
    v_next_siding_id  NUMBER;
BEGIN
    -- 1. Validation of Mandatory Inputs
    IF p_rail_line_id IS NULL OR p_line_type_id IS NULL OR p_length IS NULL OR p_max_weight IS NULL THEN
        RAISE_APPLICATION_ERROR(-20001, 'Rail Line, Line Type, Length, and Max Weight are required.');
    END IF;

    -- 2. Validate if Rail_Line exists
    SELECT COUNT(*) INTO v_rail_line_count
    FROM Rail_Line
    WHERE id = p_rail_line_id;

    IF v_rail_line_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Rail Line ID does not exist.');
    END IF;

    -- 3. Validate if Line_Type exists
    SELECT COUNT(*) INTO v_line_type_count
    FROM Line_Type
    WHERE id = p_line_type_id;

    IF v_line_type_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20003, 'Line Type ID does not exist.');
    END IF;

    -- 4. Specific Siding Validations (If applicable)
    IF p_has_siding = 1 THEN
        IF p_siding_len IS NULL OR p_siding_pos IS NULL THEN
            RAISE_APPLICATION_ERROR(-20004, 'Siding Length and Position are required when Siding is active.');
        END IF;

        IF p_siding_len >= p_length THEN
            RAISE_APPLICATION_ERROR(-20005, 'Siding length cannot be greater than or equal to the Segment length.');
        END IF;
    END IF;

    -- 5. Calculate next ID and next OrderPosition
    SELECT NVL(MAX(id), 0) + 1 INTO v_next_segment_id FROM Line_Segment;
    
    SELECT NVL(MAX(orderPosition), 0) + 1 INTO v_next_order_pos 
    FROM Line_Segment 
    WHERE Rail_LineId = p_rail_line_id;

    -- 6. Insert Segment
    INSERT INTO Line_Segment (id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
    VALUES (v_next_segment_id, p_rail_line_id, v_next_order_pos, p_max_weight, p_length, p_line_type_id);

    -- 7. Insert Siding Conditionally
    IF p_has_siding = 1 THEN
        SELECT NVL(MAX(id), 0) + 1 INTO v_next_siding_id FROM Siding;

        INSERT INTO Siding (id, position, length, segmentID)
        VALUES (v_next_siding_id, p_siding_pos, p_siding_len, v_next_segment_id);
    END IF;

    -- 8. Finalize Transaction
    COMMIT;

    RETURN v_next_segment_id;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END fncAddSegmentToLine;

```

### Function Logic Explanation

1. **Input Validation**:
* Validates that all critical parameters (Line ID, Type ID, Length, Weight) are provided.
* Raises error -20001 if any are missing.


2. **Entity Existence Validation**:
* Verifies that the parent `Rail_Line` exists.
* Verifies that the `Line_Type` exists.
* Ensures referential integrity before attempting insertion.


3. **Business Logic Validation (Siding)**:
* If `p_has_siding` is set to 1 (True), ensures `p_siding_len` and `p_siding_pos` are provided.
* **Logic Check**: Ensures the Siding length fits within the Segment length (Siding < Segment).


4. **ID Generation & Ordering**:
* Calculates the next Primary Key ID manually (`MAX(id) + 1`) as sequences are not defined in the schema.
* Calculates the next `orderPosition` for the specific Line to ensure the new segment is appended to the end of the line.


5. **Atomic Insertion Process**:
* Inserts the `Line_Segment`.
* If a siding is requested, inserts the `Siding` linked to the newly created segment.
* Uses `COMMIT` only after both operations succeed.


6. **Error Handling**:
* Uses `ROLLBACK` in the `EXCEPTION` block. This is critical: if the Segment is created but the Siding fails (e.g., due to constraint), the Segment is also removed to maintain the "All or Nothing" rule.



---

## 2. Anonymous Block Testing

### Test Code

The test anonymous block is provided in the artifact `usbd44_anonymousBlock.sql`. It demonstrates:

* Displaying line segments before insertion.
* Executing the function to add a segment WITH a siding.
* Verifying the new segment appears at the end of the list.
* Verifying the siding is correctly linked to the new segment.

### Test Flow Explanation

1. **Initialization**:
* Defines test variables (Target Line ID: 1, Length: 1500, Siding Length: 300).
* Opens cursors for `Line_Segment` and `Siding`.


2. **Pre-Insertion State**:
* Iterates through existing segments of Line 1 and prints their IDs and order.
* Lists existing sidings.


3. **Function Execution**:
* Calls `fncAddSegmentToLine` with `p_has_siding => 1`.
* Captures the returned `v_new_segment_id`.


4. **Post-Insertion State**:
* Re-runs the cursors.
* Validates that a new Segment exists with `orderPosition` = (previous max + 1).
* Validates that a new Siding exists where `segmentID` matches the returned ID.



---

## 3. Error Scenario Testing

The error scenario test (`ErrorCase/usbd44_anonymousBlock.sql`) validates the following cases:

1. **NULL Mandatory Parameters**: Ensures the function rejects null inputs for Line/Type/Length.
2. **Non-existent Rail Line ID**: Tests referential integrity check for Line.
3. **Non-existent Line Type ID**: Tests referential integrity check for Type.
4. **Siding Active but Missing Details**: Ensures that if `p_has_siding=1`, length and position must be provided.
5. **Invalid Siding Logic**: Tests the rule that Siding Length cannot be greater than Segment Length.

---

## 4. Database Schema Dependencies

### Required Tables

**Rail_Line**

* `id` (PRIMARY KEY)
* `name`

**Line_Type**

* `id` (PRIMARY KEY)
* `isElectric`
* `numberTracks`

**Line_Segment** (Main Entity)

* `id` (PRIMARY KEY)
* `Rail_LineId` (FK to Rail_Line)
* `orderPosition` (Determines sequence in the line)
* `length`
* `max_weight`
* `Line_TypeID` (FK to Line_Type)

**Siding** (Optional Sub-Entity)

* `id` (PRIMARY KEY)
* `segmentID` (FK to Line_Segment)
* `length`
* `position`

### Relationships

```
Rail_Line (1) ----< (N) Line_Segment
Line_Segment (1) ----< (0..1) Siding

```

### Key Points

* Segments are ordered within a Rail Line using `orderPosition`.
* A Siding is a child entity of a Segment.
* The creation of a Siding is dependent on the creation of a Segment.

---

## 5. Error Handling

### Custom Error Codes

| Error Code | Description | Trigger Condition |
| --- | --- | --- |
| -20001 | Missing Required Parameters | Line, Type, Length, or Weight is NULL |
| -20002 | Rail Line Not Found | Specified `p_rail_line_id` does not exist |
| -20003 | Line Type Not Found | Specified `p_line_type_id` does not exist |
| -20004 | Siding Params Missing | `p_has_siding` is 1, but length/pos is NULL |
| -20005 | Invalid Siding Length | Siding length >= Segment length |

### Error Recovery

All errors trigger an automatic `ROLLBACK`. This ensures that we never end up with a "Ghost Segment" (a segment that was supposed to have a siding but doesn't, because the siding insertion failed).

---

## Usage Example

```sql
-- Add a 2000m segment with a 400m siding to Line ID 1
DECLARE
    v_new_id NUMBER;
BEGIN
    v_new_id := fncAddSegmentToLine(
        p_rail_line_id => 1,
        p_line_type_id => 1,
        p_length       => 2000,
        p_max_weight   => 8000,
        p_has_siding   => 1,    -- Enable Siding
        p_siding_len   => 400,  -- Siding Length
        p_siding_pos   => 100   -- Position relative to segment start
    );
    
    DBMS_OUTPUT.PUT_LINE('New Segment Created with ID: ' || v_new_id);
END;
/

```

---

## Notes

* **Auto-Increment**: The function implements manual auto-increment (`MAX(id)+1`) because the provided schema does not use Sequences or Identity columns.
* **Order Management**: The function automatically places the new segment at the **end** of the line by calculating the maximum existing `orderPosition`.
* **Atomicity**: The Segment and Siding creation are treated as a single atomic transaction.

```