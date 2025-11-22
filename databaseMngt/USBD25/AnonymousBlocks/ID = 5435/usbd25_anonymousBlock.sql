DECLARE
    v_cursor SYS_REFCURSOR;
    v_endpoint_id NUMBER;
    v_endpoint_name VARCHAR2(255);
    v_endpoint_type VARCHAR2(255);
    v_latitude VARCHAR2(255);
    v_longitude VARCHAR2(255);
    v_count NUMBER := 0;
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('USBD25: Non-Stop Endpoints for Train ID 5435');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('');

    v_cursor := fncGetNonStopEndpoints(5435);

    LOOP
        FETCH v_cursor INTO v_endpoint_id, v_endpoint_name, v_endpoint_type,
                           v_latitude, v_longitude;
        EXIT WHEN v_cursor%NOTFOUND;

        v_count := v_count + 1;
        DBMS_OUTPUT.PUT_LINE('Endpoint ID: ' || v_endpoint_id);
        DBMS_OUTPUT.PUT_LINE('  Name: ' || v_endpoint_name);
        DBMS_OUTPUT.PUT_LINE('  Type: ' || v_endpoint_type);
        DBMS_OUTPUT.PUT_LINE('  Location: ' || v_latitude || ', ' || v_longitude);
        DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    END LOOP;

    CLOSE v_cursor;

    IF v_count = 0 THEN
        DBMS_OUTPUT.PUT_LINE('No non-stop endpoints found (train stops at all endpoints on route).');
    ELSE
        DBMS_OUTPUT.PUT_LINE('');
        DBMS_OUTPUT.PUT_LINE('Total non-stop endpoints: ' || v_count);
    END IF;
END;