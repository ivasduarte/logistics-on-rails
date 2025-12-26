CREATE OR REPLACE FUNCTION fncAssociateFreightWithTrain(
    p_freight_id IN NUMBER,
    p_train_id IN NUMBER
)
    RETURN NUMBER
    IS
    v_freight_count NUMBER;
    v_train_count NUMBER;
    v_wagons_associated NUMBER := 0;
    v_current_train_id NUMBER;
BEGIN
    -- Valida se existem todos os inputs necessários
    IF p_freight_id IS NULL OR p_train_id IS NULL THEN
        RAISE_APPLICATION_ERROR(-20001, 'All parameters are required for freight-train association');
    END IF;

    -- Valida se o freight existe
    SELECT COUNT(*) INTO v_freight_count
    FROM Freight
    WHERE id = p_freight_id;

    IF v_freight_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Freight ID does not exist');
    END IF;

    -- Valida se o train existe
    SELECT COUNT(*) INTO v_train_count
    FROM Train
    WHERE id = p_train_id;

    IF v_train_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20003, 'Train ID does not exist');
    END IF;

    -- Verifica se o freight já está associado a outro train
    SELECT trainID INTO v_current_train_id
    FROM Freight
    WHERE id = p_freight_id;

    IF v_current_train_id IS NOT NULL AND v_current_train_id != p_train_id THEN
        RAISE_APPLICATION_ERROR(-20004, 'Freight is already associated with another train (Train ID: ' || v_current_train_id || ')');
    END IF;

    -- Se já está associado ao mesmo train, apenas retorna a contagem de wagons
    IF v_current_train_id = p_train_id THEN
        SELECT COUNT(*) INTO v_wagons_associated
        FROM Wagon_Freight
        WHERE Freightid = p_freight_id;

        RETURN v_wagons_associated;
    END IF;

    -- Associa o freight ao train (apenas se ainda não estiver associado)
    UPDATE Freight
    SET trainID = p_train_id
    WHERE id = p_freight_id;

    -- Conta quantos wagons estão no freight
    SELECT COUNT(*) INTO v_wagons_associated
    FROM Wagon_Freight
    WHERE Freightid = p_freight_id;

    COMMIT;

    -- Retorna o número de wagons associados
    RETURN v_wagons_associated;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END fncAssociateFreightWithTrain;