package io.github.gvn2012.permission_service.entities.enums;

public enum PolicyCombiningAlgorithm {
    DENY_OVERRIDES,       // if any rule denies, deny
    PERMIT_OVERRIDES,     // if any rule permits, permit
    FIRST_APPLICABLE,     // use first matching rule
    ONLY_ONE_APPLICABLE   // error if multiple rules match
}
