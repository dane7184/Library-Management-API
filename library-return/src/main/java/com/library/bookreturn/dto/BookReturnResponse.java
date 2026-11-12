package com.library.bookreturn.dto;

import com.library.common.enums.BorrowStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response returned to clients for BookReturn.
 * borrowOwe / borrowStatus are the borrow's current state, not a snapshot at return time.
 */
public record BookReturnResponse(
        Long id,
        Long borrowId,
        Long bookId,
        String bookTitle,
        Long customerId,
        String customerName,
        Long employeeId,
        String employeeName,
        LocalDate dueDate,
        LocalDate returnDate,
        Integer amount,
        BigDecimal penaltyAmount,
        Integer borrowOwe,
        BorrowStatus borrowStatus,
        LocalDateTime createDate,
        LocalDateTime updateDate
) {
}
