package com.library.borrow.repository;

import com.library.borrow.entity.Borrow;
import com.library.common.enums.BorrowStatus;
import com.library.common.enums.RecStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowRepository extends JpaRepository<Borrow, Long> {

    /** Used by the scheduler to find overdue records. */
    List<Borrow> findByBorrowStatusInAndReturnDateBefore(Collection<BorrowStatus> statuses, LocalDate date);

    /** New vs. old customer check. */
    boolean existsByCustomerIdAndRecStatus(Long customerId, RecStatus recStatus);

    /** Copies the customer still has out (BORROW / OWE / OVER), across active borrow records. */
    @Query("""
            select coalesce(sum(b.owe), 0) from Borrow b
            where b.customer.id = :customerId
              and b.recStatus = com.library.common.enums.RecStatus.ACTIVE
              and b.borrowStatus <> com.library.common.enums.BorrowStatus.RETURN
            """)
    long sumOutstandingByCustomerId(@Param("customerId") Long customerId);

    @EntityGraph(attributePaths = {"book", "customer", "employee"})
    Optional<Borrow> findByIdAndRecStatus(Long id, RecStatus recStatus);

    /**
     * Row-locks the borrow so concurrent returns / deletes can't double-count owe.
     * No fetch graph here: PostgreSQL rejects FOR UPDATE on the outer-joined side.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Borrow b where b.id = :id and b.recStatus = :recStatus")
    Optional<Borrow> findByIdAndRecStatusForUpdate(@Param("id") Long id, @Param("recStatus") RecStatus recStatus);

    @EntityGraph(attributePaths = {"book", "customer", "employee"})
    List<Borrow> findAllByRecStatusOrderByIdAsc(RecStatus recStatus);
}
