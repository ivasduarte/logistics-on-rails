package freightMngt.dao;

import freightMngt.domain.PendingFreight;
import utils.DatabaseConnection;
import oracle.jdbc.OracleTypes;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FreightDAO {
    private static FreightDAO instance;

    private FreightDAO() {}

    public static FreightDAO getInstance() {
        if (instance == null) {
            instance = new FreightDAO();
        }
        return instance;
    }
}