INSERT INTO Owner(shortName, name, VATNumber)
VALUES ('IP', 'Infraestruturas de Portugal, SA', 'PT503933813');

INSERT INTO Operator(shortName, name, VATNumber)
VALUES ('Medway', 'Medway - Operador Ferroviário de Mercadorias, S.A', 'PT509017800');

INSERT INTO Operator(shortName, name, VATNumber)
VALUES ('Captrain', 'Captrain Portugal S.A.', 'PT507832388');

INSERT INTO Location(id, latitude, longitude) VALUES (1, '100', '100');
INSERT INTO Location(id, latitude, longitude) VALUES (2, '200', '200');
INSERT INTO Location(id, latitude, longitude) VALUES (3, '300', '300');
INSERT INTO Location(id, latitude, longitude) VALUES (4, '400', '400');
INSERT INTO Location(id, latitude, longitude) VALUES (5, '500', '500');
INSERT INTO Location(id, latitude, longitude) VALUES (6, '600', '600');
INSERT INTO Location(id, latitude, longitude) VALUES (7, '700', '700');
INSERT INTO Location(id, latitude, longitude) VALUES (8, '800', '800');
INSERT INTO Location(id, latitude, longitude) VALUES (9, '900', '900');
INSERT INTO Location(id, latitude, longitude) VALUES (10, '1000', '1000');
INSERT INTO Location(id, latitude, longitude) VALUES (11, '1100', '1100');
INSERT INTO Location(id, latitude, longitude) VALUES (12, '1200', '1200');
INSERT INTO Location(id, latitude, longitude) VALUES (13, '1300', '1300');
INSERT INTO Location(id, latitude, longitude) VALUES (14, '1400', '1400');
INSERT INTO Location(id, latitude, longitude) VALUES (15, '1500', '1500');
INSERT INTO Location(id, latitude, longitude) VALUES (16, '1600', '1600');
INSERT INTO Location(id, latitude, longitude) VALUES (17, '1700', '1700');
INSERT INTO Location(id, latitude, longitude) VALUES (18, '1800', '1800');
INSERT INTO Location(id, latitude, longitude) VALUES (19, '1900', '1900');
INSERT INTO Location(id, latitude, longitude) VALUES (20, '2000', '2000');
INSERT INTO Location(id, latitude, longitude) VALUES (21, '2100', '2100');
INSERT INTO Location(id, latitude, longitude) VALUES (22, '2200', '2200');
INSERT INTO Location(id, latitude, longitude) VALUES (23, '2300', '2300');
INSERT INTO Location(id, latitude, longitude) VALUES (50, '5000', '5000');
INSERT INTO Location(id, latitude, longitude) VALUES (45, '4500', '4500');
INSERT INTO Location(id, latitude, longitude) VALUES (48, '4800', '4800');
INSERT INTO Location(id, latitude, longitude) VALUES (43, '4300', '4300');


INSERT INTO Gauge(gaugeNumber) VALUES(1668);
INSERT INTO Gauge(gaugeNumber) VALUES(1435);

INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(1, 'Yes', 4);
INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(3, 'Yes', 4);
INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(10, 'Yes', 2);
INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(11, 'Yes', 2);
INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(15, 'Yes', 2);
INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(16, 'Yes', 2);
INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(14, 'Yes', 2);
INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(12, 'Yes', 2);
INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(13, 'Yes', 2);
INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(20, 'Yes', 2);
INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(18, 'Yes', 1);
INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(21, 'Yes', 1);
INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(22, 'Yes', 1);
INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(25, 'Yes', 1);
INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(26, 'Yes', 1);
INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(30, 'Yes', 2);
INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(31, 'Yes', 2);
INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(32, 'Yes', 2);
INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(33, 'Yes', 2);
INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(34, 'Yes', 2);
INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(35, 'Yes', 2);
INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(36, 'Yes', 2);

INSERT INTO Locomotive_Diesel(id, maxFuelCapacity) VALUES(1, 6700);
INSERT INTO Locomotive_Diesel(id, maxFuelCapacity) VALUES(2, 4882);

INSERT INTO Locomotive_Electric(id, voltage, frequency) VALUES(1, 25, 50);

INSERT INTO Locomotive_Model(id, model, numberBogies, bogies, power, length, width, height, maxSpeed, operationalSpeed, traction, weight, dieselID, electricID)
VALUES(1, 'Eurosprinter', 2, 'Bo-Bo', 5600, 19.2, 3, 4.375, 220, 70, 300, 87, 1, 1);

INSERT INTO Locomotive_Model(id, model, numberBogies, bogies, power, length, width, height, maxSpeed, operationalSpeed, traction, weight, dieselID, electricID)
VALUES(2, 'CP 1900', 2, 'Co-Co', 1623, 19.084, 3.062, 4.31, 100, 42.5, 396, 117, 1, 1);

INSERT INTO Locomotive_Model(id, model, numberBogies, bogies, power, length, width, height, maxSpeed, operationalSpeed, traction, weight, dieselID, electricID)
VALUES(3, 'E4000', 3, 'Co-Co', 3178, 23.02, 3000, 4.264, 120, 100, 400, 124, 2, 1);

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

