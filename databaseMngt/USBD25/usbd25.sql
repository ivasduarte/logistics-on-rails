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
        route_rail_lines AS (
            SELECT DISTINCT rl.id, rl.startID, rl.endID
            FROM Train t
            JOIN Path p ON t.pathID = p.id
            JOIN Endpoint_Path ep1 ON ep1.Pathid = p.id
            JOIN Endpoint_Path ep2 ON ep2.Pathid = p.id
            JOIN Rail_Line rl ON (
                (rl.startID = ep1.EndpointendpointID AND rl.endID = ep2.EndpointendpointID) OR
                (rl.startID = ep2.EndpointendpointID AND rl.endID = ep1.EndpointendpointID)
            )
            WHERE t.id = p_train_id
        ),
        all_route_endpoints AS (
            SELECT DISTINCT endpointID
            FROM Endpoint e
            WHERE EXISTS (
                SELECT 1 FROM route_rail_lines rrl
                WHERE e.endpointID = rrl.startID OR e.endpointID = rrl.endID
            )
        )

        SELECT
            e.endpointID,
            e.name AS endpoint_name,
            et."desc" AS endpoint_type,
            l.latitude,
            l.longitude
        FROM all_route_endpoints are
        JOIN Endpoint e ON are.endpointID = e.endpointID
        JOIN Endpoint_Type et ON e.typeID = et.id
        JOIN Location l ON e.locationID = l.id
        WHERE NOT EXISTS (
            SELECT 1 FROM train_path_endpoints tpe
            WHERE tpe.EndpointendpointID = e.endpointID
        )
        ORDER BY e.name;

    RETURN v_cursor;
END fncGetNonStopEndpoints;