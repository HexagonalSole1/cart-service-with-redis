package com.vallhalatech.shopping_cart.service;


import com.vallhalatech.shopping_cart.web.dtos.cart.request.AddItemRequest;
import com.vallhalatech.shopping_cart.web.dtos.response.BaseResponse;

public interface ICartService {
    BaseResponse getCartByUserId(String userId);
    BaseResponse addItemToCart(String userId, AddItemRequest request);
    BaseResponse updateItemQuantity(String userId, String productId, int quantity);
    BaseResponse removeItemFromCart(String userId, String productId);
    BaseResponse clearCart(String userId);
}