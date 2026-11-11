package com.library.borrow.mapper;

import com.library.borrow.dto.BorrowRequest;
import com.library.borrow.dto.BorrowResponse;
import com.library.borrow.entity.Borrow;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BorrowMapper {
    Borrow toEntity(BorrowRequest request);
    BorrowResponse toResponse(Borrow entity);
}
