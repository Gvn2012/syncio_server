package io.github.gvn2012.org_service.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class CreateOrgInvitationRequest {
    @NotBlank(message = "Invited email is required")
    @Email(message = "Invalid email format")
    private String invitedEmail;

    private UUID departmentId;
    private UUID positionId;
    
    // Admin can specify an explicit expiration, or we can use a default
    private Instant expiresAt;
}
