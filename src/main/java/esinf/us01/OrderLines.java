package esinf.us01;

import java.util.Objects;

public class OrderLines {
    private int lineNo;
    private String sku;
    private int qty;

    public OrderLines(int lineNo, String sku, int qty) {
        this.lineNo = lineNo;
        this.sku = sku;
        this.qty = qty;
    }

    public int getLineNo() {
        return lineNo;
    }

    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderLines)) return false;
        OrderLines that = (OrderLines) o;
        return lineNo == that.lineNo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineNo);
    }

    @Override
    public String toString() {
        return "OrderLine{" +
                "lineNo=" + lineNo +
                ", sku='" + sku + '\'' +
                ", qty=" + qty +
                '}';
    }
}
