# USEI07 — Build a Balanced 2D-Tree on Latitude and Longitude

## Objective

Build a balanced tree to support efficient spatial queries over 64 thousand stations.

To optimize construction, we use a **Bulk Loading** algorithm.

Instead of inserting node by node (which could unbalance), we preprocess the data and build the tree recursively
always dividing by the median.

## Complexity Analysis

Dominated by three phases: grouping, pre-sorting, and recursive construction.

**1. Coordinates Grouping** (unify stations that share the same latitude e longitude)

```java
    public Map<Point2D, List<Station>> groupByCoordinates(List<Station> stationList) {
        Map<Point2D, List<Station>> groupedStations = new LinkedHashMap<>();
    
        for (Station station : stationList) {
                // POINT CREATION
                groupedStations.computeIfAbsent(point, k -> new ArrayList<>()).add(station);
        }
            // ORDER SMALL BUCKETS ALPHABETICALLY
            return groupedStations;
    }
```

**Analysis:** Traverses the list of all ≈64 thousand stations

- Insertion in a `LinkedHashMap` has average complexity of **O(1)**
- Internal sorting of buckets is negligible, as the maximum bucket size is 7, which is **O(1)**

**This phase's complexity: O(m)**

---

**2. Optimized Pre-Sorting** (obtain lists of points sorted by latitude and longitude without using a traditional
O(n log n) sorting algorithm)

```java
    private List<Point2D> sortPoints(...) {
        // USES THE TREES CREATED IN USEI06
        List<Station> stations = latTree.inOrderTraversal();
        
        // LINEAR ITERATION TO FILTER UNIQUE POINTS
        return sortedPoints;
    }
```

**Analysis:**

- An in-order traversal of an existing AVL tree visits each node once: **O(m)**
- Filtering for `uniquePoints` is linear
- If we used `Collections.sort`, the cost would be **O(n log n)**. By using existing trees, we reduce
  this step to linear.

**This phase's complexity: O(m)**


**3. Recursive Tree Construction** (build the tree by dividing the space in half at each level)

```java
    public KDNode buildBalancedTree(...){
    // a) FIND MEDIAN
    int medianIndex = uniquePoints / 2;
    
    // b) DIVIDE LISTS IN LEFT AND RIGHT (LINEAR ITERATION)
    
    // c) RECURSIVE CALLS
    newNode.setLeft(buildBalancedTree(...));
    newNode.setRight(buildBalancedTree(...));
    return newNode;
}
```

**Analysis:** "Divide and Conquer." Let n ≈ 64 thousand.

- At each recursion level, we traverse the entire `latList` and `lonList` to create left and right sublists.
  This partitioning costs **O(n) per level**.
- Since we always divide by the median, the tree height is balanced: **log₂(n)**
- The total work is the sum of work at each tree level.

T(n) = 2T(n/2) + O(n)

Where: 

    - 2T(n/2) : two recursive calls with half the data
    - O(n) : linear cost of partitioning the sorted lists

(=) T(n) = O(n log n)

**This phase's complexity: O(n log n)**

---

### Final Metrics

**Final Analysis:** The complexity is the sum of the phases

**O(m) + O(m) + O(n log n)**

Since m ≈ n (the number of duplicates is small), we simplify to **O(n log n)**.

> **Time: O(n log n)**

Optimized for balanced construction

> **Space: O(n)**

For tree nodes + O(m) to store stations in buckets

> **Tree Height: 15**

(log₂ (61832) ≈ 15.9)









