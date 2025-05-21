package com.vallhalatech.shopping_cart.web.controller;

import com.vallhalatech.shopping_cart.service.ICartService;
import com.vallhalatech.shopping_cart.web.dtos.cart.request.AddItemRequest;
import com.vallhalatech.shopping_cart.web.dtos.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final ICartService cartService;

    // Obtener el carrito de un usuario
    @GetMapping("/{userId}")
    public ResponseEntity<BaseResponse> getCart(@PathVariable String userId) {
        BaseResponse response = cartService.getCartByUserId(userId);
        return response.buildResponseEntity();
    }

    // Agregar un producto al carrito
    @PostMapping("/{userId}/items")
    public ResponseEntity<BaseResponse> addItemToCart(
            @PathVariable String userId,
            @RequestBody AddItemRequest request) {
        BaseResponse response = cartService.addItemToCart(userId, request);
        return response.buildResponseEntity();
    }

    // Actualizar la cantidad de un producto en el carrito
    @PutMapping("/{userId}/items/{productId}")
    public ResponseEntity<BaseResponse> updateItemQuantity(
            @PathVariable String userId,
            @PathVariable String productId,
            @RequestParam int quantity) {
        BaseResponse response = cartService.updateItemQuantity(userId, productId, quantity);
        return response.buildResponseEntity();
    }

    // Eliminar un producto del carrito
    @DeleteMapping("/{userId}/items/{productId}")
    public ResponseEntity<BaseResponse> removeItemFromCart(
            @PathVariable String userId,
            @PathVariable String productId) {
        BaseResponse response = cartService.removeItemFromCart(userId, productId);
        return response.buildResponseEntity();
    }

    // Vaciar el carrito
    @DeleteMapping("/{userId}")
    public ResponseEntity<BaseResponse> clearCart(@PathVariable String userId) {
        BaseResponse response = cartService.clearCart(userId);
        return response.buildResponseEntity();
    }
}