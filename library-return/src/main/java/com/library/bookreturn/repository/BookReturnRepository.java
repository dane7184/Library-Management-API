package com.library.bookreturn.repository;

import com.library.bookreturn.entity.BookReturn;
import com.library.common.enums.RecStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookReturnRepository extends JpaRepository<BookReturn, Long> {

    @EntityGraph(attributePaths = {"borrow", "borrow.book", "borrow.customer", "employee"})
    Optional<BookReturn> findByIdAndRecStatus(Long id, RecStatus recStatus);

    /** Scalar lookup that doesn't load the Borrow entity, so the caller can lock it fresh afterwards. */
    @Query("select r.borrow.id from BookReturn r where r.id = :id and r.recStatus = :recStatus")
    Optional<Long> findBorrowIdByIdAndRecStatus(@Param("id") Long id, @Param("recStatus") RecStatus recStatus);

    @EntityGraph(attributePaths = {"borrow", "borrow.book", "borrow.customer", "employee"})
    List<BookReturn> findAllByRecStatusOrderByIdAsc(RecStatus recStatus);
}
