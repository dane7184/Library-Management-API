package com.library.borrow.dto;

import com.library.common.enums.BorrowStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Response returned to clients for Borrow. */
public record BorrowResponse(
        Long id,
        Long bookId,
        String bookTitle,
        Long customerId,
        String customerName,
        Long employeeId,
        String employeeName,
        LocalDate borrowDate,
        LocalDate returnDate,
        Integer amount,
        Integer owe,
        BorrowStatus borrowStatus,
        LocalDateTime createDate,
        LocalDateTime updateDate
) {
}
