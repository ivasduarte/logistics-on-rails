SELECT
    wm.model AS WAGON_MODEL,
    wm.maker AS MAKER,
    wf.*
FROM
    WAGON_FLATCAR wf
    INNER JOIN WAGON_MODEL wm ON wf.id = wm.id
ORDER BY
    wf.id;