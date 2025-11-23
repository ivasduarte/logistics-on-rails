SET SERVEROUTPUT ON;
DECLARE
    v_cursor        SYS_REFCURSOR;
    v_op_code       Operator.shortName%TYPE;
    v_op_name       Operator.name%TYPE;
    v_total         NUMBER;
    v_electric      NUMBER;
    v_percentage    NUMBER;
    v_count_rows    NUMBER := 0;
BEGIN
    DBMS_OUTPUT.PUT_LINE('==============================================');
    DBMS_OUTPUT.PUT_LINE('USBD29 - Percentage of electric locomotives');
    DBMS_OUTPUT.PUT_LINE('Operator: Captrain');
    DBMS_OUTPUT.PUT_LINE('==============================================');
    DBMS_OUTPUT.PUT_LINE('');
    v_cursor := fncGetElectricLocomotivePercentage('Captrain');
    LOOP
        FETCH v_cursor INTO v_op_code,
                           v_op_name,
                           v_total,
                           v_electric,
                           v_percentage;
        EXIT WHEN v_cursor%NOTFOUND;
        v_count_rows := v_count_rows + 1;
        DBMS_OUTPUT.PUT_LINE('Operator code: ' || v_op_code);
        DBMS_OUTPUT.PUT_LINE('Operator name: ' || v_op_name);
        DBMS_OUTPUT.PUT_LINE('Total locomotives: ' || v_total);
        DBMS_OUTPUT.PUT_LINE('Electric locomotives: ' || v_electric);
        DBMS_OUTPUT.PUT_LINE('Percentage electric: ' || v_percentage || ' %');
        DBMS_OUTPUT.PUT_LINE('----------------------------------------------');
    END LOOP;
    CLOSE v_cursor;
    IF v_count_rows = 0 THEN
        DBMS_OUTPUT.PUT_LINE('No locomotives found for this operator.');
    END IF;
END;