CREATE OR REPLACE FUNCTION fncRegisterNewFacility(
    p_name IN VARCHAR2,
    p_latitude IN VARCHAR2,
    p_longitude IN VARCHAR2,
    p_type_id IN NUMBER
)
RETURN NUMBER
IS
    v_location_id NUMBER;
    v_endpoint_id NUMBER;
    v_location_exists NUMBER := 0;
BEGIN
    -- Verifica os inputs fornecidos
    IF p_name IS NULL OR p_latitude IS NULL OR p_longitude IS NULL OR p_type_id IS NULL THEN
        RAISE_APPLICATION_ERROR(-20001, 'All parameters except entry_date are required');
    END IF;

    -- Verifica se já existe uma Location com aquelas coordenadas
    BEGIN
        SELECT id INTO v_location_id
        FROM Location
        WHERE latitude = p_latitude AND longitude = p_longitude;

        v_location_exists := 1;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_location_exists := 0;
    END;

    -- Se não existir essa localização, cria-a
    IF v_location_exists = 0 THEN
        SELECT NVL(MAX(id), 0) + 1 INTO v_location_id FROM Location;

        INSERT INTO Location (id, latitude, longitude)
        VALUES (v_location_id, p_latitude, p_longitude);
    END IF;

    -- Verifica se o tipo de Endpoint existe
    DECLARE
        v_type_exists NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_type_exists
        FROM Endpoint_Type
        WHERE id = p_type_id;

        IF v_type_exists = 0 THEN
            RAISE_APPLICATION_ERROR(-20002, 'Invalid endpoint type ID: ' || p_type_id);
        END IF;
    END;

    -- Gera um novo ID para o Endpoint
    SELECT NVL(MAX(endpointID), 0) + 1 INTO v_endpoint_id FROM Endpoint;

    -- Insere o novo Endpoint
    INSERT INTO Endpoint (endpointID, name, locationID, typeID)
    VALUES (v_endpoint_id, p_name, v_location_id, p_type_id);

    -- Retorna o ID do novo Endpoint
    RETURN v_endpoint_id;

EXCEPTION
    WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20003, 'Error registering facility: ' || SQLERRM);
END fncRegisterNewFacility;
/