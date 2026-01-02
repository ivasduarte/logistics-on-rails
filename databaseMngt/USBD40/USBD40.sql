CREATE OR REPLACE FUNCTION fncRegisterFreight(
    p_origin      IN NUMBER,
    p_destination IN NUMBER,
    p_date        IN DATE,
    p_train_id    IN NUMBER,
    p_model_id    IN NUMBER,
    p_quantity    IN NUMBER
)
    RETURN NUMBER
    IS
    v_freight_id     NUMBER;
    v_origin_exists  NUMBER;
    v_dest_exists    NUMBER;

    -- Cursor para encontrar vagões disponíveis na origem
    CURSOR c_available_wagons IS
        SELECT w.id
        FROM Wagon w
        WHERE w.modelID = p_model_id
          AND (
            -- Caso 1: Nunca viajou e está na base inicial correta
            (w.startEndpoint = p_origin
                AND NOT EXISTS (SELECT 1 FROM Wagon_Freight wf WHERE wf.Wagonid = w.id))
                OR
                -- Caso 2: A última viagem terminou na origem atual
            (p_origin = (
                SELECT f.destination
                FROM Freight f
                         JOIN Wagon_Freight wf ON f.id = wf.Freightid
                WHERE wf.Wagonid = w.id
                ORDER BY f."date" DESC, f.id DESC
                    FETCH FIRST 1 ROWS ONLY
            ))
            )
          -- Garante que o vagão não está numa carga futura ou a decorrer
          -- e limita à quantidade que precisamos
            FETCH FIRST p_quantity ROWS ONLY;

    -- Coleção para guardar os IDs encontrados
    TYPE t_wagon_ids IS TABLE OF NUMBER;
    v_wagons_found t_wagon_ids;

BEGIN
    -- Validações
    IF p_quantity <= 0 THEN
        RAISE_APPLICATION_ERROR(-20030, 'Quantity must be greater than 0.');
    END IF;

    -- Verificar se Origem e Destino existem
    SELECT COUNT(*) INTO v_origin_exists FROM Endpoint WHERE endpointID = p_origin;
    SELECT COUNT(*) INTO v_dest_exists FROM Endpoint WHERE endpointID = p_destination;

    IF v_origin_exists = 0 OR v_dest_exists = 0 THEN
        RAISE_APPLICATION_ERROR(-20031, 'Origin or Destination Endpoint not found.');
    END IF;

    -- Procurar Vagões (Bulk Collect para memória)
    OPEN c_available_wagons;
    FETCH c_available_wagons BULK COLLECT INTO v_wagons_found;
    CLOSE c_available_wagons;

    -- Verificar se encontrámos suficientes
    IF v_wagons_found.COUNT < p_quantity THEN
        RAISE_APPLICATION_ERROR(-20032,
                                'Not enough wagons available at Origin. Requested: ' || p_quantity ||
                                ', Available: ' || v_wagons_found.COUNT);
    END IF;

    -- Criar a Carga (Freight)
    -- Gerar novo ID (Max + 1)
    SELECT NVL(MAX(id), 0) + 1 INTO v_freight_id FROM Freight;

    INSERT INTO Freight (id, "date", origin, destination, trainID)
    VALUES (v_freight_id, p_date, p_origin, p_destination, p_train_id);

    -- Associar os Vagões Encontrados à Carga
    FOR i IN 1..v_wagons_found.COUNT LOOP
            INSERT INTO Wagon_Freight (Wagonid, Freightid)
            VALUES (v_wagons_found(i), v_freight_id);
        END LOOP;

    -- 6. Confirmar
    COMMIT;

    RETURN v_freight_id;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END fncRegisterFreight;
/