package io.github.gvn2012.permission_service.entities.enums;

public enum ConditionSubjectType {
    SUBJECT,      // the user/principal making the request
    RESOURCE,     // the resource being accessed
    ACTION,       // the action being performed
    ENVIRONMENT,  // contextual data (time, IP, location)
    RELATIONSHIP  // relationship between subject and resource
}
