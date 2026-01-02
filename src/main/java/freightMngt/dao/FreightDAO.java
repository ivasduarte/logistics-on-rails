package freightMngt.dao;

import freightMngt.domain.PendingFreight;
import freightMngt.domain.RollingStockDTO;
import freightMngt.domain.Station;
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
     * @return a list of RollingStockDTO objects representing the available rolling stock.
     */
    public List<RollingStockDTO> getAllRollingStock() {
        List<RollingStockDTO> stockList = new ArrayList<>();
        // Chamada à função PL/SQL que retorna um CURSOR
        String sql = "{ ? = call get_available_rolling_stock() }";

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            // Regista o parâmetro de retorno (o cursor do Oracle)
            stmt.registerOutParameter(1, OracleTypes.CURSOR);
            stmt.execute();

            // Extrai o ResultSet do parâmetro de saída
            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    // Instancia o DTO com os dados vindos da base de dados
                    // Certifica-te que os nomes das colunas ("type", "resource_id", etc.)
                    // correspondem exatamente ao que a tua função SQL devolve.
                    stockList.add(new RollingStockDTO(
                            rs.getString("type"),           // Ex: "LOCOMOTIVE" ou "WAGON"
                            rs.getInt("resource_id"),       // ID único do recurso
                            rs.getString("resource_name"),  // Nome (ex: "Inês", "Wagon 1")
                            rs.getInt("location_id"),       // ID da estação onde está
                            rs.getString("location_name"),  // Nome da estação
                            rs.getString("status")          // Ex: "PARKED"
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to fetch rolling stock: " + e.getMessage());
            e.printStackTrace();
        }
        return stockList;
    }

    public List<Station> getAllStations() {
        List<Station> stations = new ArrayList<>();

        // JOIN entre Endpoint e Location para obter as coordenadas
        String sql = "SELECT e.endpointID, e.name, l.latitude, l.longitude " +
                "FROM Endpoint e " +
                "JOIN Location l ON e.locationID = l.id " +
                "ORDER BY e.endpointID ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String id = String.valueOf(rs.getInt("endpointID"));
                String name = rs.getString("name");

                // Conversão das strings de coordenadas para double
                // (Na tua BD os inserts são '100', '200', etc.)
                double lat = 0.0;
                double lon = 0.0;
                try {
                    lat = Double.parseDouble(rs.getString("latitude"));
                    lon = Double.parseDouble(rs.getString("longitude"));
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing coordinates for station " + id);
                }

                // Usamos o construtor da classe Station que aceita ID, Nome e Coordenadas
                // Assumindo que x=lat e y=lon para cálculo de distância simples
                Station station = new Station(id, name, lat, lon, lat, lon);
                stations.add(station);
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to fetch stations with coordinates: " + e.getMessage());
        }
        return stations;
    }

    /**
     * Assembles a train by persisting the relationships in the database.
     * Includes debug prints and checks for existing trains.
     */
    public boolean assembleTrain(int trainId, java.util.Date trainDate, int pathId, List<Integer> locomotiveIds, List<Integer> wagonIds) {
        Connection conn = null;
        PreparedStatement psCheckTrain = null;
        PreparedStatement psTrain = null;
        PreparedStatement psLoco = null;
        PreparedStatement psGetFreight = null;
        PreparedStatement psWagon = null;
        boolean success = false;

        System.out.println(">>> Starting assembleTrain for Train ID: " + trainId);

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Início da Transação

            // 1. Verificar se o Train já existe
            boolean trainExists = false;
            String sqlCheck = "SELECT count(*) FROM Train WHERE id = ?";
            psCheckTrain = conn.prepareStatement(sqlCheck);
            psCheckTrain.setInt(1, trainId);
            try (ResultSet rs = psCheckTrain.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    trainExists = true;
                }
            }

            // 2. Inserir Train (apenas se não existir)
            if (!trainExists) {
                System.out.println("Inserting new Train header...");
                String sqlTrain = "INSERT INTO Train (id, \"date\", pathID) VALUES (?, ?, ?)";
                psTrain = conn.prepareStatement(sqlTrain);
                psTrain.setInt(1, trainId);
                psTrain.setDate(2, new java.sql.Date(trainDate.getTime()));
                psTrain.setInt(3, pathId);
                psTrain.executeUpdate();
            } else {
                System.out.println("Train " + trainId + " already exists. Skipping header insert.");
                // Opcional: Poderias fazer um UPDATE aqui se quisesses atualizar a data/path
            }

            // 3. Inserir Locomotivas (Tabela: Locomotive_Train)
            if (locomotiveIds != null && !locomotiveIds.isEmpty()) {
                System.out.println("Inserting " + locomotiveIds.size() + " locomotives...");
                String sqlLoco = "INSERT INTO Locomotive_Train (Locomotiveid, Trainid) VALUES (?, ?)";
                psLoco = conn.prepareStatement(sqlLoco);

                for (Integer locoId : locomotiveIds) {
                    psLoco.setInt(1, locoId);
                    psLoco.setInt(2, trainId);
                    psLoco.addBatch();
                    System.out.println(" - Queued Loco: " + locoId);
                }
                int[] locoResults = psLoco.executeBatch();
                System.out.println("Executed Loco Batch. Rows affected: " + locoResults.length);
            } else {
                System.out.println("No locomotives to insert.");
            }

            // 4. Inserir Vagões (Tabela: Wagon_Freight)
            // Primeiro: Obter o Freight ID
            int freightId = -1;
            String sqlGetFreight = "SELECT id FROM Freight WHERE trainID = ?";
            psGetFreight = conn.prepareStatement(sqlGetFreight);
            psGetFreight.setInt(1, trainId);

            try (ResultSet rs = psGetFreight.executeQuery()) {
                if (rs.next()) {
                    freightId = rs.getInt("id");
                }
            }

            if (freightId != -1 && wagonIds != null && !wagonIds.isEmpty()) {
                System.out.println("Found Freight ID " + freightId + ". Inserting " + wagonIds.size() + " wagons...");
                String sqlWagon = "INSERT INTO Wagon_Freight (Wagonid, Freightid) VALUES (?, ?)";
                psWagon = conn.prepareStatement(sqlWagon);

                for (Integer wagonId : wagonIds) {
                    psWagon.setInt(1, wagonId);
                    psWagon.setInt(2, freightId);
                    psWagon.addBatch();
                }
                psWagon.executeBatch();
            } else if (wagonIds != null && !wagonIds.isEmpty()) {
                System.out.println("[WARN] Wagons selected but NO Freight found for Train " + trainId + ". Wagons skipped.");
            }

            // Commit final
            conn.commit();
            success = true;
            System.out.println("Train assembly COMMITTED successfully.");

        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to assemble train. Rolling back! Error: " + e.getMessage());
            e.printStackTrace(); // Isto vai mostrar-te o erro exato na consola
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("Transaction rolled back.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            // Fechar todos os recursos
            try {
                if (psCheckTrain != null) psCheckTrain.close();
                if (psTrain != null) psTrain.close();
                if (psLoco != null) psLoco.close();
                if (psGetFreight != null) psGetFreight.close();
                if (psWagon != null) psWagon.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return success;
    }
}