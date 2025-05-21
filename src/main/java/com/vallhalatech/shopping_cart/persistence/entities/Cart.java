package com.vallhalatech.shopping_cart.persistence.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
@RedisHash("cart")
public class Cart implements Serializable {
    @Id
    private String id;
    private String userId;
    private Map<String, CartItem> items = new HashMap<>();
    private Date createdAt;
    private Date updatedAt;

    public void addItem(CartItem item) {
        items.put(item.getProductId(), item);
        this.updatedAt = new Date();
    }

    public void removeItem(String productId) {
        items.remove(productId);
        this.updatedAt = new Date();
    }

    public CartItem getItem(String productId) {
        return items.get(productId);
    }
}