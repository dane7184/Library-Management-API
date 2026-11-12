package com.library.bookreturn.service.impl;

import com.library.book.entity.Book;
import com.library.book.service.BookService;
import com.library.bookreturn.config.PenaltyProperties;
import com.library.bookreturn.dto.BookReturnRequest;
import com.library.bookreturn.dto.BookReturnResponse;
import com.library.bookreturn.entity.BookReturn;
import com.library.bookreturn.mapper.BookReturnMapperImpl;
import com.library.bookreturn.repository.BookReturnRepository;
import com.library.borrow.entity.Borrow;
import com.library.borrow.repository.BorrowRepository;
import com.library.common.enums.BorrowStatus;
import com.library.common.enums.RecStatus;
import com.library.common.exception.BusinessException;
import com.library.common.exception.ResourceNotFoundException;
import com.library.customer.entity.Customer;
import com.library.employee.entity.Employee;
import com.library.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookReturnServiceImplTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 12);

    @Mock private BookReturnRepository bookReturnRepository;
    @Mock private BorrowRepository borrowRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private BookService bookService;

    private BookReturnServiceImpl service;

    private final Book book = new Book();
    private final Customer customer = new Customer();
    private final Employee employee = new Employee();

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TODAY.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        service = new BookReturnServiceImpl(bookReturnRepository, new BookReturnMapperImpl(), borrowRepository,
                employeeRepository, bookService, new PenaltyProperties(new BigDecimal("1.00")), clock);

        book.setId(10L);
        book.setTitle("Clean Code");
        customer.setId(20L);
        customer.setName("Sok");
        employee.setId(30L);
        employee.setName("Dara");
    }

    /** Borrowed 10 days ago, due {@code dueInDays} from today. */
    private Borrow borrow(int amount, int owe, int dueInDays) {
        Borrow b = new Borrow();
        b.setId(1L);
        b.setBook(book);
        b.setCustomer(customer);
        b.setEmployee(employee);
        b.setBorrowDate(TODAY.minusDays(10));
        b.setReturnDate(TODAY.plusDays(dueInDays));
        b.setAmount(amount);
        b.setOwe(owe);
        b.refreshBorrowStatus(TODAY);
        return b;
    }

    private void borrowLocked(Borrow b) {
        when(borrowRepository.findByIdAndRecStatusForUpdate(1L, RecStatus.ACTIVE)).thenReturn(Optional.of(b));
    }

    private void employeeExists() {
        when(employeeRepository.findByIdAndRecStatus(30L, RecStatus.ACTIVE)).thenReturn(Optional.of(employee));
    }

    private void saveReturnsArgument() {
        when(bookReturnRepository.save(any(BookReturn.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private BookReturn existingReturn(Borrow b, int amount, LocalDate returnDate) {
        BookReturn r = new BookReturn();
        r.setId(5L);
        r.setBorrow(b);
        r.setEmployee(employee);
        r.setAmount(amount);
        r.setReturnDate(returnDate);
        return r;
    }

    @Nested
    class Create {

        @Test
        void partialReturnOnTimeLeavesOweAndNoPenalty() {
            Borrow b = borrow(3, 3, 5);
            borrowLocked(b);
            employeeExists();
            saveReturnsArgument();

            BookReturnResponse r = service.create(new BookReturnRequest(1L, 30L, null, 1));

            verify(bookService).increaseStock(10L, 1);
            assertThat(r.returnDate()).isEqualTo(TODAY);
            assertThat(r.amount()).isEqualTo(1);
            assertThat(r.penaltyAmount()).isEqualByComparingTo("0.00");
            assertThat(r.borrowOwe()).isEqualTo(2);
            assertThat(r.borrowStatus()).isEqualTo(BorrowStatus.OWE);
            assertThat(r.bookTitle()).isEqualTo("Clean Code");
            assertThat(r.customerName()).isEqualTo("Sok");
        }

        @Test
        void fullReturnMarksBorrowReturned() {
            Borrow b = borrow(2, 2, 5);
            borrowLocked(b);
            employeeExists();
            saveReturnsArgument();

            BookReturnResponse r = service.create(new BookReturnRequest(1L, 30L, null, 2));

            assertThat(r.borrowOwe()).isZero();
            assertThat(r.borrowStatus()).isEqualTo(BorrowStatus.RETURN);
        }

        @Test
        void lateReturnChargesDaysTimesBooksTimesRate() {
            Borrow b = borrow(3, 3, -4); // due 4 days ago
            borrowLocked(b);
            employeeExists();
            saveReturnsArgument();

            BookReturnResponse r = service.create(new BookReturnRequest(1L, 30L, null, 2));

            assertThat(r.penaltyAmount()).isEqualByComparingTo("8.00"); // 4 days x 2 books x $1
            assertThat(r.borrowStatus()).isEqualTo(BorrowStatus.OVER); // 1 still owed and overdue
        }

        @Test
        void rejectsReturningMoreThanOwed() {
            borrowLocked(borrow(3, 1, 5));
            employeeExists();

            assertThatThrownBy(() -> service.create(new BookReturnRequest(1L, 30L, null, 2)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("only 1 still owed");
            verify(bookService, never()).increaseStock(anyLong(), anyInt());
            verify(bookReturnRepository, never()).save(any());
        }

        @Test
        void rejectsAlreadyFullyReturnedBorrow() {
            borrowLocked(borrow(3, 0, 5));
            employeeExists();

            assertThatThrownBy(() -> service.create(new BookReturnRequest(1L, 30L, null, 1)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already fully returned");
        }

        @Test
        void rejectsReturnDateBeforeBorrowDate() {
            borrowLocked(borrow(3, 3, 5));
            employeeExists();

            assertThatThrownBy(() -> service.create(new BookReturnRequest(1L, 30L, TODAY.minusDays(11), 1)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("borrow date");
        }

        @Test
        void unknownBorrowIsNotFound() {
            when(borrowRepository.findByIdAndRecStatusForUpdate(1L, RecStatus.ACTIVE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(new BookReturnRequest(1L, 30L, null, 1)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class Update {

        @Test
        void changingReturnDateRecomputesPenalty() {
            Borrow b = borrow(3, 2, -2); // due 2 days ago
            BookReturn r = existingReturn(b, 1, TODAY.minusDays(2));
            when(bookReturnRepository.findByIdAndRecStatus(5L, RecStatus.ACTIVE)).thenReturn(Optional.of(r));
            when(bookReturnRepository.saveAndFlush(r)).thenReturn(r);

            BookReturnResponse response = service.update(5L, new BookReturnRequest(1L, 30L, TODAY, 1));

            assertThat(response.penaltyAmount()).isEqualByComparingTo("2.00");
        }

        @Test
        void cannotChangeAmount() {
            Borrow b = borrow(3, 2, 5);
            when(bookReturnRepository.findByIdAndRecStatus(5L, RecStatus.ACTIVE))
                    .thenReturn(Optional.of(existingReturn(b, 1, TODAY)));

            assertThatThrownBy(() -> service.update(5L, new BookReturnRequest(1L, 30L, TODAY, 2)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("cannot be changed");
        }
    }

    @Nested
    class Delete {

        @Test
        void reversesReturnAndSoftDeletes() {
            Borrow b = borrow(3, 0, 5);
            BookReturn r = existingReturn(b, 3, TODAY);
            when(bookReturnRepository.findBorrowIdByIdAndRecStatus(5L, RecStatus.ACTIVE)).thenReturn(Optional.of(1L));
            borrowLocked(b);
            when(bookReturnRepository.findByIdAndRecStatus(5L, RecStatus.ACTIVE)).thenReturn(Optional.of(r));

            service.delete(5L);

            verify(bookService).decreaseStock(10L, 3);
            assertThat(b.getOwe()).isEqualTo(3);
            assertThat(b.getBorrowStatus()).isEqualTo(BorrowStatus.BORROW);
            assertThat(r.getRecStatus()).isEqualTo(RecStatus.DELETED);
        }

        @Test
        void rejectsWhenBorrowWasDeleted() {
            when(bookReturnRepository.findBorrowIdByIdAndRecStatus(5L, RecStatus.ACTIVE)).thenReturn(Optional.of(1L));
            when(borrowRepository.findByIdAndRecStatusForUpdate(1L, RecStatus.ACTIVE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete(5L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("deleted borrow");
            verify(bookService, never()).decreaseStock(anyLong(), anyInt());
        }
    }

    @Test
    void penaltyIsZeroWhenEarly() {
        assertThat(service.penalty(TODAY, TODAY.minusDays(3), 5)).isEqualByComparingTo("0.00");
    }
}
