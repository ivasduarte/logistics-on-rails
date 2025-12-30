SET SERVEROUTPUT ON;

DECLARE
    v_result NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('USBD41: Remove Freight From Planned Train');
    DBMS_OUTPUT.PUT_LINE('ERROR CASES');
    DBMS_OUTPUT.PUT_LINE('========================================');

    -- TESTE 1: Train ID NULL (Esperado -20020)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 1: Testing NULL Train ID');
        v_result := fncRemoveFreightFromPlannedTrain(
            p_trainId   => NULL,
            p_freightId => 2001
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- TESTE 2: Freight ID NULL (Esperado -20020)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 2: Testing NULL Freight ID');
        v_result := fncRemoveFreightFromPlannedTrain(
            p_trainId   => 5421,
            p_freightId => NULL
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- TESTE 3: Train inexistente (Esperado -20021)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 3: Testing Non-existent Train (99999)');
        v_result := fncRemoveFreightFromPlannedTrain(
            p_trainId   => 99999,
            p_freightId => 2001
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- TESTE 4: Freight inexistente (Esperado -20022)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 4: Testing Non-existent Freight (99999)');
        v_result := fncRemoveFreightFromPlannedTrain(
            p_trainId   => 5421,
            p_freightId => 99999
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- TESTE 5: Freight NÃO associado ao Train (Esperado -20023)
    -- Aqui escolhemos um freightId que NÃO apareça no train 5421.
    -- Tenta primeiro 99998 (se existir na tabela FREIGHT troca), ou usa um freight válido que não esteja associado.
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 5: Testing Freight not associated with Train (Train=5421, Freight=2008)');
        v_result := fncRemoveFreightFromPlannedTrain(
            p_trainId   => 5421,
            p_freightId => 2008
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    DBMS_OUTPUT.PUT_LINE('Error testing complete.');
END;
/
