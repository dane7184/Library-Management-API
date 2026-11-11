package com.library.book.service;

import com.library.book.dto.BookRequest;
import com.library.book.dto.BookResponse;

import java.util.List;

public interface BookService {
    BookResponse create(BookRequest request);
    BookResponse getById(Long id);
    BookResponse update(Long id, BookRequest request);
    void delete(Long id);
    List<BookResponse> getAll();
}
