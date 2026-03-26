package io.github.gvn2012.permission_service.entities.enums;

public enum PermissionAuditAction {
    // Access checks
    ACCESS_CHECK,
    ACCESS_GRANTED,
    ACCESS_DENIED,

    // Role management
    ROLE_CREATED,
    ROLE_UPDATED,
    ROLE_DELETED,
    ROLE_ASSIGNED,
    ROLE_REVOKED,

    // Permission management
    PERMISSION_CREATED,
    PERMISSION_UPDATED,
    PERMISSION_DELETED,
    PERMISSION_GRANTED,
    PERMISSION_REVOKED,

    // Policy management
    POLICY_CREATED,
    POLICY_UPDATED,
    POLICY_ACTIVATED,
    POLICY_DEACTIVATED,
    POLICY_DELETED,

    // Override management
    OVERRIDE_CREATED,
    OVERRIDE_UPDATED,
    OVERRIDE_DELETED
}
