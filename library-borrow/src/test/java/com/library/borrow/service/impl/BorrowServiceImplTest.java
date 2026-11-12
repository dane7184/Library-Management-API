package com.library.borrow.service.impl;

import com.library.book.entity.Book;
import com.library.book.service.BookService;
import com.library.borrow.dto.BorrowRequest;
import com.library.borrow.dto.BorrowResponse;
import com.library.borrow.entity.Borrow;
import com.library.borrow.mapper.BorrowMapperImpl;
import com.library.borrow.repository.BorrowRepository;
import com.library.common.enums.BorrowStatus;
import com.library.common.enums.RecStatus;
import com.library.common.exception.BusinessException;
import com.library.common.exception.ResourceNotFoundException;
import com.library.customer.entity.Customer;
import com.library.customer.repository.CustomerRepository;
import com.library.employee.entity.Employee;
import com.library.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class BorrowServiceImplTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 12);

    @Mock private BorrowRepository borrowRepository;
    @Mock private BookService bookService;
    @Mock private CustomerRepository customerRepository;
    @Mock private EmployeeRepository employeeRepository;

    private BorrowServiceImpl borrowService;

    private final Book book = new Book();
    private final Customer customer = new Customer();
    private final Employee employee = new Employee();

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TODAY.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        borrowService = new BorrowServiceImpl(borrowRepository, new BorrowMapperImpl(), bookService,
                customerRepository, employeeRepository, clock);

        book.setId(10L);
        book.setTitle("Clean Code");
        customer.setId(20L);
        customer.setName("Sok");
        employee.setId(30L);
        employee.setName("Dara");
    }

    private static BorrowRequest request(int amount) {
        return new BorrowRequest(10L, 20L, 30L, null, TODAY.plusDays(7), amount);
    }

    private void customerAndEmployeeExist() {
        when(customerRepository.findByIdAndRecStatusForUpdate(20L, RecStatus.ACTIVE)).thenReturn(Optional.of(customer));
        when(employeeRepository.findByIdAndRecStatus(30L, RecStatus.ACTIVE)).thenReturn(Optional.of(employee));
    }

    private Borrow existingBorrow(int amount, int owe, BorrowStatus status) {
        Borrow b = new Borrow();
        b.setId(1L);
        b.setBook(book);
        b.setCustomer(customer);
        b.setEmployee(employee);
        b.setBorrowDate(TODAY.minusDays(10));
        b.setReturnDate(TODAY.minusDays(3));
        b.setAmount(amount);
        b.setOwe(owe);
        b.setBorrowStatus(status);
        return b;
    }

    @Nested
    class Create {

        @Test
        void newCustomerBorrowsOneBook() {
            customerAndEmployeeExist();
            when(borrowRepository.existsByCustomerIdAndRecStatus(20L, RecStatus.ACTIVE)).thenReturn(false);
            when(borrowRepository.sumOutstandingByCustomerId(20L)).thenReturn(0L);
            when(bookService.decreaseStock(10L, 1)).thenReturn(book);
            when(borrowRepository.save(any(Borrow.class))).thenAnswer(inv -> inv.getArgument(0));

            BorrowResponse response = borrowService.create(request(1));

            assertThat(response.bookId()).isEqualTo(10L);
            assertThat(response.bookTitle()).isEqualTo("Clean Code");
            assertThat(response.customerName()).isEqualTo("Sok");
            assertThat(response.employeeName()).isEqualTo("Dara");
            assertThat(response.borrowDate()).isEqualTo(TODAY);
            assertThat(response.amount()).isEqualTo(1);
            assertThat(response.owe()).isEqualTo(1);
            assertThat(response.borrowStatus()).isEqualTo(BorrowStatus.BORROW);
        }

        @Test
        void newCustomerCannotBorrowMoreThanOne() {
            customerAndEmployeeExist();
            when(borrowRepository.existsByCustomerIdAndRecStatus(20L, RecStatus.ACTIVE)).thenReturn(false);

            assertThatThrownBy(() -> borrowService.create(request(2)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("New customers may borrow at most 1");
            verify(bookService, never()).decreaseStock(anyLong(), anyInt());
        }

        @Test
        void existingCustomerMayBorrowFive() {
            customerAndEmployeeExist();
            when(borrowRepository.existsByCustomerIdAndRecStatus(20L, RecStatus.ACTIVE)).thenReturn(true);
            when(borrowRepository.sumOutstandingByCustomerId(20L)).thenReturn(2L);
            when(bookService.decreaseStock(10L, 5)).thenReturn(book);
            when(borrowRepository.save(any(Borrow.class))).thenAnswer(inv -> inv.getArgument(0));

            assertThat(borrowService.create(request(5)).amount()).isEqualTo(5);
        }

        @Test
        void blockedWhenMoreThanTwoBooksOutstanding() {
            customerAndEmployeeExist();
            when(borrowRepository.existsByCustomerIdAndRecStatus(20L, RecStatus.ACTIVE)).thenReturn(true);
            when(borrowRepository.sumOutstandingByCustomerId(20L)).thenReturn(3L);

            assertThatThrownBy(() -> borrowService.create(request(1)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("3 book(s) not returned");
            verify(bookService, never()).decreaseStock(anyLong(), anyInt());
        }

        @Test
        void propagatesInsufficientStock() {
            customerAndEmployeeExist();
            when(borrowRepository.existsByCustomerIdAndRecStatus(20L, RecStatus.ACTIVE)).thenReturn(true);
            when(borrowRepository.sumOutstandingByCustomerId(20L)).thenReturn(0L);
            when(bookService.decreaseStock(10L, 3)).thenThrow(new BusinessException("Not enough copies"));

            assertThatThrownBy(() -> borrowService.create(request(3))).isInstanceOf(BusinessException.class);
            verify(borrowRepository, never()).save(any());
        }

        @Test
        void rejectsReturnDateBeforeBorrowDate() {
            customerAndEmployeeExist();
            BorrowRequest req = new BorrowRequest(10L, 20L, 30L, TODAY.plusDays(5), TODAY.plusDays(1), 1);

            assertThatThrownBy(() -> borrowService.create(req))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("returnDate");
        }

        @Test
        void unknownCustomerIsNotFound() {
            when(customerRepository.findByIdAndRecStatusForUpdate(20L, RecStatus.ACTIVE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> borrowService.create(request(1))).isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class Update {

        @Test
        void extendingDueDateLiftsOverdueStatus() {
            Borrow borrow = existingBorrow(3, 1, BorrowStatus.OVER);
            when(borrowRepository.findByIdAndRecStatus(1L, RecStatus.ACTIVE)).thenReturn(Optional.of(borrow));
            when(borrowRepository.saveAndFlush(borrow)).thenReturn(borrow);

            BorrowResponse response = borrowService.update(1L,
                    new BorrowRequest(10L, 20L, 30L, null, TODAY.plusDays(3), 3));

            assertThat(response.returnDate()).isEqualTo(TODAY.plusDays(3));
            assertThat(response.borrowStatus()).isEqualTo(BorrowStatus.OWE);
        }

        @Test
        void cannotChangeAmount() {
            Borrow borrow = existingBorrow(3, 3, BorrowStatus.BORROW);
            when(borrowRepository.findByIdAndRecStatus(1L, RecStatus.ACTIVE)).thenReturn(Optional.of(borrow));

            assertThatThrownBy(() -> borrowService.update(1L,
                    new BorrowRequest(10L, 20L, 30L, null, TODAY.plusDays(3), 2)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("cannot be changed");
        }

        @Test
        void cannotUpdateFullyReturnedBorrow() {
            Borrow borrow = existingBorrow(3, 0, BorrowStatus.RETURN);
            when(borrowRepository.findByIdAndRecStatus(1L, RecStatus.ACTIVE)).thenReturn(Optional.of(borrow));

            assertThatThrownBy(() -> borrowService.update(1L, request(3)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("fully returned");
        }
    }

    @Nested
    class Delete {

        @Test
        void restocksOwedCopiesAndSoftDeletes() {
            Borrow borrow = existingBorrow(3, 2, BorrowStatus.OWE);
            when(borrowRepository.findByIdAndRecStatusForUpdate(1L, RecStatus.ACTIVE)).thenReturn(Optional.of(borrow));

            borrowService.delete(1L);

            verify(bookService).increaseStock(10L, 2);
            assertThat(borrow.getRecStatus()).isEqualTo(RecStatus.DELETED);
        }

        @Test
        void fullyReturnedBorrowDoesNotRestock() {
            Borrow borrow = existingBorrow(3, 0, BorrowStatus.RETURN);
            when(borrowRepository.findByIdAndRecStatusForUpdate(1L, RecStatus.ACTIVE)).thenReturn(Optional.of(borrow));

            borrowService.delete(1L);

            verify(bookService, never()).increaseStock(anyLong(), anyInt());
            assertThat(borrow.getRecStatus()).isEqualTo(RecStatus.DELETED);
        }
    }
}
