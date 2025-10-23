CREATE TABLE Location (
  id NUMBER(10) GENERATED AS IDENTITY,

  latitude VARCHAR2(255)
    CONSTRAINT nn_Location_latitude NOT NULL,

  longitude VARCHAR2(255)
    CONSTRAINT nn_Location_longitude NOT NULL,

  CONSTRAINT pk_Location_id PRIMARY KEY (id),
  CONSTRAINT uq_Location_coordinates UNIQUE (latitude, longitude)
);

CREATE TABLE Owner (
  shortName VARCHAR2(255)
    CONSTRAINT nn_Owner_shortName NOT NULL,

  name VARCHAR2(255)
    CONSTRAINT nn_Owner_name NOT NULL,

  VATNumber VARCHAR2(255)
    CONSTRAINT nn_Owner_VATNumber NOT NULL
    CONSTRAINT uq_Owner_VATNumber UNIQUE,

  CONSTRAINT pk_Owner_shortName PRIMARY KEY (shortName)
);

CREATE TABLE Operator (
  shortName VARCHAR2(255)
    CONSTRAINT nn_Operator_shortName NOT NULL,

  name VARCHAR2(255)
    CONSTRAINT nn_Operator_name NOT NULL,

  VATNumber VARCHAR2(255)
    CONSTRAINT nn_Operator_VATNumber NOT NULL
    CONSTRAINT uq_Operator_VATNumber UNIQUE,

  CONSTRAINT pk_Operator_shortName PRIMARY KEY (shortName)
);

CREATE TABLE Endpoint (
  endpointID NUMBER(10) GENERATED AS IDENTITY,

  name VARCHAR2(255)
    CONSTRAINT nn_Endpoint_name NOT NULL,

  locationID NUMBER(10)
    CONSTRAINT nn_Endpoint_locationID NOT NULL,

  CONSTRAINT pk_Endpoint_endpointID PRIMARY KEY (endpointID)
);

CREATE TABLE FreightYard (
  id NUMBER(10)
    CONSTRAINT nn_FreightYard_id NOT NULL,

  CONSTRAINT pk_FreightYard_id PRIMARY KEY (id)
);

CREATE TABLE Station (
  id NUMBER(10)
    CONSTRAINT nn_Station_id NOT NULL,

  hasWarehouse NUMBER
    CONSTRAINT nn_Station_hasWarehouse NOT NULL,

  hasRefrigeratedArea NUMBER
    CONSTRAINT nn_Station_hasRefrigeratedArea NOT NULL,

  hasGrainSilo NUMBER
    CONSTRAINT nn_Station_hasGrainSilo NOT NULL,

  CONSTRAINT pk_Station_id PRIMARY KEY (id)
);

CREATE TABLE Terminal (
  id NUMBER(10)
    CONSTRAINT nn_Terminal_id NOT NULL,

  hasWarehouse NUMBER
    CONSTRAINT nn_Terminal_hasWarehouse NOT NULL,

  hasRefrigeratedArea NUMBER
    CONSTRAINT nn_Terminal_hasRefrigeratedArea NOT NULL,

  hasGrainSilo NUMBER(10)
    CONSTRAINT nn_Terminal_hasGrainSilo NOT NULL,

  isIntermodal NUMBER(10)
    CONSTRAINT nn_Terminal_isIntermodal NOT NULL,

  CONSTRAINT pk_Terminal_id PRIMARY KEY (id)
);

CREATE TABLE Line_Type (
  id NUMBER(10) GENERATED AS IDENTITY,

  isElectric VARCHAR2(255)
    CONSTRAINT nn_Line_Type_isElectric NOT NULL,

  numberTracks NUMBER(10)
    CONSTRAINT nn_Line_Type_numberTracks NOT NULL,

  CONSTRAINT pk_Line_Type_id PRIMARY KEY (id)
);

