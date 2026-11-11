package com.library.common.exception;

/** Thrown when a business rule from the spec is violated (duplicate ID card, borrow limit, etc.). */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
