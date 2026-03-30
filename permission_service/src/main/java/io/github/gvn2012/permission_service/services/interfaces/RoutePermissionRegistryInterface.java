package io.github.gvn2012.permission_service.services.interfaces;

import io.github.gvn2012.permission_service.services.impls.RoutePermissionRegistryImpl;

import java.util.Optional;

public interface RoutePermissionRegistryInterface {
    Optional<RoutePermissionRegistryImpl.ResolvedRequest> resolve(String method, String path);
}