CREATE TABLE Rail_Line (
  id NUMBER(10) GENERATED AS IDENTITY,

  name VARCHAR2(255)
    CONSTRAINT nn_Rail_Line_name NOT NULL,

  ownerID VARCHAR2(255)
    CONSTRAINT nn_Rail_Line_ownerID NOT NULL,

  startID NUMBER(10)
    CONSTRAINT nn_Rail_Line_startID NOT NULL,

  endID NUMBER(10)
    CONSTRAINT nn_Rail_Line_endID NOT NULL,

  gauge NUMBER(10)
    CONSTRAINT nn_Rail_Line_gauge NOT NULL,

  CONSTRAINT pk_Rail_Line_id PRIMARY KEY (id)
);

CREATE TABLE Line_Segment (
  id NUMBER(10) GENERATED AS IDENTITY,

  Rail_LineId NUMBER(10)
    CONSTRAINT nn_Line_Segment_Rail_LineId NOT NULL,

  orderPosition NUMBER(10)
    CONSTRAINT nn_Line_Segment_orderPosition NOT NULL,

  max_weight FLOAT(10)
    CONSTRAINT nn_Line_Segment_max_weight NOT NULL,

  length NUMBER(10)
    CONSTRAINT nn_Line_Segment_length NOT NULL,

  Line_TypeID NUMBER(10)
    CONSTRAINT nn_Line_Segment_Line_TypeID NOT NULL,

  CONSTRAINT pk_Line_Segment_id PRIMARY KEY (id),
  CONSTRAINT uq_Line_Segment_RailLinePositions UNIQUE (Rail_LineId, orderPosition)
);

CREATE TABLE Rolling_Stock (
  id NUMBER(10) GENERATED AS IDENTITY,

  name VARCHAR2(255)
    CONSTRAINT nn_Rolling_Stock_name NOT NULL,

  CONSTRAINT pk_Rolling_Stock_id PRIMARY KEY (id)
);

CREATE TABLE Operator_Rolling_Stock (
  operator VARCHAR2(255)
    CONSTRAINT nn_Operator_Rolling_Stock_operator NOT NULL,

  Rolling_Stockid NUMBER(10)
    CONSTRAINT nn_Operator_Rolling_Stock_Rolling_Stockid NOT NULL,

  CONSTRAINT pk_Operator_Rolling_Stock_operator_Rolling_Stockid PRIMARY KEY (operator, Rolling_Stockid)
);

CREATE TABLE Locomotive_Model (
  id NUMBER(10) GENERATED AS IDENTITY,

  model VARCHAR2(255)
    CONSTRAINT nn_Locomotive_Model_model NOT NULL,

  numberBogies NUMBER(10)
    CONSTRAINT nn_Locomotive_Model_numberBogies NOT NULL,

  bogies VARCHAR2(255)
    CONSTRAINT nn_Locomotive_Model_bogies NOT NULL,

  power NUMBER(10)
    CONSTRAINT nn_Locomotive_Model_power NOT NULL,

  length NUMBER(10)
    CONSTRAINT nn_Locomotive_Model_length NOT NULL,

  width NUMBER(10)
    CONSTRAINT nn_Locomotive_Model_width NOT NULL,

  height NUMBER(10)
    CONSTRAINT nn_Locomotive_Model_height NOT NULL,

  maxSpeed NUMBER(10)
    CONSTRAINT nn_Locomotive_Model_maxSpeed NOT NULL,

  operationalSpeed NUMBER(10)
    CONSTRAINT nn_Locomotive_Model_operationalSpeed NOT NULL,

  traction NUMBER(10)
    CONSTRAINT nn_Locomotive_Model_traction NOT NULL,

  CONSTRAINT pk_Locomotive_Model_id PRIMARY KEY (id)
);

CREATE TABLE Locomotive_Diesel (
  id NUMBER(10)
    CONSTRAINT nn_Locomotive_Diesel_id NOT NULL,

  maxFuelCapacity NUMBER(10)
    CONSTRAINT nn_Locomotive_Diesel_maxFuelCapacity NOT NULL,

  CONSTRAINT pk_Locomotive_Diesel_id PRIMARY KEY (id)
);

