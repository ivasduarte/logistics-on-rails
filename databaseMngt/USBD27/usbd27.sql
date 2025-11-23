CREATE OR REPLACE FUNCTION fncGetGrainWagonsInTrains(p_start_date IN DATE,p_end_date   IN DATE)
RETURN SYS_REFCURSOR
IS
    v_cursor SYS_REFCURSOR;
BEGIN
    OPEN v_cursor FOR
        WITH Grain_Wagons AS (
            SELECT w.id AS wagon_id
            FROM Wagon w
            JOIN Wagon_Model wm ON wm.id = w.modelID
            JOIN Wagon_Hopper_Car whc ON whc.id = wm.id
            WHERE wm.typeID = 3
        ),
        Trains_With_Grain AS (
            SELECT DISTINCT f.trainID
            FROM Freight f
            JOIN Wagon_Freight wf ON wf.Freightid = f.id
            JOIN Grain_Wagons gw ON gw.wagon_id = wf.Wagonid
            WHERE f.data BETWEEN p_start_date AND p_end_date
              AND f.trainID IS NOT NULL
        ),
        Train_Count AS (
            SELECT COUNT(*) AS total_trains
            FROM Trains_With_Grain
        ),
        Grain_Wagon_Usage AS (
            SELECT
                wf.Wagonid AS wagon_id,
                COUNT(DISTINCT f.trainID) AS trains_used
            FROM Freight f
            JOIN Wagon_Freight wf ON wf.Freightid = f.id
            JOIN Grain_Wagons gw ON gw.wagon_id = wf.Wagonid
            JOIN Trains_With_Grain tw ON tw.trainID = f.trainID
            WHERE f.data BETWEEN p_start_date AND p_end_date
            GROUP BY wf.Wagonid
        )
        SELECT
            w.id AS wagon_id,
            wm.model AS wagon_model,
            whc.storedGrains AS grain_capacity
        FROM Grain_Wagon_Usage gu
        JOIN Train_Count tc ON 1 = 1
        JOIN Wagon w ON w.id = gu.wagon_id
        JOIN Wagon_Model wm ON wm.id = w.modelID
        JOIN Wagon_Hopper_Car whc ON whc.id = wm.id
        WHERE gu.trains_used = tc.total_trains;

    RETURN v_cursor;
END fncGetGrainWagonsInTrains;