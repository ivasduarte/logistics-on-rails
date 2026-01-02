CREATE OR REPLACE FUNCTION fncAssociateLocomotiveToTrain(
    p_locomotive_id IN NUMBER,
    p_train_id      IN NUMBER
)
    RETURN NUMBER
    IS
    v_dummy NUMBER;
BEGIN
    -- 1. Validações
    IF p_locomotive_id IS NULL OR p_train_id IS NULL THEN
        RAISE_APPLICATION_ERROR(-20010, 'Inputs required.');
    END IF;

    -- Verificar existências
    BEGIN
        SELECT 1 INTO v_dummy FROM Locomotive WHERE id = p_locomotive_id;
    EXCEPTION WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20011, 'Locomotive not found.');
    END;

    BEGIN
        SELECT 1 INTO v_dummy FROM Train WHERE id = p_train_id;
    EXCEPTION WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20012, 'Train not found.');
    END;

    -- Verificar duplicados
    SELECT COUNT(*) INTO v_dummy FROM Locomotive_Train
    WHERE Locomotiveid = p_locomotive_id AND Trainid = p_train_id;

    IF v_dummy > 0 THEN
        RAISE_APPLICATION_ERROR(-20013, 'Locomotive already associated.');
    END IF;

    -- 2. Tentar Inserir
    INSERT INTO Locomotive_Train (Locomotiveid, Trainid)
    VALUES (p_locomotive_id, p_train_id);

    COMMIT;
    RETURN 1; -- Sucesso

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE; -- Passa o erro para o utilizador
END fncAssociateLocomotiveToTrain;
/