DECLARE
    -- Variáveis de Saída
    v_new_segment_id NUMBER;

    -- Variáveis de Teste (Dados para Inserção)
    -- Vamos usar a Linha 1 (Ramal São Bento - Campanhã)
    v_target_line_id CONSTANT NUMBER := 1;
    v_target_line_type CONSTANT NUMBER := 1; -- Eletrico, 4 vias
    v_new_length CONSTANT NUMBER := 1500;
    v_new_weight CONSTANT NUMBER := 8000;

    -- Configuração do Siding (Requisito da US)
    v_has_siding CONSTANT NUMBER := 1; -- SIM
    v_siding_len CONSTANT NUMBER := 300;
    v_siding_pos CONSTANT NUMBER := 100;

    -- Cursor para listar detalhes da Linha e seus Segmentos
    CURSOR c_line_segments IS
        SELECT ls.id, ls.orderPosition, ls.length, ls.max_weight, ls.Line_TypeID
        FROM Line_Segment ls
        WHERE ls.Rail_LineId = v_target_line_id
        ORDER BY ls.orderPosition;

    -- Cursor para verificar se o Siding foi criado
    CURSOR c_sidings IS
        SELECT s.id, s.segmentID, s.length, s.position
        FROM Siding s
                 JOIN Line_Segment ls ON s.segmentID = ls.id
        WHERE ls.Rail_LineId = v_target_line_id
        ORDER BY ls.orderPosition;

BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('USBD44: Add Segment & Siding to Line Test');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('');

    -- 1. Estado Antes da Inserção
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('LINE DETAILS BEFORE INSERTION');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('Checking Line ID: ' || v_target_line_id);

    DBMS_OUTPUT.PUT_LINE('Existing Segments:');
    DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    FOR rec IN c_line_segments LOOP
            DBMS_OUTPUT.PUT_LINE('  Seg ID: ' || rec.id || ' | Order: ' || rec.orderPosition || ' | Len: ' || rec.length || ' | Type: ' || rec.Line_TypeID);
        END LOOP;

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('Existing Sidings on this Line:');
    DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    FOR rec IN c_sidings LOOP
            DBMS_OUTPUT.PUT_LINE('  Siding ID: ' || rec.id || ' | Linked to Seg ID: ' || rec.segmentID || ' | Len: ' || rec.length);
        END LOOP;

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('========================================');

    -- 2. Executar a Função
    DBMS_OUTPUT.PUT_LINE('Attempting to add new Segment (Len: '||v_new_length||') with Siding (Len: '||v_siding_len||')...');

    v_new_segment_id := fncAddSegmentToLine(
            p_rail_line_id => v_target_line_id,
            p_line_type_id => v_target_line_type,
            p_length       => v_new_length,
            p_max_weight   => v_new_weight,
            p_has_siding   => v_has_siding,
            p_siding_len   => v_siding_len,
            p_siding_pos   => v_siding_pos
                        );

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('Function executed successfully!');
    DBMS_OUTPUT.PUT_LINE('Return value (New Segment ID): ' || v_new_segment_id);

    -- 3. Estado Depois da Inserção
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('LINE DETAILS AFTER INSERTION');
    DBMS_OUTPUT.PUT_LINE('========================================');

    DBMS_OUTPUT.PUT_LINE('Segments (Check if new one appears at the end):');
    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    FOR rec IN c_line_segments LOOP
            DBMS_OUTPUT.PUT_LINE('  Seg ID: ' || rec.id || ' | Order: ' || rec.orderPosition || ' | Len: ' || rec.length);

            -- Validação visual rápida
            IF rec.id = v_new_segment_id THEN
                DBMS_OUTPUT.PUT_LINE('  >>> STATUS: NEW SEGMENT FOUND <<<');
            END IF;
        END LOOP;

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('Sidings (Check if new Siding was created):');
    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    FOR rec IN c_sidings LOOP
            DBMS_OUTPUT.PUT_LINE('  Siding ID: ' || rec.id || ' | Linked to Seg ID: ' || rec.segmentID || ' | Len: ' || rec.length);

            IF rec.segmentID = v_new_segment_id THEN
                DBMS_OUTPUT.PUT_LINE('  >>> STATUS: SUCCESS (Siding created for new segment) <<<');
            END IF;
        END LOOP;

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