package io.github.gvn2012.permission_service.entities.enums;

public enum RoleType {
    SYSTEM,      // built-in, cannot be modified
    PLATFORM,    // platform-wide roles (admin, moderator)
    CONTEXTUAL,  // context-specific (group admin, channel moderator)
    CUSTOM       // user-created roles
}
