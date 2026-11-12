package com.library.book.service;

import com.library.book.dto.BookRequest;
import com.library.book.dto.BookResponse;
import com.library.book.dto.BookSearchCriteria;
import com.library.book.entity.Book;
import com.library.common.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookService {
    BookResponse create(BookRequest request);
    BookResponse getById(Long id);
    BookResponse update(Long id, BookRequest request);
    void delete(Long id);
    List<BookResponse> getAll();
    PageResponse<BookResponse> search(BookSearchCriteria criteria, Pageable pageable);

    /**
     * Takes {@code quantity} copies out of stock (used by borrow). Locks the book row for the
     * rest of the caller's transaction. Must be called inside a transaction.
     *
     * @return the managed, locked book
     * @throws com.library.common.exception.BusinessException if not enough copies are in stock
     */
    Book decreaseStock(Long bookId, int quantity);

    /**
     * Puts {@code quantity} copies back into stock (used by return / borrow cancellation).
     *
     * @return the managed, locked book
     * @throws com.library.common.exception.BusinessException if stock would exceed the maximum
     */
    Book increaseStock(Long bookId, int quantity);
}
