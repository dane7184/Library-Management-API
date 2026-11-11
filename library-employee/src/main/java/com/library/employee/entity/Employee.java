package com.library.employee.entity;

import com.library.common.entity.BaseEntity;
import com.library.common.enums.Gender;
import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "employee")
public class Employee extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String position;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    private LocalDate dateOfBirth;
    private String placeOfBirth;
    private String address;

    @Column(name = "identity_card_no", nullable = false, unique = true, length = 30)
    private String identityCardNo;

    @Column(length = 20)
    private String phoneNumber;

    @Column(precision = 12, scale = 2)
    private BigDecimal salary;
}
