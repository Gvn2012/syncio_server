package io.github.gvn2012.post_service.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonHelper {

    private final ObjectMapper objectMapper;

    public JsonHelper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T fromJson(String json, TypeReference<T> typeReference, T defaultValue) {
        if (json == null || json.isBlank()) return defaultValue;

        try {
            return objectMapper.readValue(json, typeReference);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public <T> String toJson(T object, String defaultJson) {
        if (object == null) return defaultJson;

        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            return defaultJson;
        }
    }
}