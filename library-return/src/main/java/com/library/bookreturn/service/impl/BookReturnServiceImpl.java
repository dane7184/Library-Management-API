package com.library.bookreturn.service.impl;

import com.library.book.service.BookService;
import com.library.bookreturn.config.PenaltyProperties;
import com.library.bookreturn.dto.BookReturnRequest;
import com.library.bookreturn.dto.BookReturnResponse;
import com.library.bookreturn.entity.BookReturn;
import com.library.bookreturn.mapper.BookReturnMapper;
import com.library.bookreturn.repository.BookReturnRepository;
import com.library.bookreturn.service.BookReturnService;
import com.library.borrow.entity.Borrow;
import com.library.borrow.repository.BorrowRepository;
import com.library.common.enums.RecStatus;
import com.library.common.exception.BusinessException;
import com.library.common.exception.ResourceNotFoundException;
import com.library.employee.entity.Employee;
import com.library.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * Business rules (from spec):
 *  - A borrow can be returned in several parts; deduct from borrow.owe.
 *  - return < owe  -> OWE;  return == owe -> RETURN;  return > owe -> reject.
 *  - Late: penalty = days late x $1 x books returned.
 *  - Add returned amount back to book; set AVAILABLE when amount > 0.
 *
 * A partial return on a borrow that is already past its due date leaves it OVER rather than OWE,
 * since the overdue scheduler would flip it back to OVER anyway.
 *
 * Update only changes employee and returnDate (penalty is recomputed); borrow and amount are fixed
 * (delete the return and create a new one instead). Delete reverses the return: the copies go
 * back to the borrow's owe and are taken out of stock again.
 *
 * Lock order is always borrow row, then book row (same as borrow delete) to avoid deadlocks.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BookReturnServiceImpl implements BookReturnService {

    private final BookReturnRepository bookReturnRepository;
    private final BookReturnMapper bookReturnMapper;
    private final BorrowRepository borrowRepository;
    private final EmployeeRepository employeeRepository;
    private final BookService bookService;
    private final PenaltyProperties penaltyProperties;
    private final Clock clock;

    @Override
    public BookReturnResponse create(BookReturnRequest request) {
        Borrow borrow = borrowRepository.findByIdAndRecStatusForUpdate(request.borrowId(), RecStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow", request.borrowId()));
        Employee employee = findEmployee(request.employeeId());

        if (borrow.getOwe() == 0) {
            throw new BusinessException("Borrow " + borrow.getId() + " is already fully returned");
        }
        if (request.amount() > borrow.getOwe()) {
            throw new BusinessException("Cannot return " + request.amount() + " book(s); only "
                    + borrow.getOwe() + " still owed on borrow " + borrow.getId());
        }
        LocalDate returnDate = request.returnDate() != null ? request.returnDate() : today();
        requireNotBeforeBorrowDate(returnDate, borrow);

        bookService.increaseStock(borrow.getBook().getId(), request.amount());
        borrow.setOwe(borrow.getOwe() - request.amount());
        borrow.refreshBorrowStatus(today());

        BookReturn bookReturn = new BookReturn();
        bookReturn.setBorrow(borrow);
        bookReturn.setEmployee(employee);
        bookReturn.setReturnDate(returnDate);
        bookReturn.setAmount(request.amount());
        bookReturn.setPenaltyAmount(penalty(borrow.getReturnDate(), returnDate, request.amount()));
        return bookReturnMapper.toResponse(bookReturnRepository.save(bookReturn));
    }

    @Override
    @Transactional(readOnly = true)
    public BookReturnResponse getById(Long id) {
        return bookReturnMapper.toResponse(findActive(id));
    }

    @Override
    public BookReturnResponse update(Long id, BookReturnRequest request) {
        BookReturn bookReturn = findActive(id);
        Borrow borrow = bookReturn.getBorrow();
        if (!Objects.equals(borrow.getId(), request.borrowId())
                || !Objects.equals(bookReturn.getAmount(), request.amount())) {
            throw new BusinessException("borrow and amount cannot be changed; "
                    + "delete this return and create a new one instead");
        }
        LocalDate returnDate = request.returnDate() != null ? request.returnDate() : bookReturn.getReturnDate();
        requireNotBeforeBorrowDate(returnDate, borrow);

        if (!Objects.equals(bookReturn.getEmployee().getId(), request.employeeId())) {
            bookReturn.setEmployee(findEmployee(request.employeeId()));
        }
        bookReturn.setReturnDate(returnDate);
        bookReturn.setPenaltyAmount(penalty(borrow.getReturnDate(), returnDate, bookReturn.getAmount()));
        return bookReturnMapper.toResponse(bookReturnRepository.saveAndFlush(bookReturn));
    }

    @Override
    public void delete(Long id) {
        // Lock the borrow before loading the return, so the borrow we work on is fresh and locked.
        Long borrowId = bookReturnRepository.findBorrowIdByIdAndRecStatus(id, RecStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("BookReturn", id));
        Borrow borrow = borrowRepository.findByIdAndRecStatusForUpdate(borrowId, RecStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("Return " + id + " belongs to deleted borrow "
                        + borrowId + " and cannot be reversed"));
        BookReturn bookReturn = findActive(id);

        // Fails with a clear message if those copies have since been lent out again.
        bookService.decreaseStock(borrow.getBook().getId(), bookReturn.getAmount());
        borrow.setOwe(borrow.getOwe() + bookReturn.getAmount());
        borrow.refreshBorrowStatus(today());

        // Soft delete: set recStatus = DELETED
        bookReturn.setRecStatus(RecStatus.DELETED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookReturnResponse> getAll() {
        return bookReturnRepository.findAllByRecStatusOrderByIdAsc(RecStatus.ACTIVE).stream()
                .map(bookReturnMapper::toResponse)
                .toList();
    }

    /** days late x rate x books; zero when returned on or before the due date. */
    BigDecimal penalty(LocalDate dueDate, LocalDate returnDate, int books) {
        long daysLate = Math.max(0, ChronoUnit.DAYS.between(dueDate, returnDate));
        return penaltyProperties.perBookPerDay()
                .multiply(BigDecimal.valueOf(daysLate))
                .multiply(BigDecimal.valueOf(books))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private static void requireNotBeforeBorrowDate(LocalDate returnDate, Borrow borrow) {
        if (returnDate.isBefore(borrow.getBorrowDate())) {
            throw new BusinessException("returnDate must be on or after the borrow date " + borrow.getBorrowDate());
        }
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }

    private BookReturn findActive(Long id) {
        return bookReturnRepository.findByIdAndRecStatus(id, RecStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("BookReturn", id));
    }

    private Employee findEmployee(Long id) {
        return employeeRepository.findByIdAndRecStatus(id, RecStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
    }
}
