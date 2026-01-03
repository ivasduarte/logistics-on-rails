CREATE OR REPLACE FUNCTION fncRemoveFreightFromPlannedTrain (
    p_trainId   IN NUMBER,
    p_freightId IN NUMBER
)
RETURN NUMBER
IS
    v_train_count   NUMBER;
    v_freight_count NUMBER;
    v_assoc_count   NUMBER;
BEGIN
    -- 1. Validação de inputs
    IF p_trainId IS NULL OR p_freightId IS NULL THEN
        RAISE_APPLICATION_ERROR(-20020, 'Train ID and Freight ID are required.');
    END IF;

    -- 2. Verificar se o train existe
    SELECT COUNT(*)
    INTO v_train_count
    FROM TRAIN
    WHERE ID = p_trainId;

    IF v_train_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20021, 'Train ID does not exist.');
    END IF;

    -- 3. Verificar se o freight existe
    SELECT COUNT(*)
    INTO v_freight_count
    FROM FREIGHT
    WHERE ID = p_freightId;

    IF v_freight_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20022, 'Freight ID does not exist.');
    END IF;

    -- 4. Verificar se o freight está associado ao train (via wagons do operador)
    SELECT COUNT(*)
    INTO v_assoc_count
    FROM WAGON_FREIGHT wf
    JOIN OPERATOR_WAGON ow ON ow.WAGONID = wf.WAGONID
    JOIN TRAIN t ON UPPER(TRIM(t.OPERATOR)) = UPPER(TRIM(ow.OPERATORSHORTNAME))
    WHERE t.ID = p_trainId
      AND wf.FREIGHTID = p_freightId;

    IF v_assoc_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20023,'Freight is not associated with the given planned train.');
    END IF;

    -- 5. Remover todas as associações freight–wagon do train
    DELETE FROM WAGON_FREIGHT wf
    WHERE wf.FREIGHTID = p_freightId
      AND wf.WAGONID IN (
          SELECT ow.WAGONID
          FROM OPERATOR_WAGON ow
          JOIN TRAIN t
            ON UPPER(TRIM(t.OPERATOR)) = UPPER(TRIM(ow.OPERATORSHORTNAME))
          WHERE t.ID = p_trainId
      );

    -- 6. Retornar nº de associações removidas
    RETURN SQL%ROWCOUNT;

EXCEPTION
    WHEN OTHERS THEN
        RAISE;
END fncRemoveFreightFromPlannedTrain;
/
