-- Function to list pending freights from a specific rail line
CREATE OR REPLACE FUNCTION get_pending_freights_detailed(
    p_id_rail_line IN RAIL_LINE.ID%TYPE
) RETURN SYS_REFCURSOR IS
    v_cursor SYS_REFCURSOR;
BEGIN
    OPEN v_cursor FOR
        SELECT
            f.id,
            f.origin AS origin_id,
            f.destination AS dest_id,
            f.trainID,
            e_orig.name AS origin_name,
            e_dest.name AS dest_name
        FROM Freight f
                 JOIN Endpoint e_orig ON f.origin = e_orig.endpointID
                 JOIN Endpoint e_dest ON f.destination = e_dest.endpointID
                 JOIN Train t ON f.trainID = t.id
                 JOIN Rail_Line_Path rlp ON t.pathID = rlp.Pathid
        WHERE rlp.Rail_Lineid = p_id_rail_line
          AND f."date" IS NULL;
    RETURN v_cursor;
END;
/

-- Function to list the route plan for a specific freight
CREATE OR REPLACE FUNCTION get_route_plan_detailed(
    p_line_id NUMBER,
    p_freight_id NUMBER
)
    RETURN SYS_REFCURSOR IS
    v_cursor SYS_REFCURSOR;
BEGIN
    OPEN v_cursor FOR
        SELECT step_info AS "STEP_DESCRIPTION" FROM ( -- Adicionado Alias para o Java
                                                        SELECT
                                                            rlp."order" as sort_key,
                                                            'Order ' || LPAD(rlp."order", 2, '0') || ': ' || e.name ||
                                                            ' (Arr: ' || TO_CHAR(rlp.ARRIVALTIME, 'HH24:MI') ||
                                                            ' | Dep: ' || TO_CHAR(rlp.DEPARTURETIME, 'HH24:MI') || ')' ||
                                                            CASE WHEN e.endpointID = f.origin THEN ' -> [ORIGIN: LOAD]' ELSE '' END as step_info
                                                        FROM Freight f
                                                                 JOIN Train t ON f.trainID = t.id
                                                                 JOIN Rail_Line_Path rlp ON t.pathID = rlp.Pathid
                                                                 JOIN Rail_Line rl ON rlp.Rail_Lineid = rl.id
                                                                 JOIN Endpoint e ON rl.startID = e.endpointID
                                                        WHERE f.id = p_freight_id

                                                        UNION ALL

                                                        SELECT
                                                            999 as sort_key,
                                                            'Final Destination: ' || e.name || ' -> [DESTINATION: UNLOAD]' as step_info
                                                        FROM Freight f
                                                                 JOIN Train t ON f.trainID = t.id
                                                                 JOIN Rail_Line_Path rlp ON t.pathID = rlp.Pathid
                                                                 JOIN Rail_Line rl ON rlp.Rail_Lineid = rl.id
                                                                 JOIN Endpoint e ON rl.endID = e.endpointID
                                                        WHERE f.id = p_freight_id
                                                          AND rlp."order" = (SELECT MAX("order") FROM Rail_Line_Path WHERE Pathid = t.pathID)
                                                    )
        ORDER BY sort_key ASC;

    RETURN v_cursor;
END;
/

-- Procedure to assign a route (Endpoints of a Rail_Line) to a freight
CREATE OR REPLACE PROCEDURE pr_assign_route_by_line(
    p_id_freight   IN Freight.ID%TYPE,
    p_id_rail_line IN RAIL_LINE.ID%TYPE
) IS
    v_start_node Endpoint.endpointID%TYPE;
    v_end_node   Endpoint.endpointID%TYPE;
    v_train_id   Train.id%TYPE;
BEGIN
    -- Directly obtain the start and end endpoints of the Rail_Line defined in the template.
    SELECT startID, endID INTO v_start_node, v_end_node
    FROM Rail_Line
    WHERE id = p_id_rail_line;

    -- Get a train associated with this line (via Rail_Line_Path)
    SELECT t.id INTO v_train_id
    FROM Train t
             JOIN Rail_Line_Path rlp ON t.pathID = rlp.Pathid
    WHERE rlp.Rail_Lineid = p_id_rail_line
      AND ROWNUM = 1;

    -- Update the freight with the line endpoints and the train found.
    UPDATE Freight
    SET origin = v_start_node,
        destination = v_end_node,
        trainID = v_train_id
    WHERE id = p_id_freight;

    COMMIT;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20001, 'Insufficient data found to assign route to freight.');
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/