package io.github.gvn2012.user_service.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetUserRoleResponse {
    private String roleId;
    private String roleName;
    private String color;
    private String icon;
    private Integer displayOrder;
}
