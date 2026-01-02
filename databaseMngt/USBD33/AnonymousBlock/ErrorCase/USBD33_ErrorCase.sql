DECLARE
    v_mega_model_id   CONSTANT NUMBER := 8888;
    v_mega_loco_id    CONSTANT NUMBER := 8888;
    v_train_id        CONSTANT NUMBER := 5437;
BEGIN
    DBMS_OUTPUT.PUT_LINE('=========================================================');
    DBMS_OUTPUT.PUT_LINE('USBD33: Do Not Allow Train Excess Length Error Test');
    DBMS_OUTPUT.PUT_LINE('=========================================================');
    DBMS_OUTPUT.PUT_LINE('');

    DBMS_OUTPUT.PUT_LINE('→ Cleaning up test data...');

    -- Remove dados de execuções anteriores
    DELETE FROM Locomotive_Train WHERE Locomotiveid = v_mega_loco_id;
    DELETE FROM Locomotive WHERE id = v_mega_loco_id;
    DELETE FROM Locomotive_Model WHERE id = v_mega_model_id;

    -- 1. Criar modelo e locomotiva (800 metros)
    INSERT INTO Locomotive_Model (id, model, numberBogies, bogies, power, length, width, height, maxSpeed,
                                  operationalSpeed, traction, weight, dieselID, electricID)
    VALUES (v_mega_model_id, 'ULTRA-LONG', 2, 'Bo-Bo', 9000, 800, 3, 4, 100, 50, 400, 150, 1, 1);

    INSERT INTO Locomotive (id, name, make, modelID, entryDate, startEndpoint)
    VALUES (v_mega_loco_id, 'The-Long-One', 'TestBuild', v_mega_model_id, SYSDATE, 50);

    -- Tentar adicionar a locomotiva ao comboio para disparar o gatilho
    BEGIN
        INSERT INTO Locomotive_Train (Locomotiveid, Trainid)
        VALUES (v_mega_loco_id, v_train_id);

        DBMS_OUTPUT.PUT_LINE('[FAILURE] The system allowed a length > 750m!');
    EXCEPTION
        WHEN OTHERS THEN
            -- Captura o erro customizado do Gatilho
            IF SQLCODE = -20020 THEN
                DBMS_OUTPUT.PUT_LINE('[SUCCESS] Trigger blocked the insertion correctly.');
                DBMS_OUTPUT.PUT_LINE('[ERROR MESSAGE] ' || SQLERRM);
            ELSE
                DBMS_OUTPUT.PUT_LINE('[FAILURE] Unexpected error: ' || SQLERRM);
            END IF;
            DBMS_OUTPUT.PUT_LINE('=========================================================');

    END;

    ROLLBACK;
END;
/