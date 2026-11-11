package com.library.book.dto;

import com.library.common.enums.BookStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Response returned to clients for Book. */
public record BookResponse(
        Long id,
        String title,
        String author,
        Integer year,
        Integer amount,
        BigDecimal unitPrice,
        BookStatus bookStatus,
        LocalDateTime createDate,
        LocalDateTime updateDate
) {
}
