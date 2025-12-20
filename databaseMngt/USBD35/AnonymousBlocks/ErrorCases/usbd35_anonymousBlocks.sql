DECLARE
    v_result_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('USBD35: Testing Error Scenarios');
    DBMS_OUTPUT.PUT_LINE('========================================');

    -- TESTE 1: Parâmetros Nulos (Esperado o erro -20001)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 1: Testing NULL Latitude');
        v_result_id := fncRegisterNewFacility(
            p_name => 'Fail Terminal',
            p_latitude => NULL,  -- Esta linha causa erro
            p_longitude => '-8.0000',
            p_type_id => 1
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- TESTE 2: Tipo de Endpoint Inexistente (Esperado o erro -20002)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 2: Testing Invalid Type ID (9999)');
        v_result_id := fncRegisterNewFacility(
            p_name => 'Invalid Type Terminal',
            p_latitude => '40.0000',
            p_longitude => '-7.0000',
            p_type_id => 9999 -- Este ID não existe na tabela Endpoint_Type
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