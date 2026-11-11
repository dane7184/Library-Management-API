package com.library.bookreturn.service.impl;

import com.library.bookreturn.dto.BookReturnRequest;
import com.library.bookreturn.dto.BookReturnResponse;
import com.library.bookreturn.mapper.BookReturnMapper;
import com.library.bookreturn.repository.BookReturnRepository;
import com.library.bookreturn.service.BookReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business rules (from spec):
 *  - A borrow can be returned in several parts; deduct from borrow.owe.
 *  - return < owe  -> OWE;  return == owe -> RETURN;  return > owe -> reject.
 *  - Late: penalty = days late x $1 x books returned.
 *  - Add returned amount back to book; set AVAILABLE when amount > 0.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BookReturnServiceImpl implements BookReturnService {

    private final BookReturnRepository bookReturnRepository;
    private final BookReturnMapper bookReturnMapper;

    @Override
    public BookReturnResponse create(BookReturnRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Transactional(readOnly = true)
    public BookReturnResponse getById(Long id) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public BookReturnResponse update(Long id, BookReturnRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void delete(Long id) {
        // Soft delete: set recStatus = DELETED
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookReturnResponse> getAll() {
        throw new UnsupportedOperationException("TODO");
    }
}