INSERT INTO Wagon_Tank_Car(id, storedLiquids, storedChemicals, storedFuel)
VALUES(1525, 39, 20, 39);

INSERT INTO Wagon_Tank_Car(id, storedLiquids, storedChemicals, storedFuel)
VALUES(1523, 14, 25, 42);

INSERT INTO Wagon_Boxcar(id, storedGeneralGoods)
VALUES(1212, 58);

INSERT INTO Wagon_Refrigerated_Car (id, storedPerishableGoods)
VALUES (1, 0);

INSERT INTO Wagon_Model (id, model, maker, numberBogies, bogies, length, width, height, weight, maxSpeed, payload, volume, flatcarID, tankcarID, hoppercarID, refrigeratedcarID, boxcarID)
VALUES (1245, 'Tadgs 32 94 082 3', 'Metalsines', 2, 'Duplo', 17240, 3072, 4270, 24, 120, 56, 75, 985, 1525, 1245, 1, 1212);

INSERT INTO Wagon_Model (id, model, maker, numberBogies, bogies, length, width, height, weight, maxSpeed, payload, volume, flatcarID, tankcarID, hoppercarID, refrigeratedcarID, boxcarID)
VALUES (1278, 'Tdgs 41 94 074 1', 'Equimetal', 2, 'Duplo', 9640, 3120, 4165.5, 13.8, 100, 26.2, 38, 985, 1525, 1278, 1, 1212);

INSERT INTO Wagon_Model (id, model, maker, numberBogies, bogies, length, width, height, weight, maxSpeed, payload, volume, flatcarID, tankcarID, hoppercarID, refrigeratedcarID, boxcarID)
VALUES (1325, 'Gabs 81 94 181 1', 'Sepsa Cometna', 2, 'Duplo', 21700, 3180, 4170, 29.8, 100, 50.2, 110, 1325, 1525, 1245, 1, 1212);

INSERT INTO Wagon_Model (id, model, maker, numberBogies, bogies, length, width, height, weight, maxSpeed, payload, volume, flatcarID, tankcarID, hoppercarID, refrigeratedcarID, boxcarID)
VALUES (1104, 'Regmms 32 94 356 3', 'Metalsines', 2, 'Duplo', 14040, 3104, 2535, 21.2, 120, 60.6, 76.3, 1104, 1525, 1245, 1, 1212);

INSERT INTO Wagon_Model (id, model, maker, numberBogies, bogies, length, width, height, weight, maxSpeed, payload, volume, flatcarID, tankcarID, hoppercarID, refrigeratedcarID, boxcarID)
VALUES (1212, 'Kbs 41 94 333', 'Simmering', 2, 'Simples', 14020, 2842, 3300, 14.2, 100, 25.8, 62.7, 985, 1525, 1245, 1, 1212);

INSERT INTO Endpoint_Type(id, "desc") VALUES(1, 'Freight Yard');
INSERT INTO Endpoint_Type(id, "desc") VALUES(2, 'Station');
INSERT INTO Endpoint_Type(id, "desc") VALUES(3, 'Not Intermodal Terminal');
INSERT INTO Endpoint_Type(id, "desc") VALUES(4, 'Intermodal Terminal');

INSERT INTO Building_Type(id, "desc") VALUES(1, 'Warehouse');
INSERT INTO Building_Type(id, "desc") VALUES(2, 'Refrigerated Area');
INSERT INTO Building_Type(id, "desc") VALUES(3, 'Grain Silo');

INSERT INTO Building(buildingID, typeID) VALUES(1, 1);
INSERT INTO Building(buildingID, typeID) VALUES(2, 2);
INSERT INTO Building(buildingID, typeID) VALUES(3, 3);

INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(1, 'São Romão', 1, 2);
INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(2, 'Tamel', 2, 2);
INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(3, 'Senhora das Dores', 3, 2);
INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(4, 'Lousado', 4, 2);
INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(5, 'Porto Campanhã', 5, 1);
INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(6, 'Leandro', 6, 2);
INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(7, 'Porto São Bento', 7, 3);
INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(8, 'Barcelos', 8, 3);
INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(9, 'Vila Nova da Cerveira', 9, 2);
INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(10, 'Midões', 10, 2);
INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(11, 'Valença', 11, 1);
INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(12, 'Darque', 12, 1);
INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(13, 'Contumil', 13, 1);
INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(14, 'Ermesinde', 14, 2);
INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(15, 'São Frutuoso', 15, 2);
INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(16, 'São Pedro da Torre', 16, 3);
INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(17, 'Viana do Castelo', 17, 3);
INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(18, 'Famalicão', 18, 2);
INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(19, 'Barroselas', 19, 2);
INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(20, 'Nine', 20, 1);
INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(21, 'Caminha', 21, 1);
INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(22, 'Carvalha', 22, 2);
INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(23, 'Carreção', 23, 2);
INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(50, 'Leixões', 50, 1);
INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(45, 'São Mamede de Infesta', 45, 3);
INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(48, 'Leça do Balio', 48, 4);
INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(43, 'São Gemil', 43, 3);

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

