package com.vallhalatech.shopping_cart.web.dtos.cart.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddItemRequest {
    private String productId;
    private String productName;
    private String imageUrl;
    private BigDecimal price;
    private int quantity;
}