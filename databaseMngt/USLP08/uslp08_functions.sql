-- Função para listar fretes pendentes
CREATE OR REPLACE FUNCTION get_pending_freights_detailed RETURN SYS_REFCURSOR IS
    rc SYS_REFCURSOR;
BEGIN
    OPEN rc FOR
        SELECT
            f.id,
            'Freight Record ' || f.id as cargo_description,
            e1.name AS origin_station,
            e2.name AS destination_station
        FROM Freight f
                 JOIN Endpoint e1 ON f.origin = e1.endpointID
                 JOIN Endpoint e2 ON f.destination = e2.endpointID
        WHERE f.trainID IS NULL;
    RETURN rc;
END;
/


-- Associar uma rota a um frete
CREATE OR REPLACE FUNCTION get_route_plan_detailed(
    p_rail_line_id IN NUMBER,
    p_freight_id IN NUMBER
) RETURN SYS_REFCURSOR IS
    rc SYS_REFCURSOR;
    v_origin NUMBER;
    v_dest NUMBER;
BEGIN
    -- Obtém apenas os pontos de extremidade do frete
    SELECT origin, destination INTO v_origin, v_dest FROM Freight WHERE id = p_freight_id;

    OPEN rc FOR
        SELECT
            rlp."order" as stop_order,
            e.name AS station_name,
            CASE
                WHEN e.endpointID = v_origin THEN 'LOAD FREIGHT'
                WHEN e.endpointID = v_dest THEN 'UNLOAD FREIGHT'
                ELSE 'PASSING'
                END as action_to_perform
        FROM Rail_Line_Path rlp
                 JOIN Endpoint e ON rlp.EndpointID = e.endpointID
        WHERE rlp.Rail_Lineid = p_rail_line_id
        ORDER BY rlp."order" ASC;
    RETURN rc;
END;
/


CREATE OR REPLACE PROCEDURE assign_train_to_freight(
    p_freight_id IN NUMBER,
    p_train_id IN NUMBER
) IS
BEGIN
    UPDATE Freight SET trainID = p_train_id WHERE id = p_freight_id;
    COMMIT;
END;
/