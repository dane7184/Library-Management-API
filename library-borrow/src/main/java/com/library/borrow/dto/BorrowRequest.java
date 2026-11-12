package com.library.borrow.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Request body for create / update Borrow.
 * borrowDate defaults to today when omitted. On update only employeeId and returnDate may change.
 */
public record BorrowRequest(
        @NotNull Long bookId,
        @NotNull Long customerId,
        @NotNull Long employeeId,
        LocalDate borrowDate,
        @NotNull @FutureOrPresent LocalDate returnDate,
        @NotNull @Min(1) @Max(5) Integer amount
) {
}
