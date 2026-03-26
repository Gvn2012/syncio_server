package io.github.gvn2012.permission_service.dtos.responses;

import io.github.gvn2012.permission_service.dtos.requests.PermissionCheckRequest;
import io.github.gvn2012.permission_service.entities.enums.PermissionDecision;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class PermissionCheckResult {
    private PermissionCheckRequest request;
    private PermissionDecision decision;
    private String reason;
    private UUID matchingPolicyId;
    private List<String> evaluatedPolicies;
    private long evaluationTimeMs;
    private boolean fromCache;
}
