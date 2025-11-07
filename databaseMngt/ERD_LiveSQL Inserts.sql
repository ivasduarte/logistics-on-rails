INSERT INTO Location (id, latitude, longitude)
VALUES (1, 100, 100);

INSERT INTO Location (id, latitude, longitude)
VALUES (2, 200, 200);

INSERT INTO Location (id, latitude, longitude)
VALUES (3, 300, 300);

INSERT INTO Location (id, latitude, longitude)
VALUES (4, 400, 400);

INSERT INTO Location (id, latitude, longitude)
VALUES (5, 500, 500);

INSERT INTO Location (id, latitude, longitude)
VALUES (6, 600, 600);

INSERT INTO Location (id, latitude, longitude)
VALUES (7, 700, 700);

INSERT INTO Location (id, latitude, longitude)
VALUES (8, 800, 800);

INSERT INTO Location (id, latitude, longitude)
VALUES (9, 900, 900);

INSERT INTO Location (id, latitude, longitude)
VALUES (10, 1000, 1000);

INSERT INTO Location (id, latitude, longitude)
VALUES (11, 1100, 1100);

INSERT INTO Location (id, latitude, longitude)
VALUES (12, 1200, 1200);

INSERT INTO Location (id, latitude, longitude)
VALUES (13, 1300, 1300);

INSERT INTO Location (id, latitude, longitude)
VALUES (14, 1400, 1400);

INSERT INTO Location (id, latitude, longitude)
VALUES (15, 1500, 1500);

INSERT INTO Location (id, latitude, longitude)
VALUES (16, 1600, 1600);

INSERT INTO Location (id, latitude, longitude)
VALUES (17, 1700, 1700);

INSERT INTO Location (id, latitude, longitude)
VALUES (18, 1800, 1800);

INSERT INTO Location (id, latitude, longitude)
VALUES (19, 1900, 1900);

INSERT INTO Location (id, latitude, longitude)
VALUES (20, 2000, 2000);

INSERT INTO Location (id, latitude, longitude)
VALUES (21, 2100, 2100);

INSERT INTO Location (id, latitude, longitude)
VALUES (22, 2200, 2200);

INSERT INTO Location (id, latitude, longitude)
VALUES (23, 2300, 2300);


INSERT INTO Endpoint (endpointID, name, locationID)
VALUES (1, 'São Romão', 1);

INSERT INTO Endpoint (endpointID, name, locationID)
VALUES (2, 'Tamel', 2);

INSERT INTO Endpoint (endpointID, name, locationID)
VALUES (3, 'Senhora das Dores', 3);

INSERT INTO Endpoint (endpointID, name, locationID)
VALUES (4, 'Lousado', 4);

INSERT INTO Endpoint (endpointID, name, locationID)
VALUES (5, 'Porto Campanhã', 5);

INSERT INTO Endpoint (endpointID, name, locationID)
VALUES (6, 'Leandro', 6);

INSERT INTO Endpoint (endpointID, name, locationID)
VALUES (7, 'Porto São Bento', 7);

INSERT INTO Endpoint (endpointID, name, locationID)
VALUES (8, 'Barcelos', 8);

INSERT INTO Endpoint (endpointID, name, locationID)
VALUES (9, 'Vila Nova da Cerveira', 9);

INSERT INTO Endpoint (endpointID, name, locationID)
VALUES (10, 'Midões', 10);

INSERT INTO Endpoint (endpointID, name, locationID)
VALUES (11, 'Valença', 11);

INSERT INTO Endpoint (endpointID, name, locationID)
VALUES (12, 'Darque', 12);

INSERT INTO Endpoint (endpointID, name, locationID)
VALUES (13, 'Contumil', 13);

INSERT INTO Endpoint (endpointID, name, locationID)
VALUES (14, 'Ermesinde', 14);

INSERT INTO Endpoint (endpointID, name, locationID)
VALUES (15, 'São Frutuoso', 15);

INSERT INTO Endpoint (endpointID, name, locationID)
VALUES (16, 'São Pedro da Torre', 16);

