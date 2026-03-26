package io.github.gvn2012.permission_service.entities.enums;

public enum ConditionOperator {
    EQUALS,
    NOT_EQUALS,
    CONTAINS,
    NOT_CONTAINS,
    STARTS_WITH,
    ENDS_WITH,
    GREATER_THAN,
    GREATER_THAN_OR_EQUALS,
    LESS_THAN,
    LESS_THAN_OR_EQUALS,
    IN,
    NOT_IN,
    BETWEEN,
    MATCHES,       // regex
    EXISTS,
    NOT_EXISTS,
    IS_OWNER,      // special: checks if subject owns resource
    IS_MEMBER,     // special: checks membership
    HAS_RELATIONSHIP  // special: checks relationship exists
}
