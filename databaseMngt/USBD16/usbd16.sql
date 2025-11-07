SELECT
    rl.id        AS line_id,
    rl.name      AS line_name,
    e1.name      AS start_endpoint,
    e2.name      AS final_endpoint,
    MIN(ls.length) AS min_double_track_length
FROM
    Line_Segment ls
    INNER JOIN Rail_Line rl ON ls.Rail_LineId = rl.id
    INNER JOIN Endpoint e1   ON rl.startID     = e1.endpointID
    INNER JOIN Endpoint e2   ON rl.endID       = e2.endpointID
    INNER JOIN Line_Type lt  ON ls.Line_TypeID = lt.id
WHERE
    rl.id = 3
    AND lt.numberTracks = 2
GROUP BY
    rl.id, rl.name, e1.name, e2.name
ORDER BY
    rl.id;