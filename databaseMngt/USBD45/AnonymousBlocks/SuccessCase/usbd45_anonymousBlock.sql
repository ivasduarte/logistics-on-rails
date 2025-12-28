DECLARE
    -- Variáveis de Saída
    v_new_line_id NUMBER;

    -- Variáveis de Teste (Dados para Inserção)
    -- Valores baseados nos INSERTs fornecidos
    v_test_line_name CONSTANT VARCHAR2(255) := 'Test Line Porto-Braga';
    v_test_owner_id CONSTANT VARCHAR2(255) := 'IP'; -- Owner válido (Infraestruturas de Portugal)
    v_test_start_endpoint CONSTANT NUMBER := 14; -- Ermesinde (endpoint válido)
    v_test_end_endpoint CONSTANT NUMBER := 18; -- Famalicão (endpoint válido)
    v_test_gauge CONSTANT NUMBER := 1668; -- Gauge comum em Portugal (válido)
    v_test_segment_length CONSTANT NUMBER := 5000;
    v_test_segment_max_weight CONSTANT FLOAT := 8000;
    v_test_line_type CONSTANT NUMBER := 1; -- Line Type válido (Electric, 4 tracks)

    -- Cursor para listar todas as Rail_Lines
    CURSOR c_rail_lines IS
        SELECT rl.id, rl.name, rl.ownerID, rl.gauge,
               e_start.name AS start_endpoint,
               e_end.name AS end_endpoint
        FROM Rail_Line rl
        JOIN Endpoint e_start ON rl.startID = e_start.endpointID
        JOIN Endpoint e_end ON rl.endID = e_end.endpointID
        ORDER BY rl.id;

    -- Cursor para listar os segmentos de uma linha específica
    CURSOR c_line_segments(p_line_id NUMBER) IS
        SELECT ls.id, ls.orderPosition, ls.length, ls.max_weight, ls.Line_TypeID
        FROM Line_Segment ls
        WHERE ls.Rail_LineId = p_line_id
        ORDER BY ls.orderPosition;

BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('USBD45: Add New Rail Line Test');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('');

    -- 1. Estado Antes da Inserção
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('RAIL LINES BEFORE INSERTION');
    DBMS_OUTPUT.PUT_LINE('========================================');

    FOR rec IN c_rail_lines LOOP
        DBMS_OUTPUT.PUT_LINE('Line ID: ' || rec.id);
        DBMS_OUTPUT.PUT_LINE('  Name: ' || rec.name);
        DBMS_OUTPUT.PUT_LINE('  Owner: ' || rec.ownerID);
        DBMS_OUTPUT.PUT_LINE('  Route: ' || rec.start_endpoint || ' -> ' || rec.end_endpoint);
        DBMS_OUTPUT.PUT_LINE('  Gauge: ' || rec.gauge);
        DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    END LOOP;

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('========================================');

    -- 2. Executar a Função
    DBMS_OUTPUT.PUT_LINE('Attempting to add new Rail Line: ' || v_test_line_name);
    DBMS_OUTPUT.PUT_LINE('With first segment (Length: ' || v_test_segment_length || 'm)...');

    v_new_line_id := fncAddNewLine(
        p_line_name => v_test_line_name,
        p_owner_id => v_test_owner_id,
        p_start_endpoint_id => v_test_start_endpoint,
        p_end_endpoint_id => v_test_end_endpoint,
        p_gauge => v_test_gauge,
        p_segment_length => v_test_segment_length,
        p_segment_max_weight => v_test_segment_max_weight,
        p_line_type_id => v_test_line_type
    );

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('Function executed successfully!');
    DBMS_OUTPUT.PUT_LINE('Return value (New Rail Line ID): ' || v_new_line_id);

    -- 3. Estado Depois da Inserção
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('RAIL LINES AFTER INSERTION');
    DBMS_OUTPUT.PUT_LINE('========================================');

    FOR rec IN c_rail_lines LOOP
        DBMS_OUTPUT.PUT_LINE('Line ID: ' || rec.id);
        DBMS_OUTPUT.PUT_LINE('  Name: ' || rec.name);
        DBMS_OUTPUT.PUT_LINE('  Owner: ' || rec.ownerID);
        DBMS_OUTPUT.PUT_LINE('  Route: ' || rec.start_endpoint || ' -> ' || rec.end_endpoint);

        -- Validação visual rápida
        IF rec.id = v_new_line_id THEN
            DBMS_OUTPUT.PUT_LINE('  >>> NEWLY CREATED LINE <<<');

            DBMS_OUTPUT.PUT_LINE('  Line Segments:');
            FOR seg IN c_line_segments(rec.id) LOOP
                DBMS_OUTPUT.PUT_LINE('    Segment ID: ' || seg.id || ' | Order: ' || seg.orderPosition || ' | Length: ' || seg.length || 'm');
                DBMS_OUTPUT.PUT_LINE('    >>> FIRST SEGMENT CREATED <<<');
            END LOOP;
        END IF;

        DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    END LOOP;

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('Test Status: SUCCESS');
    DBMS_OUTPUT.PUT_LINE('========================================');

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('');
        DBMS_OUTPUT.PUT_LINE('========================================');
        DBMS_OUTPUT.PUT_LINE('ERROR DURING TEST');
        DBMS_OUTPUT.PUT_LINE('========================================');
        DBMS_OUTPUT.PUT_LINE('Error Code: ' || SQLCODE);
        DBMS_OUTPUT.PUT_LINE('Error Message: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE('Test Status: FAILED');
        DBMS_OUTPUT.PUT_LINE('========================================');
        ROLLBACK; -- Garante que o teste não suja a BD se der erro fatal
END;
/