# USEI07 — Complexity Analysis: Build a Balanced 2D-Tree

## Index
- [Objective](#objective)
- [Complexity Analysis](#complexity-analysis)
- [Summary of Metrics](#summary-of-metrics)
- [Conclusion](#conclusion)

---

## Objective

Construct a balanced 2D tree to support efficient spatial queries on approximately 64,000 stations.
To ensure optimal balance, the **Bulk Loading** algorithm is used, processing the data before construction and always
dividing it by the median.

---

## Complexity Analysis

The construction process is dominated by three main phases:

1. [Grouping](#1-grouping-by-coordinates)
2. [Pre-Ordering](#2-optimized-pre-sorting)
3. [Recursive Construction](#3-recursive-tree-construction)

### 1. Grouping By Coordinates

Since multiple stations can share the exact same location (same latitude and longitude), we group them together first
so that each node in the tree represents a unique geographic point.

```java
public Map<Point2D, List<Station>> groupByCoordinates(List<Station> stationList) {
    Map<Point2D, List<Station>> groupedStations = new LinkedHashMap<>();
    for (Station station : stationList) {
        Point2D point = new Point2D(station.getStationLatitude(), station.getStationLongitude());
        groupedStations.computeIfAbsent(point, k -> new ArrayList<>()).add(station);
    }
    return groupedStations;
}
```

- **Analysis:** Goes through the list of all $n$ stations (≈64,000) only once.
- **Cost:** Inserting into a system LinkedHashMap has an average complexity of $O(1)$.
- **Complexity of this phase:** $O(n)$


### 2. Optimized Pre-Sorting

To avoid sorting lists at each level of recursion (which would increase complexity), the lists are pre-sorted only 
once before construction begins.

```java
private List<Point2D> sortPoints(...) {
    // Utiliza as árvores AVL criadas no USEI06 para obter ordenação linear
    List<Station> stations = latTree.inOrderTraversal();
    // ... filtragem para pontos únicos ...
    return sortedPoints;
}
```

- **Analysis:** Instead of a `Collections.sort` traditional ($O(n \log n)$), the code takes advantage of the structure 
of AVL trees (`latTree` and `lonTree`).
- **Cost:** The in-order throughput of an AVL is$O(n)$, where $n$ is the number of unique points.
- **Complexity of this phase:** $O(n)$

### 3. Recursive Tree Construction

The tree is constructed by dividing the space alternately by latitude (X-axis) and longitude (Y-axis).

```java
private KDNode buildBalancedRecursive(...,List<Point2D> latList, List<Point2D> lonList, boolean divideByLatitude) {
    // Encontrar mediana (O(1) pois a lista já está ordenada)
    int medianIndex = list.size() / 2;

    // Particionar as listas em Esquerda e Direita (O(n))
    distributePoints(latList, lonList, medianPoint, ...);

    // Chamadas recursivas
    newNode.setLeft(buildBalancedRecursive(...));
    newNode.setRight(buildBalancedRecursive(...));
}
```

- **Analysis:** It uses the "Divide and Conquer" model. At each level of the tree, the method `distributePoints` traverses 
the current lists to maintain the ordering in the subtrees. This partitioning costs $O(n)$ by level. Since we always 
divide by the median, the height of the tree is strictly $\log_2(n)$.

- **Recurrence Equation:** $T(n) = 2T(n/2) + O(n)$

- **Complexity of this phase:** $O(n log n)$

### 4. Organization and Final Metrics

After construction, the code executes `organizeTree()` to alphabetically sort the stations within each node `calculateMetrics()`.

- **Analysis:** Both traverse all nodes of the tree once.
- **Complexity of this phase:** $O(n)$

---

## Summary of Metrics


| Metric      | Complexity / Value | Notes                                                                     |
|-------------|--------------------|---------------------------------------------------------------------------|
| Total Time  | $O(n log n)$       | Dominated by recursive construction and partitioning.m                    |
| Space       | $O(m)$             | Where $m$ is the total number of stations stored in the nodes' "buckets". |
| Tree Height | ≈ 15 - 16          | $\log_2(61832) \approx 15.9$ (ideal balance).                             | 

---

## Conclusion

The use of AVL pre-ordering and the systematic selection of the median ensure that the 2D tree is built efficiently 
and remains perfectly balanced, optimizing future spatial searches (nearest neighbour search or area queries).