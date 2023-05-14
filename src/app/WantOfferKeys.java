package app;

public class WantOfferKeys {

    private String items;
    private int quantity;
    private Float price;

    public WantOfferKeys(String items, int quantity, float price) {
        this.items = items;
        this.quantity = quantity;
        this.price = price;
    }

    public String getItems() {
        return items;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    @Override
    public String toString(){
        return "WantOfferKeys{" +
                "items=" + items +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }

    
    
}
