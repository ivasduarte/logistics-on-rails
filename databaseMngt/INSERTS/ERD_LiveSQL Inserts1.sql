INSERT INTO Owner(shortName, name, VATNumber)
VALUES ('IP', 'Infraestruturas de Portugal, SA', 'PT503933813');

INSERT INTO Operator(shortName, name, VATNumber)
VALUES ('Medway', 'Medway - Operador Ferroviário de Mercadorias, S.A', 'PT509017800');

INSERT INTO Operator(shortName, name, VATNumber)
VALUES ('Captrain', 'Captrain Portugal S.A.', 'PT507832388');

INSERT INTO Location(id, latitude, longitude)
VALUES (1, 100, 100);

INSERT INTO Location(id, latitude, longitude)
VALUES (2, 200, 200);

INSERT INTO Location(id, latitude, longitude)
VALUES (3, 300, 300);

INSERT INTO Location(id, latitude, longitude)
VALUES (4, 400, 400);

INSERT INTO Location(id, latitude, longitude)
VALUES (5, 500, 500);

INSERT INTO Location(id, latitude, longitude)
VALUES (6, 600, 600);

INSERT INTO Location(id, latitude, longitude)
VALUES (7, 700, 700);

INSERT INTO Location(id, latitude, longitude)
VALUES (8, 800, 800);

INSERT INTO Location(id, latitude, longitude)
VALUES (9, 900, 900);

INSERT INTO Location(id, latitude, longitude)
VALUES (10, 1000, 1000);

INSERT INTO Location(id, latitude, longitude)
VALUES (11, 1100, 1100);

INSERT INTO Location(id, latitude, longitude)
VALUES (12, 1200, 1200);

INSERT INTO Location(id, latitude, longitude)
VALUES (13, 1300, 1300);

INSERT INTO Location(id, latitude, longitude)
VALUES (14, 1400, 1400);

INSERT INTO Location(id, latitude, longitude)
VALUES (15, 1500, 1500);

INSERT INTO Location(id, latitude, longitude)
VALUES (16, 1600, 1600);

INSERT INTO Location(id, latitude, longitude)
VALUES (17, 1700, 1700);

INSERT INTO Location(id, latitude, longitude)
VALUES (18, 1800, 1800);

INSERT INTO Location(id, latitude, longitude)
VALUES (19, 1900, 1900);

INSERT INTO Location(id, latitude, longitude)
VALUES (20, 2000, 2000);

INSERT INTO Location(id, latitude, longitude)
VALUES (21, 2100, 2100);

INSERT INTO Location(id, latitude, longitude)
VALUES (22, 2200, 2200);

INSERT INTO Location(id, latitude, longitude)
VALUES (23, 2300, 2300);

INSERT INTO Location(id, latitude, longitude)
VALUES (50, 5000, 5000);

INSERT INTO Location(id, latitude, longitude)
VALUES (45, 4500, 4500);

INSERT INTO Location(id, latitude, longitude)
VALUES (48, 4800, 4800);

INSERT INTO Location(id, latitude, longitude)
VALUES (43, 4300, 4300);

INSERT INTO Gauge(gaugeNumber)
VALUES(1668);

INSERT INTO Gauge(gaugeNumber)
VALUES(1435);

INSERT INTO Line_Type(id, isElectric, numberTracks)
VALUES(1, 'Yes', 4);

INSERT INTO Line_Type(id, isElectric, numberTracks)
VALUES(3, 'Yes', 4);

INSERT INTO Line_Type(id, isElectric, numberTracks)
VALUES(10, 'Yes', 2);

INSERT INTO Line_Type(id, isElectric, numberTracks)
VALUES(11, 'Yes', 2);

INSERT INTO Line_Type(id, isElectric, numberTracks)
VALUES(15, 'Yes', 2);

INSERT INTO Line_Type(id, isElectric, numberTracks)
VALUES(16, 'Yes', 2);

INSERT INTO Line_Type(id, isElectric, numberTracks)
VALUES(14, 'Yes', 2);

INSERT INTO Line_Type(id, isElectric, numberTracks)
VALUES(12, 'Yes', 2);

INSERT INTO Line_Type(id, isElectric, numberTracks)
VALUES(13, 'Yes', 2);

INSERT INTO Line_Type(id, isElectric, numberTracks)
VALUES(20, 'Yes', 2);

