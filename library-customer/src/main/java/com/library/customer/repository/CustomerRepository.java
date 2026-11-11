package com.library.customer.repository;

import com.library.common.enums.RecStatus;
import com.library.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
