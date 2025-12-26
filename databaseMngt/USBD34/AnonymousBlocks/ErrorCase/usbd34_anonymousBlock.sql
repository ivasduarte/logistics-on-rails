DECLARE
    v_result NUMBER;

    -- Variáveis para IDs válidos existentes nos inserts
    v_valid_freight_id CONSTANT NUMBER := 2001; -- Existe e está no train 5421
    v_valid_train_id   CONSTANT NUMBER := 5421; -- Train associado ao freight 2001
    v_other_train_id   CONSTANT NUMBER := 5435; -- Outro train válido
    v_invalid_id       CONSTANT NUMBER := 99999; -- ID que não existe
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('USBD34: Testing Error Scenarios');
    DBMS_OUTPUT.PUT_LINE('========================================');

    -- Teste 1: Parâmetro freight_id nulo
    -- Passamos um Train ID válido (5421) para garantir que o erro é apenas do Freight ser NULL
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 1: Testing NULL Freight ID');
        v_result := fncAssociateFreightWithTrain(
                p_freight_id => NULL,
                p_train_id => v_valid_train_id
                    );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- Teste 2: Parâmetro train_id nulo
    -- Passamos um Freight ID válido (2001) para garantir que o erro é apenas do Train ser NULL
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 2: Testing NULL Train ID');
        v_result := fncAssociateFreightWithTrain(
                p_freight_id => v_valid_freight_id,
                p_train_id => NULL
                    );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- Teste 3: Freight ID inexistente
    -- Usamos 99999 para Freight e um ID válido para Train
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 3: Testing Non-existent Freight ID');
        v_result := fncAssociateFreightWithTrain(
                p_freight_id => v_invalid_id,
                p_train_id => v_valid_train_id
                    );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- Teste 4: Train ID inexistente
    -- Usamos ID válido para Freight e 99999 para Train
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 4: Testing Non-existent Train ID');
        v_result := fncAssociateFreightWithTrain(
                p_freight_id => v_valid_freight_id,
                p_train_id => v_invalid_id
                    );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- Teste 5: Freight já associado a outro train
    -- O Freight 2001 já foi inserido na BD associado ao Train 5421.
    -- Vamos tentar associá-lo ao Train 5435.
    -- NOTA: Se a tua função permitir updates (mudar de comboio), este bloco NÃO vai dar erro.
    -- Se a função tiver uma regra de negócio que proíbe mudar de comboio, então dará erro.
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 5: Testing Freight Already Associated with Different Train');

        DBMS_OUTPUT.PUT_LINE('Attempting to move Freight ' || v_valid_freight_id ||
                             ' (currently on ' || v_valid_train_id || ') to Train ' || v_other_train_id);

        v_result := fncAssociateFreightWithTrain(
                p_freight_id => v_valid_freight_id,
                p_train_id => v_other_train_id
                    );

        -- Se chegar aqui, a função permitiu a mudança
        DBMS_OUTPUT.PUT_LINE('Result: Operation completed (The function allows re-association).');

    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error (Business Rule): ' || SQLERRM);
    END;

    -- Usar rollback para ter a certeza que nada é guardado (especialmente se o cenário 5 funcionar)
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    DBMS_OUTPUT.PUT_LINE('Error testing complete. All changes rolled back.');
END;
/