INSERT INTO Line_Type(id, isElectric, numberTracks)
VALUES(18, 'Yes', 1);

INSERT INTO Line_Type(id, isElectric, numberTracks)
VALUES(21, 'Yes', 1);

INSERT INTO Line_Type(id, isElectric, numberTracks)
VALUES(22, 'Yes', 1);

INSERT INTO Line_Type(id, isElectric, numberTracks)
VALUES(25, 'Yes', 1);

INSERT INTO Line_Type(id, isElectric, numberTracks)
VALUES(26, 'Yes', 1);

INSERT INTO Line_Type(id, isElectric, numberTracks)
VALUES(30, 'Yes', 2);

INSERT INTO Line_Type(id, isElectric, numberTracks)
VALUES(31, 'Yes', 2);

INSERT INTO Line_Type(id, isElectric, numberTracks)
VALUES(32, 'Yes', 2);

INSERT INTO Line_Type(id, isElectric, numberTracks)
VALUES(33, 'Yes', 2);

INSERT INTO Line_Type(id, isElectric, numberTracks)
VALUES(34, 'Yes', 2);

INSERT INTO Line_Type(id, isElectric, numberTracks)
VALUES(35, 'Yes', 2);

INSERT INTO Line_Type(id, isElectric, numberTracks)
VALUES(36, 'Yes', 2);

INSERT INTO Locomotive_Type(id, name)
VALUES(1, 'Diesel');

INSERT INTO Locomotive_Type(id, name)
VALUES(2, 'Electric');

INSERT INTO Locomotive_Diesel(id, maxFuelCapacity, typeID)
VALUES(1, 6700, 1);

INSERT INTO Locomotive_Diesel(id, maxFuelCapacity, typeID)
VALUES(2, 4882, 1);

INSERT INTO Locomotive_Electric(id, voltage, frequency, typeID)
VALUES(1, 25, 50, 2);

INSERT INTO Locomotive_Model(id, model, numberBogies, bogies, power, length, width, height, maxSpeed, operationalSpeed, traction, typeID, weight)
VALUES(1, 'Eurosprinter', 2, 'Bo-Bo', 5600, 19.2, 3, 4.375, 220, 70, 300, 2, 87);

INSERT INTO Locomotive_Model(id, model, numberBogies, bogies, power, length, width, height, maxSpeed, operationalSpeed, traction, typeID, weight)
VALUES(2, 'CP 1900', 2, 'Co-Co', 1623, 19.084, 3.062, 4.31, 100, 42.5, 396, 2, 117);

INSERT INTO Locomotive_Model(id, model, numberBogies, bogies, power, length, width, height, maxSpeed, operationalSpeed, traction, typeID, weight)
VALUES(3, 'E4000', 3, 'Co-Co', 3178, 23.02, 3000, 4.264, 120, 100, 400, 1, 124);

INSERT INTO Wagon_Type(id, name)
VALUES(1, 'Flatcar');

INSERT INTO Wagon_Type(id, name)
VALUES(2, 'Tank_Car');

INSERT INTO Wagon_Type(id, name)
VALUES(3, 'Hopper_Car');

INSERT INTO Wagon_Type(id, name)
VALUES(4, 'Refrigerated_Car');

INSERT INTO Wagon_Type(id, name)
VALUES(5, 'Boxcar');

INSERT INTO Wagon_Hopper_Car (id, storedCoal, storedGrains, storedMinerals, typeID)
VALUES (1245, 80, 65, 13, 3);

INSERT INTO Wagon_Hopper_Car (id, storedCoal, storedGrains, storedMinerals, typeID)
VALUES (1278, 97, 132, 10, 3);

INSERT INTO Wagon_Flatcar (id, storedContainers, storedVehicles, storedMachinery, typeID)
VALUES (1325, 26, 45, 102, 1);

INSERT INTO Wagon_Flatcar (id, storedContainers, storedVehicles, storedMachinery, typeID)
VALUES (1104, 73, 10, 12, 1);

INSERT INTO Wagon_Flatcar (id, storedContainers, storedVehicles, storedMachinery, typeID)
VALUES (985, 12, 24, 53, 1);

INSERT INTO Wagon_Flatcar (id, storedContainers, storedVehicles, storedMachinery, typeID)
VALUES (987, 92, 38, 83, 1);

INSERT INTO Wagon_Flatcar (id, storedContainers, storedVehicles, storedMachinery, typeID)
VALUES (988, 30, 58, 61, 1);

