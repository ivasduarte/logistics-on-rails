-- DROP TABLE Building CASCADE CONSTRAINTS;
-- DROP TABLE Endpoint CASCADE CONSTRAINTS;
-- DROP TABLE Endpoint_Path CASCADE CONSTRAINTS;
-- DROP TABLE Freight CASCADE CONSTRAINTS;
-- DROP TABLE FreightYard CASCADE CONSTRAINTS;
-- DROP TABLE FreightYard_Building CASCADE CONSTRAINTS;
-- DROP TABLE Gauge CASCADE CONSTRAINTS;
-- DROP TABLE Gauge_Locomotive CASCADE CONSTRAINTS;
-- DROP TABLE Grain_Silo CASCADE CONSTRAINTS;
-- DROP TABLE Line_Segment CASCADE CONSTRAINTS;
-- DROP TABLE Line_Type CASCADE CONSTRAINTS;
-- DROP TABLE Location CASCADE CONSTRAINTS;
-- DROP TABLE Locomotive CASCADE CONSTRAINTS;
-- DROP TABLE Locomotive_Diesel CASCADE CONSTRAINTS;
-- DROP TABLE Locomotive_Electric CASCADE CONSTRAINTS;
-- DROP TABLE Locomotive_Model CASCADE CONSTRAINTS;
-- DROP TABLE Locomotive_Trains CASCADE CONSTRAINTS;
-- DROP TABLE Locomotive_Type CASCADE CONSTRAINTS;
-- DROP TABLE Operator CASCADE CONSTRAINTS;
-- DROP TABLE Operator_Locomotive CASCADE CONSTRAINTS;
-- DROP TABLE Operator_Wagon CASCADE CONSTRAINTS;
-- DROP TABLE Owner CASCADE CONSTRAINTS;
-- DROP TABLE Path CASCADE CONSTRAINTS;
-- DROP TABLE Rail_Line CASCADE CONSTRAINTS;
-- DROP TABLE Refrigerated_Area CASCADE CONSTRAINTS;
-- DROP TABLE Rolling_Stock CASCADE CONSTRAINTS;
-- DROP TABLE Siding CASCADE CONSTRAINTS;
-- DROP TABLE Station CASCADE CONSTRAINTS;
-- DROP TABLE Station_Building CASCADE CONSTRAINTS;
-- DROP TABLE Terminal CASCADE CONSTRAINTS;
-- DROP TABLE Terminal_Building CASCADE CONSTRAINTS;
-- DROP TABLE Trains CASCADE CONSTRAINTS;
-- DROP TABLE Wagon CASCADE CONSTRAINTS;
-- DROP TABLE Wagon_Boxcar CASCADE CONSTRAINTS;
-- DROP TABLE Wagon_Flatcar CASCADE CONSTRAINTS;
-- DROP TABLE Wagon_Hopper_Car CASCADE CONSTRAINTS;
-- DROP TABLE Wagon_Model CASCADE CONSTRAINTS;
-- DROP TABLE Wagon_Refrigerated_Car CASCADE CONSTRAINTS;
-- DROP TABLE Wagon_Tank_Car CASCADE CONSTRAINTS;
-- DROP TABLE Wagon_Type CASCADE CONSTRAINTS;
-- DROP TABLE Warehouse CASCADE CONSTRAINTS;

CREATE TABLE Building (
  buildingID number(10),
  typeID number(10) NOT NULL,
  PRIMARY KEY (buildingID)
);

CREATE TABLE Endpoint (
  endpointID number(10),
  name varchar2(255) NOT NULL,
  locationID number(10) NOT NULL,
  typeID number(10) NOT NULL,
  PRIMARY KEY (endpointID)
);

CREATE TABLE Endpoint_Path (
  EndpointendpointID number(10) NOT NULL,
  Pathid number(10) NOT NULL,
  PRIMARY KEY (EndpointendpointID, Pathid)
);

