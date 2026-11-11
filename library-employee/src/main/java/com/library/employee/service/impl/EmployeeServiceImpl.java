package com.library.employee.service.impl;

import com.library.common.enums.RecStatus;
import com.library.common.exception.BusinessException;
import com.library.common.exception.ResourceNotFoundException;
import com.library.employee.dto.EmployeeRequest;
import com.library.employee.dto.EmployeeResponse;
import com.library.employee.entity.Employee;
import com.library.employee.mapper.EmployeeMapper;
import com.library.employee.repository.EmployeeRepository;
import com.library.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business rules (from spec):
 *  - Reject create when identityCardNo already exists.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    public EmployeeResponse create(EmployeeRequest request) {
        if (employeeRepository.existsByIdentityCardNo(request.identityCardNo())) {
            throw new BusinessException("Employee with identity card no '" + request.identityCardNo() + "' already exists");
        }
        Employee saved = employeeRepository.save(employeeMapper.toEntity(request));
        return employeeMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getById(Long id) {
        return employeeMapper.toResponse(findActive(id));
    }

    @Override
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = findActive(id);
        if (employeeRepository.existsByIdentityCardNoAndIdNot(request.identityCardNo(), id)) {
            throw new BusinessException("Employee with identity card no '" + request.identityCardNo() + "' already exists");
        }
        employeeMapper.updateEntity(request, employee);
        return employeeMapper.toResponse(employeeRepository.saveAndFlush(employee));
    }

    @Override
    public void delete(Long id) {
        // Soft delete: set recStatus = DELETED
        findActive(id).setRecStatus(RecStatus.DELETED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAll() {
        return employeeRepository.findAllByRecStatusOrderByIdAsc(RecStatus.ACTIVE).stream()
                .map(employeeMapper::toResponse)
                .toList();
    }

    private Employee findActive(Long id) {
        return employeeRepository.findByIdAndRecStatus(id, RecStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
    }
}
