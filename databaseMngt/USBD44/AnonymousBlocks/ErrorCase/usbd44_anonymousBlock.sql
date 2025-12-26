DECLARE
    v_result NUMBER;

    -- Variáveis para IDs válidos existentes nos teus inserts
    v_valid_line_id CONSTANT NUMBER := 1; -- "Ramal São Bento - Campanhã"
    v_valid_type_id CONSTANT NUMBER := 1; -- "Electric, 4 tracks"
    v_invalid_id    CONSTANT NUMBER := 99999; -- ID que não existe

BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('USBD44: Testing Error Scenarios');
    DBMS_OUTPUT.PUT_LINE('========================================');

    -- Teste 1: Parâmetros obrigatórios nulos (ex: Line ID NULL)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 1: Testing NULL Mandatory Parameters');
        v_result := fncAddSegmentToLine(
                p_rail_line_id => NULL,
                p_line_type_id => v_valid_type_id,
                p_length       => 1000,
                p_max_weight   => 8000,
                p_has_siding   => 0
                    );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- Teste 2: Rail Line ID inexistente
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 2: Testing Non-existent Rail Line ID');
        v_result := fncAddSegmentToLine(
                p_rail_line_id => v_invalid_id, -- ID 99999
                p_line_type_id => v_valid_type_id,
                p_length       => 1000,
                p_max_weight   => 8000,
                p_has_siding   => 0
                    );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- Teste 3: Line Type ID inexistente
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 3: Testing Non-existent Line Type ID');
        v_result := fncAddSegmentToLine(
                p_rail_line_id => v_valid_line_id,
                p_line_type_id => v_invalid_id, -- ID 99999
                p_length       => 1000,
                p_max_weight   => 8000,
                p_has_siding   => 0
                    );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- Teste 4: Siding ativado mas sem detalhes (Missing Length/Position)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 4: Testing Siding active but missing details');
        v_result := fncAddSegmentToLine(
                p_rail_line_id => v_valid_line_id,
                p_line_type_id => v_valid_type_id,
                p_length       => 1000,
                p_max_weight   => 8000,
                p_has_siding   => 1,   -- Ativado
                p_siding_len   => NULL -- Detalhe em falta
            -- p_siding_pos também é NULL por default
                    );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- Teste 5: Comprimento do Siding inválido (Maior que o Segmento)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 5: Testing Siding length >= Segment length');
        DBMS_OUTPUT.PUT_LINE('Attempting to add Segment(100m) with Siding(150m)...');

        v_result := fncAddSegmentToLine(
                p_rail_line_id => v_valid_line_id,
                p_line_type_id => v_valid_type_id,
                p_length       => 100,  -- Segmento pequeno
                p_max_weight   => 8000,
                p_has_siding   => 1,
                p_siding_len   => 150,  -- Siding maior que o segmento (Erro Lógico)
                p_siding_pos   => 10
                    );

        -- Se chegar aqui, a função falhou na validação
        DBMS_OUTPUT.PUT_LINE('Result: Operation completed unexpectedly (Validation Failed).');

    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error (Business Rule): ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- Rollback final por segurança
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('Error testing complete. All transactions rolled back.');
END;
/