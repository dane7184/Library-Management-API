package com.library.book.mapper;

import com.library.book.dto.BookRequest;
import com.library.book.dto.BookResponse;
import com.library.book.entity.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BookMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "recStatus", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "bookStatus", ignore = true)
    Book toEntity(BookRequest request);

    /** Copies request fields onto an existing (managed) entity. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "recStatus", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "bookStatus", ignore = true)
    void updateEntity(BookRequest request, @MappingTarget Book entity);

    BookResponse toResponse(Book entity);
}
