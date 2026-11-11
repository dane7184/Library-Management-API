package com.library.employee.mapper;

import com.library.employee.dto.EmployeeRequest;
import com.library.employee.dto.EmployeeResponse;
import com.library.employee.entity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "recStatus", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    Employee toEntity(EmployeeRequest request);

    /** Copies request fields onto an existing (managed) entity. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "recStatus", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    void updateEntity(EmployeeRequest request, @MappingTarget Employee entity);

    EmployeeResponse toResponse(Employee entity);
}
