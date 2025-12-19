INSERT INTO Train(id, "date", pathID)
VALUES(5421, TO_date('2025-10-03', 'YYYY-MM-DD'), 1);

INSERT INTO Train(id, "date", pathID)
VALUES(5435, TO_date('2025-10-03', 'YYYY-MM-DD'), 2);

INSERT INTO Train(id, "date", pathID)
VALUES(5437, TO_date('2025-10-06', 'YYYY-MM-DD'), 3);

INSERT INTO Freight(id, "date", origin, destination, trainID)
VALUES(2001, TO_DATE('2025-10-03', 'YYYY-MM-DD'), 50, 12, 5421);

INSERT INTO Freight(id, "date", origin, destination, trainID)
VALUES(2002, TO_DATE('2025-10-03', 'YYYY-MM-DD'), 13, 11, 5421);

INSERT INTO Freight(id, "date", origin, destination, trainID)
VALUES(2003, TO_DATE('2025-10-03', 'YYYY-MM-DD'), 50, 11, 5421);

INSERT INTO Freight(id, "date", origin, destination, trainID)
VALUES(2004, TO_DATE('2025-10-03', 'YYYY-MM-DD'), 50, 21, 5421);

INSERT INTO Freight(id, "date", origin, destination, trainID)
VALUES(2005, TO_DATE('2025-10-03', 'YYYY-MM-DD'), 20, 11, 5421);

INSERT INTO Freight(id, "date", origin, destination, trainID)
VALUES(2006, TO_DATE('2025-10-03', 'YYYY-MM-DD'), 12, 50, 5435);

INSERT INTO Freight(id, "date", origin, destination, trainID)
VALUES(2050, TO_DATE('2025-10-06', 'YYYY-MM-DD'), 12, 50, 5437);

