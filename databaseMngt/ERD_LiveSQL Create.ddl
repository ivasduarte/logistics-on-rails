-- DROP TABLE Building CASCADE CONSTRAINTS;
-- DROP TABLE Building_Type CASCADE CONSTRAINTS;
-- DROP TABLE Endpoint CASCADE CONSTRAINTS;
-- DROP TABLE Endpoint_Building CASCADE CONSTRAINTS;
-- DROP TABLE Endpoint_Type CASCADE CONSTRAINTS;
-- DROP TABLE Freight CASCADE CONSTRAINTS;
-- DROP TABLE Gauge CASCADE CONSTRAINTS;
-- DROP TABLE Gauge_Locomotive CASCADE CONSTRAINTS;
-- DROP TABLE Line_Segment CASCADE CONSTRAINTS;
-- DROP TABLE Line_Type CASCADE CONSTRAINTS;
-- DROP TABLE Location CASCADE CONSTRAINTS;
-- DROP TABLE Locomotive CASCADE CONSTRAINTS;
-- DROP TABLE Locomotive_Diesel CASCADE CONSTRAINTS;
-- DROP TABLE Locomotive_Electric CASCADE CONSTRAINTS;
-- DROP TABLE Locomotive_Model CASCADE CONSTRAINTS;
-- DROP TABLE Locomotive_Train CASCADE CONSTRAINTS;
-- DROP TABLE Operator CASCADE CONSTRAINTS;
-- DROP TABLE Operator_Train CASCADE CONSTRAINTS;
-- DROP TABLE Operator_Wagon CASCADE CONSTRAINTS;
-- DROP TABLE Owner CASCADE CONSTRAINTS;
-- DROP TABLE Path CASCADE CONSTRAINTS;
-- DROP TABLE Rail_Line CASCADE CONSTRAINTS;
-- DROP TABLE Rail_Line_Path CASCADE CONSTRAINTS;
-- DROP TABLE Siding CASCADE CONSTRAINTS;
-- DROP TABLE Train CASCADE CONSTRAINTS;
-- DROP TABLE Wagon CASCADE CONSTRAINTS;
-- DROP TABLE Wagon_Boxcar CASCADE CONSTRAINTS;
-- DROP TABLE Wagon_Flatcar CASCADE CONSTRAINTS;
-- DROP TABLE Wagon_Freight CASCADE CONSTRAINTS;
-- DROP TABLE Wagon_Hopper_Car CASCADE CONSTRAINTS;
-- DROP TABLE Wagon_Model CASCADE CONSTRAINTS;
-- DROP TABLE Wagon_Refrigerated_Car CASCADE CONSTRAINTS;
-- DROP TABLE Wagon_Tank_Car CASCADE CONSTRAINTS;

CREATE TABLE Building (
  buildingID number(10),
  typeID number(10) NOT NULL,
  PRIMARY KEY (buildingID)
);

