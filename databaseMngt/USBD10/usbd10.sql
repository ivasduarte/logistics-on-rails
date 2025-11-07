SELECT
    w.id AS WAGON_NUMBER,
    ors.operator AS WAGON_OPERATOR,
    w.serviceYear AS SERVICE_YEAR,
    wm.model AS WAGON_MODEL,
    wm.maker AS MAKER,
    wt.*
FROM
    WAGON_FLATCAR wt
    INNER JOIN WAGON_MODEL wm ON wt.id = wm.id
    INNER JOIN WAGON w ON w.modelID = wm.id
    LEFT JOIN Operator_Rolling_Stock ors ON ors.Rolling_Stockid = w.rollingStockID
ORDER BY
    wt.id;