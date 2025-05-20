package iuh.fit.se.cartservice.entities;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Cart implements Serializable {
    private Map<Long, Integer> items = new HashMap<>();

    public void addItem(Long productId, int quantity) {
        items.put(productId, items.getOrDefault(productId, 0) + quantity);
    }
    public void updateItem(Long productId, int quantity) {
        if (items.containsKey(productId)) {
            items.put(productId, quantity);
        }
    }

    public void removeItem(Long productId) {
        items.remove(productId);
    }

    public Map<Long, Integer> getItems() {
        return items;
    }

    public void setItems(Map<Long, Integer> items) {
        this.items = items;
    }
}
