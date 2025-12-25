DECLARE
    v_new_electric_id NUMBER;
    v_electric_count_before NUMBER;
    v_electric_count_after NUMBER;

    -- Cursor para listar todas as locomotivas elétricas
    CURSOR c_electric_locomotives IS
        SELECT le.id, le.voltage, le.frequency
        FROM Locomotive_Electric le
        ORDER BY le.id;

BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('USBD43: Add Electric Locomotive Model Test');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('');

    -- Conta o número de locomotivas elétricas antes
    SELECT COUNT(*) INTO v_electric_count_before FROM Locomotive_Electric;
    DBMS_OUTPUT.PUT_LINE('Total electric locomotives before: ' || v_electric_count_before);
    DBMS_OUTPUT.PUT_LINE('');

    -- Lista todas as locomotivas elétricas antes
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('ELECTRIC LOCOMOTIVES BEFORE REGISTRATION');
    DBMS_OUTPUT.PUT_LINE('========================================');

    FOR rec IN c_electric_locomotives LOOP
        DBMS_OUTPUT.PUT_LINE('Electric Locomotive ID: ' || rec.id);
        DBMS_OUTPUT.PUT_LINE('  Voltage: ' || rec.voltage || ' V');
        DBMS_OUTPUT.PUT_LINE('  Frequency: ' || rec.frequency || ' Hz');
        DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    END LOOP;

    IF v_electric_count_before = 0 THEN
        DBMS_OUTPUT.PUT_LINE('No electric locomotives found in database.');
        DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    END IF;

    -- Regista uma nova locomotiva elétrica
    v_new_electric_id := fncAddElectricLocomotiveModel(
        p_voltage => 25000,
        p_frequency => 50
    );

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('New electric locomotive registered successfully!');

    -- Conta o número de locomotivas elétricas depois
    SELECT COUNT(*) INTO v_electric_count_after FROM Locomotive_Electric;
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('Total electric locomotives after: ' || v_electric_count_after);

    -- Lista todas as locomotivas elétricas depois
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('ELECTRIC LOCOMOTIVES AFTER REGISTRATION');
    DBMS_OUTPUT.PUT_LINE('========================================');

    FOR rec IN c_electric_locomotives LOOP
        DBMS_OUTPUT.PUT_LINE('Electric Locomotive ID: ' || rec.id);
        DBMS_OUTPUT.PUT_LINE('  Voltage: ' || rec.voltage || ' V');
        DBMS_OUTPUT.PUT_LINE('  Frequency: ' || rec.frequency || ' Hz');

        IF rec.id = v_new_electric_id THEN
            DBMS_OUTPUT.PUT_LINE('  >>> NEWLY REGISTERED <<<');
        END IF;

        DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    END LOOP;


EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('');
        DBMS_OUTPUT.PUT_LINE('========================================');
        DBMS_OUTPUT.PUT_LINE('ERROR DURING TEST');
        DBMS_OUTPUT.PUT_LINE('========================================');
        DBMS_OUTPUT.PUT_LINE('Error Code: ' || SQLCODE);
        DBMS_OUTPUT.PUT_LINE('Error Message: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE('Test Status: FAILED');
        DBMS_OUTPUT.PUT_LINE('========================================');
END;
/