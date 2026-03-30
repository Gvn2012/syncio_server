package io.github.gvn2012.user_service.services.interfaces;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.requests.AddNewAddressRequest;
import io.github.gvn2012.user_service.dtos.requests.UpdateAddressRequest;
import io.github.gvn2012.user_service.dtos.responses.*;

import java.util.UUID;

public interface IUserAddressService {

    APIResource<GetUserAddressResponse> getUserAddress(String userId);

    APIResource<AddNewAddressResponse> addNewAddress(UUID userId, AddNewAddressRequest request);

    APIResource<UpdateAddressResponse> updateAddress(UUID userId, UUID addressId, UpdateAddressRequest request);

    APIResource<DeleteAddressResponse> deleteAddress(UUID userId, UUID addressId);

    APIResource<SetPrimaryAddressResponse> setPrimaryAddress(UUID userId, UUID addressId);
}
