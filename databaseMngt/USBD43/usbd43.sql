CREATE OR REPLACE FUNCTION fncAddElectricLocomotiveModel(
    p_voltage IN NUMBER,
    p_frequency IN NUMBER
)
RETURN NUMBER
IS
    v_electric_id NUMBER;
BEGIN
    -- Valida se existem todos os inputs necessários
    IF p_voltage IS NULL OR p_frequency IS NULL THEN
        RAISE_APPLICATION_ERROR(-20001, 'All parameters are required for electric locomotive model');
    END IF;

    -- Valida se os valores numéricos são positivos
    IF p_voltage <= 0 OR p_frequency <= 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Voltage and frequency must be positive values');
    END IF;

    -- Gera um novo id para a locomotiva elétrica
    SELECT NVL(MAX(id), 0) + 1 INTO v_electric_id FROM Locomotive_Electric;

    -- Insere uma locomotiva elétrica com as especificações necessárias
    INSERT INTO Locomotive_Electric (id, voltage, frequency)
    VALUES (v_electric_id, p_voltage, p_frequency);

    COMMIT;

    -- Retorna o id da locomotiva elétrica
    RETURN v_electric_id;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20003, 'Error adding electric locomotive model: ' || SQLERRM);
END fncAddElectricLocomotiveModel;
/