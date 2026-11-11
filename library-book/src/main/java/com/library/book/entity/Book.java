package com.library.book.entity;

import com.library.common.entity.BaseEntity;
import com.library.common.enums.BookStatus;
import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "book")
public class Book extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(name = "publish_year")
    private Integer year;

    /** Must be between 1 and 20 on create. */
    @Column(nullable = false)
    private Integer amount;

    @Column(precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private BookStatus bookStatus = BookStatus.AVAILABLE;

    /** Keeps bookStatus consistent with stock: AVAILABLE while amount > 0. */
    public void refreshBookStatus() {
        this.bookStatus = (amount != null && amount > 0) ? BookStatus.AVAILABLE : BookStatus.NOT_AVAILABLE;
    }
}