CREATE TABLE Locomotive_Electric (
  id NUMBER(10)
    CONSTRAINT nn_Locomotive_Electric_id NOT NULL,

  voltage NUMBER(10)
    CONSTRAINT nn_Locomotive_Electric_voltage NOT NULL,

  frequency NUMBER(10)
    CONSTRAINT nn_Locomotive_Electric_frequency NOT NULL,

  CONSTRAINT pk_Locomotive_Electric_id PRIMARY KEY (id)
);

CREATE TABLE Locomotive (
  id NUMBER(10) GENERATED AS IDENTITY,

  rollingStockID NUMBER(10)
    CONSTRAINT nn_Locomotive_rollingStockID NOT NULL,

  name VARCHAR2(255)
    CONSTRAINT nn_Locomotive_name NOT NULL
    CONSTRAINT uq_Locomotive_name UNIQUE,

  make VARCHAR2(255)
    CONSTRAINT nn_Locomotive_make NOT NULL,

  modelID NUMBER(10)
    CONSTRAINT nn_Locomotive_modelID NOT NULL,

  entryYear NUMBER(10)
    CONSTRAINT nn_Locomotive_entryYear NOT NULL,

  gauge NUMBER(10)
    CONSTRAINT nn_Locomotive_gauge NOT NULL,

  CONSTRAINT pk_Locomotive_id PRIMARY KEY (id)
);

CREATE TABLE Wagon_Type (
  id NUMBER(10) GENERATED AS IDENTITY,

  typeName VARCHAR2(255)
    CONSTRAINT nn_Wagon_Type_typeName NOT NULL
    CONSTRAINT uq_Wagon_Type_typeName UNIQUE,

  CONSTRAINT pk_Wagon_Type_id PRIMARY KEY (id)
);

CREATE TABLE Wagon_Model (
  id NUMBER(10) GENERATED AS IDENTITY,

  model VARCHAR2(255)
    CONSTRAINT nn_Wagon_Model_model NOT NULL,

  maker VARCHAR2(255)
    CONSTRAINT nn_Wagon_Model_maker NOT NULL,

  numberBogies NUMBER(10)
    CONSTRAINT nn_Wagon_Model_numberBogies NOT NULL,

  bogies VARCHAR2(255)
    CONSTRAINT nn_Wagon_Model_bogies NOT NULL,

  length NUMBER(10)
    CONSTRAINT nn_Wagon_Model_length NOT NULL,

  width NUMBER(10)
    CONSTRAINT nn_Wagon_Model_width NOT NULL,

  height NUMBER(10)
    CONSTRAINT nn_Wagon_Model_height NOT NULL,

  weight NUMBER(10)
    CONSTRAINT nn_Wagon_Model_weight NOT NULL,

  maxSpeed NUMBER(10)
    CONSTRAINT nn_Wagon_Model_maxSpeed NOT NULL,

  payload NUMBER(10)
    CONSTRAINT nn_Wagon_Model_payload NOT NULL,

  volume NUMBER(10)
    CONSTRAINT nn_Wagon_Model_volume NOT NULL,

  typeID NUMBER(10)
    CONSTRAINT nn_Wagon_Model_typeID NOT NULL,

  CONSTRAINT pk_Wagon_Model_id PRIMARY KEY (id)
);

CREATE TABLE Wagon_Boxcar (
  id NUMBER(10)
    CONSTRAINT nn_Wagon_Boxcar_id NOT NULL,

  storedGeneralGoods NUMBER(10)
    CONSTRAINT nn_Wagon_Boxcar_storedGeneralGoods NOT NULL,

  CONSTRAINT pk_Wagon_Boxcar_id PRIMARY KEY (id)
);

