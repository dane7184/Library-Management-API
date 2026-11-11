package com.library.borrow.entity;

import com.library.common.entity.BaseEntity;
import com.library.book.entity.Book;
import com.library.common.enums.BorrowStatus;
import com.library.customer.entity.Customer;
import com.library.employee.entity.Employee;
import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "borrow")
public class Borrow extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    /** The borrower (spec: "Student"; covers both STUDENT and TEACHER customers). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(nullable = false)
    private LocalDate borrowDate;

    /** Due date. */
    @Column(nullable = false)
    private LocalDate returnDate;

    @Column(nullable = false)
    private Integer amount;

    /** Books still not returned. */
    @Column(nullable = false)
    private Integer owe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private BorrowStatus borrowStatus = BorrowStatus.BORROW;
}
