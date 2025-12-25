DECLARE
    v_result_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('USBD43: Testing Error Scenarios');
    DBMS_OUTPUT.PUT_LINE('========================================');

    -- Teste 1 : Parâmetros nulos
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 1: Testing NULL Voltage');
        v_result_id := fncAddElectricLocomotiveModel(
            p_voltage => NULL,
            p_frequency => 50
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- Teste 2 : Tentar inserir números negativos
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 2: Testing Negative Voltage Value');
        v_result_id := fncAddElectricLocomotiveModel(
            p_voltage => -25000,
            p_frequency => 50
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- Teste 3 : Meter a frequência a 0
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 3: Testing Zero Frequency');
        v_result_id := fncAddElectricLocomotiveModel(
            p_voltage => 25000,
            p_frequency => 0
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    -- Usar rollback para ter a certeza que nada é guardado
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    DBMS_OUTPUT.PUT_LINE('Error testing complete. All changes rolled back.');
END;
/