CREATE TABLE Wagon_Flatcar (
  id NUMBER(10)
    CONSTRAINT nn_Wagon_Flatcar_id NOT NULL,

  storedContainers NUMBER(10)
    CONSTRAINT nn_Wagon_Flatcar_storedContainers NOT NULL,

  storedVehicles NUMBER(10)
    CONSTRAINT nn_Wagon_Flatcar_storedVehicles NOT NULL,

  storedMachinery NUMBER(10)
    CONSTRAINT nn_Wagon_Flatcar_storedMachinery NOT NULL,

  CONSTRAINT pk_Wagon_Flatcar_id PRIMARY KEY (id)
);

CREATE TABLE Wagon_Hopper_Car (
  id NUMBER(10)
    CONSTRAINT nn_Wagon_Hopper_Car_id NOT NULL,

  storedCoal NUMBER(10)
    CONSTRAINT nn_Wagon_Hopper_Car_storedCoal NOT NULL,

  storedGrains NUMBER(10)
    CONSTRAINT nn_Wagon_Hopper_Car_storedGrains NOT NULL,

  storedMinerals NUMBER(10)
    CONSTRAINT nn_Wagon_Hopper_Car_storedMinerals NOT NULL,

  CONSTRAINT pk_Wagon_Hopper_Car_id PRIMARY KEY (id)
);

CREATE TABLE Wagon_Refrigerated_Car (
  id NUMBER(10)
    CONSTRAINT nn_Wagon_Refrigerated_Car_id NOT NULL,

  storedPerishableGoods NUMBER(10)
    CONSTRAINT nn_Wagon_Refrigerated_Car_storedPerishableGoods NOT NULL,

  CONSTRAINT pk_Wagon_Refrigerated_Car_id PRIMARY KEY (id)
);

CREATE TABLE Wagon_Tank_Car (
  id NUMBER(10)
    CONSTRAINT nn_Wagon_Tank_Car_id NOT NULL,

  storedLiquids NUMBER(10)
    CONSTRAINT nn_Wagon_Tank_Car_storedLiquids NOT NULL,

  storedChemicals NUMBER(10)
    CONSTRAINT nn_Wagon_Tank_Car_storedChemicals NOT NULL,

  storedFuel NUMBER(10)
    CONSTRAINT nn_Wagon_Tank_Car_storedFuel NOT NULL,

  CONSTRAINT pk_Wagon_Tank_Car_id PRIMARY KEY (id)
);

CREATE TABLE Wagon (
  id NUMBER(10) GENERATED AS IDENTITY,

  rollingStockID NUMBER(10)
    CONSTRAINT nn_Wagon_rollingStockID NOT NULL,

  modelID NUMBER(10)
    CONSTRAINT nn_Wagon_modelID NOT NULL,

  serviceYear NUMBER(10)
    CONSTRAINT nn_Wagon_serviceYear NOT NULL,

  gauge NUMBER(10)
    CONSTRAINT nn_Wagon_gauge NOT NULL,

  CONSTRAINT pk_Wagon_id PRIMARY KEY (id)
);

ALTER TABLE Endpoint 
  ADD CONSTRAINT fk_Location_Endpoint_id_locationID FOREIGN KEY (locationID) REFERENCES Location (id);

ALTER TABLE FreightYard 
  ADD CONSTRAINT fk_Endpoint_FreightYard_endpointID_id FOREIGN KEY (id) REFERENCES Endpoint (endpointID);

ALTER TABLE Station 
  ADD CONSTRAINT fk_Endpoint_Station_endpointID_id FOREIGN KEY (id) REFERENCES Endpoint (endpointID);

ALTER TABLE Terminal 
  ADD CONSTRAINT fk_Endpoint_Terminal_endpointID_id FOREIGN KEY (id) REFERENCES Endpoint (endpointID);

ALTER TABLE Line_Segment 
  ADD CONSTRAINT fk_Line_Type_Line_Segment_id_Line_TypeID FOREIGN KEY (Line_TypeID) REFERENCES Line_Type (id);

