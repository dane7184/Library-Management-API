package com.library.borrow.service.impl;

import com.library.book.entity.Book;
import com.library.book.service.BookService;
import com.library.borrow.dto.BorrowRequest;
import com.library.borrow.dto.BorrowResponse;
import com.library.borrow.entity.Borrow;
import com.library.borrow.mapper.BorrowMapper;
import com.library.borrow.repository.BorrowRepository;
import com.library.borrow.service.BorrowService;
import com.library.common.enums.BorrowStatus;
import com.library.common.enums.RecStatus;
import com.library.common.exception.BusinessException;
import com.library.common.exception.ResourceNotFoundException;
import com.library.customer.entity.Customer;
import com.library.customer.repository.CustomerRepository;
import com.library.employee.entity.Employee;
import com.library.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Business rules (from spec):
 *  - Deduct book amount; set bookStatus NOT_AVAILABLE when amount reaches 0.
 *  - Reject if requested amount > current book amount.
 *  - New customer: max 1 book. Existing customer: max 5 books.
 *  - Block borrowing while the customer has more than 2 books not returned.
 *
 * Update only changes employee and due date; book, customer and amount are fixed once stock moved
 * (delete the borrow and create a new one instead). Delete puts the still-owed copies back in stock.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BorrowServiceImpl implements BorrowService {

    static final int NEW_CUSTOMER_MAX_BOOKS = 1;
    static final int EXISTING_CUSTOMER_MAX_BOOKS = 5;
    static final int MAX_OUTSTANDING_BOOKS = 2;

    private final BorrowRepository borrowRepository;
    private final BorrowMapper borrowMapper;
    private final BookService bookService;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final Clock clock;

    @Override
    public BorrowResponse create(BorrowRequest request) {
        // Lock the customer first so two concurrent borrows can't both pass the limit checks.
        Customer customer = customerRepository.findByIdAndRecStatusForUpdate(request.customerId(), RecStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.customerId()));
        Employee employee = findEmployee(request.employeeId());

        LocalDate borrowDate = request.borrowDate() != null ? request.borrowDate() : LocalDate.now(clock);
        if (request.returnDate().isBefore(borrowDate)) {
            throw new BusinessException("returnDate must be on or after borrowDate");
        }

        boolean existingCustomer = borrowRepository.existsByCustomerIdAndRecStatus(customer.getId(), RecStatus.ACTIVE);
        int maxBooks = existingCustomer ? EXISTING_CUSTOMER_MAX_BOOKS : NEW_CUSTOMER_MAX_BOOKS;
        if (request.amount() > maxBooks) {
            throw new BusinessException((existingCustomer ? "Existing" : "New") + " customers may borrow at most "
                    + maxBooks + " book(s) at a time");
        }

        long outstanding = borrowRepository.sumOutstandingByCustomerId(customer.getId());
        if (outstanding > MAX_OUTSTANDING_BOOKS) {
            throw new BusinessException("Customer still has " + outstanding
                    + " book(s) not returned; return them before borrowing more");
        }

        Book book = bookService.decreaseStock(request.bookId(), request.amount());

        Borrow borrow = new Borrow();
        borrow.setBook(book);
        borrow.setCustomer(customer);
        borrow.setEmployee(employee);
        borrow.setBorrowDate(borrowDate);
        borrow.setReturnDate(request.returnDate());
        borrow.setAmount(request.amount());
        borrow.setOwe(request.amount());
        borrow.setBorrowStatus(BorrowStatus.BORROW);
        return borrowMapper.toResponse(borrowRepository.save(borrow));
    }

    @Override
    @Transactional(readOnly = true)
    public BorrowResponse getById(Long id) {
        return borrowMapper.toResponse(findActive(id));
    }

    @Override
    public BorrowResponse update(Long id, BorrowRequest request) {
        Borrow borrow = findActive(id);
        if (borrow.getBorrowStatus() == BorrowStatus.RETURN) {
            throw new BusinessException("Borrow " + id + " is already fully returned and cannot be changed");
        }
        if (!Objects.equals(borrow.getBook().getId(), request.bookId())
                || !Objects.equals(borrow.getCustomer().getId(), request.customerId())
                || !Objects.equals(borrow.getAmount(), request.amount())
                || (request.borrowDate() != null && !request.borrowDate().equals(borrow.getBorrowDate()))) {
            throw new BusinessException("book, customer, amount and borrowDate cannot be changed; "
                    + "delete this borrow and create a new one instead");
        }
        if (request.returnDate().isBefore(borrow.getBorrowDate())) {
            throw new BusinessException("returnDate must be on or after borrowDate");
        }

        if (!Objects.equals(borrow.getEmployee().getId(), request.employeeId())) {
            borrow.setEmployee(findEmployee(request.employeeId()));
        }
        borrow.setReturnDate(request.returnDate());
        // An extended due date lifts OVER (returnDate is validated as today or later).
        borrow.refreshBorrowStatus(LocalDate.now(clock));
        return borrowMapper.toResponse(borrowRepository.saveAndFlush(borrow));
    }

    @Override
    public void delete(Long id) {
        // Locked so a concurrent return can't change owe between the read and the restock.
        Borrow borrow = borrowRepository.findByIdAndRecStatusForUpdate(id, RecStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow", id));
        if (borrow.getOwe() > 0) {
            bookService.increaseStock(borrow.getBook().getId(), borrow.getOwe());
        }
        // Soft delete: set recStatus = DELETED
        borrow.setRecStatus(RecStatus.DELETED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowResponse> getAll() {
        return borrowRepository.findAllByRecStatusOrderByIdAsc(RecStatus.ACTIVE).stream()
                .map(borrowMapper::toResponse)
                .toList();
    }

    private Borrow findActive(Long id) {
        return borrowRepository.findByIdAndRecStatus(id, RecStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow", id));
    }

    private Employee findEmployee(Long id) {
        return employeeRepository.findByIdAndRecStatus(id, RecStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
    }
}
