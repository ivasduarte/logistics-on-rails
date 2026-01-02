DECLARE
    v_train_id      CONSTANT NUMBER := 5421;
    v_loco_id       CONSTANT NUMBER := 5630; -- Locomotiva Helena (Model 1: 19.2m)
BEGIN
    DBMS_OUTPUT.PUT_LINE('=========================================================');
    DBMS_OUTPUT.PUT_LINE('USBD33: Do Not Allow Train Excess Length Success Test');
    DBMS_OUTPUT.PUT_LINE('=========================================================');
    DBMS_OUTPUT.PUT_LINE('');

    -- Tentativa de inserir uma terceira locomotiva no comboio 5421
    -- Comprimento acumulado: (2 locos existentes * 19.2m) + (nova loco * 19.2m) + vagões < 750m
    INSERT INTO Locomotive_Train (Locomotiveid, Trainid)
    VALUES (v_loco_id, v_train_id);

    DBMS_OUTPUT.PUT_LINE('[SUCCESS] Locomotive ' || v_loco_id || ' added to Train ' || v_train_id);
    DBMS_OUTPUT.PUT_LINE('[INFO] Total length remains below the 750m threshold.');

EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20020 THEN
            DBMS_OUTPUT.PUT_LINE('[SUCCESS] The system blocked the insertion correctly.');
            DBMS_OUTPUT.PUT_LINE('[Error Message] ' || SQLERRM);
        ELSE
            DBMS_OUTPUT.PUT_LINE('[UNEXPECTED ERROR] ' || SQLERRM);
        END IF;

        DBMS_OUTPUT.PUT_LINE('');
        DBMS_OUTPUT.PUT_LINE('=========================================================');

        ROLLBACK;
END;
/