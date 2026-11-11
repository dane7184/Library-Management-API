package com.library.borrow.service;

import com.library.borrow.dto.BorrowRequest;
import com.library.borrow.dto.BorrowResponse;

import java.util.List;

public interface BorrowService {
    BorrowResponse create(BorrowRequest request);
    BorrowResponse getById(Long id);
    BorrowResponse update(Long id, BorrowRequest request);
    void delete(Long id);
    List<BorrowResponse> getAll();
}
