SELECT
    rl.id AS line_id,                    -- ID of the rail line
    rl.name AS line_name,                -- Name of the rail line
    e1.name AS start_endpoint,           -- Name of the starting endpoint
    e2.name AS final_endpoint,           -- Name of the ending endpoint
    ls.id AS segment_id,                 -- ID of the line segment
    ls.orderPosition AS segment_order,   -- Order position of segment in the line
    ls.max_weight AS max_weight,         -- Maximum weight capacity (kg/m)
    ls.length AS segment_length,         -- Length of the segment
    lt.isElectric AS is_electrified,     -- Whether segment is electrified
    lt.numberTracks AS track_amount      -- Number of tracks (single/double)
FROM
    Line_Segment ls
    INNER JOIN Rail_Line rl ON ls.Rail_LineId = rl.id
    INNER JOIN Endpoint e1 ON rl.startID = e1.endpointID
    INNER JOIN Endpoint e2 ON rl.endID = e2.endpointID
    INNER JOIN Line_Type lt ON ls.Line_TypeID = lt.id
WHERE
    (rl.startID = &start_endpoint_id AND rl.endID = &end_endpoint_id)
    OR
    (rl.startID = &end_endpoint_id AND rl.endID = &start_endpoint_id)
ORDER BY
    rl.id,                  -- First group by rail line
    ls.orderPosition;       -- Then order segments within each line