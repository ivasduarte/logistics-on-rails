# USBD25 - List Non-Stop Endpoints #

## 1. Function

### PL/SQL Code
```sql
CREATE OR REPLACE FUNCTION fncGetNonStopEndpoints(p_train_id IN NUMBER)
RETURN SYS_REFCURSOR
IS
    v_cursor SYS_REFCURSOR;
BEGIN
    OPEN v_cursor FOR
        WITH train_path_endpoints AS (
            SELECT ep.EndpointendpointID
            FROM Train t
            JOIN Path p ON t.pathID = p.id
            JOIN Endpoint_Path ep ON ep.Pathid = p.id
            WHERE t.id = p_train_id
        ),
        path_positions AS (
            SELECT
                TO_NUMBER(REGEXP_SUBSTR(p.sequenceOrder, '[^,]+', 1, LEVEL)) AS endpoint_id,
                LEVEL as seq_position
            FROM Train t
            JOIN Path p ON t.pathID = p.id
            WHERE t.id = p_train_id
            CONNECT BY LEVEL <= REGEXP_COUNT(p.sequenceOrder, ',') + 1
                AND PRIOR t.id = t.id
                AND PRIOR DBMS_RANDOM.VALUE IS NOT NULL
        ),
        consecutive_pairs AS (
            SELECT DISTINCT
                pp1.endpoint_id as stop1,
                pp2.endpoint_id as stop2
            FROM path_positions pp1
            JOIN path_positions pp2 ON pp2.seq_position = pp1.seq_position + 1
        )
        SELECT DISTINCT
            e.endpointID,
            e.name AS endpoint_name,
            et."desc" AS endpoint_type,
            l.latitude,
            l.longitude
        FROM consecutive_pairs cp
        JOIN Rail_Line rl1 ON (
            rl1.startID = cp.stop1
        )
        JOIN Rail_Line rl2 ON (
            rl2.id != rl1.id
            AND rl2.endID = cp.stop2
            AND rl1.endID = rl2.startID
        )
        JOIN Endpoint e ON e.endpointID = rl1.endID
        JOIN Endpoint_Type et ON e.typeID = et.id
        JOIN Location l ON e.locationID = l.id
        WHERE e.endpointID NOT IN (SELECT EndpointendpointID FROM train_path_endpoints)
        ORDER BY e.name;

    RETURN v_cursor;
END fncGetNonStopEndpoints;
```

### Explanation

1. **Purpose**: Returns a pointer to a dataset containing all endpoints that a train passes through but does not stop at during its planned route.
2. **Function Parameters**:
    - **Input**: `p_train_id` - The ID of the train
    - **Output**: `SYS_REFCURSOR` - A dynamic result set
3. **Logic**:
    - **train_path_endpoints**: Retrieves all stops for the train from the path
    - **path_positions**: Parses the `sequenceOrder` field to extract the ordered list of stops
    - **consecutive_pairs**: Identifies pairs of consecutive stops (stop1 → stop2)
    - **Main Query**: Finds intermediate endpoints by:
        - Looking for two rail lines that form a chain: stop1 → intermediate → stop2
        - `rl1` must start at stop1 (directional)
        - `rl2` must end at stop2 (directional)
        - The two lines must connect: `rl1.endID = rl2.startID`
        - The intermediate endpoint (where lines meet) is returned if it's not in the official stop list
4. **Joins**:
    - **Train to Path**: Gets the train's planned route
    - **Rail Lines**: Finds directional connections between consecutive stops
    - **Endpoint Details**: Joins with `Endpoint`, `Endpoint_Type`, and `Location` to get complete information

## 2. Anonymous Blocks

### SQL Code (Using ID = 5421)
```sql
DECLARE
    v_cursor SYS_REFCURSOR;
    v_endpoint_id NUMBER;
    v_endpoint_name VARCHAR2(255);
    v_endpoint_type VARCHAR2(255);
    v_latitude VARCHAR2(255);
    v_longitude VARCHAR2(255);
    v_count NUMBER := 0;
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('USBD25: Non-Stop Endpoints for Train ID 5421');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('');

    v_cursor := fncGetNonStopEndpoints(5421);

    LOOP
        FETCH v_cursor INTO v_endpoint_id, v_endpoint_name, v_endpoint_type,
                           v_latitude, v_longitude;
        EXIT WHEN v_cursor%NOTFOUND;

        v_count := v_count + 1;
        DBMS_OUTPUT.PUT_LINE('Endpoint ID: ' || v_endpoint_id);
        DBMS_OUTPUT.PUT_LINE('  Name: ' || v_endpoint_name);
        DBMS_OUTPUT.PUT_LINE('  Type: ' || v_endpoint_type);
        DBMS_OUTPUT.PUT_LINE('  Location: ' || v_latitude || ', ' || v_longitude);
        DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    END LOOP;

    CLOSE v_cursor;

    IF v_count = 0 THEN
        DBMS_OUTPUT.PUT_LINE('No non-stop endpoints found (train stops at all endpoints on route).');
    ELSE
        DBMS_OUTPUT.PUT_LINE('');
        DBMS_OUTPUT.PUT_LINE('Total non-stop endpoints: ' || v_count);
    END IF;
END;
```

### Explanation

1. **Declarations**: Defines all variables needed to store the endpoint information, plus a counter for tracking results
2. **Calling the function**: `v_cursor := fncGetNonStopEndpoints(5421);` opens the cursor for the specified train
3. **Loop**:
    - **FETCH**: Retrieves one row at a time into the local variables
    - **EXIT WHEN**: Breaks the loop when `v_cursor%NOTFOUND` returns true
    - **DBMS_OUTPUT**: Prints a formatted list of non-stop endpoints to the console
    - **Counter**: Increments `v_count` for each endpoint found
4. **Final Output**:
    - If no endpoints found, displays a message indicating the train stops everywhere
    - Otherwise, displays the total count of non-stop endpoints
