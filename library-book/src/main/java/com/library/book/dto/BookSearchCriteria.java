package com.library.book.dto;

import com.library.common.enums.BookStatus;

/** Optional filters for GET /api/v1/books/search; every field may be null. */
public record BookSearchCriteria(
        String title,
        String author,
        Integer year,
        BookStatus status
) {
}