INSERT INTO Freight(id, "date", origin, destination, trainID)
VALUES(2051, TO_DATE('2025-10-06', 'YYYY-MM-DD'), 11, 50, 5437);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(3330001, 1212, TO_DATE('2005-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(3330002,1212, TO_DATE('2005-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(3330003, 1212, TO_DATE('2005-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(3330004, 1212, TO_DATE('2005-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(3330005,1212, TO_DATE('2005-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(3330006, 1212, TO_DATE('2005-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(3330007, 1212, TO_DATE('2005-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(3330008, 1212, TO_DATE('2005-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(3330009, 1212, TO_DATE('2005-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id,  modelID, entryDate, gauge, startEndpoint)
VALUES(3330010, 1212, TO_DATE('2005-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(3563077, 1104, TO_DATE('1987-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(3563078, 1104, TO_DATE('1987-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(3563079, 1104, TO_DATE('1987-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(3563080, 1104, TO_DATE('1987-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(3563081, 1104, TO_DATE('1987-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id,  modelID, entryDate, gauge, startEndpoint)
VALUES(3563082, 1104, TO_DATE('1987-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(3563083, 1104, TO_DATE('1987-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(3563084, 1104, TO_DATE('1987-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(3563085, 1104, TO_DATE('1987-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id,  modelID, entryDate, gauge, startEndpoint)
VALUES(3563086, 1104, TO_DATE('1987-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(3563087, 1104, TO_DATE('1987-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(3563088, 1104, TO_DATE('1987-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(3563089, 1104, TO_DATE('1987-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(3563090, 1104, TO_DATE('1987-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(3563091, 1104, TO_DATE('1987-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(3563092, 1104, TO_DATE('1987-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(0823045, 1245, TO_DATE('1990-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(0823046, 1245, TO_DATE('1990-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id,  modelID, entryDate, gauge, startEndpoint)
VALUES(0823047, 1245, TO_DATE('1990-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(0823048, 1245, TO_DATE('1990-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(0741001, 1278, TO_DATE('1977-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(0741002, 1278, TO_DATE('1977-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(0741003, 1278, TO_DATE('1977-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(0741004, 1278, TO_DATE('1977-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id,  modelID, entryDate, gauge, startEndpoint)
VALUES(0741005,  1278, TO_DATE('1977-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(0741006, 1278, TO_DATE('1977-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id,  modelID, entryDate, gauge, startEndpoint)
VALUES(1811010, 1325, TO_DATE('1977-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(1811011, 1325, TO_DATE('1977-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id,  modelID, entryDate, gauge, startEndpoint)
VALUES(1811012,  1325, TO_DATE('1977-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(1811013, 1325, TO_DATE('1977-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Wagon(id, modelID, entryDate, gauge, startEndpoint)
VALUES(1811014, 1325, TO_DATE('1977-01-01', 'YYYY-MM-DD'), 1668, 50);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 3330001);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 3330002);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 3330003);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 3330004);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 3330005);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 3330006);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 3330007);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 3330008);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 3330009);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 3330010);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 3563077);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 3563078);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 3563079);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 3563080);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 3563081);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 3563082);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 3563083);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 3563084);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 3563085);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 3563086);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 3563087);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 3563088);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 3563089);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 3563090);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 3563091);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 3563092);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 0823045);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 0823046);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 0823047);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 0823048);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 0741001);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 0741002);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 0741003);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 0741004);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 0741005);

INSERT INTO Operator_Wagon(OperatorshortName, Wagonid)
VALUES('Medway', 0741006);

INSERT INTO Locomotive(id, name, make, modelID, entryDate, startEndpoint)
VALUES(5621, 'Inês', 'Siemens', 1, TO_DATE('1995-01-01','YYYY-MM-DD'), 50);

INSERT INTO Locomotive(id, name, make, modelID, entryDate, startEndpoint)
VALUES(5623, 'Paz', 'Siemens', 1, TO_DATE('1995-01-01','YYYY-MM-DD'), 50);

INSERT INTO Locomotive(id, name, make, modelID, entryDate, startEndpoint)
VALUES(5630, 'Helena', 'Siemens', 1, TO_DATE('1996-01-01','YYYY-MM-DD'), 50);

INSERT INTO Locomotive(id, name, make, modelID, entryDate, startEndpoint)
VALUES(1903, 'Eva', 'Sorefame - Alsthom', 2, TO_DATE('1981-01-01','YYYY-MM-DD'), 50);

INSERT INTO Locomotive(id, name, make, modelID, entryDate, startEndpoint)
VALUES(5034, 'Adriana', 'Stadler', 3, TO_DATE('2017-01-01','YYYY-MM-DD'), 50);

INSERT INTO Locomotive(id, name, make, modelID, entryDate, startEndpoint)
VALUES(5036, 'Marina', 'Stadler', 3, TO_DATE('2017-01-01','YYYY-MM-DD'), 50);

INSERT INTO Gauge_Locomotive(Gauge, Locomotiveid)
VALUES(1668, 5621);

INSERT INTO Gauge_Locomotive(Gauge, Locomotiveid)
VALUES(1668, 5623);

INSERT INTO Gauge_Locomotive(Gauge, Locomotiveid)
VALUES(1668, 5630);

INSERT INTO Gauge_Locomotive(Gauge, Locomotiveid)
VALUES(1668, 1903);

INSERT INTO Gauge_Locomotive(Gauge, Locomotiveid)
VALUES(1668, 5034);

INSERT INTO Gauge_Locomotive(Gauge, Locomotiveid)
VALUES(1668, 5036);

INSERT INTO Wagon_Freight(Wagonid, Freightid)
VALUES(3330001, 2001);

INSERT INTO Wagon_Freight(Wagonid, Freightid)
VALUES(3330002, 2001);

INSERT INTO Wagon_Freight(Wagonid, Freightid)
VALUES(3330004, 2001);

INSERT INTO Wagon_Freight(Wagonid, Freightid)
VALUES(3330005, 2001);

INSERT INTO Wagon_Freight(Wagonid, Freightid)
VALUES(3330006, 2001);

INSERT INTO Wagon_Freight(Wagonid, Freightid)
VALUES(3563089, 2002);

INSERT INTO Wagon_Freight(Wagonid, Freightid)
VALUES(1811011, 2003);

INSERT INTO Wagon_Freight(Wagonid, Freightid)
VALUES(1811012, 2003);

INSERT INTO Wagon_Freight(Wagonid, Freightid)
VALUES(1811013, 2004);

INSERT INTO Wagon_Freight(Wagonid, Freightid)
VALUES(3563077, 2005);

INSERT INTO Wagon_Freight(Wagonid, Freightid)
VALUES(3563078, 2005);

INSERT INTO Wagon_Freight(Wagonid, Freightid)
VALUES(3563079, 2005);

INSERT INTO Wagon_Freight(Wagonid, Freightid)
VALUES(3563080, 2005);

INSERT INTO Wagon_Freight(Wagonid, Freightid)
VALUES(3330003, 2006);

INSERT INTO Wagon_Freight(Wagonid, Freightid)
VALUES(3330007, 2006);

INSERT INTO Wagon_Freight(Wagonid, Freightid)
VALUES(3330001, 2050);

INSERT INTO Wagon_Freight(Wagonid, Freightid)
VALUES(3330002, 2050);

INSERT INTO Wagon_Freight(Wagonid, Freightid)
VALUES(3330004, 2050);

INSERT INTO Wagon_Freight(Wagonid, Freightid)
VALUES(3330005, 2050);

INSERT INTO Wagon_Freight(Wagonid, Freightid)
VALUES(3330006, 2050);

INSERT INTO Wagon_Freight(Wagonid, Freightid)
VALUES(1811011, 2051);

INSERT INTO Wagon_Freight(Wagonid, Freightid)
VALUES(1811012, 2051);

INSERT INTO Wagon_Freight(Wagonid, Freightid)
VALUES(3563077, 2051);

INSERT INTO Wagon_Freight(Wagonid, Freightid)
VALUES(3563078, 2051);

INSERT INTO Wagon_Freight(Wagonid, Freightid)
VALUES(3563079, 2051);

INSERT INTO Wagon_Freight(Wagonid, Freightid)
VALUES(3563080, 2051);

INSERT INTO Locomotive_Train(Locomotiveid, Trainid)
VALUES(5621, 5421);

INSERT INTO Locomotive_Train(Locomotiveid, Trainid)
VALUES(5623, 5421);

INSERT INTO Locomotive_Train(Locomotiveid, Trainid)
VALUES(5623, 5435);

INSERT INTO Locomotive_Train(Locomotiveid, Trainid)
VALUES(5621, 5437);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(1, 1);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(2, 1);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(3, 1);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(4, 1);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(5, 1);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(6, 1);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(7, 1);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(8, 1);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(9, 1);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(10, 2);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(11, 2);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(12, 2);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(13, 2);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(14, 2);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(15, 2);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(16, 2);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(17, 2);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(18, 2);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(19, 3);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(20, 3);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(21, 3);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(22, 3);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(23, 3);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(50, 3);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(45, 3);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(48, 3);

INSERT INTO Endpoint_Building(EndpointendpointID, BuildingbuildingID)
VALUES(43, 3);
