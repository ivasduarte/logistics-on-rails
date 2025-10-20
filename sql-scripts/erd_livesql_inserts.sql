insert into LOCATION(ID, LATITUDE, LONGITUDE)
values(1, 100, 100),
(2, 200, 200),
(3, 300, 300),
(4, 400, 400),
(5, 500, 500),
(6, 600, 600),
(7, 700, 700),
(8, 800, 800),
(9, 900, 900),
(10, 1000, 1000),
(11, 1100, 1100),
(12, 1200, 1200),
(13, 1300, 1300),
(14, 1400, 1400),
(15, 1500, 1500),
(16, 1600, 1600),
(17, 1700, 1700),
(18, 1800, 1800),
(19, 1900, 1900),
(20, 2000, 2000),
(21, 2100, 2100),
(22, 2200, 2200),
(23, 2300, 2300);

insert into ENDPOINT(ENDPOINTID, NAME, LOCATIONID)
values(1, 'São Romão', 1),
(2, 'Tamel', 2),
(3, 'Senhora das Dores', 3),
(4, 'Lousado', 4),
(5, 'Porto Campanhã', 5),
(6, 'Leandro', 6),
(7, 'Porto São Bento', 7),
(8, 'Barcelos', 8),
(9, 'Vila Nova da Cerveira', 9),
(10, 'Midões', 10),
(11, 'Valença', 11),
(12, 'Darque', 12),
(13, 'Contumil', 13),
(14, 'Ermesinde', 14),
(15, 'São Frutuoso', 15),
(16, 'São Pedro da Torre', 16),
(17, 'Viana do Castelo', 17),
(18, 'Famalicão', 18),
(19, 'Barroselas', 19),
(20, 'Nine', 20),
(21, 'Caminha', 21),
(22, 'Carvalha', 22),
(23, 'Carreço', 23);

insert into OPERATOR(SHORTNAME, NAME, VATNUMBER)
values('Medway', 'Medway - Operador Ferroviário de Mercadorias, S.A', 'PT509017800');

insert into OWNER(SHORTNAME, NAME, VATNUMBER)
values('IP', 'Infraestruturas de Portugal, SA', 'PT503933813');

INSERT INTO RAIL_LINE(ID, NAME, OWNERID, STARTID, ENDID, GAUGE)
values(1, 'Ramal São Bento - Campanhã', 'IP', 7, 5, 1668),
(2, 'Ramal Camapanhã - Nine', 'IP', 5, 20, 1668),
(3, 'Ramal Nine - Barcelos', 'IP', 20, 8, 1668),
(4, 'Ramal Barcelos - Viana', 'IP', 8, 17, 1668),
(5, 'Ramal viana - Caminha', 'IP', 17, 21, 1668),
(6, 'Ramal Caminha - Torre', 'IP', 21, 16, 1668),
(7, 'Ramal Torre - Valença', 'IP', 16, 11, 1668);

insert into LINE_TYPE(ID, ISELECTRIC, NUMBERTRACKS)
values(1, 'Yes', 4),
(2, 'Yes', 2);

insert into LINE_SEGMENT(ID, LINE_TYPEID, MAX_WEIGHT, LENGTH, RAIL_LINEID, ORDERPOSITION)
values(1, 1, 8000, 2618, 1, 1),
(10, 2, 8000, 29003, 2, 1),
(11, 2, 8000, 10000, 2, 2),
(15, 2, 8000, 5286, 3,1),
(16, 2, 8000, 6000, 3,2),
(14, 2, 8000, 10387, 4,1),
(12, 2, 8000, 12000, 4,2),
(13, 2, 6400, 8000, 4,3),
(20, 2, 8000, 6000, 5,1),
(21, 2, 8000, 3000, 5,2),
(22, 2, 8000, 15000, 5,3),
(25, 2, 8000, 20829, 6, 1),
(26, 2, 8000, 4264, 7,1);

INSERT INTO ROLLING_STOCK(ID, NAME)
VALUES(1, 'WAGON'),
(2, 'LOCOMOTIVE');

INSERT INTO OPERATOR_ROLLING_STOCK(OPERATOR, ROLLING_STOCKID)
VALUES('Medway', 1),
('Medway', 2);

