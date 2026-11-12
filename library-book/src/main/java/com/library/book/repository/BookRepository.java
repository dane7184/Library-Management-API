package com.library.book.repository;

import com.library.book.entity.Book;
import com.library.common.enums.RecStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    /** Checks all rows (incl. soft-deleted) because the DB unique constraint does too. */
    Optional<Book> findFirstByTitleIgnoreCaseAndAuthorIgnoreCaseAndYear(String title, String author, Integer year);

    Optional<Book> findByIdAndRecStatus(Long id, RecStatus recStatus);

    List<Book> findAllByRecStatusOrderByIdAsc(RecStatus recStatus);

    /** Row-locks the book (SELECT ... FOR UPDATE) so concurrent stock changes are serialized. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Book b where b.id = :id and b.recStatus = :recStatus")
    Optional<Book> findByIdAndRecStatusForUpdate(@Param("id") Long id, @Param("recStatus") RecStatus recStatus);

    /** Row-locks the book regardless of recStatus. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Book b where b.id = :id")
    Optional<Book> findByIdForUpdate(@Param("id") Long id);
}