INSERT INTO Endpoint (endpointID, name, locationID)
VALUES (17, 'Viana do Castelo', 17);

INSERT INTO Endpoint (endpointID, name, locationID)
VALUES (18, 'Famalicão', 18);

INSERT INTO Endpoint (endpointID, name, locationID)
VALUES (19, 'Barroselas', 19);

INSERT INTO Endpoint (endpointID, name, locationID)
VALUES (20, 'Nine', 20);

INSERT INTO Endpoint (endpointID, name, locationID)
VALUES (21, 'Caminha', 21);

INSERT INTO Endpoint (endpointID, name, locationID)
VALUES (22, 'Carvalha', 22);

INSERT INTO Endpoint (endpointID, name, locationID)
VALUES (23, 'Carreço', 23);


INSERT INTO Operator (shortName, name, VATNumber)
VALUES ('Medway', 'Medway - Operador Ferroviário de Mercadorias, S.A', 'PT509017800');


INSERT INTO Owner (shortName, name, VATNumber)
VALUES ('IP', 'Infraestruturas de Portugal, SA', 'PT503933813');


INSERT INTO Rail_Line (id, name, ownerID, startID, endID, gauge)
VALUES (1, 'Ramal São Bento - Campanhã', 'IP', 7, 5, 1668);

INSERT INTO Rail_Line (id, name, ownerID, startID, endID, gauge)
VALUES (2, 'Ramal Camapanhã - Nine', 'IP', 5, 20, 1668);

INSERT INTO Rail_Line (id, name, ownerID, startID, endID, gauge)
VALUES (3, 'Ramal Nine - Barcelos', 'IP', 20, 8, 1668);

INSERT INTO Rail_Line (id, name, ownerID, startID, endID, gauge)
VALUES (4, 'Ramal Barcelos - Viana', 'IP', 8, 17, 1668);

INSERT INTO Rail_Line (id, name, ownerID, startID, endID, gauge)
VALUES (5, 'Ramal viana - Caminha', 'IP', 17, 21, 1668);

INSERT INTO Rail_Line (id, name, ownerID, startID, endID, gauge)
VALUES (6, 'Ramal Caminha - Torre', 'IP', 21, 16, 1668);

INSERT INTO Rail_Line (id, name, ownerID, startID, endID, gauge)
VALUES (7, 'Ramal Torre - Valença', 'IP', 16, 11, 1668);


INSERT INTO Line_Type (id, isElectric, numberTracks)
VALUES (1, 'Yes', 4);

INSERT INTO Line_Type (id, isElectric, numberTracks)
VALUES (2, 'Yes', 2);


INSERT INTO Line_Segment (id, Line_TypeID, max_weight, length, Rail_LineId, orderPosition)
VALUES (1, 1, 8000, 2618, 1, 1);

INSERT INTO Line_Segment (id, Line_TypeID, max_weight, length, Rail_LineId, orderPosition)
VALUES (10, 2, 8000, 29003, 2, 1);

INSERT INTO Line_Segment (id, Line_TypeID, max_weight, length, Rail_LineId, orderPosition)
VALUES (11, 2, 8000, 10000, 2, 2);

INSERT INTO Line_Segment (id, Line_TypeID, max_weight, length, Rail_LineId, orderPosition)
VALUES (15, 2, 8000, 5286, 3, 1);

INSERT INTO Line_Segment (id, Line_TypeID, max_weight, length, Rail_LineId, orderPosition)
VALUES (16, 2, 8000, 6000, 3, 2);

INSERT INTO Line_Segment (id, Line_TypeID, max_weight, length, Rail_LineId, orderPosition)
VALUES (14, 2, 8000, 10387, 4, 1);

INSERT INTO Line_Segment (id, Line_TypeID, max_weight, length, Rail_LineId, orderPosition)
VALUES (12, 2, 8000, 12000, 4, 2);

INSERT INTO Line_Segment (id, Line_TypeID, max_weight, length, Rail_LineId, orderPosition)
VALUES (13, 2, 6400, 8000, 4, 3);

