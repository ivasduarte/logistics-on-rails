INSERT INTO Wagon (id, modelID, entryDate, gauge, startEndpoint)
VALUES (10, 1212, TO_DATE('2005-01-01', 'YYYY-MM-DD'), 1668, 9);

INSERT INTO Freight (id, "date", origin, destination, trainID)
VALUES (901, NULL, 7, 5, 5421);

INSERT INTO Freight (id, "date", origin, destination, trainID)
VALUES (902, NULL, 5, 13, 5437);

INSERT INTO Freight (id, "date", origin, destination, trainID)
VALUES (162, NULL, 13, 20, 5421);

INSERT INTO Freight (id, "date", origin, destination, trainID)
VALUES (56, NULL, 20, 8, 5435);

INSERT INTO Freight (id, "date", origin, destination, trainID)
VALUES (100, NULL, 8, 12, 5435);

INSERT INTO Freight (id, "date", origin, destination, trainID)
VALUES (78, NULL, 21, 23, 5437);

INSERT INTO Wagon_Freight (Wagonid, Freightid)
VALUES (10, 901);

COMMIT;