INSERT INTO Wagon_Tank_Car(id, storedLiquids, storedChemicals, storedFuel, typeID)
VALUES(1525, 39, 20, 39, 2);

INSERT INTO Wagon_Tank_Car(id, storedLiquids, storedChemicals, storedFuel, typeID)
VALUES(1523, 14, 25, 42, 2);

INSERT INTO Wagon_Boxcar(id, storedGeneralGoods, typeID)
VALUES(1212, 58, 5);

INSERT INTO Wagon_Model (id, model, maker, numberBogies, bogies, length, width, height, weight, maxSpeed, payload, volume, typeID)
VALUES (1245, 'Tadgs 32 94 082 3', 'Metalsines', 2, 'Duplo', 17240, 3072, 4270, 24, 120, 56, 75, 3);

INSERT INTO Wagon_Model (id, model, maker, numberBogies, bogies, length, width, height, weight, maxSpeed, payload, volume, typeID)
VALUES (1278, 'Tdgs 41 94 074 1', 'Equimetal', 2, 'Duplo', 9640, 3120, 4165.5, 13.8, 100, 26.2, 38, 3);

INSERT INTO Wagon_Model (id, model, maker, numberBogies, bogies, length, width, height, weight, maxSpeed, payload, volume, typeID)
VALUES (1325, 'Gabs 81 94 181 1', 'Sepsa Cometna', 2, 'Duplo', 21700, 3180, 4170, 29.8, 100, 50.2, 110, 1);

INSERT INTO Wagon_Model (id, model, maker, numberBogies, bogies, length, width, height, weight, maxSpeed, payload, volume, typeID)
VALUES (1104, 'Regmms 32 94 356 3', 'Metalsines', 2, 'Duplo', 14040, 3104, 2535, 21.2, 120, 60.6, 76.3, 1);

INSERT INTO Wagon_Model (id, model, maker, numberBogies, bogies, length, width, height, weight, maxSpeed, payload, volume, typeID)
VALUES (985, 'Lgs 22 94 441 6', 'Metalsines', 1, 'Simples', 13860, 2850, 1060, 11.9, 120, 28.1, 76.3, 1);

INSERT INTO Wagon_Model (id, model, maker, numberBogies, bogies, length, width, height, weight, maxSpeed, payload, volume, typeID)
VALUES (987, 'Sgnss 12 94 455 2', 'Emef', 2, 'Duplo', 18116, 2950, 1030, 21.6, 120, 68.4, 76.3, 1);

INSERT INTO Wagon_Model (id, model, maker, numberBogies, bogies, length, width, height, weight, maxSpeed, payload, volume, typeID)
VALUES (988, 'Sgnss 12 94 455 2', 'Emef', 2, 'Duplo', 18116, 2950, 1030, 21.6, 120, 68.4, 76.3, 1);

INSERT INTO Wagon_Model (id, model, maker, numberBogies, bogies, length, width, height, weight, maxSpeed, payload, volume, typeID)
VALUES (1525, 'Zaes 81 94 788', 'Equimetal', 2, 'Duplo', 13800, 2950, 4226, 22.9, 120, 57.1, 64.6, 2);

INSERT INTO Wagon_Model (id, model, maker, numberBogies, bogies, length, width, height, weight, maxSpeed, payload, volume, typeID)
VALUES (1523, 'Zaes 81 94 788', 'Equimetal', 2, 'Duplo', 13800, 2950, 4226, 22.9, 120, 57.1, 64.6, 2);

INSERT INTO Wagon_Model (id, model, maker, numberBogies, bogies, length, width, height, weight, maxSpeed, payload, volume, typeID)
VALUES (1212, 'Kbs 41 94 333', 'Simmering', 2, 'Simples', 14020, 2842, 3300, 14.2, 100, 25.8, 62.7, 5);

INSERT INTO Endpoint_Type(id, "desc")
VALUES(1, 'Freight Yard');

INSERT INTO Endpoint_Type(id, "desc")
VALUES(2, 'Station');

INSERT INTO Endpoint_Type(id, "desc")
VALUES(3, 'Not Intermodal Terminal');

INSERT INTO Endpoint_Type(id, "desc")
VALUES(4, 'Intermodal Terminal');

INSERT INTO Building_Type(id, "desc")
VALUES(1, 'Warehouse');

