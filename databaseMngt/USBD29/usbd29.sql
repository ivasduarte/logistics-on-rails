CREATE OR REPLACE FUNCTION fncGetElectricLocomotivePercentage(p_operator IN Operator.shortName%TYPE)
RETURN SYS_REFCURSOR
IS
    v_cursor SYS_REFCURSOR;
BEGIN
    OPEN v_cursor FOR
        SELECT
            t.operator                      AS operator_code,
            o.name                          AS operator_name,
            COUNT(DISTINCT l.id)            AS total_locomotives,
            COUNT(DISTINCT CASE WHEN lt.name = 'Electric' THEN l.id END)
                         AS electric_locomotives,
            CASE
                WHEN COUNT(DISTINCT l.id) = 0 THEN 0
                ELSE ROUND(COUNT(DISTINCT CASE WHEN lt.name = 'Electric' THEN l.id END) * 100/ COUNT(DISTINCT l.id),2)
            END AS percentage_electric
        FROM Train t
        JOIN Operator o
          ON o.shortName = t.operator
        JOIN Locomotive_Train lt_rel
          ON lt_rel.Trainid = t.id
        JOIN Locomotive l
          ON l.id = lt_rel.Locomotiveid
        JOIN Locomotive_Model lm
          ON lm.id = l.modelID
        JOIN Locomotive_Type lt
          ON lt.id = lm.typeID
        WHERE t.operator = p_operator
        GROUP BY t.operator, o.name;
    RETURN v_cursor;
END fncGetElectricLocomotivePercentage;