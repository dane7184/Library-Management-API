package com.library.bookreturn.mapper;

import com.library.bookreturn.dto.BookReturnRequest;
import com.library.bookreturn.dto.BookReturnResponse;
import com.library.bookreturn.entity.BookReturn;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookReturnMapper {
    BookReturn toEntity(BookReturnRequest request);
    BookReturnResponse toResponse(BookReturn entity);
}
