# USEI14 - Maximum throughput between two Hubs

## Objective
The goal of USEI14 is to determine the maximum throughput 
between two specified hubs in a railway network.

---

## Complexity Analysis
The maximum flow complexity is primarily determined by the process of finding augmenting paths.
In the worst case, finding a flow path takes $\mathcal{O}(E)$, using DFS algorithm.

Each augmentation path increases the flow by at least 1 unit.
If the maximum flow value is $F$, the total complexity becomes $\mathcal{O}(E \times F)$.