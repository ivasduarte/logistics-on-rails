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