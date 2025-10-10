package esinf.us01;

import java.util.Objects;

public class Item {
    private String sku;
    private String itemName;
    private String itemCategory;
    private String unit;
    private double weight;
    private double unitWeight;

    public Item(String sku, String itemName, String itemCategory, String unit, double weight, double unitWeight) {
        this.sku = sku;
        this.itemName = itemName;
        this.itemCategory = itemCategory;
        this.unit = unit;
        this.weight = weight;
        this.unitWeight = unitWeight;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemCategory() {
        return itemCategory;
    }

    public void setItemCategory(String itemCategory) {
        this.itemCategory = itemCategory;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getUnitWeight() {
        return unitWeight;
    }

    public void setUnitWeight(double unitWeight) {
        this.unitWeight = unitWeight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item item = (Item) o;
        return Objects.equals(sku, item.sku);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sku);
    }

    @Override
    public String toString() {
        return "Item{" +
                "sku='" + sku + '\'' +
                ", itemName='" + itemName + '\'' +
                ", itemCategory='" + itemCategory + '\'' +
                ", unit='" + unit + '\'' +
                ", weight=" + weight +
                ", unitWeight=" + unitWeight +
                '}';
    }
}
