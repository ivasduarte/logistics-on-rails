SELECT
    rl.id        AS line_id,
    rl.name      AS line_name,
    e1.name      AS start_endpoint,
    e2.name      AS final_endpoint,
    COUNT(*) OVER (PARTITION BY rl.startID) AS total_lines_departing
FROM
    Rail_Line rl
    INNER JOIN Endpoint e1 ON rl.startID = e1.endpointID
    INNER JOIN Endpoint e2 ON rl.endID   = e2.endpointID
WHERE
    rl.startID = 5
ORDER BY
    rl.id;