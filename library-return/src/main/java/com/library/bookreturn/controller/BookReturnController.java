package com.library.bookreturn.controller;

import com.library.common.response.ApiResponse;
import com.library.bookreturn.dto.BookReturnRequest;
import com.library.bookreturn.dto.BookReturnResponse;
import com.library.bookreturn.service.BookReturnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/returns")
@RequiredArgsConstructor
public class BookReturnController {

    private final BookReturnService bookReturnService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BookReturnResponse> create(@Valid @RequestBody BookReturnRequest request) {
        return ApiResponse.ok(bookReturnService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<BookReturnResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(bookReturnService.getById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<BookReturnResponse> update(@PathVariable Long id, @Valid @RequestBody BookReturnRequest request) {
        return ApiResponse.ok(bookReturnService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        bookReturnService.delete(id);
        return ApiResponse.ok("Deleted", null);
    }

    @GetMapping
    public ApiResponse<List<BookReturnResponse>> getAll() {
        return ApiResponse.ok(bookReturnService.getAll());
    }
}
