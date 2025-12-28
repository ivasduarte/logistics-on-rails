DECLARE
    v_new_building_id NUMBER;
    v_building_count_before NUMBER;
    v_building_count_after NUMBER;

    -- Cursor para listar todos os Buildings com suas Facilities
    CURSOR c_buildings IS
        SELECT b.buildingID, b.typeID, bt."desc" AS building_type,
               e.endpointID, e.name AS facility_name
        FROM building b
        JOIN Building_Type bt ON b.typeID = bt.id
        LEFT JOIN Endpoint_Building eb ON b.buildingID = eb.BuildingbuildingID
        LEFT JOIN Endpoint e ON eb.EndpointendpointID = e.endpointID
        ORDER BY b.buildingID, e.endpointID;

BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('USBD36: Add Building to Facility Test');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('');

    -- Conta o número de Buildings antes
    SELECT COUNT(*) INTO v_building_count_before FROM building;
    DBMS_OUTPUT.PUT_LINE('Total buildings before: ' || v_building_count_before);
    DBMS_OUTPUT.PUT_LINE('');

    -- Lista todos os Buildings antes
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('BUILDINGS BEFORE REGISTRATION');
    DBMS_OUTPUT.PUT_LINE('========================================');

    FOR rec IN c_buildings LOOP
        DBMS_OUTPUT.PUT_LINE('Building ID: ' || rec.buildingID || ' | Type ID: ' || rec.typeID);
        DBMS_OUTPUT.PUT_LINE('  Type: ' || rec.building_type);
        DBMS_OUTPUT.PUT_LINE('  Facility: ' || NVL(rec.facility_name, 'N/A') || ' (ID: ' || NVL(TO_CHAR(rec.endpointID), 'N/A') || ')');
        DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    END LOOP;

    -- Adiciona um novo Building a uma Facility existente
    v_new_building_id := fncAddBuildingToFacility(
        p_facility_id => 1,
        p_building_type_id => 1
    );

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('New building registered successfully!');
    DBMS_OUTPUT.PUT_LINE('New Building ID: ' || v_new_building_id);

    -- Lista todos os Buildings depois
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('BUILDINGS AFTER REGISTRATION');
    DBMS_OUTPUT.PUT_LINE('========================================');

    FOR rec IN c_buildings LOOP
        DBMS_OUTPUT.PUT_LINE('Building ID: ' || rec.buildingID || ' | Type ID: ' || rec.typeID);
        DBMS_OUTPUT.PUT_LINE('  Type: ' || rec.building_type);
        DBMS_OUTPUT.PUT_LINE('  Facility: ' || NVL(rec.facility_name, 'N/A') || ' (ID: ' || NVL(TO_CHAR(rec.endpointID), 'N/A') || ')');

        IF rec.buildingID = v_new_building_id THEN
            DBMS_OUTPUT.PUT_LINE('  >>> NEWLY REGISTERED <<<');
        END IF;

        DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    END LOOP;

    -- Rollback para não afetar a base de dados
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('Test complete. Changes rolled back.');
END;
/