INSERT INTO Path(id)
VALUES(1);

INSERT INTO Path(id)
VALUES(2);

INSERT INTO Path(id)
VALUES(3);

INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(24, 1, 1, TIMESTAMP '2025-10-03 08:00:00', TIMESTAMP '2025-10-03 08:15:00');
INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(23, 1, 2, TIMESTAMP '2025-10-03 08:30:00', TIMESTAMP '2025-10-03 08:45:00');
INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(22, 1, 3, TIMESTAMP '2025-10-03 09:00:00', TIMESTAMP '2025-10-03 09:15:00');
INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(21, 1, 4, TIMESTAMP '2025-10-03 09:30:00', TIMESTAMP '2025-10-03 09:45:00');
INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(3, 1, 5, TIMESTAMP '2025-10-03 10:00:00', TIMESTAMP '2025-10-03 10:15:00');
INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(4, 1, 6, TIMESTAMP '2025-10-03 10:30:00', TIMESTAMP '2025-10-03 10:45:00');
INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(5, 1, 7, TIMESTAMP '2025-10-03 11:00:00', TIMESTAMP '2025-10-03 11:15:00');
INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(6, 1, 8, TIMESTAMP '2025-10-03 11:30:00', TIMESTAMP '2025-10-03 11:45:00');
INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(7, 1, 9, TIMESTAMP '2025-10-03 12:00:00', TIMESTAMP '2025-10-03 12:15:00');
INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(8, 1, 10, TIMESTAMP '2025-10-03 12:30:00', TIMESTAMP '2025-10-03 12:45:00');
INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(9, 1, 11, TIMESTAMP '2025-10-03 13:00:00', TIMESTAMP '2025-10-03 13:15:00');

INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(9, 2, 1, TIMESTAMP '2025-10-03 14:00:00', TIMESTAMP '2025-10-03 14:15:00');
INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(8, 2, 2, TIMESTAMP '2025-10-03 14:30:00', TIMESTAMP '2025-10-03 14:45:00');
INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(7, 2, 3, TIMESTAMP '2025-10-03 15:00:00', TIMESTAMP '2025-10-03 15:15:00');
INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(6, 2, 4, TIMESTAMP '2025-10-03 15:30:00', TIMESTAMP '2025-10-03 15:45:00');
INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(5, 2, 5, TIMESTAMP '2025-10-03 16:00:00', TIMESTAMP '2025-10-03 16:15:00');
INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(4, 2, 6, TIMESTAMP '2025-10-03 16:30:00', TIMESTAMP '2025-10-03 16:45:00');
INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(3, 2, 7, TIMESTAMP '2025-10-03 17:00:00', TIMESTAMP '2025-10-03 17:15:00');
INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(21, 2, 8, TIMESTAMP '2025-10-03 17:30:00', TIMESTAMP '2025-10-03 17:45:00');
INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(22, 2, 9, TIMESTAMP '2025-10-03 18:00:00', TIMESTAMP '2025-10-03 18:15:00');
INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(23, 2, 10, TIMESTAMP '2025-10-03 18:30:00', TIMESTAMP '2025-10-03 18:45:00');
INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(24, 2, 11, TIMESTAMP '2025-10-03 19:00:00', TIMESTAMP '2025-10-03 19:15:00');

INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(9, 3, 1, TIMESTAMP '2025-10-06 08:00:00', TIMESTAMP '2025-10-06 08:15:00');
INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(8, 3, 2, TIMESTAMP '2025-10-06 08:30:00', TIMESTAMP '2025-10-06 08:45:00');
INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(7, 3, 3, TIMESTAMP '2025-10-06 09:00:00', TIMESTAMP '2025-10-06 09:15:00');
INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(6, 3, 4, TIMESTAMP '2025-10-06 09:30:00', TIMESTAMP '2025-10-06 09:45:00');
INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(5, 3, 5, TIMESTAMP '2025-10-06 10:00:00', TIMESTAMP '2025-10-06 10:15:00');
INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(4, 3, 6, TIMESTAMP '2025-10-06 10:30:00', TIMESTAMP '2025-10-06 10:45:00');
INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(3, 3, 7, TIMESTAMP '2025-10-06 11:00:00', TIMESTAMP '2025-10-06 11:15:00');
INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(21, 3, 8, TIMESTAMP '2025-10-06 11:30:00', TIMESTAMP '2025-10-06 11:45:00');
INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(22, 3, 9, TIMESTAMP '2025-10-06 12:00:00', TIMESTAMP '2025-10-06 12:15:00');
INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(23, 3, 10, TIMESTAMP '2025-10-06 12:30:00', TIMESTAMP '2025-10-06 12:45:00');
INSERT INTO Rail_Line_Path(Rail_Lineid, Pathid, "order", arrivalTime, departureTime) VALUES(24, 3, 11, TIMESTAMP '2025-10-06 13:00:00', TIMESTAMP '2025-10-06 13:15:00');
