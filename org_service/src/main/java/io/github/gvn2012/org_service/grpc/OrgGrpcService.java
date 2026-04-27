package io.github.gvn2012.org_service.grpc;

import io.github.gvn2012.grpc.org.*;
import io.github.gvn2012.org_service.dtos.requests.CreateOrganizationRequest;
import io.github.gvn2012.org_service.dtos.responses.CreateOrganizationResponse;
import io.github.gvn2012.org_service.entities.enums.OrganizationSize;
import io.github.gvn2012.org_service.services.interfaces.IOrganizationService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.LocalDate;
import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class OrgGrpcService extends OrgServiceGrpc.OrgServiceImplBase {

    private final IOrganizationService organizationService;

    @Override
    public void createOrganization(CreateOrgGrpcRequest request, StreamObserver<CreateOrgGrpcResponse> responseObserver) {
        CreateOrganizationRequest serviceRequest = CreateOrganizationRequest.builder()
                .name(request.getName())
                .legalName(request.getLegalName())
                .description(request.getDescription())
                .industry(request.getIndustry())
                .website(request.getWebsite())
                .logoUrl(request.getLogoUrl())
                .foundedDate(request.getFoundedDate().isEmpty() ? null : LocalDate.parse(request.getFoundedDate()))
                .registrationNumber(request.getRegistrationNumber())
                .taxId(request.getTaxId())
                .organizationSize(request.getOrganizationSize().isEmpty() ? null : OrganizationSize.valueOf(request.getOrganizationSize()))
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .ownerId(request.getOwnerId())
                .email(request.getEmail())
                .build();

        CreateOrganizationResponse response = organizationService.createOrganization(
                UUID.fromString(request.getOwnerId()), serviceRequest);

        responseObserver.onNext(CreateOrgGrpcResponse.newBuilder()
                .setId(response.getId().toString())
                .setSlug(response.getSlug())
                .build());
        responseObserver.onCompleted();
    }
}
