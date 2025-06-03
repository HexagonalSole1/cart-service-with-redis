package com.vallhalatech.shopping_cart.client.product;

import com.vallhalatech.shopping_cart.client.dtos.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "product-service"
)
public interface IProductClient {
    @GetMapping("/products/{id}")
    ResponseEntity<BaseResponse> getProductById(@PathVariable("id") Long id);
}