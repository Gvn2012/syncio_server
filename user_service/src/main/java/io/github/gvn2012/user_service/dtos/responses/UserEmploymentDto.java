package io.github.gvn2012.user_service.dtos.responses;

import io.github.gvn2012.user_service.entities.enums.EmploymentStatus;
import io.github.gvn2012.user_service.entities.enums.EmploymentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEmploymentDto {
    private String id;
    private String organizationId;
    private String departmentId;
    private String teamId;
    private String employeeCode;
    private String jobTitleId;
    private String jobLevelId;
    private EmploymentType employmentType;
    private EmploymentStatus employmentStatus;
    private String managerId;
    private LocalDate hireDate;
    private LocalDate probationEndDate;
    private LocalDate terminationDate;
    private String workLocation;
    private Boolean current;
}
