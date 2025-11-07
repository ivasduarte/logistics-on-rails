SELECT
    rl.id AS line_id,
    rl.name AS line_name,
    e1.name AS start_endpoint,
    e2.name AS final_endpoint,
    ls.id AS segment_id,
    ls.orderPosition AS segment_order,
    ls.max_weight AS max_weight,
    ls.length AS segment_length,
    lt.isElectric AS is_electrified,
    lt.numberTracks AS track_amount
FROM
    Line_Segment ls
    INNER JOIN Rail_Line rl ON ls.Rail_LineId = rl.id
    INNER JOIN Endpoint e1 ON rl.startID = e1.endpointID
    INNER JOIN Endpoint e2 ON rl.endID = e2.endpointID
    INNER JOIN Line_Type lt ON ls.Line_TypeID = lt.id
WHERE
    (rl.startID = 5 AND rl.endID = 7)
    OR
    (rl.startID = 7 AND rl.endID = 5)
ORDER BY
    rl.id,
    ls.orderPosition;