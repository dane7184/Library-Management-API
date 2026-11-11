package com.library.borrow.repository;

import com.library.borrow.entity.Borrow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BorrowRepository extends JpaRepository<Borrow, Long> {
    /** Used by the scheduler to find overdue records. */
    java.util.List<Borrow> findByBorrowStatusInAndReturnDateBefore(
            java.util.Collection<com.library.common.enums.BorrowStatus> statuses, java.time.LocalDate date);

    /** New vs. old customer check. */
    boolean existsByCustomerId(Long customerId);
}
