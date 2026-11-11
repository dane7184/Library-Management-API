package com.library.customer.service;

import com.library.customer.dto.CustomerRequest;
import com.library.customer.dto.CustomerResponse;

import java.util.List;

public interface CustomerService {
    CustomerResponse create(CustomerRequest request);
    CustomerResponse getById(Long id);
    CustomerResponse update(Long id, CustomerRequest request);
    void delete(Long id);
    List<CustomerResponse> getAll();
}