INSERT INTO Building_Type(id, "desc")
VALUES(2, 'Refrigerated Area');

INSERT INTO Building_Type(id, "desc")
VALUES(3, 'Grain Silo');

INSERT INTO Building(buildingID, typeID)
VALUES(1, 1);

INSERT INTO Building(buildingID, typeID)
VALUES(2, 2);

INSERT INTO Building(buildingID, typeID)
VALUES(3, 3);

INSERT INTO Endpoint(endpointID, name, locationID, typeID)
VALUES(1, 'São Romão', 1, 2);

INSERT INTO Endpoint(endpointID, name, locationID, typeID)
VALUES(2, 'Tamel', 2, 2);

INSERT INTO Endpoint(endpointID, name, locationID, typeID)
VALUES(3, 'Senhora das Dores', 3, 2);

INSERT INTO Endpoint(endpointID, name, locationID, typeID)
VALUES(4, 'Lousado', 4, 2);

INSERT INTO Endpoint(endpointID, name, locationID, typeID)
VALUES(5, 'Porto Campanhã', 5, 1);

INSERT INTO Endpoint(endpointID, name, locationID, typeID)
VALUES(6, 'Leandro', 6, 2);

INSERT INTO Endpoint(endpointID, name, locationID, typeID)
VALUES(7, 'Porto São Bento', 7, 3);

INSERT INTO Endpoint(endpointID, name, locationID, typeID)
VALUES(8, 'Barcelos', 8, 3);

INSERT INTO Endpoint(endpointID, name, locationID, typeID)
VALUES(9, 'Vila Nova da Cerveira', 9, 2);

INSERT INTO Endpoint(endpointID, name, locationID, typeID)
VALUES(10, 'Midões', 10, 2);

INSERT INTO Endpoint(endpointID, name, locationID, typeID)
VALUES(11, 'Valença', 11, 1);

INSERT INTO Endpoint(endpointID, name, locationID, typeID)
VALUES(12, 'Darque', 12, 1);

INSERT INTO Endpoint(endpointID, name, locationID, typeID)
VALUES(13, 'Contumil', 13, 1);

INSERT INTO Endpoint(endpointID, name, locationID, typeID)
VALUES(14, 'Ermesinde', 14, 2);

INSERT INTO Endpoint(endpointID, name, locationID, typeID)
VALUES(15, 'São Frutuoso', 15, 2);

INSERT INTO Endpoint(endpointID, name, locationID, typeID)
VALUES(16, 'São Pedro da Torre', 16, 3);

INSERT INTO Endpoint(endpointID, name, locationID, typeID)
VALUES(17, 'Viana do Castelo', 17, 3);

INSERT INTO Endpoint(endpointID, name, locationID, typeID)
VALUES(18, 'Famalicão', 18, 2);

INSERT INTO Endpoint(endpointID, name, locationID, typeID)
VALUES(19, 'Barroselas', 19, 2);

INSERT INTO Endpoint(endpointID, name, locationID, typeID)
VALUES(20, 'Nine', 20, 1);

INSERT INTO Endpoint(endpointID, name, locationID, typeID)
VALUES(21, 'Caminha', 21, 1);

INSERT INTO Endpoint(endpointID, name, locationID, typeID)
VALUES(22, 'Carvalha', 22, 2);

INSERT INTO Endpoint(endpointID, name, locationID, typeID)
VALUES(23, 'Carreço', 23, 2);

INSERT INTO Endpoint(endpointID, name, locationID, typeID)
VALUES(50, 'Leixões', 50, 1);

INSERT INTO Endpoint(endpointID, name, locationID, typeID)
VALUES(45, 'São Mamede de Infesta', 45, 3);

INSERT INTO Endpoint(endpointID, name, locationID, typeID)
VALUES(48, 'Leça do Balio', 48, 4);

INSERT INTO Endpoint(endpointID, name, locationID, typeID)
VALUES(43, 'São Gemil', 43, 3);

INSERT INTO Rail_Line(id, name, ownerID, startID, endID, gauge)
VALUES(1, 'Ramal São Bento - Campanhã', 'IP', 7, 5, 1668);

INSERT INTO Rail_Line(id, name, ownerID, startID, endID, gauge)
VALUES(2, 'Ramal Campanhã - Contumil', 'IP', 5, 13, 1668);

