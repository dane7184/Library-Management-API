package com.library.book.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request body for create / update Book.
 * amount accepts 0..20 here (matches the DB check); create additionally requires >= 1 in the service.
 */
public record BookRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 255) String author,
        @Positive Integer year,
        @NotNull @Min(0) @Max(20) Integer amount,
        @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal unitPrice
) {
}
