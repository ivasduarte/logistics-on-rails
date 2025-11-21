DECLARE
    v_cursor SYS_REFCURSOR;
    v_segment_id NUMBER;
    v_segment_order NUMBER;
    v_rail_line_name VARCHAR2(255);
    v_length NUMBER;
    v_max_weight FLOAT;
    v_is_electric VARCHAR2(255);
    v_num_tracks NUMBER;
    v_start_point VARCHAR2(255);
    v_end_point VARCHAR2(255);
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('USBD23: Line Segments for Train ID 5435');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('');

    v_cursor := fncGetTrainRouteSegments(5435);

    LOOP
        FETCH v_cursor INTO v_segment_id, v_segment_order, v_rail_line_name,
                           v_length, v_max_weight, v_is_electric, v_num_tracks,
                           v_start_point, v_end_point;
        EXIT WHEN v_cursor%NOTFOUND;

        DBMS_OUTPUT.PUT_LINE('Segment ID: ' || v_segment_id);
        DBMS_OUTPUT.PUT_LINE('  Rail Line: ' || v_rail_line_name);
        DBMS_OUTPUT.PUT_LINE('  Length: ' || v_length || ' meters');
        DBMS_OUTPUT.PUT_LINE('  Electric: ' || v_is_electric);
        DBMS_OUTPUT.PUT_LINE('  Tracks: ' || v_num_tracks);
        DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    END LOOP;

    CLOSE v_cursor;

    IF v_segment_id IS NULL THEN
        DBMS_OUTPUT.PUT_LINE('No segments found for this train.');
    END IF;
END;