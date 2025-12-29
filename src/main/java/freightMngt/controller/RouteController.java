package freightMngt.controller;

import freightMngt.dao.FreightDAO;
import utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RouteController {

    public List<String> getDetailedRoutePlan(int lineId, int freightId) {
        return FreightDAO.getInstance().getRoutePlan(lineId, freightId);
    }

    public List<String> getAvailableRailLines() {
        List<String> lines = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {

            // Query to get available rail lines from the database
            String sql = "SELECT id, name FROM Rail_Line ORDER BY id ASC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lines.add(String.format("Line ID: %d | Name: %s",
                        rs.getInt("id"),
                        rs.getString("name")));
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Couldn't list rail lines: " + e.getMessage());
        }
        return lines;
    }
}