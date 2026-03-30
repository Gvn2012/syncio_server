package io.github.gvn2012.user_service.entities.enums;

public enum UserAuditAction {
    LOGIN,
    LOGOUT,
    PASSWORD_CHANGE,
    PASSWORD_RESET,
    EMAIL_CHANGE,
    PHONE_CHANGE,
    PROFILE_UPDATE,
    ACCOUNT_DEACTIVATED,
    ACCOUNT_REACTIVATED,
    ACCOUNT_BANNED,
    ACCOUNT_SUSPENDED,
    ROLE_CHANGE,
    SESSION_REVOKED
}