ALTER TABLE Line_Segment 
  ADD CONSTRAINT fk_Rail_Line_Line_Segment_id_Rail_LineId FOREIGN KEY (Rail_LineId) REFERENCES Rail_Line (id);

ALTER TABLE Rail_Line 
  ADD CONSTRAINT fk_Owner_Rail_Line_shortName_ownerID FOREIGN KEY (ownerID) REFERENCES Owner (shortName);

ALTER TABLE Rail_Line 
  ADD CONSTRAINT fk_Endpoint_Rail_Line_endpointID_startID FOREIGN KEY (startID) REFERENCES Endpoint (endpointID);

ALTER TABLE Rail_Line 
  ADD CONSTRAINT fk_Endpoint_Rail_Line_endpointID_endID FOREIGN KEY (endID) REFERENCES Endpoint (endpointID);

ALTER TABLE Locomotive 
  ADD CONSTRAINT fk_Rolling_Stock_Locomotive_id_rollingStockID FOREIGN KEY (rollingStockID) REFERENCES Rolling_Stock (id);

ALTER TABLE Locomotive 
  ADD CONSTRAINT fk_Locomotive_Model_Locomotive_id_modelID FOREIGN KEY (modelID) REFERENCES Locomotive_Model (id);

ALTER TABLE Locomotive_Diesel 
  ADD CONSTRAINT fk_Locomotive_Model_Locomotive_Diesel_id_id FOREIGN KEY (id) REFERENCES Locomotive_Model (id);

ALTER TABLE Locomotive_Electric 
  ADD CONSTRAINT fk_Locomotive_Model_Locomotive_Electric_id_id FOREIGN KEY (id) REFERENCES Locomotive_Model (id);

ALTER TABLE Wagon 
  ADD CONSTRAINT fk_Rolling_Stock_Wagon_id_rollingStockID FOREIGN KEY (rollingStockID) REFERENCES Rolling_Stock (id);

ALTER TABLE Wagon 
  ADD CONSTRAINT fk_Wagon_Model_Wagon_id_modelID FOREIGN KEY (modelID) REFERENCES Wagon_Model (id);

ALTER TABLE Wagon_Model 
  ADD CONSTRAINT fk_Wagon_Type_Wagon_Model_id_typeID FOREIGN KEY (typeID) REFERENCES Wagon_Type (id);

ALTER TABLE Wagon_Boxcar 
  ADD CONSTRAINT fk_Wagon_Model_Wagon_Boxcar_id_id FOREIGN KEY (id) REFERENCES Wagon_Model (id);

ALTER TABLE Wagon_Flatcar 
  ADD CONSTRAINT fk_Wagon_Model_Wagon_Flatcar_id_id FOREIGN KEY (id) REFERENCES Wagon_Model (id);

ALTER TABLE Wagon_Hopper_Car 
  ADD CONSTRAINT fk_Wagon_Model_Wagon_Hopper_Car_id_id FOREIGN KEY (id) REFERENCES Wagon_Model (id);

ALTER TABLE Wagon_Refrigerated_Car 
  ADD CONSTRAINT fk_Wagon_Model_Wagon_Refrigerated_Car_id_id FOREIGN KEY (id) REFERENCES Wagon_Model (id);

ALTER TABLE Wagon_Tank_Car 
  ADD CONSTRAINT fk_Wagon_Model_Wagon_Tank_Car_id_id FOREIGN KEY (id) REFERENCES Wagon_Model (id);

ALTER TABLE Operator_Rolling_Stock 
  ADD CONSTRAINT fk_Operator_Operator_Rolling_Stock_shortName_operator FOREIGN KEY (operator) REFERENCES Operator (shortName);

ALTER TABLE Operator_Rolling_Stock 
  ADD CONSTRAINT fk_Rolling_Stock_Operator_Rolling_Stock_id_Rolling_Stockid FOREIGN KEY (Rolling_Stockid) REFERENCES Rolling_Stock (id);