INSERT INTO Rail_Line(id, name, ownerID, startID, endID, gauge)
VALUES(3, 'Ramal Contumil - Nine', 'IP', 13, 20, 1668);

INSERT INTO Rail_Line(id, name, ownerID, startID, endID, gauge)
VALUES(4, 'Ramal Nine - Barcelos', 'IP', 20, 8, 1668);

INSERT INTO Rail_Line(id, name, ownerID, startID, endID, gauge)
VALUES(5, 'Ramal Barcelos - Darque', 'IP', 8, 12, 1668);

INSERT INTO Rail_Line(id, name, ownerID, startID, endID, gauge)
VALUES(6, 'Ramal Darque - Viana', 'IP', 12, 17, 1668);

INSERT INTO Rail_Line(id, name, ownerID, startID, endID, gauge)
VALUES(7, 'Ramal Viana - Caminha', 'IP', 17, 21, 1668);

INSERT INTO Rail_Line(id, name, ownerID, startID, endID, gauge)
VALUES(8, 'Ramal Caminha - Torre', 'IP', 21, 16, 1668);

INSERT INTO Rail_Line(id, name, ownerID, startID, endID, gauge)
VALUES(9, 'Ramal Torre - Valença', 'IP', 16, 11, 1668);

INSERT INTO Rail_Line(id, name, ownerID, startID, endID, gauge)
VALUES(21, 'Ramal Contumil - São Gemil', 'IP', 13, 43, 1668);

INSERT INTO Rail_Line(id, name, ownerID, startID, endID, gauge)
VALUES(22, 'Ramal São Gemil - São Mamede de Infesta', 'IP', 43, 45, 1668);

INSERT INTO Rail_Line(id, name, ownerID, startID, endID, gauge)
VALUES(23, 'Ramal São Mamede de Infesta - Leça do Balio', 'IP', 45, 48, 1668);

INSERT INTO Rail_Line(id, name, ownerID, startID, endID, gauge)
VALUES(24, 'Ramal Leça do Balio - Leixões', 'IP', 48, 50, 1668);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(1, 1, 1, 8000, 2618, 1);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(3, 2, 1, 8000, 2443, 3);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(10, 3, 1, 8000, 26560, 10);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(11, 3, 2, 8000, 10000, 11);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(15, 4, 1, 8000, 5286, 15);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(16, 4, 2, 8000, 6000, 16);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(14, 5, 1, 8000, 10387, 14);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(12, 5, 2, 8000, 12000, 12);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(13, 5, 3, 8000, 3100, 13);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(20, 6, 1, 6400, 4890, 20);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(18, 7, 1, 8000, 6000, 18);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(21, 7, 2, 8000, 5000, 21);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(22, 7, 3, 8000, 12000, 22);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(25, 8, 1, 8000, 20829, 25);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(26, 9, 1, 8000, 4264, 26);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(30, 21, 1, 8000, 3883, 30);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(31, 22, 1, 8400, 1174, 31);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(32, 22, 2, 8000, 2534, 32);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(33, 23, 1, 8000, 1566, 33);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(34, 23, 2, 8000, 1453, 34);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(35, 24, 1, 8100, 3597, 35);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(36, 24, 2, 8000, 4334, 36);

INSERT INTO Siding(id, position, length, segmentID)
VALUES(21, 2000, 864, 21);

INSERT INTO Siding(id, position, length, segmentID)
VALUES(25, 11000, 266, 25);

INSERT INTO Rolling_Stock(id, name)
VALUES (1, 'WAGON');

INSERT INTO Rolling_Stock(id, name)
VALUES (2, 'LOCOMOTIVE');

INSERT INTO Path(id, sequenceOrder, "start", "end", time)
VALUES(1, '50, 48, 45, 43, 13, 20, 8, 12, 17, 21, 16, 11', 50, 11, TO_TIMESTAMP('09:45', 'HH24:MI'));

INSERT INTO Path(id, sequenceOrder, "start", "end", time)
VALUES(2, '11, 16, 21, 17, 12, 8, 20, 13, 43, 45, 48, 50', 11, 50, TO_TIMESTAMP('18:00', 'HH24:MI'));

INSERT INTO Path(id, sequenceOrder, "start", "end", time)
VALUES(3, '11, 16, 21, 17, 12, 8, 20, 13, 43, 45, 48, 50', 11, 50, TO_TIMESTAMP('10:00', 'HH24:MI'));