package com.library.borrow.service.impl;

import com.library.borrow.dto.BorrowRequest;
import com.library.borrow.dto.BorrowResponse;
import com.library.borrow.mapper.BorrowMapper;
import com.library.borrow.repository.BorrowRepository;
import com.library.borrow.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business rules (from spec):
 *  - Deduct book amount; set bookStatus NOT_AVAILABLE when amount reaches 0.
 *  - Reject if requested amount > current book amount.
 *  - New customer: max 1 book. Existing customer: max 5 books.
 *  - Block borrowing while the customer has more than 2 books not returned.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BorrowServiceImpl implements BorrowService {

    private final BorrowRepository borrowRepository;
    private final BorrowMapper borrowMapper;

    @Override
    public BorrowResponse create(BorrowRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Transactional(readOnly = true)
    public BorrowResponse getById(Long id) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public BorrowResponse update(Long id, BorrowRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void delete(Long id) {
        // Soft delete: set recStatus = DELETED
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowResponse> getAll() {
        throw new UnsupportedOperationException("TODO");
    }
}
