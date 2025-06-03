package com.vallhalatech.shopping_cart.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vallhalatech.shopping_cart.client.product.IProductClient;
import com.vallhalatech.shopping_cart.persistence.entities.Cart;
import com.vallhalatech.shopping_cart.persistence.entities.CartItem;
import com.vallhalatech.shopping_cart.persistence.repositories.CartRepository;
import com.vallhalatech.shopping_cart.service.ICartService;
import com.vallhalatech.shopping_cart.web.dtos.cart.request.AddItemRequest;
import com.vallhalatech.shopping_cart.web.dtos.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements ICartService {

    private final CartRepository cartRepository;
    private final IProductClient productClient;
    private final ObjectMapper objectMapper;

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
            log.error("Error al obtener el carrito para el usuario {}: {}", userId, e.getMessage(), e);
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
            if (request.getProductId() == null) {
                return BaseResponse.builder()
                        .message("El ID del producto es requerido")
                        .success(false)
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .build();
            }

            try {
                // Convertir ID de String a Long para llamar al servicio de productos
                Long productId = Long.parseLong(request.getProductId());
                ResponseEntity<com.vallhalatech.shopping_cart.client.dtos.BaseResponse> productResponse =
                        productClient.getProductById(productId);

                if (!productResponse.getStatusCode().is2xxSuccessful() ||
                        productResponse.getBody() == null ||
                        !Boolean.TRUE.equals(productResponse.getBody().getSuccess())) {

                    log.warn("Producto no encontrado o no disponible: ID {}", request.getProductId());

                    return BaseResponse.builder()
                            .message("El producto no existe o no está disponible")
                            .success(false)
                            .httpStatus(HttpStatus.BAD_REQUEST)
                            .build();
                }

                Map<String, Object> productData = objectMapper.convertValue(
                        productResponse.getBody().getData(),
                        HashMap.class
                );

                Integer availableStock = (Integer) productData.get("stock");
                if (availableStock != null && availableStock < request.getQuantity()) {
                    return BaseResponse.builder()
                            .message("Stock insuficiente. Stock disponible: " + availableStock)
                            .success(false)
                            .httpStatus(HttpStatus.BAD_REQUEST)
                            .build();
                }

                // Opcional: Actualizar información del producto desde el servicio
                if (productData.get("name") != null) {
                    request.setProductName((String) productData.get("name"));
                }
                if (productData.get("imageUrl") != null) {
                    request.setImageUrl((String) productData.get("imageUrl"));
                }
                if (productData.get("price") != null) {
                    request.setPrice(objectMapper.convertValue(productData.get("price"), java.math.BigDecimal.class));
                }

            } catch (NumberFormatException e) {
                log.error("ID de producto inválido: {}", request.getProductId(), e);
                return BaseResponse.builder()
                        .message("ID de producto inválido")
                        .success(false)
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .build();
            } catch (Exception e) {
                log.error("Error al validar producto con ID {}: {}", request.getProductId(), e.getMessage(), e);
                // Decide si quieres continuar o retornar un error según tu política de negocio
                // Si quieres ser estricto, descomenta las siguientes líneas:
                /*
                return BaseResponse.builder()
                        .message("Error al validar el producto: " + e.getMessage())
                        .success(false)
                        .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build();
                */
            }

            // Continuar con la lógica actual de agregar al carrito
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

            log.info("Producto agregado al carrito: userID={}, productID={}, cantidad={}",
                    userId, request.getProductId(), request.getQuantity());

            return BaseResponse.builder()
                    .data(updatedCart)
                    .message("Producto agregado al carrito correctamente")
                    .success(true)
                    .httpStatus(HttpStatus.OK)
                    .build();
        } catch (Exception e) {
            log.error("Error al agregar producto al carrito: userID={}, productID={}, error={}",
                    userId, request.getProductId(), e.getMessage(), e);

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

            // Si la cantidad está aumentando, verificar el stock disponible
            if (quantity > item.getQuantity()) {
                try {
                    Long productIdLong = Long.parseLong(productId);
                    ResponseEntity<com.vallhalatech.shopping_cart.client.dtos.BaseResponse> productResponse =
                            productClient.getProductById(productIdLong);

                    if (productResponse.getStatusCode().is2xxSuccessful() &&
                            productResponse.getBody() != null &&
                            Boolean.TRUE.equals(productResponse.getBody().getSuccess())) {

                        Map<String, Object> productData = objectMapper.convertValue(
                                productResponse.getBody().getData(),
                                HashMap.class
                        );

                        Integer availableStock = (Integer) productData.get("stock");
                        int additionalQuantity = quantity - item.getQuantity();

                        if (availableStock != null && availableStock < additionalQuantity) {
                            return BaseResponse.builder()
                                    .message("Stock insuficiente. Stock disponible: " + availableStock)
                                    .success(false)
                                    .httpStatus(HttpStatus.BAD_REQUEST)
                                    .build();
                        }
                    }
                } catch (Exception e) {
                    log.error("Error validando stock para producto ID {}: {}", productId, e.getMessage(), e);
                    // Decide si quieres continuar o retornar un error según tu política de negocio
                }
            }

            if (quantity <= 0) {
                cart.removeItem(productId);
                log.info("Producto eliminado del carrito por cantidad cero: userID={}, productID={}", userId, productId);
            } else {
                item.setQuantity(quantity);
                log.info("Cantidad actualizada en carrito: userID={}, productID={}, nuevaCantidad={}",
                        userId, productId, quantity);
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
            log.error("Error al actualizar cantidad: userID={}, productID={}, error={}",
                    userId, productId, e.getMessage(), e);

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

            log.info("Producto eliminado del carrito: userID={}, productID={}", userId, productId);

            return BaseResponse.builder()
                    .data(updatedCart)
                    .message("Producto eliminado del carrito correctamente")
                    .success(true)
                    .httpStatus(HttpStatus.OK)
                    .build();
        } catch (Exception e) {
            log.error("Error al eliminar producto: userID={}, productID={}, error={}",
                    userId, productId, e.getMessage(), e);

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

            log.info("Carrito vaciado: userID={}", userId);

            return BaseResponse.builder()
                    .message("Carrito vaciado correctamente")
                    .success(true)
                    .httpStatus(HttpStatus.OK)
                    .build();
        } catch (Exception e) {
            log.error("Error al vaciar carrito: userID={}, error={}", userId, e.getMessage(), e);

            return BaseResponse.builder()
                    .message("Error al vaciar carrito: " + e.getMessage())
                    .success(false)
                    .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
    private Cart findOrCreateCart(String userId) {
        log.info("Buscando carrito para usuario: {}", userId);
        Cart cart = cartRepository.findByUserId(userId);

        if (cart == null) {
            log.info("No se encontró carrito existente para usuario: {}, creando uno nuevo", userId);
            cart = new Cart();
            cart.setId(UUID.randomUUID().toString());
            cart.setUserId(userId);
            cart.setCreatedAt(new Date());
            cart.setUpdatedAt(new Date());
            cart = cartRepository.save(cart);
            log.info("Carrito nuevo creado: ID={}, userID={}", cart.getId(), userId);
        } else {
            log.info("Carrito existente encontrado: ID={}, userID={}, items={}",
                    cart.getId(), userId, cart.getItems().size());
        }
        return cart;
    }
}