CREATE TABLE Building_Type (
  id number(10),
  "desc" varchar2(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Endpoint (
  endpointID number(10),
  name varchar2(255) NOT NULL,
  locationID number(10) NOT NULL,
  typeID number(10) NOT NULL,
  PRIMARY KEY (endpointID)
);

CREATE TABLE Endpoint_Building (
  EndpointendpointID number(10) NOT NULL,
  BuildingbuildingID number(10) NOT NULL,
  PRIMARY KEY (EndpointendpointID, BuildingbuildingID)
);

CREATE TABLE Endpoint_Type (
  id number(10),
  "desc" varchar2(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Freight (
  id number(10),
  "date" date NOT NULL,
  origin number(10) NOT NULL,
  destination number(10) NOT NULL,
  trainID number(10),
  PRIMARY KEY (id)
);

CREATE TABLE Gauge (
  gaugeNumber number(10),
  PRIMARY KEY (gaugeNumber)
);

CREATE TABLE Gauge_Locomotive (
  Gauge number(10) NOT NULL,
  Locomotiveid number(10) NOT NULL,
  PRIMARY KEY (Gauge, Locomotiveid)
);

CREATE TABLE Line_Segment (
  id number(10),
  Rail_LineId number(10) NOT NULL,
  orderPosition number(10) NOT NULL,
  max_weight float(10) NOT NULL,
  length number(10) NOT NULL,
  Line_TypeID number(10) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT RailLinePositions UNIQUE (Rail_LineId, orderPosition)
);

CREATE TABLE Line_Type (
  id number(10),
  isElectric varchar2(255) NOT NULL,
  numberTracks number(10) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Location (
  id number(10),
  latitude varchar2(255) NOT NULL,
  longitude varchar2(255) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT coordinates UNIQUE (latitude, longitude)
);

CREATE TABLE Locomotive (
  id number(10),
  name varchar2(255) NOT NULL UNIQUE,
  make varchar2(255) NOT NULL,
  modelID number(10) NOT NULL,
  entryDate date NOT NULL,
  startEndpoint number(10) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Locomotive_Diesel (
  id number(10),
  maxFuelCapacity number(10) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Locomotive_Electric (
  id number(10),
  voltage number(10) NOT NULL,
  frequency number(10) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Locomotive_Model (
  id number(10),
  model varchar2(255) NOT NULL,
  numberBogies number(10) NOT NULL,
  bogies varchar2(255) NOT NULL,
  power number(10) NOT NULL,
  length number(10) NOT NULL,
  width number(10) NOT NULL,
  height number(10) NOT NULL,
  maxSpeed number(10) NOT NULL,
  operationalSpeed number(10) NOT NULL,
  traction number(10) NOT NULL,
  weight number(10) NOT NULL,
  dieselID number(10) NOT NULL,
  electricID number(10) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Locomotive_Train (
  Locomotiveid number(10) NOT NULL,
  Trainid number(10) NOT NULL,
  PRIMARY KEY (Locomotiveid, Trainid)
);

CREATE TABLE Operator (
  shortName varchar2(255) NOT NULL,
  name varchar2(255) NOT NULL,
  VATNumber varchar2(255) NOT NULL UNIQUE,
  PRIMARY KEY (shortName)
);

CREATE TABLE Operator_Train (
  OperatorshortName varchar2(255) NOT NULL,
  Trainid number(10) NOT NULL,
  PRIMARY KEY (OperatorshortName, Trainid)
);

CREATE TABLE Operator_Wagon (
  OperatorshortName varchar2(255) NOT NULL,
  Wagonid number(10) NOT NULL,
  PRIMARY KEY (OperatorshortName, Wagonid)
);

CREATE TABLE Owner (
  shortName varchar2(255) NOT NULL,
  name varchar2(255) NOT NULL,
  VATNumber varchar2(255) NOT NULL UNIQUE,
  PRIMARY KEY (shortName)
);

CREATE TABLE Path (
  id number(10),
  PRIMARY KEY (id)
);

CREATE TABLE Rail_Line (
  id number(10),
  name varchar2(255) NOT NULL,
  ownerID varchar2(255) NOT NULL,
  startID number(10) NOT NULL,
  endID number(10) NOT NULL,
  gauge number(10) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Rail_Line_Path (
  Rail_Lineid number(10) NOT NULL,
  Pathid number(10) NOT NULL,
  "order" number(10) NOT NULL,
  arrivalTime timestamp(0) NOT NULL,
  departureTime timestamp(0) NOT NULL,
  PRIMARY KEY (Rail_Lineid, Pathid)
);

CREATE TABLE Siding (
  id number(10),
  position number(10) NOT NULL,
  length number(10) NOT NULL,
  segmentID number(10) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Train (
  id number(10),
  "date" date NOT NULL,
  pathID number(10) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Wagon (
  id number(10),
  modelID number(10) NOT NULL,
  entryDate date NOT NULL,
  gauge number(10) NOT NULL,
  startEndpoint number(10) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Wagon_Boxcar (
  id number(10),
  storedGeneralGoods number(10) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Wagon_Flatcar (
  id number(10),
  storedContainers number(10) NOT NULL,
  storedVehicles number(10) NOT NULL,
  storedMachinery number(10) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Wagon_Freight (
  Wagonid number(10) NOT NULL,
  Freightid number(10) NOT NULL,
  PRIMARY KEY (Wagonid, Freightid)
);

CREATE TABLE Wagon_Hopper_Car (
  id number(10),
  storedCoal number(10) NOT NULL,
  storedGrains number(10) NOT NULL,
  storedMinerals number(10) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Wagon_Model (
  id number(10),
  model varchar2(255) NOT NULL,
  maker varchar2(255) NOT NULL,
  numberBogies number(10) NOT NULL,
  bogies varchar2(255) NOT NULL,
  length number(10) NOT NULL,
  width number(10) NOT NULL,
  height number(10) NOT NULL,
  weight number(10) NOT NULL,
  maxSpeed number(10) NOT NULL,
  payload number(10) NOT NULL,
  volume number(10) NOT NULL,
  flatcarID number(10) NOT NULL,
  tankcarID number(10) NOT NULL,
  hoppercarID number(10) NOT NULL,
  refrigeratedcarID number(10) NOT NULL,
  boxcarID number(10) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Wagon_Refrigerated_Car (
  id number(10),
  storedPerishableGoods number(10) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Wagon_Tank_Car (
  id number(10),
  storedLiquids number(10) NOT NULL,
  storedChemicals number(10) NOT NULL,
  storedFuel number(10) NOT NULL,
  PRIMARY KEY (id)
);

ALTER TABLE Endpoint
  ADD CONSTRAINT FKEndpoint308143 FOREIGN KEY (locationID) REFERENCES Location (id);

ALTER TABLE Line_Segment
  ADD CONSTRAINT FKLine_Segme478929 FOREIGN KEY (Line_TypeID) REFERENCES Line_Type (id);

ALTER TABLE Line_Segment
  ADD CONSTRAINT FKLine_Segme711926 FOREIGN KEY (Rail_LineId) REFERENCES Rail_Line (id);

ALTER TABLE Rail_Line
  ADD CONSTRAINT FKRail_Line180694 FOREIGN KEY (ownerID) REFERENCES Owner (shortName);

ALTER TABLE Locomotive
  ADD CONSTRAINT FKLocomotive826659 FOREIGN KEY (modelID) REFERENCES Locomotive_Model (id);

ALTER TABLE Wagon
  ADD CONSTRAINT FKWagon943492 FOREIGN KEY (modelID) REFERENCES Wagon_Model (id);

ALTER TABLE Rail_Line
  ADD CONSTRAINT FKRail_Line37767 FOREIGN KEY (startID) REFERENCES Endpoint (endpointID);

ALTER TABLE Rail_Line
  ADD CONSTRAINT FKRail_Line801339 FOREIGN KEY (endID) REFERENCES Endpoint (endpointID);

ALTER TABLE Operator_Wagon
  ADD CONSTRAINT FKOperator_W58759 FOREIGN KEY (OperatorshortName) REFERENCES Operator (shortName);

ALTER TABLE Operator_Wagon
  ADD CONSTRAINT FKOperator_W424175 FOREIGN KEY (Wagonid) REFERENCES Wagon (id);

ALTER TABLE Gauge_Locomotive
  ADD CONSTRAINT FKGauge_Loco300090 FOREIGN KEY (Gauge) REFERENCES Gauge (gaugeNumber);

ALTER TABLE Gauge_Locomotive
  ADD CONSTRAINT FKGauge_Loco514747 FOREIGN KEY (Locomotiveid) REFERENCES Locomotive (id);

ALTER TABLE Wagon
  ADD CONSTRAINT FKWagon204688 FOREIGN KEY (gauge) REFERENCES Gauge (gaugeNumber);

ALTER TABLE Freight
  ADD CONSTRAINT FKFreight349314 FOREIGN KEY (origin) REFERENCES Endpoint (endpointID);

ALTER TABLE Freight
  ADD CONSTRAINT FKFreight878394 FOREIGN KEY (destination) REFERENCES Endpoint (endpointID);

ALTER TABLE Freight
  ADD CONSTRAINT FKFreight834835 FOREIGN KEY (trainID) REFERENCES Train (id);

ALTER TABLE Train
  ADD CONSTRAINT FKTrain811655 FOREIGN KEY (pathID) REFERENCES Path (id);

ALTER TABLE Siding
  ADD CONSTRAINT FKSiding202536 FOREIGN KEY (segmentID) REFERENCES Line_Segment (id);

ALTER TABLE Rail_Line
  ADD CONSTRAINT FKRail_Line384735 FOREIGN KEY (gauge) REFERENCES Gauge (gaugeNumber);

ALTER TABLE Locomotive_Train
  ADD CONSTRAINT FKLocomotive258978 FOREIGN KEY (Locomotiveid) REFERENCES Locomotive (id);

ALTER TABLE Locomotive_Train
  ADD CONSTRAINT FKLocomotive807369 FOREIGN KEY (Trainid) REFERENCES Train (id);

ALTER TABLE Endpoint
  ADD CONSTRAINT FKEndpoint25100 FOREIGN KEY (typeID) REFERENCES Endpoint_Type (id);

ALTER TABLE Endpoint_Building
  ADD CONSTRAINT FKEndpoint_B882802 FOREIGN KEY (EndpointendpointID) REFERENCES Endpoint (endpointID);

ALTER TABLE Endpoint_Building
  ADD CONSTRAINT FKEndpoint_B414552 FOREIGN KEY (BuildingbuildingID) REFERENCES Building (buildingID);

ALTER TABLE Building
  ADD CONSTRAINT FKBuilding722917 FOREIGN KEY (typeID) REFERENCES Building_Type (id);

ALTER TABLE Wagon_Freight
  ADD CONSTRAINT FKWagon_Frei638479 FOREIGN KEY (Wagonid) REFERENCES Wagon (id);

ALTER TABLE Wagon_Freight
  ADD CONSTRAINT FKWagon_Frei897902 FOREIGN KEY (Freightid) REFERENCES Freight (id);

ALTER TABLE Locomotive_Model
  ADD CONSTRAINT FKLocomotive694275 FOREIGN KEY (dieselID) REFERENCES Locomotive_Diesel (id);

ALTER TABLE Locomotive_Model
  ADD CONSTRAINT FKLocomotive378395 FOREIGN KEY (electricID) REFERENCES Locomotive_Electric (id);

ALTER TABLE Wagon_Model
  ADD CONSTRAINT FKWagon_Mode747674 FOREIGN KEY (flatcarID) REFERENCES Wagon_Flatcar (id);

ALTER TABLE Wagon_Model
  ADD CONSTRAINT FKWagon_Mode993974 FOREIGN KEY (tankcarID) REFERENCES Wagon_Tank_Car (id);

ALTER TABLE Wagon_Model
  ADD CONSTRAINT FKWagon_Mode276974 FOREIGN KEY (hoppercarID) REFERENCES Wagon_Hopper_Car (id);

ALTER TABLE Wagon_Model
  ADD CONSTRAINT FKWagon_Mode489969 FOREIGN KEY (refrigeratedcarID) REFERENCES Wagon_Refrigerated_Car (id);

ALTER TABLE Wagon_Model
  ADD CONSTRAINT FKWagon_Mode529349 FOREIGN KEY (boxcarID) REFERENCES Wagon_Boxcar (id);

ALTER TABLE Rail_Line_Path
  ADD CONSTRAINT FKRail_Line_422390 FOREIGN KEY (Rail_Lineid) REFERENCES Rail_Line (id);

ALTER TABLE Rail_Line_Path
  ADD CONSTRAINT FKRail_Line_993049 FOREIGN KEY (Pathid) REFERENCES Path (id);

ALTER TABLE Locomotive
  ADD CONSTRAINT FKLocomotive977974 FOREIGN KEY (startEndpoint) REFERENCES Endpoint (endpointID);

ALTER TABLE Wagon
  ADD CONSTRAINT FKWagon551511 FOREIGN KEY (startEndpoint) REFERENCES Endpoint (endpointID);

ALTER TABLE Operator_Train
  ADD CONSTRAINT FKOperator_T788688 FOREIGN KEY (OperatorshortName) REFERENCES Operator (shortName);

ALTER TABLE Operator_Train
  ADD CONSTRAINT FKOperator_T653494 FOREIGN KEY (Trainid) REFERENCES Train (id);