package io.github.gvn2012.org_service.services.interfaces;

import io.github.gvn2012.org_service.dtos.requests.CreateDepartmentRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateDepartmentRequest;
import io.github.gvn2012.org_service.dtos.responses.CreateDepartmentResponse;
import io.github.gvn2012.org_service.dtos.responses.DepartmentDto;
import io.github.gvn2012.org_service.dtos.responses.UpdateDepartmentResponse;

import java.util.List;
import java.util.UUID;

public interface IDepartmentService {
    
    CreateDepartmentResponse createDepartment(UUID orgId, UUID requestingUserId, CreateDepartmentRequest request);
    
    DepartmentDto getDepartment(UUID orgId, UUID deptId, UUID requestingUserId);
    
    UpdateDepartmentResponse updateDepartment(UUID orgId, UUID deptId, UUID requestingUserId, UpdateDepartmentRequest request);
    
    void deleteDepartment(UUID orgId, UUID deptId, UUID requestingUserId);
    
    List<DepartmentDto> getDepartments(UUID orgId, UUID requestingUserId);
}
