CREATE OR REPLACE FUNCTION fncAddNewLine(
    p_line_name IN VARCHAR2,
    p_owner_id IN VARCHAR2,
    p_start_endpoint_id IN NUMBER,
    p_end_endpoint_id IN NUMBER,
    p_gauge IN NUMBER,
    p_segment_length IN NUMBER,
    p_segment_max_weight IN FLOAT,
    p_line_type_id IN NUMBER
)
RETURN NUMBER
IS
    v_line_name_count NUMBER;
    v_owner_count NUMBER;
    v_start_endpoint_count NUMBER;
    v_end_endpoint_count NUMBER;
    v_gauge_count NUMBER;
    v_line_type_count NUMBER;
    v_next_line_id NUMBER;
    v_next_segment_id NUMBER;
BEGIN
    -- 1. Validação de Inputs Obrigatórios
    IF p_line_name IS NULL OR p_owner_id IS NULL OR p_start_endpoint_id IS NULL
       OR p_end_endpoint_id IS NULL OR p_gauge IS NULL OR p_segment_length IS NULL
       OR p_segment_max_weight IS NULL OR p_line_type_id IS NULL THEN
        RAISE_APPLICATION_ERROR(-20001, 'All parameters are required');
    END IF;

    -- 2. Valida se já existe uma Rail_Line com aquele nome
    SELECT COUNT(*) INTO v_line_name_count
    FROM Rail_Line
    WHERE name = p_line_name;

    IF v_line_name_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Rail Line with name ' || p_line_name || ' already exists');
    END IF;

    -- 3. Valida se o Owner existe
    SELECT COUNT(*) INTO v_owner_count
    FROM Owner
    WHERE shortName = p_owner_id;

    IF v_owner_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20003, 'Owner with ID ' || p_owner_id || ' does not exist');
    END IF;

    -- 4. Valida se o Start Endpoint existe
    SELECT COUNT(*) INTO v_start_endpoint_count
    FROM Endpoint
    WHERE endpointID = p_start_endpoint_id;

    IF v_start_endpoint_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20004, 'Start Endpoint with ID ' || p_start_endpoint_id || ' does not exist');
    END IF;

    -- 5. Valida se o End Endpoint existe
    SELECT COUNT(*) INTO v_end_endpoint_count
    FROM Endpoint
    WHERE endpointID = p_end_endpoint_id;

    IF v_end_endpoint_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20005, 'End Endpoint with ID ' || p_end_endpoint_id || ' does not exist');
    END IF;

    -- 6. Valida se o Gauge existe
    SELECT COUNT(*) INTO v_gauge_count
    FROM Gauge
    WHERE gaugeNumber = p_gauge;

    IF v_gauge_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20006, 'Gauge with number ' || p_gauge || ' does not exist');
    END IF;

    -- 7. Valida se o Line_Type existe
    SELECT COUNT(*) INTO v_line_type_count
    FROM Line_Type
    WHERE id = p_line_type_id;

    IF v_line_type_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20007, 'Line Type with ID ' || p_line_type_id || ' does not exist');
    END IF;

    -- 8. Calcular o próximo ID da Rail_Line
    SELECT NVL(MAX(id), 0) + 1 INTO v_next_line_id FROM Rail_Line;

    -- 9. Inserir a nova Rail_Line
    INSERT INTO Rail_Line (id, name, ownerID, startID, endID, gauge)
    VALUES (v_next_line_id, p_line_name, p_owner_id, p_start_endpoint_id, p_end_endpoint_id, p_gauge);

    -- 10. Calcular o próximo ID do Line_Segment
    SELECT NVL(MAX(id), 0) + 1 INTO v_next_segment_id FROM Line_Segment;

    -- 11. Inserir o primeiro Line_Segment (orderPosition = 1)
    INSERT INTO Line_Segment (id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
    VALUES (v_next_segment_id, v_next_line_id, 1, p_segment_max_weight, p_segment_length, p_line_type_id);

    -- 12. Finalizar Transação
    COMMIT;

    -- Retorna o ID da nova Rail_Line
    RETURN v_next_line_id;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE; -- Propaga o erro original para o utilizador ver o que falhou
END fncAddNewLine;
/