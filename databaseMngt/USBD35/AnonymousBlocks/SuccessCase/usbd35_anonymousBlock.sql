DECLARE
    v_new_endpoint_id NUMBER;
    v_facility_count_before NUMBER;
    v_facility_count_after NUMBER;

    -- Cursor para listar todos os Endpoints
    CURSOR c_facilities IS
        SELECT e.endpointID, e.name, et."desc" AS endpoint_type,
               l.latitude, l.longitude
        FROM Endpoint e
        JOIN Endpoint_Type et ON e.typeID = et.id
        JOIN Location l ON e.locationID = l.id
        ORDER BY e.endpointID;

BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('USBD35: Register New Facility Test');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('');

    -- Conta o número de Endpoints antes
    SELECT COUNT(*) INTO v_facility_count_before FROM Endpoint;
    DBMS_OUTPUT.PUT_LINE('Total facilities before: ' || v_facility_count_before);
    DBMS_OUTPUT.PUT_LINE('');

    -- Lista todos os Endpoints antes
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('FACILITIES BEFORE REGISTRATION');
    DBMS_OUTPUT.PUT_LINE('========================================');

    FOR rec IN c_facilities LOOP
        DBMS_OUTPUT.PUT_LINE('Facility ID: ' || rec.endpointID);
        DBMS_OUTPUT.PUT_LINE('  Name: ' || rec.name);
        DBMS_OUTPUT.PUT_LINE('  Type: ' || rec.endpoint_type);
        DBMS_OUTPUT.PUT_LINE('  Location: ' || rec.latitude || ', ' || rec.longitude);
        DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    END LOOP;

    -- Regista um novo Endpoint
    v_new_endpoint_id := fncRegisterNewFacility(
        p_name => 'Porto Intermodal Terminal',
        p_latitude => '41.1579',
        p_longitude => '-8.6291',
        p_type_id => 1
    );

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('New facility registered successfully!');
    DBMS_OUTPUT.PUT_LINE('New Endpoint ID: ' || v_new_endpoint_id);

    -- Lista todos os Endpoints Depois
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('FACILITIES AFTER REGISTRATION');
    DBMS_OUTPUT.PUT_LINE('========================================');

    FOR rec IN c_facilities LOOP
        DBMS_OUTPUT.PUT_LINE('Facility ID: ' || rec.endpointID);
        DBMS_OUTPUT.PUT_LINE('  Name: ' || rec.name);

        IF rec.endpointID = v_new_endpoint_id THEN
            DBMS_OUTPUT.PUT_LINE('  >>> NEWLY REGISTERED <<<');
        END IF;

        DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    END LOOP;

END;