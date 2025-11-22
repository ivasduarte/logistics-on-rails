INSERT INTO Rail_Line(id, name, ownerID, startID, endID, gauge)
VALUES(40, 'Ramal Contumil - Ermesinde', 'IP', 13, 14, 1668);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(50, 40, 1, 8000, 13000, 10);

INSERT INTO Rail_Line(id, name, ownerID, startID, endID, gauge)
VALUES(41, 'Ramal Ermesinde - Nine', 'IP', 14, 20, 1668);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(51, 41, 1, 8000, 13560, 11);
-- ===================================================================================
INSERT INTO Rail_Line(id, name, ownerID, startID, endID, gauge)
VALUES(42, 'Ramal Nine - Carreço', 'IP', 20, 23, 1668);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(52, 42, 1, 8000, 8000, 15);

INSERT INTO Rail_Line(id, name, ownerID, startID, endID, gauge)
VALUES(43, 'Ramal Carreço - Contumil', 'IP', 23, 13, 1668);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(53, 43, 1, 8000, 10000, 16);
-- ===================================================================================
INSERT INTO Rail_Line(id, name, ownerID, startID, endID, gauge)
VALUES(44, 'Ramal São Gemil - Carvalha', 'IP', 43, 22, 1668);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(54, 44, 1, 8000, 1800, 30);

INSERT INTO Rail_Line(id, name, ownerID, startID, endID, gauge)
VALUES(45, 'Ramal Carvalha - São Mamede', 'IP', 22, 45, 1668);

INSERT INTO Line_Segment(id, Rail_LineId, orderPosition, max_weight, length, Line_TypeID)
VALUES(55, 45, 1, 8000, 1900, 31);
