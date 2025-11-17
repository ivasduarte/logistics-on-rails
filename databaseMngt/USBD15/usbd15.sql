SELECT
    e.endpointID   AS endpoint_id,
    e.name         AS endpoint_name,
    COUNT(*)       AS total_lines_departing
FROM
    Rail_Line rl
    INNER JOIN Endpoint e
        ON rl.startID = e.endpointID
WHERE
    e.endpointID = 5
GROUP BY
    e.endpointID, e.name
ORDER BY
    e.endpointID;
