package freightMngt.controller;

import freightMngt.dao.FreightDAO;
import freightMngt.domain.RollingStockDTO;
import freightMngt.domain.Station;
import java.util.*;
import java.util.stream.Collectors;

public class TrainAssemblyController {

    private final FreightDAO freightDAO;

    public TrainAssemblyController() {
        this.freightDAO = FreightDAO.getInstance();
    }

    public List<Station> getAllStations() {
        return freightDAO.getAllStations();
    }

    /**
     * Calcula distância usando dados da Base de Dados (Coordenadas)
     * sem depender do grafo em memória.
     */
    public List<RollingStockDTO> getRollingStockForAssembly(String routeOriginId) {
        List<RollingStockDTO> rawList = freightDAO.getAllRollingStock();

        if (routeOriginId == null) return rawList;

        // 1. Obter todas as estações com as suas coordenadas (vindas da BD)
        List<Station> allStations = freightDAO.getAllStations();

        // Criar um mapa para acesso rápido: ID (String) -> Station Object
        Map<String, Station> stationMap = allStations.stream()
                .collect(Collectors.toMap(Station::getNodeID, s -> s));

        // 2. Encontrar a estação de origem
        Station originStation = stationMap.get(routeOriginId);

        if (originStation == null) {
            System.err.println("Origin station '" + routeOriginId + "' not found in database.");
            return rawList;
        }

        // 3. Calcular distâncias para os itens PARKED
        for (RollingStockDTO item : rawList) {
            if ("PARKED".equals(item.getStatus())) {
                String itemLocationId = String.valueOf(item.getLocationId());
                Station itemStation = stationMap.get(itemLocationId);

                if (itemStation != null) {
                    // Cálculo da Distância Euclidiana baseada nas coordenadas da BD
                    double dist = calculateEuclideanDistance(originStation, itemStation);
                    item.setDistanceFromSource(dist);
                } else {
                    // Se não encontrarmos a estação onde o item está, distância máxima
                    item.setDistanceFromSource(Double.MAX_VALUE);
                }
            } else {
                // Itens em movimento não têm prioridade de distância nesta lógica
                item.setDistanceFromSource(Double.MAX_VALUE);
            }
        }

        // 4. Ordenar conforme Critério de Aceitação
        Collections.sort(rawList, (o1, o2) -> {
            boolean isP1 = "PARKED".equals(o1.getStatus());
            boolean isP2 = "PARKED".equals(o2.getStatus());

            if (isP1 && isP2) {
                // Ambos PARKED: Ordem DESCENDENTE de distância
                return Double.compare(o2.getDistanceFromSource(), o1.getDistanceFromSource());
            } else if (isP1) {
                return -1; // Parked primeiro
            } else if (isP2) {
                return 1;
            } else {
                return 0;
            }
        });

        return rawList;
    }

    private double calculateEuclideanDistance(Station s1, Station s2) {
        double dx = s1.getStationLatitude() - s2.getStationLatitude();
        double dy = s1.getStationLongitude() - s2.getStationLongitude();
        return Math.sqrt(dx * dx + dy * dy);
    }

    public boolean saveTrainAssembly(int trainId, java.util.Date trainDate, int pathId, List<Integer> locos, List<Integer> wagons) {
        return freightDAO.assembleTrain(trainId, trainDate, pathId, locos, wagons);
    }
}