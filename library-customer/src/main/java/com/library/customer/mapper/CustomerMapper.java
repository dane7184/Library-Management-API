package com.library.customer.mapper;

import com.library.customer.dto.CustomerRequest;
import com.library.customer.dto.CustomerResponse;
import com.library.customer.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "recStatus", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    Customer toEntity(CustomerRequest request);

    /** Copies request fields onto an existing (managed) entity. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "recStatus", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    void updateEntity(CustomerRequest request, @MappingTarget Customer entity);

    CustomerResponse toResponse(Customer entity);
}
