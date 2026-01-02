# USEI08 - Search by Geographical Area

### Objective

Query a 2D KD-Tree for all stations within a specified latitude/longitude range with optional filters (city status, main station designation, and country). The search uses KD-tree pruning to avoid scanning the entire dataset.

---

### Complexity Analysis

The analysis covers the geographical range search operation, which is the core functionality of USEI08.

**Phase 1: Geographical Range Search**

This phase occurs in `GeographicalSearch.searchByArea()` and performs a recursive range query on the pre-built KD-Tree.

```java
public List<Station> searchByArea(double latMin, double latMax, 
                                  double lonMin, double lonMax,
                                  Boolean isCity, Boolean isMainStation, 
                                  String country) {
    if (!isValidRange(latMin, latMax, lonMin, lonMax)) {
        throw new IllegalArgumentException("Invalid coordinates range.");
    }
    
    List<Station> stationsInArea = new ArrayList<>();
    
    searchAreaRecursive(kdTree.getRoot(), latMin, latMax, lonMin, lonMax, 
                        true, stationsInArea);
    
    return applyFilters(stationsInArea, isCity, isMainStation, country);
}
```

**Sub-operation 1.1: Validate Range**

```java
private boolean isValidRange(double latMin, double latMax, 
                             double lonMin, double lonMax) {
    return latMin >= LAT_MIN && latMax <= LAT_MAX &&
            lonMin >= LON_MIN && lonMax <= LON_MAX &&
            latMin <= latMax && lonMin <= lonMax;
}
```

**Analysis:**

Simple constant-time comparisons.

**Temporal complexity of Sub-operation 1.1: $\mathcal{O}(1)$**

---

**Sub-operation 1.2: Recursive Range Search with Pruning**

```java
private void searchAreaRecursive(KDNode node, double latMin, double latMax,
                                 double lonMin, double lonMax,
                                 boolean divideByLatitude, List<Station> result) {
    if (node == null) return;
    
    double nodeLat = node.getCoordinates().getStationLatitude();
    double nodeLon = node.getCoordinates().getStationLongitude();
    
    boolean inLatRange = nodeLat >= latMin && nodeLat <= latMax;
    boolean inLonRange = nodeLon >= lonMin && nodeLon <= lonMax;
    
    if (inLatRange && inLonRange) {
        result.addAll(node.getStationList());
    }
    
    if (divideByLatitude) {
        if (latMin <= nodeLat) {
            searchAreaRecursive(node.getLeft(), latMin, latMax, lonMin, lonMax, 
                               !divideByLatitude, result);
        }
        if (latMax >= nodeLat) {
            searchAreaRecursive(node.getRight(), latMin, latMax, lonMin, lonMax, 
                               !divideByLatitude, result);
        }
    } else {
        if (lonMin <= nodeLon) {
            searchAreaRecursive(node.getLeft(), latMin, latMax, lonMin, lonMax, 
                               !divideByLatitude, result);
        }
        if (lonMax >= nodeLon) {
            searchAreaRecursive(node.getRight(), latMin, latMax, lonMin, lonMax, 
                               !divideByLatitude, result);
        }
    }
}
```

**Analysis:**

Let $P$ = number of unique coordinate points in the tree, $h$ = tree height, and $K$ = number of stations in the query result.

At each node:
- Range check: $\mathcal{O}(1)$
- Add stations: $\mathcal{O}(B)$ where $B$ is the bucket size at that node
- Recursive exploration: Only subtrees that intersect the query region

**Best Case** (small query region, effective pruning): $\mathcal{O}(h + K) = \mathcal{O}(\log P + K)$

**Average Case** (typical rectangular query): $\mathcal{O}(\sqrt{P} + K)$

**Worst Case** (query covers entire space): $\mathcal{O}(P + K)$

**Temporal complexity of Sub-operation 1.2: $\mathcal{O}(\sqrt{P} + K)$ average case**

---

**Sub-operation 1.3: Applying Filters**

```java
private List<Station> applyFilters(List<Station> stations, Boolean isCity,
                                   Boolean isMainStation, String country) {
    return stations.stream()
            .filter(station -> isCity == null || station.isCity() == isCity)
            .filter(station -> isMainStation == null || 
                    station.isMainStation() == isMainStation)
            .filter(station -> country == null || country.equalsIgnoreCase("all")
                    || station.getStationCountry().equalsIgnoreCase(country))
            .collect(Collectors.toList());
}
```

**Analysis:**

Iterates through all $K$ stations found in the geographical range with $\mathcal{O}(1)$ filter predicates.

**Temporal complexity of Sub-operation 1.3: $\mathcal{O}(K)$**

---

**Combined Phase 1 Complexity:**

$$\mathcal{O}(1) + \mathcal{O}(\sqrt{P} + K) + \mathcal{O}(K) = \mathcal{O}(\sqrt{P} + K)$$

**Temporal complexity of Phase 1: $\mathcal{O}(\sqrt{P} + K)$ average case**

---

### Final Metrics

| Scenario                          | Temporal Complexity      |
|:----------------------------------|:-------------------------|
| **Best Case**                     | $\mathcal{O}(\log P + K)$ |
| **Average Case**                  | $\mathcal{O}(\sqrt{P} + K)$ |
| **Worst Case**                    | $\mathcal{O}(P + K)$     |

Where:
- $P$ = number of unique coordinate points in the KD-tree
- $K$ = number of stations matching the geographical range and filters

**Temporal Complexity: $\mathcal{O}(\sqrt{P} + K)$**