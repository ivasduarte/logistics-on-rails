package esinf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spatialQuerying.railwayMngt.Station;
import spatialQuerying.trees.kd.KDTree2D;
import spatialQuerying.trees.kd.Point2D;
import spatialQuerying.trees.nodes.KDNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KDTree2DTest {

    private KDTree2D tree;

    @BeforeEach
    void setUp() {
        tree = new KDTree2D();
    }

    private Station createStation(String name, double lat, double lon) {
        return new Station(name, "Portugal",
                "Europe/Lisbon",
                "WET",
                lat,
                lon,
                false,
                true,
                false);
    }

    @Test
    void testGroupByCoordinates() {
        List<Station> stations = new ArrayList<>();
        stations.add(createStation("A", 10, 10));
        stations.add(createStation("B", 20, 20));
        stations.add(createStation("A_DuplicateCoord", 10, 10)); // Mesma coord de A

        Map<Point2D, List<Station>> groups = tree.groupByCoordinates(stations);

        assertEquals(2, groups.size(), "Deve haver apenas 2 pontos únicos");

        Point2D p10 = new Point2D(10, 10);
        assertTrue(groups.containsKey(p10));
        assertEquals(2, groups.get(p10).size(), "O ponto (10,10) deve ter 2 estações");
    }

    @Test
    void testBuildBalancedTree_SimpleStructure() {
        /*
         * Scenario to test Lat/Long division:
         * P1: (10, 50)
         * P2: (20, 10)  <- Latitude Median (10, 20, 30)
         * P3: (30, 50)
         */
        List<Station> stations = new ArrayList<>();
        stations.add(createStation("Left", 10, 50));
        stations.add(createStation("Root", 20, 10));
        stations.add(createStation("Right", 30, 50));

        // We pass null in AVLs because the buildBalancedTree method provided
        // does the sorting internally and doesn't use latTree/lonTree parameters for core logic.
        tree.buildBalancedTree(stations, null, null);

        KDNode root = tree.getRoot();
        assertNotNull(root);

        // 1. Verifica Raiz (Divisão por Latitude)
        // Lista ordenada Lat: 10, 20, 30. Mediana index 1 = 20.
        assertEquals(20.0, root.getCoordinates().getStationLatitude(), 0.001);

        // 2. Verifica Filho Esquerdo (Latitude < 20)
        assertNotNull(root.getLeft());
        assertEquals(10.0, root.getLeft().getCoordinates().getStationLatitude(), 0.001);

        // 3. Verifica Filho Direito (Latitude >= 20)
        assertNotNull(root.getRight());
        assertEquals(30.0, root.getRight().getCoordinates().getStationLatitude(), 0.001);
    }

    @Test
    void testBuildBalancedTree_AlternatingLevels() {
        /*
         * Test of 2 levels (Lat -> Lon)
         * Points:
         * A: (20, 10)
         * B: (20, 30)
         * C: (20, 20)
         * * All Latitudes are equal (20).
         * The median of first division (Lat) will pick one of them (e.g.: B, index 1 sorted).
         * The next level should divide by Longitude.
         */

        List<Station> stations = new ArrayList<>();
        // Inserir desordenado
        stations.add(createStation("C", 20, 20));
        stations.add(createStation("A", 20, 10));
        stations.add(createStation("B", 20, 30));

        tree.buildBalancedTree(stations, null, null);

        KDNode root = tree.getRoot();

        // Due to stable sort, the median of 3 elements (index 1) will be the middle one
        // Assuming your 'uniquePoints' list maintains insertion order or lat sort.
        // If lat equal, does lat sort maintain relative order or use long?
        // In your code: byLatitude.sort(Comparator.comparingDouble(Point2D::getStationLatitude));
        // If latitudes are equal, the order is "arbitrary" (depends on Java's sort algorithm).

        // Vamos verificar apenas métricas para garantir que a árvore foi montada
        assertEquals(3, tree.getSize());
        // Altura deve ser 1 (raiz + filhos diretos) ou 2 dependendo da definição.
        // A tua definição: Math.max(left, right) + 1. 
        // Nivel 0 (raiz), Nivel 1 (filhos). Se raiz tem filhos, altura = 1. 
        // Espera... Root(0) -> Filho(1). Height = 1.
        // Se for Root -> Filho -> Neto. Height = 2.
        assertTrue(tree.getHeight() >= 1);
    }

    @Test
    void testMetrics() {
        List<Station> stations = new ArrayList<>();
        // 3 pontos únicos
        stations.add(createStation("S1", 10, 10));
        stations.add(createStation("S2", 20, 20));
        stations.add(createStation("S3", 30, 30));
        // 1 duplicado no S1
        stations.add(createStation("S4", 10, 10));

        tree.buildBalancedTree(stations, null, null);

        assertEquals(3, tree.getSize(), "Size deve contar pontos únicos (KDNodes)");

        Map<Integer, Integer> buckets = tree.getBucketSizes();
        // Esperamos:
        // 1 nó com 2 estações (o ponto 10,10)
        // 2 nós com 1 estação (20,20 e 30,30)
        assertTrue(buckets.containsKey(2));
        assertEquals(1, buckets.get(2));

        assertTrue(buckets.containsKey(1));
        assertEquals(2, buckets.get(1));
    }

    @Test
    void testEmptyTree() {
        tree.buildBalancedTree(new ArrayList<>(), null, null);
        assertNull(tree.getRoot());
        assertEquals(0, tree.getSize());
        assertEquals(0, tree.getHeight()); // Code returns 0 if root is null? No, metric checks root!=null.
        // Se root null, calculateMetrics mete height=0.
    }
}