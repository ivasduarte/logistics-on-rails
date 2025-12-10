package freightMngt.graph;

import java.util.Objects;

/**
 *
 * @param <V> - Vertex value type
 * @param <E> - Edge value type
 */
public class Edge<V, E> {
    private final V vOrigin;
    private final V vDestination;
    private E weight;

    /**
     * Complete constructor.
     * @param vOrigin   origin vertice
     * @param vDestination  destination vertice
     * @param weight    edge weight
     */
    public Edge(V vOrigin, V vDestination, E weight) {
        if(vOrigin == null || vDestination == null) throw new RuntimeException("Edges vertices cannot be null");
        this.vOrigin = vOrigin;
        this.vDestination = vDestination;
        this.weight = weight;
    }

    public V getVOrigin() {
        return vOrigin;
    }

    public V getVTarget() {
        return vDestination;
    }

    public E getWeight() {
        return weight;
    }

    public void setWeight(E weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return String.format("%s -> %s \nWeight: %s", vOrigin, vDestination, weight);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        @SuppressWarnings("Unchecked") Edge<V, E> edge = (Edge<V, E>) o;
        return vOrigin.equals(edge.vOrigin) &&
                vDestination.equals(edge.vDestination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vOrigin, vDestination);
    }
}
