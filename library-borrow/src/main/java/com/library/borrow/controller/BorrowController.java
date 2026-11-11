package com.library.borrow.controller;

import com.library.common.response.ApiResponse;
import com.library.borrow.dto.BorrowRequest;
import com.library.borrow.dto.BorrowResponse;
import com.library.borrow.service.BorrowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BorrowResponse> create(@Valid @RequestBody BorrowRequest request) {
        return ApiResponse.ok(borrowService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<BorrowResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(borrowService.getById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<BorrowResponse> update(@PathVariable Long id, @Valid @RequestBody BorrowRequest request) {
        return ApiResponse.ok(borrowService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        borrowService.delete(id);
        return ApiResponse.ok("Deleted", null);
    }

    @GetMapping
    public ApiResponse<List<BorrowResponse>> getAll() {
        return ApiResponse.ok(borrowService.getAll());
    }
}
