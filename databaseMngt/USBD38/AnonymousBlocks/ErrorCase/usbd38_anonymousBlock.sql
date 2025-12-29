SET SERVEROUTPUT ON;

DECLARE
    v_result_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('USBD38: Testing Error Scenarios');
    DBMS_OUTPUT.PUT_LINE('========================================');

    -- TESTE 1: Gauge NULL (Esperado o erro -20010)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 1: Testing NULL Gauge');
        v_result_id := fncAddGauge(
            p_gaugeNumber => NULL  -- Esta linha causa erro
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- TESTE 2: Gauge inválido (<= 0) (Esperado o erro -20012)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 2: Testing Invalid Gauge (0)');
        v_result_id := fncAddGauge(
            p_gaugeNumber => 0  -- Esta linha causa erro
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- TESTE 3: Gauge já existente (Esperado o erro -20011)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 3: Testing Existing Gauge (1668)');
        v_result_id := fncAddGauge(
            p_gaugeNumber => 1668 -- Este gauge já existe
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    -- Como são testes de erro, garantimos que nada foi gravado
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    DBMS_OUTPUT.PUT_LINE('Error testing complete.');
END;
/
