package freightMngt.controller;

import utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RouteController {

    public List<String> getPendingFreights() {
        List<String> freights = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "{ ? = call get_pending_freights_detailed() }";
            CallableStatement stmt = conn.prepareCall(sql);
            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.execute();

            ResultSet rs = (ResultSet) stmt.getObject(1);
            while (rs.next()) {
                freights.add(String.format("ID: %d | %s | From: %s  | To: %s",
                        rs.getInt("id"),
                        rs.getString("cargo_description"),
                        rs.getString("origin_station"),
                        rs.getString("destination_station")));
            }
        } catch (SQLException e) {
            System.err.println("Error listing freights: " + e.getMessage());
        }
        return freights;
    }

    public List<String> getDetailedRoutePlan(int railLineId, int freightId) {
        List<String> plan = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "{ ? = call get_route_plan_detailed(?, ?) }";
            CallableStatement stmt = conn.prepareCall(sql);
            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setInt(2, railLineId);
            stmt.setInt(3, freightId);
            stmt.execute();

            ResultSet rs = (ResultSet) stmt.getObject(1);
            while (rs.next()) {
                plan.add(String.format("%d. Station: %-15s | Action: %s",
                        rs.getInt("stop_order"),
                        rs.getString("station_name"),
                        rs.getString("action_to_perform")));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving route plan: " + e.getMessage());
        }
        return plan;
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
            System.err.println("Error listing rail lines: " + e.getMessage());
        }
        return lines;
    }
}