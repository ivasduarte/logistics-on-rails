CREATE OR REPLACE FUNCTION fncAddBuildingToFacility(
    p_facility_id IN NUMBER,
    p_building_type_id IN NUMBER
)
RETURN NUMBER
IS
    v_building_id NUMBER;
    v_building_type_exists NUMBER;
    v_dummy NUMBER;
BEGIN
    -- Verifica os inputs fornecidos
    IF p_facility_id IS NULL OR p_building_type_id IS NULL THEN
        RAISE_APPLICATION_ERROR(-20001, 'Facility ID and Building Type ID are required');
    END IF;

    -- Verifica se o Endpoint (facility) existe
    BEGIN
        SELECT 1 INTO v_dummy
        FROM Endpoint
        WHERE endpointID = p_facility_id;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20002, 'Facility with ID ' || p_facility_id || ' does not exist');
    END;

    -- Verifica se o tipo de Building existe
    BEGIN
        SELECT COUNT(*) INTO v_building_type_exists
        FROM Building_Type
        WHERE id = p_building_type_id;

        IF v_building_type_exists = 0 THEN
            RAISE_APPLICATION_ERROR(-20003, 'Invalid building type ID: ' || p_building_type_id);
        END IF;
    END;

    -- Gera um novo ID para o Building
    SELECT NVL(MAX(buildingID), 0) + 1 INTO v_building_id FROM building;

    -- Insere o novo Building
    INSERT INTO building (buildingID, typeID)
    VALUES (v_building_id, p_building_type_id);

    -- Associa o Building ao Endpoint (facility) através da tabela Endpoint_Building
    INSERT INTO Endpoint_Building (EndpointendpointID, BuildingbuildingID)
    VALUES (p_facility_id, v_building_id);

    -- Retorna o ID do novo Building
    RETURN v_building_id;

EXCEPTION
    WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20004, 'Error adding building to facility: ' || SQLERRM);
END fncAddBuildingToFacility;
/