CREATE TABLE Freight (
  id number(10),
  data date NOT NULL,
  origin number(10) NOT NULL,
  destination number(10) NOT NULL,
  trainID number(10),
  PRIMARY KEY (id)
);

CREATE TABLE FreightYard (
  id number(10),
  PRIMARY KEY (id)
);

CREATE TABLE FreightYard_Building (
  FreightYardid number(10) NOT NULL,
  BuildingbuildingID number(10) NOT NULL,
  PRIMARY KEY (FreightYardid, BuildingbuildingID)
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

CREATE TABLE Grain_Silo (
  id number(10),
  PRIMARY KEY (id)
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
  rollingStockID number(10) NOT NULL,
  name varchar2(255) NOT NULL UNIQUE,
  make varchar2(255) NOT NULL,
  modelID number(10) NOT NULL,
  entryYear number(10) NOT NULL,
  gauge number(10) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Locomotive_Diesel (
  id number(10),
  maxFuelCapacity number(10) NOT NULL,
  typeID number(10) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Locomotive_Electric (
  id number(10),
  voltage number(10) NOT NULL,
  frequency number(10) NOT NULL,
  typeID number(10) NOT NULL,
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
  typeID number(10) NOT NULL,
  weight number(10) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Locomotive_Trains (
  Locomotiveid number(10) NOT NULL,
  Trainsid number(10) NOT NULL,
  PRIMARY KEY (Locomotiveid, Trainsid)
);

CREATE TABLE Locomotive_Type (
  id number(10),
  name varchar2(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Operator (
  shortName varchar2(255) NOT NULL,
  name varchar2(255) NOT NULL,
  VATNumber varchar2(255) NOT NULL UNIQUE,
  PRIMARY KEY (shortName)
);

CREATE TABLE Operator_Locomotive (
  OperatorshortName varchar2(255) NOT NULL,
  Locomotiveid number(10) NOT NULL,
  PRIMARY KEY (OperatorshortName, Locomotiveid)
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
  sequenceOrder varchar2(255) NOT NULL,
  "start" number(10) NOT NULL,
  "end" number(10) NOT NULL,
  time timestamp(7) NOT NULL,
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

CREATE TABLE Refrigerated_Area (
  id number(10),
  PRIMARY KEY (id)
);

CREATE TABLE Rolling_Stock (
  id number(10),
  name varchar2(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Siding (
  id number(10),
  position number(10) NOT NULL,
  length number(10) NOT NULL,
  segmentID number(10) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Station (
  id number(10),
  PRIMARY KEY (id)
);

CREATE TABLE Station_Building (
  Stationid number(10) NOT NULL,
  BuildingbuildingID number(10) NOT NULL,
  PRIMARY KEY (Stationid, BuildingbuildingID)
);

CREATE TABLE Terminal (
  id number(10),
  isIntermodal number(10) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Terminal_Building (
  Terminalid number(10) NOT NULL,
  BuildingbuildingID number(10) NOT NULL,
  PRIMARY KEY (Terminalid, BuildingbuildingID)
);

CREATE TABLE Trains (
  id number(10),
  operator varchar2(255) NOT NULL,
  "date" date NOT NULL,
  pathID number(10) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Wagon (
  id number(10),
  rollingStockID number(10) NOT NULL,
  modelID number(10) NOT NULL,
  serviceYear number(10) NOT NULL,
  gauge number(10) NOT NULL,
  freightID number(10) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Wagon_Boxcar (
  id number(10),
  storedGeneralGoods number(10) NOT NULL,
  typeID number(10) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Wagon_Flatcar (
  id number(10),
  storedContainers number(10) NOT NULL,
  storedVehicles number(10) NOT NULL,
  storedMachinery number(10) NOT NULL,
  typeID number(10) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Wagon_Hopper_Car (
  id number(10),
  storedCoal number(10) NOT NULL,
  storedGrains number(10) NOT NULL,
  storedMinerals number(10) NOT NULL,
  typeID number(10) NOT NULL,
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
  typeID number(10) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Wagon_Refrigerated_Car (
  id number(10),
  storedPerishableGoods number(10) NOT NULL,
  typeID number(10) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Wagon_Tank_Car (
  id number(10),
  storedLiquids number(10) NOT NULL,
  storedChemicals number(10) NOT NULL,
  storedFuel number(10) NOT NULL,
  typeID number(10) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Wagon_Type (
  id number(10),
  name varchar2(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Warehouse (
  id number(10),
  PRIMARY KEY (id)
);

ALTER TABLE Endpoint
  ADD CONSTRAINT FKEndpoint308143 FOREIGN KEY (locationID) REFERENCES Location (id);

ALTER TABLE Line_Segment
  ADD CONSTRAINT FKLine_Segme478929 FOREIGN KEY (Line_TypeID) REFERENCES Line_Type (id);

ALTER TABLE Locomotive
  ADD CONSTRAINT FKLocomotive883881 FOREIGN KEY (rollingStockID) REFERENCES Rolling_Stock (id);

ALTER TABLE Wagon
  ADD CONSTRAINT FKWagon514172 FOREIGN KEY (rollingStockID) REFERENCES Rolling_Stock (id);

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

TER TABLE Operator_Locomotive
  ADD CONSTRAINT FKOperator_L744142 FOREIGN KEY (OperatorshortName) REFERENCES Operator (shortName);

ALTER TABLE Operator_Locomotive
  ADD CONSTRAINT FKOperator_L333656 FOREIGN KEY (Locomotiveid) REFERENCES Locomotive (id);

ALTER TABLE Gauge_Locomotive
  ADD CONSTRAINT FKGauge_Loco300090 FOREIGN KEY (Gauge) REFERENCES Gauge (gaugeNumber);

ALTER TABLE Gauge_Locomotive
  ADD CONSTRAINT FKGauge_Loco514747 FOREIGN KEY (Locomotiveid) REFERENCES Locomotive (id);

ALTER TABLE Wagon
  ADD CONSTRAINT FKWagon204688 FOREIGN KEY (gauge) REFERENCES Gauge (gaugeNumber);

ALTER TABLE Endpoint
  ADD CONSTRAINT FKEndpoint209750 FOREIGN KEY (typeID) REFERENCES FreightYard (id);

ALTER TABLE Endpoint
  ADD CONSTRAINT FKEndpoint464387 FOREIGN KEY (typeID) REFERENCES Station (id);

ALTER TABLE Endpoint
  ADD CONSTRAINT FKEndpoint658728 FOREIGN KEY (typeID) REFERENCES Terminal (id);

ALTER TABLE Building
  ADD CONSTRAINT FKBuilding547203 FOREIGN KEY (typeID) REFERENCES Warehouse (id);

ALTER TABLE Building
  ADD CONSTRAINT FKBuilding301549 FOREIGN KEY (typeID) REFERENCES Refrigerated_Area (id);

ALTER TABLE Building
  ADD CONSTRAINT FKBuilding264464 FOREIGN KEY (typeID) REFERENCES Grain_Silo (id);

ALTER TABLE Station_Building
  ADD CONSTRAINT FKStation_Bu255491 FOREIGN KEY (Stationid) REFERENCES Station (id);

ALTER TABLE Station_Building
  ADD CONSTRAINT FKStation_Bu571280 FOREIGN KEY (BuildingbuildingID) REFERENCES Building (buildingID);

ALTER TABLE FreightYard_Building
  ADD CONSTRAINT FKFreightYar238471 FOREIGN KEY (FreightYardid) REFERENCES FreightYard (id);

ALTER TABLE FreightYard_Building
  ADD CONSTRAINT FKFreightYar818103 FOREIGN KEY (BuildingbuildingID) REFERENCES Building (buildingID);

ALTER TABLE Terminal_Building
  ADD CONSTRAINT FKTerminal_B584545 FOREIGN KEY (Terminalid) REFERENCES Terminal (id);

ALTER TABLE Terminal_Building
  ADD CONSTRAINT FKTerminal_B837636 FOREIGN KEY (BuildingbuildingID) REFERENCES Building (buildingID);

ALTER TABLE Wagon
  ADD CONSTRAINT FKWagon428152 FOREIGN KEY (freightID) REFERENCES Freight (id);

TER TABLE Freight
  ADD CONSTRAINT FKFreight349314 FOREIGN KEY (origin) REFERENCES Endpoint (endpointID);

ALTER TABLE Freight
  ADD CONSTRAINT FKFreight878394 FOREIGN KEY (destination) REFERENCES Endpoint (endpointID);

ALTER TABLE Freight
  ADD CONSTRAINT FKFreight78608 FOREIGN KEY (trainID) REFERENCES Trains (id);

ALTER TABLE Trains
  ADD CONSTRAINT FKTrains246491 FOREIGN KEY (pathID) REFERENCES Path (id);

ALTER TABLE Siding
  ADD CONSTRAINT FKSiding202536 FOREIGN KEY (segmentID) REFERENCES Line_Segment (id);

ALTER TABLE Locomotive_Model
  ADD CONSTRAINT FKLocomotive170194 FOREIGN KEY (typeID) REFERENCES Locomotive_Type (id);

ALTER TABLE Locomotive_Diesel
  ADD CONSTRAINT FKLocomotive941955 FOREIGN KEY (typeID) REFERENCES Locomotive_Type (id);

ALTER TABLE Locomotive_Electric
  ADD CONSTRAINT FKLocomotive668716 FOREIGN KEY (typeID) REFERENCES Locomotive_Type (id);

ALTER TABLE Wagon_Model
  ADD CONSTRAINT FKWagon_Mode208621 FOREIGN KEY (typeID) REFERENCES Wagon_Type (id);

ALTER TABLE Wagon_Flatcar
  ADD CONSTRAINT FKWagon_Flat69490 FOREIGN KEY (typeID) REFERENCES Wagon_Type (id);

ALTER TABLE Wagon_Tank_Car
  ADD CONSTRAINT FKWagon_Tank811591 FOREIGN KEY (typeID) REFERENCES Wagon_Type (id);

ALTER TABLE Wagon_Hopper_Car
  ADD CONSTRAINT FKWagon_Hopp583826 FOREIGN KEY (typeID) REFERENCES Wagon_Type (id);

ALTER TABLE Wagon_Boxcar
  ADD CONSTRAINT FKWagon_Boxc480480 FOREIGN KEY (typeID) REFERENCES Wagon_Type (id);

ALTER TABLE Wagon_Refrigerated_Car
  ADD CONSTRAINT FKWagon_Refr405235 FOREIGN KEY (typeID) REFERENCES Wagon_Type (id);

ALTER TABLE Rail_Line
  ADD CONSTRAINT FKRail_Line384735 FOREIGN KEY (gauge) REFERENCES Gauge (gaugeNumber);

ALTER TABLE Endpoint_Path
  ADD CONSTRAINT FKEndpoint_P396597 FOREIGN KEY (EndpointendpointID) REFERENCES Endpoint (endpointID);

ALTER TABLE Endpoint_Path
  ADD CONSTRAINT FKEndpoint_P77142 FOREIGN KEY (Pathid) REFERENCES Path (id);

ALTER TABLE Locomotive_Trains
  ADD CONSTRAINT FKLocomotive331926 FOREIGN KEY (Locomotiveid) REFERENCES Locomotive (id);

ALTER TABLE Locomotive_Trains
  ADD CONSTRAINT FKLocomotive511663 FOREIGN KEY (Trainsid) REFERENCES Trains (id);

ALTER TABLE Path
  ADD CONSTRAINT FKPath229404 FOREIGN KEY ("start") REFERENCES Endpoint (endpointID);

ALTER TABLE Path
  ADD CONSTRAINT FKPath427672 FOREIGN KEY ("end") REFERENCES Endpoint (endpointID);