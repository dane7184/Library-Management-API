package com.library.bookreturn.entity;

import com.library.common.entity.BaseEntity;
import com.library.borrow.entity.Borrow;
import com.library.employee.entity.Employee;
import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "book_return")
public class BookReturn extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "borrow_id")
    private Borrow borrow;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(nullable = false)
    private LocalDate returnDate;

    @Column(nullable = false)
    private Integer amount;

    /** $1 per book per day late. */
    @Column(precision = 10, scale = 2)
    private BigDecimal penaltyAmount = BigDecimal.ZERO;
}
