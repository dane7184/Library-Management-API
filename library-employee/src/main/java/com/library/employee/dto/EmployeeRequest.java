package com.library.employee.dto;

import com.library.common.enums.Gender;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Request body for create / update Employee. */
public record EmployeeRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 50) String position,
        Gender gender,
        @Past LocalDate dateOfBirth,
        @Size(max = 255) String placeOfBirth,
        @Size(max = 255) String address,
        @NotBlank @Size(max = 30) String identityCardNo,
        @Size(max = 20) String phoneNumber,
        @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal salary
) {
}
