package com.library.bookreturn.service;

import com.library.bookreturn.dto.BookReturnRequest;
import com.library.bookreturn.dto.BookReturnResponse;

import java.util.List;

public interface BookReturnService {
    BookReturnResponse create(BookReturnRequest request);
    BookReturnResponse getById(Long id);
    BookReturnResponse update(Long id, BookReturnRequest request);
    void delete(Long id);
    List<BookReturnResponse> getAll();
}
