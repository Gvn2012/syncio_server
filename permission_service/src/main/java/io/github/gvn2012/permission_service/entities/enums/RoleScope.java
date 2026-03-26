package io.github.gvn2012.permission_service.entities.enums;

public enum RoleScope {
    GLOBAL,        // platform-wide
    ORGANIZATION,  // within an organization
    GROUP,         // within a group
    CHANNEL,       // within a channel
    PROJECT,       // within a project
    RESOURCE       // specific resource
}
