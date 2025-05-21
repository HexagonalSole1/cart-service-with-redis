package com.vallhalatech.shopping_cart.service.impl;

import com.vallhalatech.shopping_cart.client.product.IProductClient;
import com.vallhalatech.shopping_cart.persistence.entities.Cart;
import com.vallhalatech.shopping_cart.persistence.entities.CartItem;
import com.vallhalatech.shopping_cart.persistence.repositories.CartRepository;
import com.vallhalatech.shopping_cart.service.ICartService;
import com.vallhalatech.shopping_cart.web.dtos.cart.request.AddItemRequest;
import com.vallhalatech.shopping_cart.web.dtos.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements ICartService {

    private final CartRepository cartRepository;
    private final IProductClient productClient;

    @Override
    public BaseResponse getCartByUserId(String userId) {
        try {
            Cart cart = findOrCreateCart(userId);
            return BaseResponse.builder()
                    .data(cart)
                    .message("Carrito obtenido correctamente")
                    .success(true)
                    .httpStatus(HttpStatus.OK)
                    .build();
        } catch (Exception e) {
            return BaseResponse.builder()
                    .message("Error al obtener el carrito: " + e.getMessage())
                    .success(false)
                    .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    @Override
    public BaseResponse addItemToCart(String userId, AddItemRequest request) {
        try {
            Cart cart = findOrCreateCart(userId);

            CartItem existingItem = cart.getItem(request.getProductId());
            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
                existingItem.setPrice(request.getPrice());
                existingItem.setProductName(request.getProductName());
                existingItem.setImageUrl(request.getImageUrl());
            } else {
                CartItem newItem = new CartItem();
                newItem.setProductId(request.getProductId());
                newItem.setProductName(request.getProductName());
                newItem.setImageUrl(request.getImageUrl());
                newItem.setPrice(request.getPrice());
                newItem.setQuantity(request.getQuantity());
                newItem.setAddedAt(new Date());

                cart.addItem(newItem);
            }

            cart.setUpdatedAt(new Date());
            Cart updatedCart = cartRepository.save(cart);

            return BaseResponse.builder()
                    .data(updatedCart)
                    .message("Producto agregado al carrito correctamente")
                    .success(true)
                    .httpStatus(HttpStatus.OK)
                    .build();
        } catch (Exception e) {
            return BaseResponse.builder()
                    .message("Error al agregar producto al carrito: " + e.getMessage())
                    .success(false)
                    .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    @Override
    public BaseResponse updateItemQuantity(String userId, String productId, int quantity) {
        try {
            Cart cart = findOrCreateCart(userId);
            CartItem item = cart.getItem(productId);

            if (item == null) {
                return BaseResponse.builder()
                        .message("Producto no encontrado en el carrito")
                        .success(false)
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .build();
            }

            if (quantity <= 0) {
                cart.removeItem(productId);
            } else {
                item.setQuantity(quantity);
            }

            cart.setUpdatedAt(new Date());
            Cart updatedCart = cartRepository.save(cart);

            return BaseResponse.builder()
                    .data(updatedCart)
                    .message("Cantidad actualizada correctamente")
                    .success(true)
                    .httpStatus(HttpStatus.OK)
                    .build();
        } catch (Exception e) {
            return BaseResponse.builder()
                    .message("Error al actualizar cantidad: " + e.getMessage())
                    .success(false)
                    .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    @Override
    public BaseResponse removeItemFromCart(String userId, String productId) {
        try {
            Cart cart = findOrCreateCart(userId);

            if (cart.getItem(productId) == null) {
                return BaseResponse.builder()
                        .message("Producto no encontrado en el carrito")
                        .success(false)
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .build();
            }

            cart.removeItem(productId);
            cart.setUpdatedAt(new Date());
            Cart updatedCart = cartRepository.save(cart);

            return BaseResponse.builder()
                    .data(updatedCart)
                    .message("Producto eliminado del carrito correctamente")
                    .success(true)
                    .httpStatus(HttpStatus.OK)
                    .build();
        } catch (Exception e) {
            return BaseResponse.builder()
                    .message("Error al eliminar producto: " + e.getMessage())
                    .success(false)
                    .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    @Override
    public BaseResponse clearCart(String userId) {
        try {
            Cart cart = findOrCreateCart(userId);
            cart.getItems().clear();
            cart.setUpdatedAt(new Date());
            cartRepository.save(cart);

            return BaseResponse.builder()
                    .message("Carrito vaciado correctamente")
                    .success(true)
                    .httpStatus(HttpStatus.OK)
                    .build();
        } catch (Exception e) {
            return BaseResponse.builder()
                    .message("Error al vaciar carrito: " + e.getMessage())
                    .success(false)
                    .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    private Cart findOrCreateCart(String userId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            cart = new Cart();
            cart.setId(UUID.randomUUID().toString());
            cart.setUserId(userId);
            cart.setCreatedAt(new Date());
            cart.setUpdatedAt(new Date());
            cart = cartRepository.save(cart);
        }
        return cart;
    }
}