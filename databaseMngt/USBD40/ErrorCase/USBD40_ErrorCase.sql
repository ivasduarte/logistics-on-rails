DECLARE
    v_freight_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('=========================================================');
    DBMS_OUTPUT.PUT_LINE('USBD40: Register a Freight In The Database Error Test');
    DBMS_OUTPUT.PUT_LINE('=========================================================');
    DBMS_OUTPUT.PUT_LINE('');

    DBMS_OUTPUT.PUT_LINE('Attempting to register cargo from a location WITHOUT railcars (Origin 1)...');
    DBMS_OUTPUT.PUT_LINE(' ');

    v_freight_id := fncRegisterFreight(
            p_origin      => 1,      -- São Romão
            p_destination => 50,
            p_date        => SYSDATE,
            p_train_id    => NULL,
            p_model_id    => 1212,
            p_quantity    => 1
                    );

    DBMS_OUTPUT.PUT_LINE('[ERROR] The system made it possible to create cargo without train cars.');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20032 THEN
            DBMS_OUTPUT.PUT_LINE('[TEST SUCCESSFUL] The system was blocked due to a lack of train cars.');
            DBMS_OUTPUT.PUT_LINE('[Error Message] ' || SQLERRM);
        ELSE
            DBMS_OUTPUT.PUT_LINE('[UNEXPECTED ERROR] ' || SQLERRM);
        END IF;

        DBMS_OUTPUT.PUT_LINE('=========================================================');

END;
/