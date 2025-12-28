BEGIN
    -- 1. Limpeza de dados de movimento (Filhos primeiro)
    DELETE FROM Rail_Line_Path WHERE Rail_Lineid = 1;
    DELETE FROM Freight WHERE id = 50;

    -- 2. Garantir infraestrutura básica (Ignorar se já existir)
    BEGIN INSERT INTO Path (id) VALUES (1); EXCEPTION WHEN OTHERS THEN NULL; END;
    BEGIN INSERT INTO Path (id) VALUES (2); EXCEPTION WHEN OTHERS THEN NULL; END;

    BEGIN INSERT INTO Endpoint_Type (id, "desc") VALUES (1, 'Station'); EXCEPTION WHEN OTHERS THEN NULL; END;
    BEGIN INSERT INTO Location (id, latitude, longitude) VALUES (1, '41.1', '-8.6'); EXCEPTION WHEN OTHERS THEN NULL; END;
    BEGIN INSERT INTO Owner (shortName, name, VATNumber) VALUES ('CP', 'Comboios de Portugal', '123456789'); EXCEPTION WHEN OTHERS THEN NULL; END;
    BEGIN INSERT INTO Gauge (gaugeNumber) VALUES (1668); EXCEPTION WHEN OTHERS THEN NULL; END;

    -- 3. Garantir os Endpoints (IDs 10 e 11)
    BEGIN
        INSERT INTO Endpoint (endpointID, name, locationID, typeID) VALUES (10, 'Porto-Sao Bento', 1, 1);
    EXCEPTION WHEN OTHERS THEN
        UPDATE Endpoint SET name = 'Porto-Sao Bento' WHERE endpointID = 10;
    END;

    BEGIN
        INSERT INTO Endpoint (endpointID, name, locationID, typeID) VALUES (11, 'Porto-Campanha', 1, 1);
    EXCEPTION WHEN OTHERS THEN
        UPDATE Endpoint SET name = 'Porto-Campanha' WHERE endpointID = 11;
    END;

    -- 4. Garantir a Rail_Line (ID 1) - FUNDAMENTAL para a linha 46 não falhar
    BEGIN
        INSERT INTO Rail_Line (id, name, ownerID, startID, endID, gauge)
        VALUES (1, 'Linha Norte', 'CP', 10, 11, 1668);
    EXCEPTION WHEN OTHERS THEN
        -- Se já existir, garante que os dados estão corretos para o teste
        UPDATE Rail_Line SET name = 'Linha Norte', ownerID = 'CP', startID = 10, endID = 11, gauge = 1668 WHERE id = 1;
    END;

    -- 5. Criar a Rota (Aqui já temos a Rail_Lineid = 1 garantida)
    INSERT INTO Rail_Line_Path (Rail_Lineid, Pathid, "order", EndpointID, arrivalTime, departureTime)
    VALUES (1, 1, 1, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

    INSERT INTO Rail_Line_Path (Rail_Lineid, Pathid, "order", EndpointID, arrivalTime, departureTime)
    VALUES (1, 2, 2, 11, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

    -- 6. Criar o Frete Pendente
    INSERT INTO Freight (id, "date", origin, destination, trainID)
    VALUES (50, SYSDATE, 10, 11, NULL);

    COMMIT;
END;
/