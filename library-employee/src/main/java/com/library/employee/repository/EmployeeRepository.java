package com.library.employee.repository;

import com.library.common.enums.RecStatus;
import com.library.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /** Checks all rows (incl. soft-deleted) because the DB unique constraint does too. */
    boolean existsByIdentityCardNo(String identityCardNo);

    boolean existsByIdentityCardNoAndIdNot(String identityCardNo, Long id);

    Optional<Employee> findByIdAndRecStatus(Long id, RecStatus recStatus);

    List<Employee> findAllByRecStatusOrderByIdAsc(RecStatus recStatus);
}
