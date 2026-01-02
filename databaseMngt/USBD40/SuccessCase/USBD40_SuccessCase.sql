DECLARE
    v_freight_id NUMBER;
    v_origin     NUMBER := 50;   -- Leixões
    v_dest       NUMBER := 11;   -- Valença
    v_model      NUMBER := 1212;
    v_qtd        NUMBER := 2;    -- Queremos 2 vagões
BEGIN
    DBMS_OUTPUT.PUT_LINE('=========================================================');
    DBMS_OUTPUT.PUT_LINE('USBD40: Register a Freight In The Database Success Test');
    DBMS_OUTPUT.PUT_LINE('=========================================================');
    DBMS_OUTPUT.PUT_LINE('');

    DBMS_OUTPUT.PUT_LINE('Attempting to register cargo (Origin 50 -> Destination 11)...');

    v_freight_id := fncRegisterFreight(
            p_origin      => v_origin,
            p_destination => v_dest,
            p_date        => SYSDATE,
            p_train_id    => NULL,   -- Ainda sem comboio
            p_model_id    => v_model,
            p_quantity    => v_qtd
                    );

    DBMS_OUTPUT.PUT_LINE('   [SUCCESS] Cargo created with ID ' || v_freight_id);

    -- Verificar associações
    FOR r IN (SELECT Wagonid FROM Wagon_Freight WHERE Freightid = v_freight_id) LOOP
            DBMS_OUTPUT.PUT_LINE('   -> Associated Wagon: ' || r.Wagonid);
        END LOOP;

    -- Limpeza
    DBMS_OUTPUT.PUT_LINE('→ Cleaning test data...');
    DELETE FROM Wagon_Freight WHERE Freightid = v_freight_id;
    DELETE FROM Freight WHERE id = v_freight_id;
    COMMIT;
    DBMS_OUTPUT.PUT_LINE('   [CLEAN UP COMPLETE] Test data cleaned.');

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
END;
/