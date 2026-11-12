package com.library.book.service.impl;

import com.library.book.dto.BookRequest;
import com.library.book.dto.BookResponse;
import com.library.book.dto.BookSearchCriteria;
import com.library.book.entity.Book;
import com.library.book.mapper.BookMapperImpl;
import com.library.book.repository.BookRepository;
import com.library.common.enums.BookStatus;
import com.library.common.enums.RecStatus;
import com.library.common.exception.BusinessException;
import com.library.common.exception.ResourceNotFoundException;
import com.library.common.response.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    private BookServiceImpl bookService;

    @BeforeEach
    void setUp() {
        bookService = new BookServiceImpl(bookRepository, new BookMapperImpl());
    }

    private static BookRequest request(int amount) {
        return new BookRequest("Clean Code", "Robert Martin", 2008, amount, new BigDecimal("12.50"));
    }

    private static Book book(long id, int amount) {
        Book b = new Book();
        b.setId(id);
        b.setTitle("Clean Code");
        b.setAuthor("Robert Martin");
        b.setYear(2008);
        b.setAmount(amount);
        b.refreshBookStatus();
        return b;
    }

    @Nested
    class Create {

        @Test
        void savesBookAsAvailable() {
            when(bookRepository.findFirstByTitleIgnoreCaseAndAuthorIgnoreCaseAndYear(anyString(), anyString(), any()))
                    .thenReturn(Optional.empty());
            when(bookRepository.save(any(Book.class))).thenAnswer(inv -> {
                Book b = inv.getArgument(0);
                b.setId(1L);
                return b;
            });

            BookResponse response = bookService.create(request(5));

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.amount()).isEqualTo(5);
            assertThat(response.bookStatus()).isEqualTo(BookStatus.AVAILABLE);
        }

        @Test
        void rejectsZeroAmount() {
            assertThatThrownBy(() -> bookService.create(request(0)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("between 1 and 20");
            verify(bookRepository, never()).save(any());
        }

        @Test
        void rejectsDuplicateTitleAuthorYear() {
            when(bookRepository.findFirstByTitleIgnoreCaseAndAuthorIgnoreCaseAndYear("Clean Code", "Robert Martin", 2008))
                    .thenReturn(Optional.of(book(7L, 3)));

            assertThatThrownBy(() -> bookService.create(request(5)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already exists with id 7")
                    .hasMessageContaining("PUT /api/v1/books/7");
            verify(bookRepository, never()).save(any());
        }
    }

    @Nested
    class Update {

        @Test
        void updatesAmountAndDerivesStatus() {
            Book existing = book(1L, 5);
            when(bookRepository.findByIdAndRecStatusForUpdate(1L, RecStatus.ACTIVE)).thenReturn(Optional.of(existing));
            when(bookRepository.findFirstByTitleIgnoreCaseAndAuthorIgnoreCaseAndYear(anyString(), anyString(), any()))
                    .thenReturn(Optional.of(existing));
            when(bookRepository.saveAndFlush(existing)).thenReturn(existing);

            BookResponse response = bookService.update(1L, request(0));

            assertThat(response.amount()).isZero();
            assertThat(response.bookStatus()).isEqualTo(BookStatus.NOT_AVAILABLE);
        }

        @Test
        void rejectsCollisionWithAnotherBook() {
            when(bookRepository.findByIdAndRecStatusForUpdate(1L, RecStatus.ACTIVE)).thenReturn(Optional.of(book(1L, 5)));
            when(bookRepository.findFirstByTitleIgnoreCaseAndAuthorIgnoreCaseAndYear(anyString(), anyString(), any()))
                    .thenReturn(Optional.of(book(2L, 5)));

            assertThatThrownBy(() -> bookService.update(1L, request(5)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("id 2");
        }

        @Test
        void throwsNotFoundForMissingBook() {
            when(bookRepository.findByIdAndRecStatusForUpdate(9L, RecStatus.ACTIVE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookService.update(9L, request(5)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Test
    void deleteSoftDeletes() {
        Book existing = book(1L, 5);
        when(bookRepository.findByIdAndRecStatus(1L, RecStatus.ACTIVE)).thenReturn(Optional.of(existing));

        bookService.delete(1L);

        assertThat(existing.getRecStatus()).isEqualTo(RecStatus.DELETED);
        verify(bookRepository, never()).delete(any(Book.class));
    }

    @Test
    void getByIdThrowsWhenMissing() {
        when(bookRepository.findByIdAndRecStatus(1L, RecStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getById(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchWrapsPageIntoPageResponse() {
        Pageable pageable = PageRequest.of(1, 2);
        when(bookRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(book(3L, 1), book(4L, 0)), pageable, 5));

        PageResponse<BookResponse> page = bookService.search(
                new BookSearchCriteria("clean", null, null, BookStatus.AVAILABLE), pageable);

        assertThat(page.content()).extracting(BookResponse::id).containsExactly(3L, 4L);
        assertThat(page.page()).isEqualTo(1);
        assertThat(page.totalElements()).isEqualTo(5);
        assertThat(page.totalPages()).isEqualTo(3);
        assertThat(page.first()).isFalse();
        assertThat(page.last()).isFalse();
    }

    @Nested
    class Stock {

        @Test
        void decreaseStockDeductsAndMarksUnavailableAtZero() {
            Book existing = book(1L, 2);
            when(bookRepository.findByIdAndRecStatusForUpdate(1L, RecStatus.ACTIVE)).thenReturn(Optional.of(existing));

            Book result = bookService.decreaseStock(1L, 2);

            assertThat(result.getAmount()).isZero();
            assertThat(result.getBookStatus()).isEqualTo(BookStatus.NOT_AVAILABLE);
        }

        @Test
        void decreaseStockRejectsMoreThanAvailable() {
            Book existing = book(1L, 1);
            when(bookRepository.findByIdAndRecStatusForUpdate(1L, RecStatus.ACTIVE)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> bookService.decreaseStock(1L, 2))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("requested 2, available 1");
            assertThat(existing.getAmount()).isEqualTo(1);
        }

        @Test
        void decreaseStockRejectsNonPositiveQuantity() {
            assertThatThrownBy(() -> bookService.decreaseStock(1L, 0)).isInstanceOf(BusinessException.class);
            verify(bookRepository, never()).findByIdAndRecStatusForUpdate(any(), any());
        }

        @Test
        void increaseStockAddsAndMarksAvailable() {
            Book existing = book(1L, 0);
            when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(existing));

            Book result = bookService.increaseStock(1L, 3);

            assertThat(result.getAmount()).isEqualTo(3);
            assertThat(result.getBookStatus()).isEqualTo(BookStatus.AVAILABLE);
        }

        @Test
        void increaseStockRejectsExceedingMaximum() {
            Book existing = book(1L, 19);
            when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> bookService.increaseStock(1L, 2))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("maximum stock of 20");
            assertThat(existing.getAmount()).isEqualTo(19);
        }

        @Test
        void increaseStockWorksForSoftDeletedBook() {
            Book existing = book(1L, 0);
            existing.setRecStatus(RecStatus.DELETED);
            when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(existing));

            assertThat(bookService.increaseStock(1L, 1).getAmount()).isEqualTo(1);
        }
    }
}
