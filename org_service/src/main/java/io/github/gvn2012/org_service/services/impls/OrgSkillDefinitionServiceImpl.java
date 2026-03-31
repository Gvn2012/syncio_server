package io.github.gvn2012.org_service.services.impls;

import io.github.gvn2012.org_service.dtos.mappers.OrgSkillDefinitionMapper;
import io.github.gvn2012.org_service.dtos.requests.CreateOrgSkillDefinitionRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateOrgSkillDefinitionRequest;
import io.github.gvn2012.org_service.dtos.responses.OrgSkillDefinitionDto;
import io.github.gvn2012.org_service.entities.OrgSkillDefinition;
import io.github.gvn2012.org_service.entities.Organization;
import io.github.gvn2012.org_service.repositories.OrgSkillDefinitionRepository;
import io.github.gvn2012.org_service.repositories.OrganizationRepository;
import io.github.gvn2012.org_service.services.interfaces.IOrgSkillDefinitionService;
import io.github.gvn2012.org_service.exceptions.BadRequestException;
import io.github.gvn2012.org_service.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrgSkillDefinitionServiceImpl implements IOrgSkillDefinitionService {

    private final OrgSkillDefinitionRepository skillDefinitionRepository;
    private final OrganizationRepository organizationRepository;

    @Override
    @Transactional
    public OrgSkillDefinitionDto createSkillDefinition(UUID orgId, CreateOrgSkillDefinitionRequest request) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new NotFoundException("Organization not found"));

        if (skillDefinitionRepository.findByOrganization_IdAndCode(orgId, request.getCode()).isPresent()) {
            throw new BadRequestException("Skill definition code already exists in this organization");
        }

        OrgSkillDefinition skillDefinition = OrgSkillDefinitionMapper.toEntity(request);
        skillDefinition.setOrganization(organization);

        OrgSkillDefinition savedSkillDefinition = skillDefinitionRepository.save(skillDefinition);
        return OrgSkillDefinitionMapper.toDto(savedSkillDefinition);
    }

    @Override
    @Transactional
    public OrgSkillDefinitionDto updateSkillDefinition(UUID orgId, UUID skillDefinitionId,
            UpdateOrgSkillDefinitionRequest request) {
        OrgSkillDefinition skillDefinition = skillDefinitionRepository
                .findByIdAndOrganization_Id(skillDefinitionId, orgId)
                .orElseThrow(() -> new NotFoundException("Skill definition not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            skillDefinition.setName(request.getName());
        }

        if (request.getDescription() != null) {
            skillDefinition.setDescription(request.getDescription());
        }

        if (request.getCategory() != null) {
            skillDefinition.setCategory(request.getCategory());
        }

        if (request.getDisplayOrder() != null) {
            skillDefinition.setDisplayOrder(request.getDisplayOrder());
        }

        if (request.getActive() != null) {
            skillDefinition.setActive(request.getActive());
        }

        OrgSkillDefinition updatedSkillDefinition = skillDefinitionRepository.save(skillDefinition);
        return OrgSkillDefinitionMapper.toDto(updatedSkillDefinition);
    }

    @Override
    @Transactional(readOnly = true)
    public OrgSkillDefinitionDto getSkillDefinitionById(UUID orgId, UUID skillDefinitionId) {
        OrgSkillDefinition skillDefinition = skillDefinitionRepository
                .findByIdAndOrganization_Id(skillDefinitionId, orgId)
                .orElseThrow(() -> new NotFoundException("Skill definition not found"));
        return OrgSkillDefinitionMapper.toDto(skillDefinition);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrgSkillDefinitionDto> getSkillDefinitionsByOrgId(UUID orgId) {
        return skillDefinitionRepository.findByOrganization_IdAndActiveTrue(orgId).stream()
                .map(OrgSkillDefinitionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrgSkillDefinitionDto> getSkillDefinitionsByOrgIdAndCategory(UUID orgId, String category) {
        return skillDefinitionRepository.findByOrganization_IdAndCategoryAndActiveTrue(orgId, category).stream()
                .map(OrgSkillDefinitionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSkillDefinition(UUID orgId, UUID skillDefinitionId) {
        OrgSkillDefinition skillDefinition = skillDefinitionRepository
                .findByIdAndOrganization_Id(skillDefinitionId, orgId)
                .orElseThrow(() -> new NotFoundException("Skill definition not found"));
        skillDefinition.setActive(false);
        skillDefinitionRepository.save(skillDefinition);
    }
}
