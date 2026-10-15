package shop_service;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private Long userId;
    private List<CartItem>items = new ArrayList<>();

    public Cart(){}

    public Cart(Long userId){
        this.userId = userId;
    }
    public double getTax(){
        return getSubtotal() * 0.20;
    }
    public double getSubtotal(){
        return items.stream().mapToDouble(CartItem::getSubtotal).sum();
    }
    public double getTotal(){
        return getSubtotal() + getTax();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }
}
