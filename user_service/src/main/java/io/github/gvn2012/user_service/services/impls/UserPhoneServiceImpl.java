package io.github.gvn2012.user_service.services.impls;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.responses.GetUserPhoneResponse;
import io.github.gvn2012.user_service.dtos.responses.PhoneDto;
import io.github.gvn2012.user_service.entities.UserPhone;
import io.github.gvn2012.user_service.entities.enums.PhoneStatus;
import io.github.gvn2012.user_service.repositories.UserPhoneRepository;
import io.github.gvn2012.user_service.services.interfaces.IUserPhoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserPhoneServiceImpl implements IUserPhoneService {

    private final UserPhoneRepository userPhoneRepository;

    @Override
    public APIResource<GetUserPhoneResponse> getUserPhone(String userId) {

        Set<PhoneDto> phones = userPhoneRepository
                .findAllByUser_Id(UUID.fromString(userId))
                .stream()
                .filter(phone -> phone.getStatus() != PhoneStatus.REMOVED)
                .map(phone -> new PhoneDto(
                        phone.getId().toString(),
                        phone.getCountryCode(),
                        phone.getPhoneNumber(),
                        phone.getVerified(),
                        phone.getPrimary()
                ))
                .collect(Collectors.toSet());

        return APIResource.ok(
                "Get user phone successfully",
                new GetUserPhoneResponse(phones)
        );


    }
}
