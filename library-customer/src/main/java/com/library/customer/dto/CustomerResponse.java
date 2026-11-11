package com.library.customer.dto;

import com.library.common.enums.CustomerType;
import com.library.common.enums.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Response returned to clients for Customer. */
public record CustomerResponse(
        Long id,
        String name,
        Gender gender,
        LocalDate dateOfBirth,
        String address,
        CustomerType customerType,
        String identityCardNo,
        String phoneNumber,
        LocalDateTime createDate,
        LocalDateTime updateDate
) {
}
