package io.github.gvn2012.user_service.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfilePictureRequest {

    @NotNull(message = "Image ID must not be null")
    private UUID imageId;
}
