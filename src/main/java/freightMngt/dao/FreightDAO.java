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


    /**
     * Get the operational plan details (station by station) with loading/unloading information.
     */
    public List<String> getRoutePlan(int railLineId, int freightId) {
        List<String> plan = new ArrayList<>();
        String sql = "{ ? = call get_route_plan_detailed(?, ?) }";

        try (Connection conn = utils.DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.registerOutParameter(1, oracle.jdbc.OracleTypes.CURSOR);
            stmt.setInt(2, railLineId);
            stmt.setInt(3, freightId);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    plan.add(rs.getString("step_description"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Couldn't obtain route plan: " + e.getMessage());
        }
        return plan;
    }

}