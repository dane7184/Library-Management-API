package com.library.book.controller;

import com.library.common.response.ApiResponse;
import com.library.book.dto.BookRequest;
import com.library.book.dto.BookResponse;
import com.library.book.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BookResponse> create(@Valid @RequestBody BookRequest request) {
        return ApiResponse.ok(bookService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<BookResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(bookService.getById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<BookResponse> update(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return ApiResponse.ok(bookService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return ApiResponse.ok("Deleted", null);
    }

    @GetMapping
    public ApiResponse<List<BookResponse>> getAll() {
        return ApiResponse.ok(bookService.getAll());
    }
}
