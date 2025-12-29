package freightMngt.domain;

public class PendingFreight {
    private int id;
    private int origin;
    private int destination;
    private Integer trainID;
    private String originName;
    private String destName;

    public PendingFreight(int id, int origin, int destination, Integer trainID, String originName, String destName) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        this.trainID = trainID;
        this.originName = originName;
        this.destName = destName;
    }

    public int getId() { return id; }
    public int getOrigin() { return origin; }
    public int getDestination() { return destination; }
    public Integer getTrainID() { return trainID; }
    public String getOriginName() { return originName; }
    public String getDestName() { return destName; }
}