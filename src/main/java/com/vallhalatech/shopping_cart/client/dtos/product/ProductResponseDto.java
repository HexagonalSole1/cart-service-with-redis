package com.vallhalatech.shopping_cart.client.dtos.product;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductResponseDto {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private Integer stock;
    private Boolean isActive;
}