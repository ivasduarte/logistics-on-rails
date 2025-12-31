CREATE OR REPLACE FUNCTION get_available_rolling_stock
    RETURN SYS_REFCURSOR AS
    c_stock SYS_REFCURSOR;
BEGIN
    OPEN c_stock FOR
        -- LOCOMOTIVAS
        SELECT
            'LOCOMOTIVE' AS type,
            l.id AS resource_id,
            l.name AS resource_name,
            e_current.endpointID AS location_id, -- Mantemos ID atual para cálculos de distância
            CASE
                -- Se estiver num comboio, mostramos o destino final da mercadoria (Freight)
                WHEN lt.Trainid IS NOT NULL THEN e_dest.name
                ELSE e_current.name
                END AS location_name,
            CASE
                WHEN lt.Trainid IS NOT NULL THEN 'IN_TRANSIT'
                ELSE 'PARKED'
                END AS status
        FROM Locomotive l
                 JOIN Endpoint e_current ON l.startEndpoint = e_current.endpointID
            -- Tentar ligar a um comboio e à sua mercadoria para saber o destino
                 LEFT JOIN Locomotive_Train lt ON l.id = lt.Locomotiveid
                 LEFT JOIN Freight f ON f.trainID = lt.Trainid
                 LEFT JOIN Endpoint e_dest ON f.destination = e_dest.endpointID

        UNION ALL

        -- VAGÕES
        SELECT
            'WAGON' AS type,
            w.id AS resource_id,
            'Wagon Model ' || w.modelID AS resource_name,
            e_current.endpointID AS location_id,
            CASE
                -- Se estiver numa mercadoria, mostramos o destino dessa mercadoria
                WHEN wf.Freightid IS NOT NULL THEN e_dest.name
                ELSE e_current.name
                END AS location_name,
            CASE
                WHEN wf.Freightid IS NOT NULL THEN 'IN_TRANSIT'
                ELSE 'PARKED'
                END AS status
        FROM Wagon w
                 JOIN Endpoint e_current ON w.startEndpoint = e_current.endpointID
            -- Tentar ligar a uma mercadoria ativa
                 LEFT JOIN Wagon_Freight wf ON w.id = wf.Wagonid
                 LEFT JOIN Freight f ON wf.Freightid = f.id
                 LEFT JOIN Endpoint e_dest ON f.destination = e_dest.endpointID;

    RETURN c_stock;
END;
/