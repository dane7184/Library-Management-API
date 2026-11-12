package com.library.bookreturn.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

/**
 * Request body for create / update BookReturn.
 * returnDate defaults to today when omitted. On update only employeeId and returnDate may change.
 */
public record BookReturnRequest(
        @NotNull Long borrowId,
        @NotNull Long employeeId,
        @PastOrPresent LocalDate returnDate,
        @NotNull @Min(1) Integer amount
) {
}
