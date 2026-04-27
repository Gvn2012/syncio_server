package io.github.gvn2012.permission_service.grpc;

import io.github.gvn2012.grpc.permission.*;
import io.github.gvn2012.permission_service.dtos.APIResource;
import io.github.gvn2012.permission_service.dtos.responses.GetUserRoleResponse;
import io.github.gvn2012.permission_service.services.interfaces.RoleServiceInterface;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;

@GrpcService
@RequiredArgsConstructor
public class PermissionGrpcService extends PermissionServiceGrpc.PermissionServiceImplBase {

    private final RoleServiceInterface roleService;

    @Override
    public void getUserRoles(UserRolesRequest request, StreamObserver<UserRolesResponse> responseObserver) {
        APIResource<List<GetUserRoleResponse>> resource = roleService.getUserRole(request.getUserId());
        
        UserRolesResponse.Builder responseBuilder = UserRolesResponse.newBuilder();
        
        if (resource.isSuccess() && resource.getData() != null) {
            for (GetUserRoleResponse role : resource.getData()) {
                RoleSummary.Builder roleBuilder = RoleSummary.newBuilder()
                        .setRoleId(role.getRoleId())
                        .setRoleName(role.getRoleName());
                
                if (role.getColor() != null) {
                    roleBuilder.setColor(role.getColor());
                }
                if (role.getIcon() != null) {
                    roleBuilder.setIcon(role.getIcon());
                }
                if (role.getDisplayOrder() != null) {
                    roleBuilder.setDisplayOrder(role.getDisplayOrder());
                }
                
                responseBuilder.addRoles(roleBuilder.build());
            }
        }
        
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void initUserRole(InitUserRoleRequest request, StreamObserver<InitUserRoleResponse> responseObserver) {
        APIResource<Boolean> resource = roleService.initUserRole(request.getUserId(), request.getRegistrationType());
        
        InitUserRoleResponse.Builder responseBuilder = InitUserRoleResponse.newBuilder()
                .setSuccess(resource.isSuccess() && resource.getData() != null && resource.getData());
        
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}
