CREATE OR REPLACE TRIGGER trg_CheckTrainLength_Compound
    FOR INSERT OR UPDATE ON Locomotive_Train
    COMPOUND TRIGGER

    -- Estrutura para guardar os IDs dos comboios afetados nesta transação
    TYPE t_train_ids IS TABLE OF NUMBER INDEX BY PLS_INTEGER;
    v_trains_affected t_train_ids;

    -- Constante do limite
    c_MAX_LENGTH CONSTANT NUMBER := 750;

    -- Guardar o ID do comboio que está a ser modificado
BEFORE EACH ROW IS
BEGIN
    -- Guardamos o ID do comboio num array para verificar no fim
    v_trains_affected(:NEW.Trainid) := 1;
END BEFORE EACH ROW;

    AFTER STATEMENT IS
        v_train_id     NUMBER;
        v_locos_len    NUMBER;
        v_wagons_len   NUMBER;
        v_total_len    NUMBER;
        v_index        NUMBER;
    BEGIN
        -- Percorrer todos os comboios
        v_index := v_trains_affected.FIRST;

        WHILE v_index IS NOT NULL LOOP
                v_train_id := v_index;

                -- Calcular comprimento das locomotivas
                SELECT NVL(SUM(lm.length), 0)
                INTO v_locos_len
                FROM Locomotive_Train lt
                         JOIN Locomotive l ON lt.Locomotiveid = l.id
                         JOIN Locomotive_Model lm ON l.modelID = lm.id
                WHERE lt.Trainid = v_train_id;

                -- Calcular comprimento dos vagões (convertendo mm para metros)
                SELECT NVL(SUM(wm.length), 0) / 1000
                INTO v_wagons_len
                FROM Freight f
                         JOIN Wagon_Freight wf ON f.id = wf.Freightid
                         JOIN Wagon w ON wf.Wagonid = w.id
                         JOIN Wagon_Model wm ON w.modelID = wm.id
                WHERE f.trainID = v_train_id;

                -- 3. Validar
                v_total_len := v_locos_len + v_wagons_len;

                IF v_total_len > c_MAX_LENGTH THEN
                    RAISE_APPLICATION_ERROR(-20020,
                                            'USBD33: Train length limit exceeded (' || c_MAX_LENGTH || 'm). ' ||
                                            'Train ID: ' || v_train_id || ' | Current Length: ' || v_total_len || 'm');
                END IF;

                v_index := v_trains_affected.NEXT(v_index);
            END LOOP;
    END AFTER STATEMENT;

END trg_CheckTrainLength_Compound;
/