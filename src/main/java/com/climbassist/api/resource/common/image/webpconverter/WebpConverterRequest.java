package com.climbassist.api.resource.common.image.webpconverter;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Builder
@Value
public class WebpConverterRequest {

    @Builder
    @Value
    public static class S3Location {
        @NonNull String bucket;
        @NonNull String key;
    }

    @NonNull S3Location sourceLocation;
    @NonNull S3Location destinationLocation;
}
