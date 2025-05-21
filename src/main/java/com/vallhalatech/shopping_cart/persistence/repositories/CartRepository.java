package com.vallhalatech.shopping_cart.persistence.repositories;

import com.vallhalatech.shopping_cart.persistence.entities.Cart;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends CrudRepository<Cart, String> {
    Cart findByUserId(String userId);
}
