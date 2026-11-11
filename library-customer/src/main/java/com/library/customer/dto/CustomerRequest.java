package com.library.customer.dto;

import com.library.common.enums.CustomerType;
import com.library.common.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Request body for create / update Customer. */
public record CustomerRequest(
        @NotBlank @Size(max = 100) String name,
        Gender gender,
        @Past LocalDate dateOfBirth,
        @Size(max = 255) String address,
        @NotNull CustomerType customerType,
        @NotBlank @Size(max = 30) String identityCardNo,
        @Size(max = 20) String phoneNumber
) {
}
