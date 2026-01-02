# USEI10 - Radius Search and Density Summary

### Objective

Search for all stations within a given radius (in km) of a target point using Haversine distance, sort results by distance and station name, and generate a density summary by country and city status.

---

### Complexity Analysis

The analysis covers the radius search operation with KD-tree pruning, sorting, and density summary generation.

**Phase 1: Radius Search**

This phase occurs in `RadiusSearch.searchByRadius()` and performs a recursive radius query on the pre-built KD-Tree.

```java
public StationResult searchByRadius(double latitude, double longitude, double radiusKm) {
    Point2D target = new Point2D(latitude, longitude);
    List<StationDistance> candidates = new ArrayList<>();
    
    // Search the KD-Tree for stations within radius
    searchRadius(kdTree.getRoot(), target, radiusKm, true, candidates);
    
    // Sort by distance (ASC), then by station name (ASC)
    candidates.sort(Comparator
            .comparingDouble(StationDistance::getDistance)
            .thenComparing(sd -> sd.getStation().getStationName()));
    
    // Extract stations and generate density summary
    List<Station> stations = new ArrayList<>();
    for (StationDistance sd : candidates) {
        stations.add(sd.getStation());
    }
    
    Map<String, Integer> densitySummary = generateDensitySummary(stations);
    
    return new StationResult(stations, densitySummary);
}
```

**Sub-operation 1.1: Recursive Radius Search with Pruning**

```java
private void searchRadius(KDNode node, Point2D target, double radiusKm,
                          boolean divideByLatitude, List<StationDistance> results) {
    if (node == null) return;
    
    // Calculate distance to this node
    double distance = haversineDistance(
            target.getStationLatitude(), target.getStationLongitude(),
            node.getCoordinates().getStationLatitude(), 
            node.getCoordinates().getStationLongitude()
    );
    
    // If this node is within radius, add all its stations
    if (distance <= radiusKm) {
        for (Station station : node.getStationList()) {
            results.add(new StationDistance(station, distance));
        }
    }
    
    // Calculate perpendicular distance to splitting plane
    double perpendicularDist = Math.abs(targetValue - splitValue);
    double perpendicularDistKm = perpendicularDist * 111.0;
    
    // Search near side
    searchRadius(nearNode, target, radiusKm, !divideByLatitude, results);
    
    // Only search far side if splitting plane intersects search circle
    if (perpendicularDistKm <= radiusKm) {
        searchRadius(farNode, target, radiusKm, !divideByLatitude, results);
    }
}
```

**Analysis:**

Let $P$ = number of unique coordinate points in the tree, $h$ = tree height, and $K$ = number of stations within the radius.

At each node:
- Haversine distance calculation: $\mathcal{O}(1)$ (trigonometric operations)
- Add stations if within radius: $\mathcal{O}(B)$ where $B$ is the bucket size
- Perpendicular distance calculation: $\mathcal{O}(1)$
- Recursive exploration: Only subtrees that could intersect the search circle

**Best Case** (small radius, effective pruning): $\mathcal{O}(h + K) = \mathcal{O}(\log P + K)$

**Average Case** (typical radius query): $\mathcal{O}(\sqrt{P} + K)$

**Worst Case** (large radius covering entire space): $\mathcal{O}(P + K)$

**Temporal complexity of Sub-operation 1.1: $\mathcal{O}(\sqrt{P} + K)$ average case**

---

**Sub-operation 1.2: Sorting Results**

```java
candidates.sort(Comparator
        .comparingDouble(StationDistance::getDistance)
        .thenComparing(sd -> sd.getStation().getStationName()));
```

**Analysis:**

Sorting $K$ stations by distance (primary) and station name (secondary) using comparison-based sorting.

**Temporal complexity of Sub-operation 1.2: $\mathcal{O}(K \log K)$**

---

**Sub-operation 1.3: Extract Stations**

```java
List<Station> stations = new ArrayList<>();
for (StationDistance sd : candidates) {
    stations.add(sd.getStation());
}
```

**Analysis:**

Simple linear iteration through $K$ results.

**Temporal complexity of Sub-operation 1.3: $\mathcal{O}(K)$**

---

**Sub-operation 1.4: Generate Density Summary**

```java
private Map<String, Integer> generateDensitySummary(List<Station> stations) {
    Map<String, Integer> summary = new TreeMap<>();
    
    for (Station station : stations) {
        String country = station.getStationCountry();
        String cityStatus = station.isCity() ? "City" : "NotCity";
        String key = country + "_" + cityStatus;
        
        summary.put(key, summary.getOrDefault(key, 0) + 1);
    }
    
    return summary;
}
```

**Analysis:**

Iterates through $K$ stations with $\mathcal{O}(\log C)$ TreeMap operations, where $C$ is the number of unique country-city combinations (typically small and bounded).

**Temporal complexity of Sub-operation 1.4: $\mathcal{O}(K \log C)$**

Since $C$ is bounded by a small constant (e.g., $2 \times$ number of countries), this simplifies to $\mathcal{O}(K)$ in practice.

---

**Combined Phase 1 Complexity:**

$$\mathcal{O}(\sqrt{P} + K) + \mathcal{O}(K \log K) + \mathcal{O}(K) + \mathcal{O}(K)$$

The sorting operation dominates when $K$ is large:

$$\mathcal{O}(\sqrt{P} + K \log K)$$

**Temporal complexity of Phase 1: $\mathcal{O}(\sqrt{P} + K \log K)$ average case**

---

### Final Metrics

| Scenario                          | Temporal Complexity                |
|:----------------------------------|:-----------------------------------|
| **Best Case**                     | $\mathcal{O}(\log P + K \log K)$   |
| **Average Case**                  | $\mathcal{O}(\sqrt{P} + K \log K)$ |
| **Worst Case**                    | $\mathcal{O}(P + K \log K)$        |

Where:
- $P$ = number of unique coordinate points in the KD-tree
- $K$ = number of stations within the search radius
- $C$ = number of unique country-city combinations (bounded constant)

**Temporal Complexity: $\mathcal{O}(\sqrt{P} + K \log K)$**