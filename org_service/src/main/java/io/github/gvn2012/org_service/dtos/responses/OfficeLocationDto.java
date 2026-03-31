package io.github.gvn2012.org_service.dtos.responses;

import io.github.gvn2012.org_service.entities.enums.OfficeStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class OfficeLocationDto {
    private UUID id;
    private String name;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String phoneNumber;
    private String email;
    private Boolean headquarters;
    private Integer capacity;
    private OfficeStatus status;
    
    private Instant createdAt;
    private Instant updatedAt;
}
