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