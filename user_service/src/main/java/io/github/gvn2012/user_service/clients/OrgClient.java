package io.github.gvn2012.user_service.clients;

import io.github.gvn2012.grpc.org.*;
import io.github.gvn2012.user_service.exceptions.InternalServerErrorException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

@Component
public class OrgClient {

    @GrpcClient("org-service")
    private OrgServiceGrpc.OrgServiceBlockingStub orgServiceStub;

    public Mono<Map<String, Object>> createOrganization(Map<String, Object> request) {
        return Mono.fromCallable(() -> {
            CreateOrgGrpcRequest.Builder builder = CreateOrgGrpcRequest.newBuilder()
                    .setName((String) request.getOrDefault("name", ""))
                    .setOwnerId((String) request.getOrDefault("ownerId", ""))
                    .setEmail((String) request.getOrDefault("email", ""));

            if (request.containsKey("legalName")) builder.setLegalName((String) request.get("legalName"));
            if (request.containsKey("description")) builder.setDescription((String) request.get("description"));
            if (request.containsKey("industry")) builder.setIndustry((String) request.get("industry"));
            if (request.containsKey("website")) builder.setWebsite((String) request.get("website"));
            if (request.containsKey("logoUrl")) builder.setLogoUrl((String) request.get("logoUrl"));
            if (request.containsKey("foundedDate")) builder.setFoundedDate(request.get("foundedDate").toString());
            if (request.containsKey("registrationNumber")) builder.setRegistrationNumber((String) request.get("registrationNumber"));
            if (request.containsKey("taxId")) builder.setTaxId((String) request.get("taxId"));
            if (request.containsKey("organizationSize")) builder.setOrganizationSize((String) request.get("organizationSize"));

            return orgServiceStub.createOrganization(builder.build());
        })
        .subscribeOn(Schedulers.boundedElastic())
        .map(response -> (Map<String, Object>) Map.<String, Object>of(
                "id", response.getId(),
                "slug", response.getSlug()
        ))
        .onErrorMap(e -> new InternalServerErrorException("Org service error: " + e.getMessage()));
    }
}
