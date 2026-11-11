package com.library.employee.service;

import com.library.employee.dto.EmployeeRequest;
import com.library.employee.dto.EmployeeResponse;

import java.util.List;

public interface EmployeeService {
    EmployeeResponse create(EmployeeRequest request);
    EmployeeResponse getById(Long id);
    EmployeeResponse update(Long id, EmployeeRequest request);
    void delete(Long id);
    List<EmployeeResponse> getAll();
}
