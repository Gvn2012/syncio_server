package io.github.gvn2012.org_service.services.impls;

import io.github.gvn2012.org_service.dtos.mappers.OrganizationMapper;
import io.github.gvn2012.org_service.dtos.requests.CreateOrganizationRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateOrganizationRequest;
import io.github.gvn2012.org_service.dtos.responses.CreateOrganizationResponse;
import io.github.gvn2012.org_service.dtos.responses.OrgAvailabilityResponse;
import io.github.gvn2012.org_service.dtos.responses.OrganizationDto;
import io.github.gvn2012.org_service.dtos.responses.UpdateOrganizationResponse;
import io.github.gvn2012.org_service.entities.Organization;
import io.github.gvn2012.org_service.entities.enums.OrganizationStatus;
import io.github.gvn2012.org_service.exceptions.BadRequestException;
import io.github.gvn2012.org_service.exceptions.ForbiddenException;
import io.github.gvn2012.org_service.exceptions.NotFoundException;
import io.github.gvn2012.org_service.repositories.OrganizationRepository;
import io.github.gvn2012.org_service.services.interfaces.IOrganizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.kafka.core.KafkaTemplate;
import io.github.gvn2012.shared.kafka_events.OrgCreatedEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements IOrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final io.github.gvn2012.org_service.clients.UploadClient uploadClient;

    @Override
    @Transactional
    public CreateOrganizationResponse createOrganization(UUID requestingUserId, CreateOrganizationRequest request) {
        log.info("Creating organization for user {}", requestingUserId);

        String slug = generateSlug(request.getName());

        if (organizationRepository.existsBySlug(slug)) {
            slug = slug + "-" + UUID.randomUUID().toString().substring(0, 8);
        }

        Organization org = new Organization();
        org.setName(request.getName());
        org.setLegalName(request.getLegalName());
        org.setSlug(slug);
        org.setDescription(request.getDescription());
        org.setIndustry(request.getIndustry());
        org.setWebsite(request.getWebsite());
        org.setLogoUrl(request.getLogoUrl());
        org.setLogoPath(request.getLogoPath());
        org.setFoundedDate(request.getFoundedDate() != null ? request.getFoundedDate() : LocalDate.now());
        org.setRegistrationNumber(request.getRegistrationNumber());
        org.setTaxId(request.getTaxId());
        org.setOrganizationSize(request.getOrganizationSize());
        org.setAddress(request.getAddress());
        org.setCity(request.getCity());
        org.setState(request.getState());
        org.setCountry(request.getCountry());
        org.setPostalCode(request.getPostalCode());
        org.setOwnerId(requestingUserId);
        org.setStatus(OrganizationStatus.ACTIVE);

        Organization savedOrg = organizationRepository.save(org);

        OrgCreatedEvent event = OrgCreatedEvent.builder()
                .orgId(savedOrg.getId().toString())
                .ownerId(requestingUserId.toString())
                .name(savedOrg.getName())
                .email(request.getEmail())
                .eventId(UUID.randomUUID().toString())
                .build();
        kafkaTemplate.send("org.created", savedOrg.getId().toString(), event);

        return CreateOrganizationResponse.builder()
                .id(savedOrg.getId())
                .slug(savedOrg.getSlug())
                .build();
    }

    @Override
    public OrganizationDto getOrganization(UUID orgId) {
        Organization org = getOrganizationOrThrow(orgId);
        OrganizationDto dto = organizationMapper.toDto(org);
        enrichOrganizationUrls(Collections.singletonList(dto));
        return dto;
    }

    @Override
    @Transactional
    public UpdateOrganizationResponse updateOrganization(UUID orgId, UUID requestingUserId,
            UpdateOrganizationRequest request) {
        Organization org = getOrganizationOrThrow(orgId);

        if (!org.getOwnerId().equals(requestingUserId)) {
            throw new ForbiddenException("Only the organization owner can update the organization");
        }

        if (request.getName() != null) {
            org.setName(request.getName());
        }
        if (request.getLegalName() != null)
            org.setLegalName(request.getLegalName());
        if (request.getDescription() != null)
            org.setDescription(request.getDescription());
        if (request.getIndustry() != null)
            org.setIndustry(request.getIndustry());
        if (request.getWebsite() != null)
            org.setWebsite(request.getWebsite());
        if (request.getLogoUrl() != null)
            org.setLogoUrl(request.getLogoUrl());
        if (request.getLogoPath() != null)
            org.setLogoPath(request.getLogoPath());
        if (request.getFoundedDate() != null)
            org.setFoundedDate(request.getFoundedDate());
        if (request.getRegistrationNumber() != null)
            org.setRegistrationNumber(request.getRegistrationNumber());
        if (request.getTaxId() != null)
            org.setTaxId(request.getTaxId());
        if (request.getOrganizationSize() != null)
            org.setOrganizationSize(request.getOrganizationSize());
        if (request.getAddress() != null)
            org.setAddress(request.getAddress());
        if (request.getCity() != null)
            org.setCity(request.getCity());
        if (request.getState() != null)
            org.setState(request.getState());
        if (request.getCountry() != null)
            org.setCountry(request.getCountry());
        if (request.getPostalCode() != null)
            org.setPostalCode(request.getPostalCode());

        Organization updatedOrg = organizationRepository.save(org);

        return UpdateOrganizationResponse.builder()
                .id(updatedOrg.getId())
                .slug(updatedOrg.getSlug())
                .build();
    }

    @Override
    @Transactional
    public void deleteOrganization(UUID orgId, UUID requestingUserId) {
        Organization org = getOrganizationOrThrow(orgId);

        if (!org.getOwnerId().equals(requestingUserId)) {
            throw new ForbiddenException("Only the organization owner can delete the organization");
        }

        org.setStatus(OrganizationStatus.DELETED);
        organizationRepository.save(org);
    }

    @Override
    @Transactional
    public OrgAvailabilityResponse getOrgAvailability(String name) {
        OrgAvailabilityResponse response = new OrgAvailabilityResponse();
        String normalizedName = name.toLowerCase().trim();
        boolean exists = organizationRepository.existsByName(normalizedName);
        response.setIsNameAvailable(!exists);

        if (exists) {
            Set<String> recommendedNames = new HashSet<>();
            Random random = new Random();

            while (recommendedNames.size() < 3) {
                int number = 100 + random.nextInt(900);
                String candidate = normalizedName + number;

                if (!organizationRepository.existsByName(candidate)) {
                    recommendedNames.add(candidate);
                }
            }
            response.setRecommendedNames(recommendedNames);
        }

        return response;
    }

    @Override
    public List<OrganizationDto> getOrgsByOwner(UUID ownerId) {
        List<Organization> orgs = organizationRepository.findByOwnerId(ownerId);
        List<OrganizationDto> dtos = orgs.stream()
                .map(organizationMapper::toDto)
                .collect(Collectors.toList());
        enrichOrganizationUrls(dtos);
        return dtos;
    }

    private void enrichOrganizationUrls(List<OrganizationDto> dtos) {
        if (dtos == null || dtos.isEmpty()) return;

        Set<String> pathsToSign = dtos.stream()
                .map(OrganizationDto::getLogoPath)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (pathsToSign.isEmpty()) return;

        io.github.gvn2012.org_service.dtos.responses.DownloadUrlResponseDTO signedUrlsRes = uploadClient.getDownloadUrls(new io.github.gvn2012.org_service.dtos.requests.DownloadUrlRequestDTO(pathsToSign));
        Map<String, String> signedUrls = signedUrlsRes != null ? signedUrlsRes.getDownloadUrls() : Map.of();

        for (OrganizationDto dto : dtos) {
            if (dto.getLogoPath() != null) {
                dto.setLogoUrl(signedUrls.getOrDefault(dto.getLogoPath(), dto.getLogoUrl()));
            }
        }
    }

    private Organization getOrganizationOrThrow(UUID orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> new NotFoundException("Organization not found with id: " + orgId));
    }

    private String generateSlug(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Organization name cannot be empty");
        }
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
    }
}