insert into WAGON_MODEL(ID, MODEL, MAKER, NUMBERBOGIES, BOGIES, LENGTH, WIDTH, HEIGHT, WEIGHT, MAXSPEED, PAYLOAD, VOLUME)
values(1245, 'Tadgs 32 94 082 3', 'Metalsines', 2, 'Duplo', 17240, 3072, 4270, 24, 120, 56, 75),
(1278, 'Tdgs 41 94 074 1', 'Equimetal', 2, 'Duplo', 9640, 3120, 4165.5, 13.8, 100, 26.2, 38),
(1325, 'Gabs 81 94 181 1', 'Sepsa Cometna', 2, 'Duplo', 21700, 3180, 4170, 29.8, 100, 50.2, 110),
(1104, 'Regmms 32 94 356 3', 'Metalsines', 2, 'Duplo', 14040, 3104, 2535, 21.2, 120, 60.6, 76.3),
(985, 'Lgs 22 94 441 6', 'Metalsines', 1, 'Simples', 13860, 2850, 1060, 11.9, 120, 28.1, 76.3),
(987, 'Sgnss 12 94 455 2', 'Emef', 2, 'Duplo', 18116, 2950, 1030, 21.6, 120, 68.4, 76.3),
(988, 'Sgnss 12 94 455 2', 'Emef', 2, 'Duplo', 18116, 2950, 1030, 21.6, 120, 68.4, 76.3);

insert into WAGON(ID, ROLLINGSTOCKID, MODELID, SERVICEYEAR, GAUGE)
VALUES(3563077, 1, 1104, 1987, 1668),
(3563078, 1, 1104, 1987, 1688),
(3563079, 1, 1104, 1987, 1688),
(3563080, 1, 1104, 1987, 1688),
(3563081, 1, 1104, 1987, 1688),
(3563082, 1, 1104, 1987, 1688),
(3563083, 1, 1104, 1987, 1688),
(3563084, 1, 1104, 1987, 1688),
(3563085, 1, 1104, 1987, 1688),
(3563086, 1, 1104, 1987, 1688),
(3563087, 1, 1104, 1987, 1688),
(3563088, 1, 1104, 1987, 1688),
(3563089, 1, 1104, 1987, 1688),
(3563090, 1, 1104, 1987, 1688),
(3563091, 1, 1104, 1987, 1688),
(3563092, 1, 1104, 1987, 1688),
(0823045, 1, 1245, 1990, 1688),
(0823046, 1, 1245, 1990, 1688),
(0823047, 1, 1245, 1990, 1688),
(0823048, 1, 1245, 1990, 1688),
(0741001, 1, 1278, 1977, 1688),
(0741002, 1, 1278, 1977, 1688),
(0741003, 1, 1278, 1977, 1688),
(0741004, 1, 1278, 1977, 1688),
(0741005, 1, 1278, 1977, 1688),
(0741006, 1, 1278, 1977, 1688),
(1811010, 1, 1325, 1977, 1688),
(1811011, 1, 1325, 1977, 1688),
(1811012, 1, 1325, 1977, 1688),
(1811013, 1, 1325, 1977, 1688),
(1811014, 1, 1325, 1977, 1688),
(1811015, 1, 1325, 1977, 1688);

insert into WAGON_HOPPER_CAR(ID, STOREDCOAL, STOREDGRAINS, STOREDMINERALS)
values(1245, 80, 65, 13),
(1278, 97, 132, 10);

insert into WAGON_FLATCAR(ID, STOREDCONTAINERS, STOREDVEHICLES, STOREDMACHINERY)
values(1325, 26, 45, 102),
(1104, 73, 10, 12),
(985, 12, 24, 53),
(987, 92, 38, 83),
(988, 30, 58, 61);

INSERT INTO LOCOMOTIVE_MODEL(ID, MODEL, NUMBERBOGIES, BOGIES, POWER, LENGTH, WIDTH, HEIGHT, MAXSPEED, OPERATIONALSPEED, TRACTION)
VALUES(1, 'Eurosprinter', 2, 'Bo-Bo', 5600, 19.2, 3, 4.375, 220, 70, 300),
(2, 'CP 1900', 2, 'Co-Co', 1623, 19.084, 3.062, 4.31, 100, 42.5, 396);

INSERT INTO LOCOMOTIVE(ID, ROLLINGSTOCKID, NAME, MAKE, MODELID, ENTRYYEAR, GAUGE)
VALUES(5621, 2, 'Inês', 'Siemens', 1, 1995, 1668),
(5623, 2, 'Paz', 'Siemens', 1, 1995, 1668),
(5630, 2, 'Helena', 'Siemens', 1, 1996, 1668),
(1903, 2, 'Eva', 'Sorefame - Alsthom', 2, 1981, 1668);

insert into LOCOMOTIVE_DIESEL(ID, MAXFUELCAPACITY)
VALUES(2, 4882);

insert into LOCOMOTIVE_ELECTRIC(ID, VOLTAGE, FREQUENCY)
VALUES(1, 25, 50);