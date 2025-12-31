package freightMngt.controller;

import freightMngt.dao.FreightDAO;
import freightMngt.domain.RollingStockDTO;
import freightMngt.domain.Station;
import java.util.*;

public class TrainAssemblyController {

    private final FreightDAO freightDAO;
    private final NetworkController networkController;

    public TrainAssemblyController(NetworkController networkController) {
        this.freightDAO = FreightDAO.getInstance();
        this.networkController = networkController;
    }

    /**
     * Retorna a lista de material circulante ordenada e processada
     * de acordo com a User Story.
     * * @param routeOriginId O ID da estação de partida da rota (para cálculo de distâncias).
     */
    public List<RollingStockDTO> getRollingStockForAssembly(String routeOriginId) {
        List<RollingStockDTO> rawList = freightDAO.getAllRollingStock();
        Station originStation = networkController.getStationByID(routeOriginId);

        if (originStation == null) {
            System.err.println("Origin station not found in graph.");
            return rawList;
        }

        // Calcular distâncias para os itens PARKED
        for (RollingStockDTO item : rawList) {
            if ("PARKED".equals(item.getStatus())) {
                Station itemLocation = networkController.getStationByID(String.valueOf(item.getLocationId()));

                if (itemLocation != null) {
                    // Usar shortestPath do NetworkController
                    // Usamos soma de Double e comparador padrão
                    LinkedList<Station> path = new LinkedList<>();
                    Double distance = networkController.shortestPathBellmanFord(
                            originStation,
                            itemLocation,
                            Double::compare,
                            Double::sum,
                            0.0,
                            path
                    );

                    if (distance != null) {
                        item.setDistanceFromSource(distance);
                    } else {
                        // Se não há caminho, definimos -1 ou max value
                        item.setDistanceFromSource(Double.MAX_VALUE);
                    }
                }
            }
        }

        // Ordenar conforme Critério de Aceitação:
        // 1. Recursos PARKED devem estar ordenados por distância DESCENDENTE da origem.
        // 2. Recursos IN_TRANSIT aparecem (pode ser no fim ou início, a US não especifica, vamos pôr no fim).
        Collections.sort(rawList, (o1, o2) -> {
            boolean isP1 = "PARKED".equals(o1.getStatus());
            boolean isP2 = "PARKED".equals(o2.getStatus());

            if (isP1 && isP2) {
                // Ambos PARKED: Ordem DESCENDENTE de distância
                return Double.compare(o2.getDistanceFromSource(), o1.getDistanceFromSource());
            } else if (isP1) {
                return -1; // Parked vem primeiro (prioridade para montar)
            } else if (isP2) {
                return 1;
            } else {
                return 0; // Ambos In Transit
            }
        });

        return rawList;
    }

    public boolean saveTrainAssembly(int trainId, List<Integer> locos, List<Integer> wagons) {
        return freightDAO.assembleTrain(trainId, locos, wagons);
    }

    public List<Station> getAllStations() {
        return networkController.getStations();
    }
}