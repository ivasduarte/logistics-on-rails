SET SERVEROUTPUT ON;
DECLARE
    v_cursor      SYS_REFCURSOR;
    v_wagon_id    Wagon.id%TYPE;
    v_model       Wagon_Model.model%TYPE;
    v_capacity    Wagon_Hopper_Car.storedGrains%TYPE;
    v_count       NUMBER := 0;
BEGIN
    DBMS_OUTPUT.PUT_LINE('==============================================');
    DBMS_OUTPUT.PUT_LINE(' USBD27 - Grain wagons used in all trains');
    DBMS_OUTPUT.PUT_LINE(' Period: 2025-10-01 to 2025-10-10');
    DBMS_OUTPUT.PUT_LINE('==============================================');
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('WAGON_ID | MODEL | GRAIN_CAPACITY');
    DBMS_OUTPUT.PUT_LINE('-------------------------------------');

    v_cursor := fncGetGrainWagonsUsedInAllTrains(p_start_date => DATE '2025-10-01',p_end_date   => DATE '2025-10-10');
    LOOP
        FETCH v_cursor INTO v_wagon_id, v_model, v_capacity;
        EXIT WHEN v_cursor%NOTFOUND;
        v_count := v_count + 1;
        DBMS_OUTPUT.PUT_LINE(v_wagon_id || ' | ' ||v_model    || ' | ' ||v_capacity);
    END LOOP;
    CLOSE v_cursor;
    DBMS_OUTPUT.PUT_LINE('');
    IF v_count = 0 THEN
        DBMS_OUTPUT.PUT_LINE('No grain wagons found for this period.');
    ELSE
        DBMS_OUTPUT.PUT_LINE('Total wagons: ' || v_count);
    END IF;
END;