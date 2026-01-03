# USEI09 - Proximity Search (Nearest-N with Filters)

### Objective

Find the N nearest stations to a target coordinate using a KD-Tree with optional time zone filtering. The algorithm uses Haversine distance for accurate geographic distance calculation and employs branch pruning to minimize tree traversal.

---

### Complexity Analysis

The analysis focuses on **k-NN search using KD-Tree** with a Max-Heap (Priority Queue) for maintaining the k nearest neighbors, along with branch pruning optimization.

---

## Algorithm: k-Nearest Neighbors Search in KD-Tree

**Implementation:** `ProximitySearchController.findNearestStations()`

This algorithm performs efficient k-NN search in a 2D KD-Tree by:
- Using a Max-Heap to track the k nearest neighbors found so far
- Pruning branches that cannot contain closer neighbors
- Applying optional filters (e.g., time zone) during traversal

**Approach:** Recursive tree traversal with branch pruning
- Maintain a Priority Queue (Max-Heap) of size k to store nearest neighbors
- At each node, calculate distance and update the heap if applicable
- Recursively explore the "near" side first (based on splitting dimension)
- Conditionally explore the "far" side only if it might contain closer points
- Use perpendicular distance to the splitting plane for pruning decisions

```java
public Map<String, Object> findNearestStations(double targetLat, double targetLon, 
                                                int n, String timeZoneFilter) {
    // Initialize result containers
    List<Station> resultStations = new ArrayList<>();
    List<Double> resultDistances = new ArrayList<>();

    // Early exit for empty tree or invalid k
    if (kdTree.getRoot() == null || n <= 0) {
        return buildResponse(resultStations, resultDistances, 
                           targetLat, targetLon, n, timeZoneFilter);
    }

    // Max-Heap: stores k nearest neighbors (largest distance at top)
    PriorityQueue<StationDistance> nearestStations = new PriorityQueue<>(
        (a, b) -> {
            int distComp = Double.compare(b.distance, a.distance);
            if (distComp != 0) return distComp;
            return b.station.getStationName().compareTo(a.station.getStationName());
        }
    );

    // Set up filter predicate
    Predicate<Station> filter = timeZoneFilter == null 
        ? s -> true 
        : s -> timeZoneFilter.equalsIgnoreCase(s.getStationTimeZoneGroup());

    // Perform recursive k-NN search
    findNearestNRecursive(kdTree.getRoot(), targetLat, targetLon, 
                         n, nearestStations, filter, true);

    // Extract results from heap and sort
    List<StationDistance> tempList = new ArrayList<>();
    while (!nearestStations.isEmpty()) {
        tempList.add(0, nearestStations.poll());
    }

    tempList.sort((a, b) -> {
        int distComp = Double.compare(a.distance, b.distance);
        if (distComp != 0) return distComp;
        return a.station.getStationName().compareTo(b.station.getStationName());
    });

    for (StationDistance sd : tempList) {
        resultStations.add(sd.station);
        resultDistances.add(sd.distance);
    }

    return buildResponse(resultStations, resultDistances, 
                        targetLat, targetLon, n, timeZoneFilter);
}
```

---

## Phase-by-Phase Analysis

### Phase 1: Initialization and Setup

```java
List<Station> resultStations = new ArrayList<>();       // O(1)
List<Double> resultDistances = new ArrayList<>();       // O(1)

if (kdTree.getRoot() == null || n <= 0) {              // O(1)
    return buildResponse(...);                          // O(1)
}

PriorityQueue<StationDistance> nearestStations = 
    new PriorityQueue<>((a, b) -> {...});               // O(1)

Predicate<Station> filter = timeZoneFilter == null 
    ? s -> true 
    : s -> timeZoneFilter.equalsIgnoreCase(...);        // O(1)
```

**Complexity:** $\mathcal{O}(1)$

---

### Phase 2: Recursive k-NN Search

**Implementation:** `findNearestNRecursive()`

