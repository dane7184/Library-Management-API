package com.library.customer.repository;

import com.library.common.enums.RecStatus;
import com.library.customer.entity.Customer;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /** Checks all rows (incl. soft-deleted) because the DB unique constraint does too. */
    boolean existsByIdentityCardNo(String identityCardNo);

    boolean existsByIdentityCardNoAndIdNot(String identityCardNo, Long id);

    Optional<Customer> findByIdAndRecStatus(Long id, RecStatus recStatus);

    List<Customer> findAllByRecStatusOrderByIdAsc(RecStatus recStatus);

    /** Row-locks the customer so per-customer borrow limits can't be raced by concurrent requests. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Customer c where c.id = :id and c.recStatus = :recStatus")
    Optional<Customer> findByIdAndRecStatusForUpdate(@Param("id") Long id, @Param("recStatus") RecStatus recStatus);
}
