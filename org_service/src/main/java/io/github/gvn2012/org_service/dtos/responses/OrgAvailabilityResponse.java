package io.github.gvn2012.org_service.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrgAvailabilityResponse {
    private Boolean isNameAvailable;
    private Set<String> recommendedNames;
}
