-- Function 1: Get all trains
CREATE OR REPLACE FUNCTION get_all_trains
RETURN SYS_REFCURSOR
IS
    v_cursor SYS_REFCURSOR;
BEGIN
OPEN v_cursor FOR
SELECT t.id, ot.OperatorshortName as operator, t."date" as train_date, t.pathID
FROM Train t
         LEFT JOIN Operator_Train ot ON t.id = ot.Trainid
ORDER BY t.id;
RETURN v_cursor;
END get_all_trains;
/

-- Function 2: Validate train ID
CREATE OR REPLACE FUNCTION is_valid_train_id(p_train_id IN NUMBER)
RETURN NUMBER
IS
    v_count NUMBER;
BEGIN
SELECT COUNT(*) INTO v_count
FROM Train
WHERE id = p_train_id;

IF v_count > 0 THEN
        RETURN 1;
ELSE
        RETURN 0;
END IF;
EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END is_valid_train_id;
/

-- Function 3: Get all endpoints
CREATE OR REPLACE FUNCTION get_all_endpoints
RETURN SYS_REFCURSOR
IS
    v_cursor SYS_REFCURSOR;
BEGIN
OPEN v_cursor FOR
SELECT endpointID, name
FROM Endpoint
ORDER BY endpointID;
RETURN v_cursor;
END get_all_endpoints;
/

-- Function 4: Get endpoint by ID
CREATE OR REPLACE FUNCTION get_endpoint_by_id(p_endpoint_id IN NUMBER)
RETURN SYS_REFCURSOR
IS
    v_cursor SYS_REFCURSOR;
BEGIN
OPEN v_cursor FOR
SELECT endpointID, name
FROM Endpoint
WHERE endpointID = p_endpoint_id;
RETURN v_cursor;
END get_endpoint_by_id;
/

-- Function 5: Get connected rail lines for an endpoint
CREATE OR REPLACE FUNCTION get_connected_rail_lines(p_endpoint_id IN NUMBER)
RETURN SYS_REFCURSOR
IS
    v_cursor SYS_REFCURSOR;
BEGIN
OPEN v_cursor FOR
SELECT id, name, startID, endID
FROM Rail_Line
WHERE startID = p_endpoint_id OR endID = p_endpoint_id;
RETURN v_cursor;
END get_connected_rail_lines;
/

-- Function 6: Get all rail lines with endpoint names
CREATE OR REPLACE FUNCTION get_all_rail_lines
RETURN SYS_REFCURSOR
IS
    v_cursor SYS_REFCURSOR;
BEGIN
OPEN v_cursor FOR
SELECT rl.id, rl.name,
       e1.name as startName,
       e2.name as endName
FROM Rail_Line rl
         JOIN Endpoint e1 ON rl.startID = e1.endpointID
         JOIN Endpoint e2 ON rl.endID = e2.endpointID
ORDER BY rl.id;
RETURN v_cursor;
END get_all_rail_lines;
/

-- Function 7: Get segments for a rail line
CREATE OR REPLACE FUNCTION get_segments_for_rail_line(p_rail_line_id IN NUMBER)
RETURN SYS_REFCURSOR
IS
    v_cursor SYS_REFCURSOR;
BEGIN
OPEN v_cursor FOR
SELECT ls.id, ls.length, ls.max_weight,
       lt.numberTracks, lt.isElectric
FROM Line_Segment ls
         JOIN Line_Type lt ON ls.Line_TypeID = lt.id
WHERE ls.Rail_LineId = p_rail_line_id
ORDER BY ls.orderPosition;
RETURN v_cursor;
END get_segments_for_rail_line;
/

-- Function 8: Get train with locomotives
CREATE OR REPLACE FUNCTION get_train_with_locomotives(p_train_id IN NUMBER)
RETURN SYS_REFCURSOR
IS
    v_cursor SYS_REFCURSOR;
BEGIN
OPEN v_cursor FOR
SELECT t.id, ot.OperatorshortName as operator, t."date" as train_date, t.pathID,
       l.id as locId, l.name as locName,
       lm.power, lm.weight as locWeight,
       lm.maxSpeed, lm.operationalSpeed
FROM Train t
         LEFT JOIN Operator_Train ot ON t.id = ot.Trainid
         LEFT JOIN Locomotive_Train lt ON t.id = lt.Trainid
         LEFT JOIN Locomotive l ON lt.Locomotiveid = l.id
         LEFT JOIN Locomotive_Model lm ON l.modelID = lm.id
WHERE t.id = p_train_id;
RETURN v_cursor;
END get_train_with_locomotives;
/

-- Function 9: Get wagons for a train
CREATE OR REPLACE FUNCTION get_wagons_for_train(p_train_id IN NUMBER)
RETURN SYS_REFCURSOR
IS
    v_cursor SYS_REFCURSOR;
BEGIN
OPEN v_cursor FOR
SELECT w.id, wm.weight, wm.payload, wm.maxSpeed,
       COALESCE(wb.storedGeneralGoods, 0) +
       COALESCE(wh.storedCoal, 0) +
       COALESCE(wh.storedGrains, 0) +
       COALESCE(wt.storedLiquids, 0) +
       COALESCE(wt.storedChemicals, 0) +
       COALESCE(wf.storedContainers, 0) +
       COALESCE(wf.storedVehicles, 0) +
       COALESCE(wr.storedPerishableGoods, 0) as cargoLoad
FROM Freight f
         LEFT JOIN Wagon_Freight wf2 ON f.id = wf2.Freightid
         LEFT JOIN Wagon w ON wf2.Wagonid = w.id
         LEFT JOIN Wagon_Model wm ON w.modelID = wm.id
         LEFT JOIN Wagon_Boxcar wb ON w.id = wb.id
         LEFT JOIN Wagon_Hopper_Car wh ON w.id = wh.id
         LEFT JOIN Wagon_Tank_Car wt ON w.id = wt.id
         LEFT JOIN Wagon_Flatcar wf ON w.id = wf.id
         LEFT JOIN Wagon_Refrigerated_Car wr ON w.id = wr.id
WHERE f.trainID = p_train_id;
RETURN v_cursor;
END get_wagons_for_train;
/

-- Function 10: Get sidings for a segment
CREATE OR REPLACE FUNCTION get_sidings_for_segment(p_segment_id IN NUMBER)
RETURN SYS_REFCURSOR
IS
    v_cursor SYS_REFCURSOR;
BEGIN
OPEN v_cursor FOR
SELECT s.id, s.position, s.length, s.segmentID
FROM Siding s
WHERE s.segmentID = p_segment_id
ORDER BY s.position;
RETURN v_cursor;
END get_sidings_for_segment;
/