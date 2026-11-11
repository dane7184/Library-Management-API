package com.library.bookreturn.repository;

import com.library.bookreturn.entity.BookReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookReturnRepository extends JpaRepository<BookReturn, Long> {
}
