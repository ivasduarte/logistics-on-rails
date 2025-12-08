package freightMngt.domain;

public class HubMetrics<V> {
    private final V vertex;
    private double degree;
    private double strength;
    private double betweenness;
    private double harmonic;
    private double hubScore;

    public HubMetrics(V vertex, double deg, double str, double betw, double harm, double score) {
        this.vertex = vertex;
        this.degree = deg;
        this.strength = str;
        this.betweenness = betw;
        this.harmonic = harm;
        this.hubScore = score;
    }

    public V getVertex() { return vertex; }
    public double getDegree() { return degree; }
    public double getStrength() { return strength; }
    public double getBetweenness() { return betweenness; }
    public double getHarmonic() { return harmonic; }
    public double getHubScore() { return hubScore; }
}