package com.vallhalatech.shopping_cart.client.product;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(
        name = "product-service"
)
public interface IProductClient {
    @GetMapping("/products/{productId}")
    Object getProductById(@PathVariable("productId") String productId);
}
