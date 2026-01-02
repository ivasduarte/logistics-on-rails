INSERT INTO Location(id, latitude, longitude) VALUES (30, '3000', '3000');
INSERT INTO Location(id, latitude, longitude) VALUES (31, '3100', '3100');
INSERT INTO Location(id, latitude, longitude) VALUES (32, '3200', '3200');
INSERT INTO Location(id, latitude, longitude) VALUES (33, '3300', '3300');
INSERT INTO Location(id, latitude, longitude) VALUES (35, '3500', '3500');

INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(30, 'Braga', 30, 2);
INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(31, 'Manzagão', 31, 2);
INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(32, 'Cerqueiral', 32, 2);
INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(33, 'Gemieira', 33, 2);
INSERT INTO Endpoint(endpointID, name, locationID, typeID) VALUES(35, 'Paredes de Coura', 35, 2);

INSERT INTO Rail_Line(id, name, ownerID, startID, endID, gauge)
VALUES(30, 'Ramal Braga', 'IP', 31, 30, 1668);

INSERT INTO Rail_Line(id, name, ownerID, startID, endID, gauge)
VALUES(31, 'Ramal Nine - Manzagão', 'IP', 20, 31, 1668);

INSERT INTO Rail_Line(id, name, ownerID, startID, endID, gauge)
VALUES(32, 'Ramal Manzagão - Cerqueiral', 'IP', 31, 32, 1668);

INSERT INTO Rail_Line(id, name, ownerID, startID, endID, gauge)
VALUES(35, 'Ramal Cerqueiral - Gemieira', 'IP', 32, 33, 1668);

INSERT INTO Rail_Line(id, name, ownerID, startID, endID, gauge)
VALUES(36, 'Ramal Gemieira - Paredes de Coura', 'IP', 33, 35, 1668);

INSERT INTO Rail_Line(id, name, ownerID, startID, endID, gauge)
VALUES(37, 'Ramal Paredes de Coura - Valença', 'IP', 35, 11, 1668);

INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(50, 'Yes', 2);
INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(51, 'Yes', 2);
INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(52, 'Yes', 2);
INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(53, 'Yes', 2);
INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(54, 'Yes', 2);
INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(55, 'Yes', 2);
INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(58, 'Yes', 2);
INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(59, 'Yes', 2);
INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(60, 'Yes', 2);
INSERT INTO Line_Type(id, isElectric, numberTracks) VALUES(61, 'Yes', 2);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(50, 30, 1, 8000, 3555, 50);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(51, 31, 5, 8000, 1222, 51);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(52, 31, 4, 8000, 1760, 52);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(53, 31, 3, 8000, 1720, 53);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(54, 31, 2, 8000, 3350, 54);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(55, 31, 1, 8000, 3470, 55);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(58, 32, 1, 8000, 8050, 58);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(59, 35, 1, 8000, 22320, 59);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(60, 36, 1, 8000, 16310, 60);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(61, 37, 1, 8000, 15200, 61);

INSERT INTO Wagon_Model (id, model, maker, numberBogies, bogies, length, width, height, weight, maxSpeed, payload, volume, flatcarID, tankcarID, hoppercarID, refrigeratedcarID, boxcarID)
VALUES (985, 'Lgs 22 94 441 6', 'Metalsines', 2, 'Simples', 13860, 2850, 1060, 11.9, 120, 28.1, 76.3, 985, 1525, 1245, 1, 1212);

INSERT INTO Wagon_Model (id, model, maker, numberBogies, bogies, length, width, height, weight, maxSpeed, payload, volume, flatcarID, tankcarID, hoppercarID, refrigeratedcarID, boxcarID)
VALUES (987, 'Sgnss 12 94 455 2', 'Emef', 2, 'Duplo', 18116, 2950, 1030, 21.6, 120, 68.4, 76.3, 987, 1525, 1245, 1, 1212);

INSERT INTO Wagon_Model (id, model, maker, numberBogies, bogies, length, width, height, weight, maxSpeed, payload, volume, flatcarID, tankcarID, hoppercarID, refrigeratedcarID, boxcarID)
VALUES (988, 'Sgnss 12 94 455 2', 'Emef', 2, 'Duplo', 18116, 2950, 1030, 21.6, 120, 68.4, 76.3, 988, 1525, 1245, 1, 1212);

INSERT INTO Wagon_Model (id, model, maker, numberBogies, bogies, length, width, height, weight, maxSpeed, payload, volume, flatcarID, tankcarID, hoppercarID, refrigeratedcarID, boxcarID)
VALUES (1525, 'Zaes 81 94 788', 'Equimetal', 2, 'Duplo', 13800, 2950, 4226, 22.9, 120, 57.1, 64.6, 985, 1525, 1245, 1, 1212);

INSERT INTO Wagon_Model (id, model, maker, numberBogies, bogies, length, width, height, weight, maxSpeed, payload, volume, flatcarID, tankcarID, hoppercarID, refrigeratedcarID, boxcarID)
VALUES (1523, 'Zaes 81 94 788', 'Equimetal', 2, 'Duplo', 13800, 2950, 4226, 22.9, 120, 57.1, 64.6, 985, 1523, 1245, 1, 1212);

INSERT INTO Freight(id, "date", origin, destination, trainID)
VALUES(2007, TO_DATE('2025-10-03', 'YYYY-MM-DD'), 50, 5, 5421);

INSERT INTO Wagon_Freight(Wagonid, Freightid)
VALUES(3563090, 2007);

INSERT INTO Wagon_Freight(Wagonid, Freightid)
VALUES(3563091, 2007);

INSERT INTO Wagon_Freight(Wagonid, Freightid)
VALUES(3563092, 2007);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(30, 1);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(31, 2);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(32, 2);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(33, 2);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(35, 2);

INSERT INTO Operator_Train (OperatorshortName, Trainid) VALUES ('Medway', 5421);
INSERT INTO Operator_Train (OperatorshortName, Trainid) VALUES ('Medway', 5435);
INSERT INTO Operator_Train (OperatorshortName, Trainid) VALUES ('Medway', 5437);