DECLARE
    v_result_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('USBD36: Testing Error Scenarios');
    DBMS_OUTPUT.PUT_LINE('========================================');

    -- TESTE 1: Parâmetro Facility ID Nulo (Esperado o erro -20001)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 1: Testing NULL Facility ID');
        v_result_id := fncAddBuildingToFacility(
            p_facility_id => NULL,  -- Esta linha causa erro
            p_building_type_id => 1
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- TESTE 2: Parâmetro Building Type ID Nulo (Esperado o erro -20001)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 2: Testing NULL Building Type ID');
        v_result_id := fncAddBuildingToFacility(
            p_facility_id => 1,
            p_building_type_id => NULL  -- Esta linha causa erro
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- TESTE 3: Facility Inexistente (Esperado o erro -20002)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 3: Testing Non-existent Facility (ID 99999)');
        v_result_id := fncAddBuildingToFacility(
            p_facility_id => 99999, -- Este ID não existe
            p_building_type_id => 1
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- TESTE 4: Tipo de Building Inexistente (Esperado o erro -20003)
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Scenario 4: Testing Invalid Building Type ID (9999)');
        v_result_id := fncAddBuildingToFacility(
            p_facility_id => 1, -- Assumindo que existe
            p_building_type_id => 9999 -- Este ID não existe na tabela Building_Type
        );
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Caught Expected Error: ' || SQLERRM);
    END;

    -- Como são testes de erro, garantimos que nada foi gravado
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    DBMS_OUTPUT.PUT_LINE('Error testing complete.');
END;
/