```java
private void findNearestNRecursive(KDNode node, double targetLat, double targetLon, 
                                    int n, PriorityQueue<StationDistance> nearestStations, 
                                    Predicate<Station> filter, boolean divideByLatitude) {
    if (node == null) return;                           // O(1)

    // Calculate distance to current node
    Point2D coords = node.getCoordinates();             // O(1)
    double distance = haversineDistance(targetLat, targetLon, 
                                       coords.getStationLatitude(), 
                                       coords.getStationLongitude());  // O(1)

    // Check all stations at this node
    for (Station station : node.getStationList()) {    // O(s) per node
        if (filter.test(station)) {                    // O(1)
            if (nearestStations.size() < n) {          // O(1)
                nearestStations.offer(new StationDistance(station, distance));  // O(log k)
            } else {
                StationDistance farthest = nearestStations.peek();  // O(1)
                boolean shouldReplace = 
                    distance < farthest.distance || 
                    (distance == farthest.distance && 
                     station.getStationName().compareTo(
                         farthest.station.getStationName()) < 0);    // O(1)

                if (shouldReplace) {
                    nearestStations.poll();            // O(log k)
                    nearestStations.offer(new StationDistance(station, distance));  // O(log k)
                }
            }
        }
    }

    // Determine near and far sides
    double targetValue = divideByLatitude ? targetLat : targetLon;    // O(1)
    double nodeValue = divideByLatitude 
        ? coords.getStationLatitude() 
        : coords.getStationLongitude();                // O(1)

    KDNode nearSide, farSide;
    if (targetValue < nodeValue) {                     // O(1)
        nearSide = node.getLeft();
        farSide = node.getRight();
    } else {
        nearSide = node.getRight();
        farSide = node.getLeft();
    }

    // Recursively search near side
    findNearestNRecursive(nearSide, targetLat, targetLon, 
                         n, nearestStations, filter, !divideByLatitude);

    // Conditionally search far side (pruning)
    if (shouldExploreFarSide(targetLat, targetLon, nodeValue, 
                            divideByLatitude, nearestStations, n)) {  // O(1)
        findNearestNRecursive(farSide, targetLat, targetLon, 
                             n, nearestStations, filter, !divideByLatitude);
    }
}
```

#### Per-Node Operations Analysis

**At each node:**
1. **Distance Calculation:**
    - Haversine distance computation: $\mathcal{O}(1)$

2. **Station Processing:**
    - Let $s$ = number of stations at current node
    - For each station: filter check $\mathcal{O}(1)$ + heap operation $\mathcal{O}(\log k)$
    - **Per node:** $\mathcal{O}(s \log k)$

3. **Determine Split Direction:** $\mathcal{O}(1)$

4. **Recursive Calls:**
    - Near side: Always explored
    - Far side: Conditionally explored (pruning)

**Per-Node Complexity:** $\mathcal{O}(s \log k)$

---

#### Tree Traversal Analysis

**Best Case (Balanced Tree, Effective Pruning):**
- Tree height: $h = \mathcal{O}(\log N)$
- Nodes visited: $\mathcal{O}(\log N)$ (path from root to target region + minimal backtracking)
- Total stations processed: $\mathcal{O}(s \cdot \log N)$

**Complexity:** $\mathcal{O}(s \cdot \log N \cdot \log k)$

Where:
- $N$ = total number of nodes in KD-Tree
- $s$ = average stations per node
- $k$ = number of neighbors requested

---

**Average Case (Well-Constructed Tree):**
- Balanced tree with good spatial distribution
- Pruning eliminates most far-side branches
- Nodes visited: $\mathcal{O}(\log N)$ to $\mathcal{O}(\sqrt{N})$

**Complexity:** $\mathcal{O}(s \cdot \log N \cdot \log k)$ to $\mathcal{O}(s \cdot \sqrt{N} \cdot \log k)$

---

**Worst Case (Degenerate Tree or Poor Pruning):**
- Unbalanced tree (degenerates to linked list): height $h = \mathcal{O}(N)$
- Poor spatial distribution: pruning ineffective
- Must visit all nodes: $\mathcal{O}(N)$

