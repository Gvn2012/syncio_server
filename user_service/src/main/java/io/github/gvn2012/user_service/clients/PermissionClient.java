package io.github.gvn2012.user_service.clients;

import io.github.gvn2012.grpc.permission.InitUserRoleRequest;
import io.github.gvn2012.grpc.permission.PermissionServiceGrpc;
import io.github.gvn2012.grpc.permission.UserRolesRequest;
import io.github.gvn2012.user_service.dtos.responses.GetUserRoleResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PermissionClient {

    @GrpcClient("permission-service")
    private PermissionServiceGrpc.PermissionServiceBlockingStub permissionServiceStub;

    public Mono<List<GetUserRoleResponse>> getUserRole(String userId) {
        return Mono.fromCallable(() -> permissionServiceStub.getUserRoles(
                UserRolesRequest.newBuilder().setUserId(userId).build()))
                .subscribeOn(Schedulers.boundedElastic())
                .map(response -> response.getRolesList().stream()
                        .map(role -> new GetUserRoleResponse(
                                role.getRoleId(),
                                role.getRoleName(),
                                role.getColor(),
                                role.getIcon(),
                                role.getDisplayOrder()))
                        .collect(Collectors.toList()));
    }

    public Mono<Boolean> initUserRole(String userId, String registrationType) {
        return Mono.fromCallable(() -> permissionServiceStub.initUserRole(
                InitUserRoleRequest.newBuilder()
                        .setUserId(userId)
                        .setRegistrationType(registrationType)
                        .build()))
                .subscribeOn(Schedulers.boundedElastic())
                .map(response -> response.getSuccess());
    }
}