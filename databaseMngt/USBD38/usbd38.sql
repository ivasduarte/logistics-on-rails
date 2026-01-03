CREATE OR REPLACE FUNCTION fncAddGauge (
    p_gaugeNumber IN NUMBER
)
RETURN NUMBER
IS
    v_count NUMBER;
BEGIN
    IF p_gaugeNumber IS NULL THEN
        RAISE_APPLICATION_ERROR(-20010, 'Gauge number is required.');
    END IF;

    IF p_gaugeNumber <= 0 THEN
            RAISE_APPLICATION_ERROR(-20012, 'Gauge number must be greater than 0.');
        END IF;

    SELECT COUNT(*)
    INTO v_count
    FROM GAUGE
    WHERE GAUGENUMBER = p_gaugeNumber;

    IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20011, 'Gauge already exists.');
    END IF;

    INSERT INTO GAUGE (GAUGENUMBER)
    VALUES (p_gaugeNumber);

    RETURN p_gaugeNumber;

EXCEPTION
    WHEN OTHERS THEN
        RAISE;
END fncAddGauge;
/