**Complexity:** $\mathcal{O}(s \cdot N \cdot \log k)$

---

### Phase 3: Branch Pruning Decision

**Implementation:** `shouldExploreFarSide()`

```java
private boolean shouldExploreFarSide(double targetLat, double targetLon, 
                                     double nodeValue, boolean divideByLatitude, 
                                     PriorityQueue<StationDistance> nearestStations, 
                                     int n) {
    // If heap not full, must explore all sides
    if (nearestStations.size() < n) {                  // O(1)
        return true;
    }

    // Calculate perpendicular distance to splitting plane
    double perpendicularDistance;
    if (divideByLatitude) {
        perpendicularDistance = Math.abs(targetLat - nodeValue) * 111.0;  // O(1)
    } else {
        double latRad = Math.toRadians(targetLat);     // O(1)
        perpendicularDistance = Math.abs(targetLon - nodeValue) 
                              * 111.0 * Math.cos(latRad);  // O(1)
    }

    // Prune if perpendicular distance exceeds current k-th nearest distance
    return perpendicularDistance < nearestStations.peek().distance;  // O(1)
}
```

**Complexity:** $\mathcal{O}(1)$

**Pruning Effectiveness:**
- If `perpendicularDistance >= nearestStations.peek().distance`: Far side is pruned (no traversal)
- This dramatically reduces nodes visited in practice
- Critical for achieving logarithmic performance in balanced trees

---

### Phase 4: Result Extraction and Sorting

```java
// Extract from Max-Heap (reverse order)
List<StationDistance> tempList = new ArrayList<>();
while (!nearestStations.isEmpty()) {                   // k iterations
    tempList.add(0, nearestStations.poll());          // O(log k) per poll
}

// Sort by distance (ascending) and name (tie-breaker)
tempList.sort((a, b) -> {                             // O(k log k)
    int distComp = Double.compare(a.distance, b.distance);
    if (distComp != 0) return distComp;
    return a.station.getStationName().compareTo(b.station.getStationName());
});

// Transfer to result lists
for (StationDistance sd : tempList) {                 // O(k)
    resultStations.add(sd.station);
    resultDistances.add(sd.distance);
}
```

**Analysis:**
1. **Heap Extraction:** $k$ iterations × $\mathcal{O}(\log k)$ = $\mathcal{O}(k \log k)$
2. **Sorting:** $\mathcal{O}(k \log k)$
3. **List Transfer:** $\mathcal{O}(k)$

**Complexity:** $\mathcal{O}(k \log k)$

---

### Phase 5: Build Response

**Implementation:** `buildResponse()`

```java
private Map<String, Object> buildResponse(List<Station> stations, 
                                          List<Double> distances, 
                                          double targetLat, double targetLon, 
                                          int n, String timeZoneFilter) {
    Map<String, Object> response = new HashMap<>();

    response.put("stations", new ArrayList<>(stations));           // O(k)
    response.put("distances", new ArrayList<>(distances));         // O(k)

    Map<String, Double> targetCoordinates = new HashMap<>();
    targetCoordinates.put("latitude", targetLat);                  // O(1)
    targetCoordinates.put("longitude", targetLon);                 // O(1)
    response.put("targetCoordinates", targetCoordinates);

    Map<String, Integer> searchParameters = new HashMap<>();
    searchParameters.put("requestedNeighbors", n);                 // O(1)
    searchParameters.put("treeHeight", kdTree.getHeight());        // O(1)
    searchParameters.put("resultsFound", stations.size());         // O(1)
    response.put("searchParameters", searchParameters);

    List<String> appliedFilters = new ArrayList<>();
    if (timeZoneFilter != null && !timeZoneFilter.isEmpty()) {    // O(1)
        appliedFilters.add(timeZoneFilter);                        // O(1)
    }
    response.put("appliedFilters", appliedFilters);

    return response;
}
```

**Complexity:** $\mathcal{O}(k)$

---

## Haversine Distance Calculation

**Implementation:** `haversineDistance()`

