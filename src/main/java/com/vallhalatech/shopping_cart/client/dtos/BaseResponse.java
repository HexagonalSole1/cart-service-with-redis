package com.vallhalatech.shopping_cart.client.dtos;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class BaseResponse {
    private Object data;
    private String message;
    private Boolean success;
    private HttpStatus httpStatus;

}