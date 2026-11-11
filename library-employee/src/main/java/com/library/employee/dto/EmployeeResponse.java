package com.library.employee.dto;

import com.library.common.enums.Gender;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Response returned to clients for Employee. */
public record EmployeeResponse(
        Long id,
        String name,
        String position,
        Gender gender,
        LocalDate dateOfBirth,
        String placeOfBirth,
        String address,
        String identityCardNo,
        String phoneNumber,
        BigDecimal salary,
        LocalDateTime createDate,
        LocalDateTime updateDate
) {
}