```java
private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
    double lat1Rad = Math.toRadians(lat1);                         // O(1)
    double lon1Rad = Math.toRadians(lon1);                         // O(1)
    double lat2Rad = Math.toRadians(lat2);                         // O(1)
    double lon2Rad = Math.toRadians(lon2);                         // O(1)

    double dLat = lat2Rad - lat1Rad;                              // O(1)
    double dLon = lon2Rad - lon1Rad;                              // O(1)

    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) 
             + Math.cos(lat1Rad) * Math.cos(lat2Rad) 
             * Math.sin(dLon / 2) * Math.sin(dLon / 2);           // O(1)

    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));   // O(1)

    return EARTH_RADIUS_KM * c;                                   // O(1)
}
```

**Complexity:** $\mathcal{O}(1)$

All trigonometric operations are constant time.

---

## Final Complexity Summary

| Operation | Best Case | Average Case | Worst Case |
|:----------|:----------|:-------------|:-----------|
| **Initialization** | $\mathcal{O}(1)$ | $\mathcal{O}(1)$ | $\mathcal{O}(1)$ |
| **k-NN Search (Tree Traversal)** | $\mathcal{O}(s \cdot \log N \cdot \log k)$ | $\mathcal{O}(s \cdot \sqrt{N} \cdot \log k)$ | $\mathcal{O}(s \cdot N \cdot \log k)$ |
| **Result Extraction & Sorting** | $\mathcal{O}(k \log k)$ | $\mathcal{O}(k \log k)$ | $\mathcal{O}(k \log k)$ |
| **Build Response** | $\mathcal{O}(k)$ | $\mathcal{O}(k)$ | $\mathcal{O}(k)$ |

### Overall USEI09 Complexity

**Best Case (Balanced Tree with Effective Pruning):**
$$\mathcal{O}(s \cdot \log N \cdot \log k + k \log k)$$

Since typically $k \ll N$, this simplifies to:
$$\mathcal{O}(s \cdot \log N \cdot \log k)$$

---

**Average Case (Well-Constructed KD-Tree):**
$$\mathcal{O}(s \cdot \sqrt{N} \cdot \log k + k \log k)$$

For practical applications with balanced trees:
$$\mathcal{O}(s \cdot \log N \cdot \log k)$$ to $$\mathcal{O}(s \cdot \sqrt{N} \cdot \log k)$$

---

**Worst Case (Degenerate Tree):**
$$\mathcal{O}(s \cdot N \cdot \log k + k \log k)$$

Dominated by tree traversal:
$$\mathcal{O}(s \cdot N \cdot \log k)$$

---

## Key Performance Factors

### 1. Tree Balance
- **Balanced tree:** Logarithmic height enables $\mathcal{O}(\log N)$ traversal
- **Unbalanced tree:** Can degrade to linear $\mathcal{O}(N)$ traversal

### 2. Pruning Effectiveness
- **Good spatial distribution:** Most far-side branches pruned
- **Poor distribution:** Pruning ineffective, more nodes visited

### 3. Stations per Node ($s$)
- Affects the constant factor in complexity
- Multiple stations at same coordinates increase per-node processing

### 4. Value of $k$
- Heap operations: $\mathcal{O}(\log k)$ per station
- Result sorting: $\mathcal{O}(k \log k)$
- For small $k$ (typical use case), impact is minimal

### 5. Filter Selectivity
- Time zone filter doesn't change asymptotic complexity
- May reduce actual stations added to heap
- Still requires visiting same nodes (pruning based on distance, not filters)

---

## Space Complexity

**Auxiliary Space:**
- Priority Queue: $\mathcal{O}(k)$ for storing nearest neighbors
- Result lists: $\mathcal{O}(k)$ for stations and distances
- Recursion stack: $\mathcal{O}(h)$ where $h$ is tree height
    - Best/Average: $\mathcal{O}(\log N)$
    - Worst: $\mathcal{O}(N)$

**Total Space Complexity:** $\mathcal{O}(k + h)$

Best case: $\mathcal{O}(k + \log N)$
Worst case: $\mathcal{O}(k + N)$