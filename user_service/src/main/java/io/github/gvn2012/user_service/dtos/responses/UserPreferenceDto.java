package io.github.gvn2012.user_service.dtos.responses;

import io.github.gvn2012.user_service.entities.enums.PreferenceCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPreferenceDto {
    private String id;
    private PreferenceCategory category;
    private String preferenceKey;
    private String preferenceValue;
}
