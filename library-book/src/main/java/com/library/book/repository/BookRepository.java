package com.library.book.repository;

import com.library.book.entity.Book;
import com.library.common.enums.RecStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /** Checks all rows (incl. soft-deleted) because the DB unique constraint does too. */
    Optional<Book> findFirstByTitleIgnoreCaseAndAuthorIgnoreCaseAndYear(String title, String author, Integer year);

    Optional<Book> findByIdAndRecStatus(Long id, RecStatus recStatus);

    List<Book> findAllByRecStatusOrderByIdAsc(RecStatus recStatus);
}