INSERT INTO Line_Segment (id, Line_TypeID, max_weight, length, Rail_LineId, orderPosition)
VALUES (20, 2, 8000, 6000, 5, 1);

INSERT INTO Line_Segment (id, Line_TypeID, max_weight, length, Rail_LineId, orderPosition)
VALUES (21, 2, 8000, 3000, 5, 2);

INSERT INTO Line_Segment (id, Line_TypeID, max_weight, length, Rail_LineId, orderPosition)
VALUES (22, 2, 8000, 15000, 5, 3);

INSERT INTO Line_Segment (id, Line_TypeID, max_weight, length, Rail_LineId, orderPosition)
VALUES (25, 2, 8000, 20829, 6, 1);

INSERT INTO Line_Segment (id, Line_TypeID, max_weight, length, Rail_LineId, orderPosition)
VALUES (26, 2, 8000, 4264, 7, 1);


INSERT INTO Rolling_Stock (id, name)
VALUES (1, 'WAGON');

INSERT INTO Rolling_Stock (id, name)
VALUES (2, 'LOCOMOTIVE');


INSERT INTO Operator_Rolling_Stock (operator, Rolling_Stockid)
VALUES ('Medway', 1);

INSERT INTO Operator_Rolling_Stock (operator, Rolling_Stockid)
VALUES ('Medway', 2);


INSERT INTO Wagon_Model (id, model, maker, numberBogies, bogies, length, width, height, weight, maxSpeed, payload, volume)
VALUES (1245, 'Tadgs 32 94 082 3', 'Metalsines', 2, 'Duplo', 17240, 3072, 4270, 24, 120, 56, 75);

INSERT INTO Wagon_Model (id, model, maker, numberBogies, bogies, length, width, height, weight, maxSpeed, payload, volume)
VALUES (1278, 'Tdgs 41 94 074 1', 'Equimetal', 2, 'Duplo', 9640, 3120, 4165.5, 13.8, 100, 26.2, 38);

INSERT INTO Wagon_Model (id, model, maker, numberBogies, bogies, length, width, height, weight, maxSpeed, payload, volume)
VALUES (1325, 'Gabs 81 94 181 1', 'Sepsa Cometna', 2, 'Duplo', 21700, 3180, 4170, 29.8, 100, 50.2, 110);

INSERT INTO Wagon_Model (id, model, maker, numberBogies, bogies, length, width, height, weight, maxSpeed, payload, volume)
VALUES (1104, 'Regmms 32 94 356 3', 'Metalsines', 2, 'Duplo', 14040, 3104, 2535, 21.2, 120, 60.6, 76.3);

INSERT INTO Wagon_Model (id, model, maker, numberBogies, bogies, length, width, height, weight, maxSpeed, payload, volume)
VALUES (985, 'Lgs 22 94 441 6', 'Metalsines', 1, 'Simples', 13860, 2850, 1060, 11.9, 120, 28.1, 76.3);

INSERT INTO Wagon_Model (id, model, maker, numberBogies, bogies, length, width, height, weight, maxSpeed, payload, volume)
VALUES (987, 'Sgnss 12 94 455 2', 'Emef', 2, 'Duplo', 18116, 2950, 1030, 21.6, 120, 68.4, 76.3);

INSERT INTO Wagon_Model (id, model, maker, numberBogies, bogies, length, width, height, weight, maxSpeed, payload, volume)
VALUES (988, 'Sgnss 12 94 455 2', 'Emef', 2, 'Duplo', 18116, 2950, 1030, 21.6, 120, 68.4, 76.3);


INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (3563077, 1, 1104, 1987, 1668);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (3563078, 1, 1104, 1987, 1688);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (3563079, 1, 1104, 1987, 1688);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (3563080, 1, 1104, 1987, 1688);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (3563081, 1, 1104, 1987, 1688);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (3563082, 1, 1104, 1987, 1688);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (3563083, 1, 1104, 1987, 1688);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (3563084, 1, 1104, 1987, 1688);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (3563085, 1, 1104, 1987, 1688);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (3563086, 1, 1104, 1987, 1688);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (3563087, 1, 1104, 1987, 1688);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (3563088, 1, 1104, 1987, 1688);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (3563089, 1, 1104, 1987, 1688);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (3563090, 1, 1104, 1987, 1688);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (3563091, 1, 1104, 1987, 1688);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (3563092, 1, 1104, 1987, 1688);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (0823045, 1, 1245, 1990, 1688);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (0823046, 1, 1245, 1990, 1688);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (0823047, 1, 1245, 1990, 1688);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (0823048, 1, 1245, 1990, 1688);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (0741001, 1, 1278, 1977, 1688);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (0741002, 1, 1278, 1977, 1688);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (0741003, 1, 1278, 1977, 1688);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (0741004, 1, 1278, 1977, 1688);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (0741005, 1, 1278, 1977, 1688);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (0741006, 1, 1278, 1977, 1688);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (1811010, 1, 1325, 1977, 1688);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (1811011, 1, 1325, 1977, 1688);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (1811012, 1, 1325, 1977, 1688);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (1811013, 1, 1325, 1977, 1688);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (1811014, 1, 1325, 1977, 1688);

INSERT INTO Wagon (id, rollingStockID, modelID, serviceYear, gauge)
VALUES (1811015, 1, 1325, 1977, 1688);


INSERT INTO Wagon_Hopper_Car (id, storedCoal, storedGrains, storedMinerals)
VALUES (1245, 80, 65, 13);

INSERT INTO Wagon_Hopper_Car (id, storedCoal, storedGrains, storedMinerals)
VALUES (1278, 97, 132, 10);


INSERT INTO Wagon_Flatcar (id, storedContainers, storedVehicles, storedMachinery)
VALUES (1325, 26, 45, 102);

INSERT INTO Wagon_Flatcar (id, storedContainers, storedVehicles, storedMachinery)
VALUES (1104, 73, 10, 12);

INSERT INTO Wagon_Flatcar (id, storedContainers, storedVehicles, storedMachinery)
VALUES (985, 12, 24, 53);

INSERT INTO Wagon_Flatcar (id, storedContainers, storedVehicles, storedMachinery)
VALUES (987, 92, 38, 83);

INSERT INTO Wagon_Flatcar (id, storedContainers, storedVehicles, storedMachinery)
VALUES (988, 30, 58, 61);


INSERT INTO Locomotive_Model (id, model, numberBogies, bogies, power, length, width, height, maxSpeed, operationalSpeed, traction)
VALUES (1, 'Eurosprinter', 2, 'Bo-Bo', 5600, 19.2, 3, 4.375, 220, 70, 300);

INSERT INTO Locomotive_Model (id, model, numberBogies, bogies, power, length, width, height, maxSpeed, operationalSpeed, traction)
VALUES (2, 'CP 1900', 2, 'Co-Co', 1623, 19.084, 3.062, 4.31, 100, 42.5, 396);


INSERT INTO Locomotive (id, rollingStockID, name, make, modelID, entryYear, gauge)
VALUES (5621, 2, 'Inês', 'Siemens', 1, 1995, 1668);

INSERT INTO Locomotive (id, rollingStockID, name, make, modelID, entryYear, gauge)
VALUES (5623, 2, 'Paz', 'Siemens', 1, 1995, 1668);

INSERT INTO Locomotive (id, rollingStockID, name, make, modelID, entryYear, gauge)
VALUES (5630, 2, 'Helena', 'Siemens', 1, 1996, 1668);

INSERT INTO Locomotive (id, rollingStockID, name, make, modelID, entryYear, gauge)
VALUES (1903, 2, 'Eva', 'Sorefame - Alsthom', 2, 1981, 1668);


INSERT INTO Locomotive_Diesel (id, maxFuelCapacity)
VALUES (2, 4882);


INSERT INTO Locomotive_Electric (id, voltage, frequency)
VALUES (1, 25, 50);
