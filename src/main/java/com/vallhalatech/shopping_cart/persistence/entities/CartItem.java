package com.vallhalatech.shopping_cart.persistence.entities;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;


@Data
@Setter
@Getter
@RedisHash
public class CartItem implements Serializable {
    private String productId;
    private String productName;
    private String imageUrl;     // Información básica del producto
    private BigDecimal price;    // Precio al momento de agregar
    private int quantity;
    private Date addedAt;


}