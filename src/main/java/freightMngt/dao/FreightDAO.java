package freightMngt.dao;

import freightMngt.domain.PendingFreight;
import freightMngt.domain.RollingStockDTO;
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


    /**
     * Get the list of pending shipments for a specific line.
     */
    public List<PendingFreight> getPendingFreightsByLine(int railLineId) {
        List<PendingFreight> freights = new ArrayList<>();
        String sql = "{ ? = call get_pending_freights_detailed(?) }";

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.registerOutParameter(1, OracleTypes.CURSOR);
            stmt.setInt(2, railLineId);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    PendingFreight pf = new PendingFreight(
                            rs.getInt("id"),
                            rs.getInt("origin_id"),
                            rs.getInt("dest_id"),
                            rs.getInt("trainID"),
                            rs.getString("origin_name"),
                            rs.getString("dest_name")
                    );
                    freights.add(pf);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] " + e.getMessage());
        }
        return freights;
    }

    /**
     * Assigns the route and train to a freight shipment.
     */
    public boolean assignRouteToFreight(int freightId, int railLineId) {
        String sql = "{ call pr_assign_route_by_line(?, ?) }";

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, freightId);
            stmt.setInt(2, railLineId);
            stmt.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("[ERROR] Couldn't assign route: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves a list of all available rolling stock from the database.
     * The rolling stock includes locomotives and wagons, along with their
     * current status and location details.
     *
     * @return a list of RollingStockDTO objects representing the available rolling stock,
     *         or an empty list if no data is found or an error occurs.
     */
    public List<RollingStockDTO> getAllRollingStock() {
        List<RollingStockDTO> stockList = new ArrayList<>();
        String sql = "{ ? = call get_available_rolling_stock() }";

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.registerOutParameter(1, OracleTypes.CURSOR);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    stockList.add(new RollingStockDTO(
                            rs.getString("type"),
                            rs.getInt("resource_id"),
                            rs.getString("resource_name"),
                            rs.getInt("location_id"),
                            rs.getString("location_name"),
                            rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to fetch rolling stock: " + e.getMessage());
        }
        return stockList;
    }

    /**
     * Assembles a train by persisting the relationships in the database.
     * - Locomotives are added to Locomotive_Train.
     * - Wagons are added to Wagon_Freight (linked to the Freight assigned to this train).
     */
    public boolean assembleTrain(int trainId, List<Integer> locomotiveIds, List<Integer> wagonIds) {
        Connection conn = null;
        PreparedStatement psLoco = null;
        PreparedStatement psGetFreight = null;
        PreparedStatement psWagon = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            // Desligar auto-commit para garantir transação atómica (tudo ou nada)
            conn.setAutoCommit(false);

            // 1. Inserir Locomotivas (Tabela: Locomotive_Train)
            String sqlLoco = "INSERT INTO Locomotive_Train (Locomotiveid, Trainid) VALUES (?, ?)";
            psLoco = conn.prepareStatement(sqlLoco);

            for (Integer locoId : locomotiveIds) {
                psLoco.setInt(1, locoId);
                psLoco.setInt(2, trainId);
                psLoco.addBatch();
            }
            psLoco.executeBatch();

            // 2. Inserir Vagões (Tabela: Wagon_Freight)
            // Primeiro: Precisamos saber qual é o Freight ID associado a este Train ID
            // (Assumindo que o comboio já tem uma mercadoria atribuída via USLP08 ou similar)
            int freightId = -1;
            String sqlGetFreight = "SELECT id FROM Freight WHERE trainID = ?";
            psGetFreight = conn.prepareStatement(sqlGetFreight);
            psGetFreight.setInt(1, trainId);

            try (ResultSet rs = psGetFreight.executeQuery()) {
                if (rs.next()) {
                    freightId = rs.getInt("id");
                }
            }

            if (freightId != -1 && !wagonIds.isEmpty()) {
                // Se encontrámos a mercadoria, associamos os vagões a ela
                String sqlWagon = "INSERT INTO Wagon_Freight (Wagonid, Freightid) VALUES (?, ?)";
                psWagon = conn.prepareStatement(sqlWagon);

                for (Integer wagonId : wagonIds) {
                    psWagon.setInt(1, wagonId);
                    psWagon.setInt(2, freightId);
                    psWagon.addBatch();
                }
                psWagon.executeBatch();
            } else if (!wagonIds.isEmpty()) {
                System.err.println("[WARN] Wagons selected but no Freight found for Train " + trainId + ". Wagons not saved.");
                // Opcional: Podes decidir fazer rollback aqui se for obrigatório ter Freight
            }

            // Commit final
            conn.commit();
            success = true;
            System.out.println("Train assembly saved successfully.");

        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to assemble train: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            // Fechar recursos manualmente ou usar try-with-resources se refatorares
            try {
                if (psLoco != null) psLoco.close();
                if (psGetFreight != null) psGetFreight.close();
                if (psWagon != null) psWagon.close();
                if (conn != null) {
                    conn.setAutoCommit(true); // Restaurar default
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return success;
    }
}