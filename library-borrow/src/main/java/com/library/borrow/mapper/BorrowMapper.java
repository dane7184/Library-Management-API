package com.library.borrow.mapper;

import com.library.borrow.dto.BorrowResponse;
import com.library.borrow.entity.Borrow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Entities are built in the service because they need managed Book/Customer/Employee references. */
@Mapper(componentModel = "spring")
public interface BorrowMapper {

    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "bookTitle", source = "book.title")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.name")
    BorrowResponse toResponse(Borrow entity);
}
