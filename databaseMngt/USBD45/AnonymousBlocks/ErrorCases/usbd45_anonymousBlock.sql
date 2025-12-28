DECLARE
    v_result NUMBER;

    -- Variáveis para IDs válidos existentes
    v_valid_owner_id CONSTANT VARCHAR2(255) := 'IP';
    v_valid_start_endpoint CONSTANT NUMBER := 14; -- Ermesinde
    v_valid_end_endpoint CONSTANT NUMBER := 18; -- Famalicão
    v_valid_gauge CONSTANT NUMBER := 1668;
    v_valid_line_type CONSTANT NUMBER := 1;
    v_invalid_id CONSTANT NUMBER := 99999; -- ID que não existe

BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('USBD45: Testing Error Scenarios');
    DBMS_OUTPUT.PUT_LINE('========================================');

    -- Teste 1: Parâmetro Line Name Nulo (Esperado erro -20001)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 1: Testing NULL Line Name');
        v_result := fncAddNewLine(
            p_line_name => NULL, -- Esta linha causa erro
            p_owner_id => v_valid_owner_id,
            p_start_endpoint_id => v_valid_start_endpoint,
            p_end_endpoint_id => v_valid_end_endpoint,
            p_gauge => v_valid_gauge,
            p_segment_length => 5000,
            p_segment_max_weight => 8000,
            p_line_type_id => v_valid_line_type
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- Teste 2: Nome de Linha Duplicado (Esperado erro -20002)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 2: Testing Duplicate Line Name');
        v_result := fncAddNewLine(
            p_line_name => 'Ramal São Bento - Campanhã', -- Nome que já existe
            p_owner_id => v_valid_owner_id,
            p_start_endpoint_id => v_valid_start_endpoint,
            p_end_endpoint_id => v_valid_end_endpoint,
            p_gauge => v_valid_gauge,
            p_segment_length => 5000,
            p_segment_max_weight => 8000,
            p_line_type_id => v_valid_line_type
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- Teste 3: Owner ID inexistente (Esperado erro -20003)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 3: Testing Non-existent Owner ID');
        v_result := fncAddNewLine(
            p_line_name => 'Test Line Invalid Owner',
            p_owner_id => 'INVALID', -- Owner que não existe
            p_start_endpoint_id => v_valid_start_endpoint,
            p_end_endpoint_id => v_valid_end_endpoint,
            p_gauge => v_valid_gauge,
            p_segment_length => 5000,
            p_segment_max_weight => 8000,
            p_line_type_id => v_valid_line_type
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- Teste 4: Start Endpoint inexistente (Esperado erro -20004)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 4: Testing Non-existent Start Endpoint');
        v_result := fncAddNewLine(
            p_line_name => 'Test Line Invalid Start',
            p_owner_id => v_valid_owner_id,
            p_start_endpoint_id => v_invalid_id, -- Endpoint que não existe
            p_end_endpoint_id => v_valid_end_endpoint,
            p_gauge => v_valid_gauge,
            p_segment_length => 5000,
            p_segment_max_weight => 8000,
            p_line_type_id => v_valid_line_type
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- Teste 5: End Endpoint inexistente (Esperado erro -20005)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 5: Testing Non-existent End Endpoint');
        v_result := fncAddNewLine(
            p_line_name => 'Test Line Invalid End',
            p_owner_id => v_valid_owner_id,
            p_start_endpoint_id => v_valid_start_endpoint,
            p_end_endpoint_id => v_invalid_id, -- Endpoint que não existe
            p_gauge => v_valid_gauge,
            p_segment_length => 5000,
            p_segment_max_weight => 8000,
            p_line_type_id => v_valid_line_type
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- Teste 6: Gauge inexistente (Esperado erro -20006)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 6: Testing Non-existent Gauge');
        v_result := fncAddNewLine(
            p_line_name => 'Test Line Invalid Gauge',
            p_owner_id => v_valid_owner_id,
            p_start_endpoint_id => v_valid_start_endpoint,
            p_end_endpoint_id => v_valid_end_endpoint,
            p_gauge => v_invalid_id, -- Gauge que não existe
            p_segment_length => 5000,
            p_segment_max_weight => 8000,
            p_line_type_id => v_valid_line_type
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- Teste 7: Line Type inexistente (Esperado erro -20007)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 7: Testing Non-existent Line Type');
        v_result := fncAddNewLine(
            p_line_name => 'Test Line Invalid Type',
            p_owner_id => v_valid_owner_id,
            p_start_endpoint_id => v_valid_start_endpoint,
            p_end_endpoint_id => v_valid_end_endpoint,
            p_gauge => v_valid_gauge,
            p_segment_length => 5000,
            p_segment_max_weight => 8000,
            p_line_type_id => v_invalid_id -- Line Type que não existe
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    -- Rollback final por segurança
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    DBMS_OUTPUT.PUT_LINE('Error testing complete. All transactions rolled back.');
END;
/