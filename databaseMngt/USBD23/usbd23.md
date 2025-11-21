# USBD23 - List Line Segments  #

## 1. Function 

### PL/SQL Code

```sql
CREATE OR REPLACE FUNCTION fncGetTrainRouteSegments(p_train_id IN NUMBER)
RETURN SYS_REFCURSOR
IS
    v_cursor SYS_REFCURSOR;
BEGIN
    OPEN v_cursor FOR
        SELECT DISTINCT
            ls.id AS segment_id,
            ls.orderPosition AS segment_order,
            rl.name AS rail_line_name,
            ls.length AS segment_length_meters,
            ls.max_weight AS max_weight_tons,
            lt.isElectric AS is_electric,
            lt.numberTracks AS number_of_tracks,
            e_start.name AS start_point,
            e_end.name AS end_point
        FROM Train t
        JOIN Path p ON t.pathID = p.id
        JOIN Endpoint_Path ep_start ON ep_start.Pathid = p.id
        JOIN Endpoint_Path ep_end ON ep_end.Pathid = p.id
        JOIN Rail_Line rl ON (
            (rl.startID = ep_start.EndpointendpointID AND rl.endID = ep_end.EndpointendpointID) OR
            (rl.startID = ep_end.EndpointendpointID AND rl.endID = ep_start.EndpointendpointID)
        )
        JOIN Line_Segment ls ON ls.Rail_LineId = rl.id
        JOIN Line_Type lt ON ls.Line_TypeID = lt.id
        JOIN Endpoint e_start ON rl.startID = e_start.endpointID
        JOIN Endpoint e_end ON rl.endID = e_end.endpointID
        WHERE t.id = p_train_id
        ORDER BY ls.orderPosition;

    RETURN v_cursor;
END fncGetTrainRouteSegments;
```

### Explanation

1. **Purpose**: Returns a pointer to a dataset, which contains attributes of all the line segments a certain train will need to traverse.
2. **Function Parameters**: 
   - **Input**: `p_train_id` - The ID of the train
   - **Output**: `SYS_REF` - A dynamic result set
3. **Joins**:
   - **Train to Path**;
   - **Segment Details** : By joining `Line_Segment` and `Line_Type` to get all the necessary information;

## 2. Anonymous Blocks

### SQL Code (Using ID = 5421)

```sql
DECLARE
    v_cursor SYS_REFCURSOR;
    v_segment_id NUMBER;
    v_segment_order NUMBER;
    v_rail_line_name VARCHAR2(255);
    v_length NUMBER;
    v_max_weight FLOAT;
    v_is_electric VARCHAR2(255);
    v_num_tracks NUMBER;
    v_start_point VARCHAR2(255);
    v_end_point VARCHAR2(255);
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('USBD23: Line Segments for Train ID 5421');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('');

    v_cursor := fncGetTrainRouteSegments(5421);

    LOOP
        FETCH v_cursor INTO v_segment_id, v_segment_order, v_rail_line_name,
                           v_length, v_max_weight, v_is_electric, v_num_tracks,
                           v_start_point, v_end_point;

        EXIT WHEN v_cursor%NOTFOUND;

        DBMS_OUTPUT.PUT_LINE('Segment ID: ' || v_segment_id);
        DBMS_OUTPUT.PUT_LINE('  Rail Line: ' || v_rail_line_name);
        DBMS_OUTPUT.PUT_LINE('  Length: ' || v_length || ' meters');
        DBMS_OUTPUT.PUT_LINE('  Electric: ' || v_is_electric);
        DBMS_OUTPUT.PUT_LINE('  Tracks: ' || v_num_tracks);
        DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    END LOOP;

    CLOSE v_cursor;

    IF v_segment_id IS NULL THEN
        DBMS_OUTPUT.PUT_LINE('No segments found for this train.');
    END IF;
END;
```

### Explanation

1. **Declarations**: Defines all variables needed to store the information;
2. **Calling the funcion**: `v_cursor := fncGetTrainRouteSegments(5421);` opens the cursor for train with ID 5421;
3. **Loop**: 
- **FETCH**:Retrieves one row at a time into the local variables;
- **EXIT WHEN**: Breaks the loop when the statement `v_cursor%NOTFOUND` returns true;
- **DBMS_OUTPUT**: Prints a formatted list of the segment properties to the console.