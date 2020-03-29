package com.climbassist.api.resource.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.experimental.UtilityClass;

import java.io.IOException;

/**
 * Utility class to be called by any class that implements DynamoDBTypeConverter
 * We can't generify the direct subclasses of DynamoDBTypeConverter because the unconvert() method in that class doesn't
 * accept a Class parameter.
 */
@Builder
@UtilityClass
public final class GenericTypeConverter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().configure(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static <T> String convert(T object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new InvalidFormatException(object, e);
        }
    }

    public static <T> T unconvert(String object, TypeReference<T> typeReference) {
        try {
            return OBJECT_MAPPER.readValue(object, typeReference);
        } catch (IOException e) {
            throw new InvalidFormatException(object, typeReference.getType()
                    .getTypeName(), e);
        }
    }
}
