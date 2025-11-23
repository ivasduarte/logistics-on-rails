INSERT INTO Locomotive_Train (Locomotiveid, Trainid) VALUES (5621, 5421);
INSERT INTO Locomotive_Train (Locomotiveid, Trainid) VALUES (5623, 5435);

INSERT INTO Locomotive_Train (Locomotiveid, Trainid) VALUES (5034, 5437);
INSERT INTO Locomotive_Train (Locomotiveid, Trainid) VALUES (5036, 5437);

===========================================================================

INSERT INTO Train(id, operator, "date", pathID)
VALUES (6001, 'Captrain', TO_DATE('2025-10-05','YYYY-MM-DD'), 1);

INSERT INTO Train(id, operator, "date", pathID)
VALUES (6002, 'Captrain', TO_DATE('2025-10-06','YYYY-MM-DD'), 2);

INSERT INTO Locomotive_Train (Locomotiveid, Trainid) VALUES (5034, 6001);
INSERT INTO Locomotive_Train (Locomotiveid, Trainid) VALUES (5036, 6001);
INSERT INTO Locomotive_Train (Locomotiveid, Trainid) VALUES (5621, 6002);