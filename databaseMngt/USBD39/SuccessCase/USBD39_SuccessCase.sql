DECLARE
    v_resultado NUMBER;
    v_loco_id   NUMBER := 5630; -- Locomotiva 'Helena'
    v_train_id  NUMBER := 5437; -- Comboio
BEGIN
    DBMS_OUTPUT.PUT_LINE('=========================================================');
    DBMS_OUTPUT.PUT_LINE('USBD39: Associate a Locomotive to a Train Success Test');
    DBMS_OUTPUT.PUT_LINE('=========================================================');
    DBMS_OUTPUT.PUT_LINE('');

    -- 1. Tentar Associar (Cria o registo)
    DBMS_OUTPUT.PUT_LINE('→ Calling association function...');
    v_resultado := fncAssociateLocomotiveToTrain(v_loco_id, v_train_id);

    IF v_resultado = 1 THEN
        DBMS_OUTPUT.PUT_LINE('   [SUCCESS] Association created in database.');

        -- Verificar se está lá mesmo
        FOR r IN (SELECT * FROM Locomotive_Train WHERE Locomotiveid = v_loco_id AND Trainid = v_train_id) LOOP
                DBMS_OUTPUT.PUT_LINE('   [CHECKING] Record found in database.');
            END LOOP;

        -- 2. Limpar a sujidade (Apagar o registo criado)
        DBMS_OUTPUT.PUT_LINE('→ Cleaning test data...');
        DELETE FROM Locomotive_Train
        WHERE Locomotiveid = v_loco_id AND Trainid = v_train_id;

        -- Confirmar a remoção
        IF SQL%ROWCOUNT > 0 THEN
            DBMS_OUTPUT.PUT_LINE('   [CLEAN UP COMPLETE] Association removed successfully.');
            COMMIT; -- Gravar a remoção
        ELSE
            DBMS_OUTPUT.PUT_LINE('   [WARNING] It was not possible to remove the record.');
        END IF;

    END IF;

    DBMS_OUTPUT.PUT_LINE('=========================================================');

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('');
        DBMS_OUTPUT.PUT_LINE('=========================================================');
        DBMS_OUTPUT.PUT_LINE('ERROR DURING TEST');
        DBMS_OUTPUT.PUT_LINE('=========================================================');
        DBMS_OUTPUT.PUT_LINE('Error Code: ' || SQLCODE);
        DBMS_OUTPUT.PUT_LINE('Error Message: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE('Test Status: FAILED');
        DBMS_OUTPUT.PUT_LINE('=========================================================');

        -- Tenta limpar em caso de erro, se o registo tiver ficado lá
        BEGIN
            DELETE FROM Locomotive_Train WHERE Locomotiveid = v_loco_id AND Trainid = v_train_id;
            COMMIT;
            DBMS_OUTPUT.PUT_LINE('   (Emergency cleanup attempt performed)');
        EXCEPTION WHEN OTHERS THEN NULL;
        END;
END;
/