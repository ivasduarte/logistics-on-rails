DECLARE
    v_wagons_count NUMBER;
    v_freight_train_before NUMBER;
    v_freight_train_after NUMBER;

    -- ID de teste (selecionado dos teus inserts)
    -- Freight 2001 tem vagões e está inicialmente no Train 5421
    v_test_freight_id CONSTANT NUMBER := 2001;
    -- Vamos tentar mudar para o Train 5435
    v_target_train_id CONSTANT NUMBER := 5435;

    -- Cursor para listar detalhes do freight
    CURSOR c_freight_details IS
        SELECT f.id, f."date", f.origin, f.destination, f.trainID,
               (SELECT COUNT(*) FROM Wagon_Freight wf WHERE wf.Freightid = f.id) as wagon_count
        FROM Freight f
        WHERE f.id = v_test_freight_id -- USAR VARIÁVEL CORRIGIDA
        ORDER BY f.id;

    -- Cursor para listar wagons do freight
    CURSOR c_freight_wagons IS
        SELECT wf.Wagonid, w.modelID, w.gauge
        FROM Wagon_Freight wf
                 JOIN Wagon w ON w.id = wf.Wagonid
        WHERE wf.Freightid = v_test_freight_id -- USAR VARIÁVEL CORRIGIDA
        ORDER BY wf.Wagonid;

BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('USBD34: Associate Freight with Train Test');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('');

    -- Mostra detalhes do freight antes
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('FREIGHT DETAILS BEFORE ASSOCIATION');
    DBMS_OUTPUT.PUT_LINE('========================================');

    FOR rec IN c_freight_details LOOP
            DBMS_OUTPUT.PUT_LINE('Freight ID: ' || rec.id);
            DBMS_OUTPUT.PUT_LINE('  Date: ' || rec."date");
            DBMS_OUTPUT.PUT_LINE('  Origin Endpoint: ' || rec.origin);
            DBMS_OUTPUT.PUT_LINE('  Destination Endpoint: ' || rec.destination);
            DBMS_OUTPUT.PUT_LINE('  Current Train ID: ' || NVL(TO_CHAR(rec.trainID), 'NOT ASSIGNED'));
            DBMS_OUTPUT.PUT_LINE('  Number of Wagons: ' || rec.wagon_count);
            v_freight_train_before := rec.trainID;
        END LOOP;

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('Wagons in this freight:');
    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    FOR rec IN c_freight_wagons LOOP
            DBMS_OUTPUT.PUT_LINE('  Wagon ID: ' || rec.Wagonid || ' | Model: ' || rec.modelID || ' | Gauge: ' || rec.gauge);
        END LOOP;

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('========================================');

    -- Associa o freight ao train
    -- Utilizando os IDs corrigidos
    DBMS_OUTPUT.PUT_LINE('Attempting to associate Freight ' || v_test_freight_id || ' to Train ' || v_target_train_id || '...');

    v_wagons_count := fncAssociateFreightWithTrain(
            p_freight_id => v_test_freight_id,
            p_train_id => v_target_train_id
                      );

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('Function executed successfully!');
    DBMS_OUTPUT.PUT_LINE('Return value (wagons count): ' || v_wagons_count);

    -- Mostra detalhes do freight depois
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('FREIGHT DETAILS AFTER ASSOCIATION');
    DBMS_OUTPUT.PUT_LINE('========================================');

    FOR rec IN c_freight_details LOOP
            DBMS_OUTPUT.PUT_LINE('Freight ID: ' || rec.id);
            DBMS_OUTPUT.PUT_LINE('  Date: ' || rec."date");
            DBMS_OUTPUT.PUT_LINE('  Origin Endpoint: ' || rec.origin);
            DBMS_OUTPUT.PUT_LINE('  Destination Endpoint: ' || rec.destination);
            DBMS_OUTPUT.PUT_LINE('  Current Train ID: ' || NVL(TO_CHAR(rec.trainID), 'NOT ASSIGNED'));
            DBMS_OUTPUT.PUT_LINE('  Number of Wagons: ' || rec.wagon_count);
            v_freight_train_after := rec.trainID;

            IF rec.trainID = v_target_train_id THEN
                DBMS_OUTPUT.PUT_LINE('  >>> STATUS: SUCCESS (Train ID updated correctly) <<<');
            ELSE
                DBMS_OUTPUT.PUT_LINE('  >>> STATUS: FAILED (Train ID did not change) <<<');
            END IF;
        END LOOP;

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('Wagons now associated with Train ID ' || v_freight_train_after || ':');
    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    FOR rec IN c_freight_wagons LOOP
            DBMS_OUTPUT.PUT_LINE('  Wagon ID: ' || rec.Wagonid || ' | Model: ' || rec.modelID || ' | Gauge: ' || rec.gauge);
        END LOOP;

    DBMS_OUTPUT.PUT_LINE('========================================');

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('');
        DBMS_OUTPUT.PUT_LINE('========================================');
        DBMS_OUTPUT.PUT_LINE('ERROR DURING TEST');
        DBMS_OUTPUT.PUT_LINE('========================================');
        DBMS_OUTPUT.PUT_LINE('Error Code: ' || SQLCODE);
        DBMS_OUTPUT.PUT_LINE('Error Message: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE('Test Status: FAILED');
        DBMS_OUTPUT.PUT_LINE('========================================');
END;
/