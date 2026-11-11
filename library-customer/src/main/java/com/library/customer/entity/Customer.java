package com.library.customer.entity;

import com.library.common.entity.BaseEntity;
import com.library.common.enums.CustomerType;
import com.library.common.enums.Gender;
import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "customer")
public class Customer extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    private LocalDate dateOfBirth;
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CustomerType customerType;

    @Column(name = "identity_card_no", nullable = false, unique = true, length = 30)
    private String identityCardNo;

    @Column(length = 20)
    private String phoneNumber;
}
