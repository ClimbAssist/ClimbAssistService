package com.climbassist.api.resource.common.image.webpconverter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor // required for @Builder, because of a bug
@Builder
@Data
@NoArgsConstructor
public class WebpConverterResponse {

    @AllArgsConstructor // required for @Builder, because of a bug
    @Builder
    @Data
    @NoArgsConstructor
    public static class Body {
        @NonNull
        private String message;
    }

    private int statusCode;
    @NonNull
    private Body body;
}
