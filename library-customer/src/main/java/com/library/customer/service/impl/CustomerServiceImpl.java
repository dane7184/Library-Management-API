package com.library.customer.service.impl;

import com.library.common.enums.RecStatus;
import com.library.common.exception.BusinessException;
import com.library.common.exception.ResourceNotFoundException;
import com.library.customer.dto.CustomerRequest;
import com.library.customer.dto.CustomerResponse;
import com.library.customer.entity.Customer;
import com.library.customer.mapper.CustomerMapper;
import com.library.customer.repository.CustomerRepository;
import com.library.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business rules (from spec):
 *  - Customer type: STUDENT or TEACHER (enforced by the enum + @NotNull).
 *  - Reject create when identityCardNo already exists.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public CustomerResponse create(CustomerRequest request) {
        if (customerRepository.existsByIdentityCardNo(request.identityCardNo())) {
            throw new BusinessException("Customer with identity card no '" + request.identityCardNo() + "' already exists");
        }
        Customer saved = customerRepository.save(customerMapper.toEntity(request));
        return customerMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getById(Long id) {
        return customerMapper.toResponse(findActive(id));
    }

    @Override
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = findActive(id);
        if (customerRepository.existsByIdentityCardNoAndIdNot(request.identityCardNo(), id)) {
            throw new BusinessException("Customer with identity card no '" + request.identityCardNo() + "' already exists");
        }
        customerMapper.updateEntity(request, customer);
        return customerMapper.toResponse(customerRepository.saveAndFlush(customer));
    }

    @Override
    public void delete(Long id) {
        // Soft delete: set recStatus = DELETED
        findActive(id).setRecStatus(RecStatus.DELETED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAll() {
        return customerRepository.findAllByRecStatusOrderByIdAsc(RecStatus.ACTIVE).stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    private Customer findActive(Long id) {
        return customerRepository.findByIdAndRecStatus(id, RecStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }
}
