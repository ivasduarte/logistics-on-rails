SET SERVEROUTPUT ON;

DECLARE
    v_result NUMBER;

    v_trainId   NUMBER := 5421;
    v_freightId NUMBER := 2001;

    CURSOR c_all IS
        SELECT wf.WAGONID, wf.FREIGHTID
        FROM WAGON_FREIGHT wf
        JOIN OPERATOR_WAGON ow ON ow.WAGONID = wf.WAGONID
        JOIN TRAIN t
          ON UPPER(TRIM(t.OPERATOR)) = UPPER(TRIM(ow.OPERATORSHORTNAME))
        WHERE t.ID = v_trainId
        ORDER BY wf.WAGONID, wf.FREIGHTID;

    v_before NUMBER;
    v_after  NUMBER;

BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('USBD41: Remove Freight From Planned Train');
    DBMS_OUTPUT.PUT_LINE('SUCCESS CASE');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('');

    -- BEFORE COUNT (all freights)
    SELECT COUNT(*) INTO v_before
    FROM WAGON_FREIGHT wf
    JOIN OPERATOR_WAGON ow ON ow.WAGONID = wf.WAGONID
    JOIN TRAIN t
      ON UPPER(TRIM(t.OPERATOR)) = UPPER(TRIM(ow.OPERATORSHORTNAME))
    WHERE t.ID = v_trainId;

    DBMS_OUTPUT.PUT_LINE('Total freight associations BEFORE: ' || v_before);
    DBMS_OUTPUT.PUT_LINE('');

    DBMS_OUTPUT.PUT_LINE('--- BEFORE (all freights in the train) ---');
    FOR rec IN c_all LOOP
        DBMS_OUTPUT.PUT_LINE(
            'WagonID=' || rec.WAGONID ||
            ' | FreightID=' || rec.FREIGHTID
        );

        IF rec.FREIGHTID = v_freightId THEN
            DBMS_OUTPUT.PUT_LINE('   >>> TARGET TO REMOVE <<<');
        END IF;

        DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    END LOOP;

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('>> Executing fncRemoveFreightFromPlannedTrain');
    DBMS_OUTPUT.PUT_LINE('');

    v_result := fncRemoveFreightFromPlannedTrain(
        v_trainId,
        v_freightId
    );

    DBMS_OUTPUT.PUT_LINE('Removed associations: ' || v_result);
    DBMS_OUTPUT.PUT_LINE('');

    -- AFTER COUNT (all freights)
    SELECT COUNT(*) INTO v_after
    FROM WAGON_FREIGHT wf
    JOIN OPERATOR_WAGON ow ON ow.WAGONID = wf.WAGONID
    JOIN TRAIN t
      ON UPPER(TRIM(t.OPERATOR)) = UPPER(TRIM(ow.OPERATORSHORTNAME))
    WHERE t.ID = v_trainId;

    DBMS_OUTPUT.PUT_LINE('Total freight associations AFTER: ' || v_after);
    DBMS_OUTPUT.PUT_LINE('');

    DBMS_OUTPUT.PUT_LINE('--- AFTER (remaining freights in the train) ---');
    FOR rec IN c_all LOOP
        DBMS_OUTPUT.PUT_LINE(
            'WagonID=' || rec.WAGONID ||
            ' | FreightID=' || rec.FREIGHTID
        );

        IF rec.FREIGHTID = v_freightId THEN
            DBMS_OUTPUT.PUT_LINE('   ⚠ ERROR: freight still present');
        END IF;

        DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    END LOOP;

    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('Test complete. Changes rolled back.');
END;
/
