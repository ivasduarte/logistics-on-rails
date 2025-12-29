SET SERVEROUTPUT ON;

DECLARE
    v_new_gauge NUMBER;
    v_count_before NUMBER;
    v_count_after  NUMBER;

    CURSOR c_gauges IS
        SELECT GAUGENUMBER
        FROM GAUGE
        ORDER BY GAUGENUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('USBD38: Add Gauge Test');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('');

    SELECT COUNT(*) INTO v_count_before FROM GAUGE;
    DBMS_OUTPUT.PUT_LINE('Total gauges before: ' || v_count_before);
    DBMS_OUTPUT.PUT_LINE('');

    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('GAUGES BEFORE INSERTION');
    DBMS_OUTPUT.PUT_LINE('========================================');

    FOR rec IN c_gauges LOOP
        DBMS_OUTPUT.PUT_LINE('Gauge: ' || rec.GAUGENUMBER);
        DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    END LOOP;

    v_new_gauge := fncAddGauge(1000);

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('New gauge inserted successfully!');
    DBMS_OUTPUT.PUT_LINE('New Gauge Number: ' || v_new_gauge);

    SELECT COUNT(*) INTO v_count_after FROM GAUGE;
    DBMS_OUTPUT.PUT_LINE('Total gauges after: ' || v_count_after);
    DBMS_OUTPUT.PUT_LINE('');

    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('GAUGES AFTER INSERTION');
    DBMS_OUTPUT.PUT_LINE('========================================');

    FOR rec IN c_gauges LOOP
        DBMS_OUTPUT.PUT_LINE('Gauge: ' || rec.GAUGENUMBER);

        IF rec.GAUGENUMBER = v_new_gauge THEN
            DBMS_OUTPUT.PUT_LINE('  >>> NEWLY INSERTED <<<');
        END IF;

        DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    END LOOP;

    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('Test complete. Changes rolled back.');
END;
/
