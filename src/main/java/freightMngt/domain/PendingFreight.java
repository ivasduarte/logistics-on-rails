package freightMngt.domain;

public class PendingFreight {
    private int id;
    private String description;
    private String originName;
    private String destName;

    public PendingFreight(int id, String description, String originName, String destName) {
        this.id = id;
        this.description = description;
        this.originName = originName;
        this.destName = destName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOriginName() {
        return originName;
    }

    public void setOriginName(String originName) {
        this.originName = originName;
    }

    public String getDestName() {
        return destName;
    }

    public void setDestName(String destName) {
        this.destName = destName;
    }
}
