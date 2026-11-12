package com.library.bookreturn.mapper;

import com.library.bookreturn.dto.BookReturnResponse;
import com.library.bookreturn.entity.BookReturn;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Entities are built in the service because they need managed Borrow/Employee references. */
@Mapper(componentModel = "spring")
public interface BookReturnMapper {

    @Mapping(target = "borrowId", source = "borrow.id")
    @Mapping(target = "bookId", source = "borrow.book.id")
    @Mapping(target = "bookTitle", source = "borrow.book.title")
    @Mapping(target = "customerId", source = "borrow.customer.id")
    @Mapping(target = "customerName", source = "borrow.customer.name")
    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.name")
    @Mapping(target = "dueDate", source = "borrow.returnDate")
    @Mapping(target = "borrowOwe", source = "borrow.owe")
    @Mapping(target = "borrowStatus", source = "borrow.borrowStatus")
    BookReturnResponse toResponse(BookReturn entity);
}
