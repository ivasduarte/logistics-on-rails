DECLARE
    v_resultado NUMBER;
    v_loco_id   NUMBER := 5621; -- Locomotiva 'Inês'
    v_train_id  NUMBER := 5421; -- Comboio existente
BEGIN
    DBMS_OUTPUT.PUT_LINE('=========================================================');
    DBMS_OUTPUT.PUT_LINE('USBD39: Associate a Locomotive to a Train Error Test');
    DBMS_OUTPUT.PUT_LINE('=========================================================');
    DBMS_OUTPUT.PUT_LINE('');

    DBMS_OUTPUT.PUT_LINE('Attempting to Create a Duplicate Association (' || v_loco_id || ' - ' || v_train_id || ')...');

    -- Chamada da função
    v_resultado := fncAssociateLocomotiveToTrain(v_loco_id, v_train_id);

    -- Se chegar aqui, o teste falhou
    DBMS_OUTPUT.PUT_LINE('    [TEST FAILURE] The function improperly allowed duplicates.');


EXCEPTION
    WHEN OTHERS THEN
        -- O erro esperado é o -20013 definido na função
        IF SQLCODE = -20013 THEN
            DBMS_OUTPUT.PUT_LINE('');
            DBMS_OUTPUT.PUT_LINE('[TEST SUCCESSFUL] The function blocked the duplicate correctly.');
            DBMS_OUTPUT.PUT_LINE('[Error Message] ' || SQLERRM);
        ELSE
            DBMS_OUTPUT.PUT_LINE('[UNEXPECTED ERROR]' || SQLERRM);
        END IF;
        DBMS_OUTPUT.PUT_LINE('=========================================================');
END;
/