CREATE OR REPLACE FUNCTION fncAddSegmentToLine(
    p_rail_line_id   IN NUMBER,
    p_line_type_id   IN NUMBER,
    p_length         IN NUMBER,
    p_max_weight     IN NUMBER,
    p_has_siding     IN NUMBER, -- 1 para Sim, 0 para Não
    p_siding_len     IN NUMBER DEFAULT NULL,
    p_siding_pos     IN NUMBER DEFAULT NULL
)
    RETURN NUMBER
    IS
    v_rail_line_count NUMBER;
    v_line_type_count NUMBER;
    v_next_segment_id NUMBER;
    v_next_order_pos  NUMBER;
    v_next_siding_id  NUMBER;
BEGIN
    -- 1. Validação de Inputs Obrigatórios Básicos
    IF p_rail_line_id IS NULL OR p_line_type_id IS NULL OR p_length IS NULL OR p_max_weight IS NULL THEN
        RAISE_APPLICATION_ERROR(-20001, 'Rail Line, Line Type, Length, and Max Weight are required.');
    END IF;

    -- 2. Valida se a Rail_Line existe
    SELECT COUNT(*) INTO v_rail_line_count
    FROM Rail_Line
    WHERE id = p_rail_line_id;

    IF v_rail_line_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Rail Line ID does not exist.');
    END IF;

    -- 3. Valida se o Line_Type existe
    SELECT COUNT(*) INTO v_line_type_count
    FROM Line_Type
    WHERE id = p_line_type_id;

    IF v_line_type_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20003, 'Line Type ID does not exist.');
    END IF;

    -- 4. Validações Específicas de Siding (Se aplicável)
    IF p_has_siding = 1 THEN
        IF p_siding_len IS NULL OR p_siding_pos IS NULL THEN
            RAISE_APPLICATION_ERROR(-20004, 'Siding Length and Position are required when Siding is active.');
        END IF;

        IF p_siding_len >= p_length THEN
            RAISE_APPLICATION_ERROR(-20005, 'Siding length cannot be greater than or equal to the Segment length.');
        END IF;
    END IF;

    -- 5. Calcular o próximo ID e a próxima OrderPosition
    -- Nota: Em produção idealmente usarias SEQUENCES, mas aqui calculamos com MAX baseados nos dados fornecidos
    SELECT NVL(MAX(id), 0) + 1 INTO v_next_segment_id FROM Line_Segment;

    SELECT NVL(MAX(orderPosition), 0) + 1 INTO v_next_order_pos
    FROM Line_Segment
    WHERE Rail_LineId = p_rail_line_id;

    -- 6. Inserir o Segmento (Line_Segment)
    -- Colunas baseadas no teu DDL: id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID
    INSERT INTO Line_Segment (id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
    VALUES (v_next_segment_id, p_rail_line_id, v_next_order_pos, p_max_weight, p_length, p_line_type_id);

    -- 7. Inserir o Siding Condicionalmente
    IF p_has_siding = 1 THEN
        SELECT NVL(MAX(id), 0) + 1 INTO v_next_siding_id FROM Siding;

        -- Colunas baseadas no teu DDL: id, position, length, segmentID
        INSERT INTO Siding (id, position, length, segmentID)
        VALUES (v_next_siding_id, p_siding_pos, p_siding_len, v_next_segment_id);
    END IF;

    -- 8. Finalizar Transação
    COMMIT;

    -- Retorna o ID do novo segmento
    RETURN v_next_segment_id;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE; -- Propaga o erro original (Oracle code) para o utilizador ver o que falhou
END fncAddSegmentToLine;
/