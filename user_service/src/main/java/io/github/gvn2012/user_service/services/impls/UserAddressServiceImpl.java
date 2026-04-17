package io.github.gvn2012.user_service.services.impls;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.requests.AddNewAddressRequest;
import io.github.gvn2012.user_service.dtos.requests.UpdateAddressRequest;
import io.github.gvn2012.user_service.dtos.responses.*;
import io.github.gvn2012.user_service.entities.User;
import io.github.gvn2012.user_service.entities.UserAddress;
import io.github.gvn2012.user_service.entities.enums.AddressStatus;
import io.github.gvn2012.user_service.exceptions.BadRequestException;
import io.github.gvn2012.user_service.exceptions.NotFoundException;
import io.github.gvn2012.user_service.repositories.UserAddressRepository;
import io.github.gvn2012.user_service.repositories.UserRepository;
import io.github.gvn2012.user_service.services.interfaces.IUserAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAddressServiceImpl implements IUserAddressService {

    private final UserAddressRepository userAddressRepository;
    private final UserRepository userRepository;

    @Override
    public APIResource<GetUserAddressResponse> getUserAddress(String userId) {
        Set<AddressDto> addresses = userAddressRepository
                .findByUser_IdAndStatusNot(UUID.fromString(userId), AddressStatus.REMOVED)
                .stream()
                .map(address -> new AddressDto(
                        address.getId().toString(),
                        address.getAddressType(),
                        address.getAddressLine1(),
                        address.getAddressLine2(),
                        address.getCity(),
                        address.getState(),
                        address.getPostalCode(),
                        address.getCountry(),
                        address.getPrimary()))
                .collect(Collectors.toSet());

        return APIResource.ok(
                "Get user addresses successfully",
                new GetUserAddressResponse(addresses));
    }

    @Override
    @Transactional
    public APIResource<AddNewAddressResponse> addNewAddress(UUID userId, AddNewAddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        UserAddress address = new UserAddress();
        address.setUser(user);
        address.setAddressType(request.getAddressType());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setPrimary(false);

        userAddressRepository.save(address);

        return APIResource.ok(
                "Address added successfully",
                new AddNewAddressResponse(address.getId().toString()),
                HttpStatus.CREATED);
    }

    @Override
    @Transactional
    public APIResource<UpdateAddressResponse> updateAddress(UUID userId, UUID addressId, UpdateAddressRequest request) {
        UserAddress address = userAddressRepository
                .findByIdAndUser_IdAndStatusNot(addressId, userId, AddressStatus.REMOVED)
                .orElseThrow(() -> new NotFoundException("Address not found"));

        if (request.getAddressType() != null) {
            address.setAddressType(request.getAddressType());
        }
        if (request.getAddressLine1() != null) {
            address.setAddressLine1(request.getAddressLine1());
        }
        if (request.getAddressLine2() != null) {
            address.setAddressLine2(request.getAddressLine2());
        }
        if (request.getCity() != null) {
            address.setCity(request.getCity());
        }
        if (request.getState() != null) {
            address.setState(request.getState());
        }
        if (request.getPostalCode() != null) {
            address.setPostalCode(request.getPostalCode());
        }
        if (request.getCountry() != null) {
            address.setCountry(request.getCountry());
        }

        userAddressRepository.save(address);

        return APIResource.ok(
                "Address updated successfully",
                new UpdateAddressResponse(address.getId().toString()));
    }

    @Override
    @Transactional
    public APIResource<DeleteAddressResponse> deleteAddress(UUID userId, UUID addressId) {
        UserAddress address = userAddressRepository
                .findByIdAndUser_IdAndStatusNot(addressId, userId, AddressStatus.REMOVED)
                .orElseThrow(() -> new NotFoundException("Address not found"));

        if (Boolean.TRUE.equals(address.getPrimary())) {
            List<UserAddress> addresses = userAddressRepository.findByUser_IdAndStatusNot(userId,
                    AddressStatus.REMOVED);
            if (addresses.size() > 1) {
                throw new BadRequestException("Cannot delete primary address. Set another address as primary first.");
            }
        }

        address.setStatus(AddressStatus.REMOVED);
        userAddressRepository.save(address);

        return APIResource.ok(
                "Address deleted successfully",
                new DeleteAddressResponse(address.getId().toString()));
    }

    @Override
    @Transactional
    public APIResource<SetPrimaryAddressResponse> setPrimaryAddress(UUID userId, UUID addressId) {
        UserAddress address = userAddressRepository
                .findByIdAndUser_IdAndStatusNot(addressId, userId, AddressStatus.REMOVED)
                .orElseThrow(() -> new NotFoundException("Address not found"));

        if (Boolean.TRUE.equals(address.getPrimary())) {
            throw new BadRequestException("This address is already the primary address");
        }

        String previousPrimaryId = null;
        Optional<UserAddress> currentPrimaryOpt = userAddressRepository.findByUser_IdAndPrimaryTrueAndStatusNot(userId,
                AddressStatus.REMOVED);
        if (currentPrimaryOpt.isPresent()) {
            previousPrimaryId = currentPrimaryOpt.get().getId().toString();
            currentPrimaryOpt.get().setPrimary(false);
            userAddressRepository.save(currentPrimaryOpt.get());
        }

        address.setPrimary(true);
        userAddressRepository.save(address);

        return APIResource.ok(
                "Primary address updated successfully",
                new SetPrimaryAddressResponse(address.getId().toString(), previousPrimaryId));
    }
}
