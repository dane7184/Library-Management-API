package com.library.book.service.impl;

import com.library.book.dto.BookRequest;
import com.library.book.dto.BookResponse;
import com.library.book.dto.BookSearchCriteria;
import com.library.book.entity.Book;
import com.library.book.mapper.BookMapper;
import com.library.book.repository.BookRepository;
import com.library.book.service.BookService;
import com.library.common.enums.RecStatus;
import com.library.common.exception.BusinessException;
import com.library.common.exception.ResourceNotFoundException;
import com.library.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.library.book.specification.BookSpecifications.*;

/**
 * Business rules (from spec):
 *  - If (title, author, year) already exists, reject and suggest updating the amount.
 *  - amount must be >= 1 and <= 20 on create (0..20 afterwards, as borrows reduce it).
 *  - bookStatus is derived from amount.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BookServiceImpl implements BookService {

    private static final int MIN_AMOUNT_ON_CREATE = 1;
    /** Mirrors the DB check constraint on book.amount. */
    static final int MAX_AMOUNT = 20;

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Override
    public BookResponse create(BookRequest request) {
        if (request.amount() < MIN_AMOUNT_ON_CREATE) {
            throw new BusinessException("amount must be between 1 and 20 when creating a book");
        }
        bookRepository.findFirstByTitleIgnoreCaseAndAuthorIgnoreCaseAndYear(request.title(), request.author(), request.year())
                .ifPresent(existing -> {
                    throw new BusinessException("Book '" + existing.getTitle() + "' by " + existing.getAuthor()
                            + " (" + existing.getYear() + ") already exists with id " + existing.getId()
                            + "; update its amount instead (PUT /api/v1/books/" + existing.getId() + ")");
                });

        Book book = bookMapper.toEntity(request);
        book.refreshBookStatus();
        return bookMapper.toResponse(bookRepository.save(book));
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponse getById(Long id) {
        return bookMapper.toResponse(findActive(id));
    }

    @Override
    public BookResponse update(Long id, BookRequest request) {
        // Locked so an admin edit can't overwrite a concurrent borrow/return stock change.
        Book book = findActiveForUpdate(id);
        bookRepository.findFirstByTitleIgnoreCaseAndAuthorIgnoreCaseAndYear(request.title(), request.author(), request.year())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new BusinessException("Another book with the same title, author and year already exists (id "
                            + other.getId() + ")");
                });

        bookMapper.updateEntity(request, book);
        book.refreshBookStatus();
        return bookMapper.toResponse(bookRepository.saveAndFlush(book));
    }

    @Override
    public void delete(Long id) {
        // Soft delete: set recStatus = DELETED
        findActive(id).setRecStatus(RecStatus.DELETED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> getAll() {
        return bookRepository.findAllByRecStatusOrderByIdAsc(RecStatus.ACTIVE).stream()
                .map(bookMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookResponse> search(BookSearchCriteria criteria, Pageable pageable) {
        Specification<Book> spec = Specification.allOf(
                isActive(),
                titleContains(criteria.title()),
                authorContains(criteria.author()),
                yearEquals(criteria.year()),
                statusEquals(criteria.status()));
        return PageResponse.of(bookRepository.findAll(spec, pageable).map(bookMapper::toResponse));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Book decreaseStock(Long bookId, int quantity) {
        requirePositive(quantity);
        Book book = findActiveForUpdate(bookId);
        if (quantity > book.getAmount()) {
            throw new BusinessException("Not enough copies of '" + book.getTitle() + "': requested " + quantity
                    + ", available " + book.getAmount());
        }
        book.setAmount(book.getAmount() - quantity);
        book.refreshBookStatus();
        return book;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Book increaseStock(Long bookId, int quantity) {
        requirePositive(quantity);
        // Not filtered by recStatus: copies of a soft-deleted book can still come back.
        Book book = bookRepository.findByIdForUpdate(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", bookId));
        if (book.getAmount() + quantity > MAX_AMOUNT) {
            throw new BusinessException("Returning " + quantity + " copies of '" + book.getTitle()
                    + "' would exceed the maximum stock of " + MAX_AMOUNT + " (current " + book.getAmount() + ")");
        }
        book.setAmount(book.getAmount() + quantity);
        book.refreshBookStatus();
        return book;
    }

    private static void requirePositive(int quantity) {
        if (quantity < 1) {
            throw new BusinessException("quantity must be at least 1");
        }
    }

    private Book findActive(Long id) {
        return bookRepository.findByIdAndRecStatus(id, RecStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Book", id));
    }

    private Book findActiveForUpdate(Long id) {
        return bookRepository.findByIdAndRecStatusForUpdate(id, RecStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Book", id));
    }
}
