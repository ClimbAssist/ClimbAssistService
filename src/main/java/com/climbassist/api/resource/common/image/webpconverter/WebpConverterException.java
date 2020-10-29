package com.climbassist.api.resource.common.image.webpconverter;

import com.climbassist.api.ApiException;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

public class WebpConverterException extends ApiException {

    public WebpConverterException(@NonNull final String message) {
        super(String.format(
                "Unable to convert image to WEBP format. Ensure the image uploaded is a valid JPG image. %s", message));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getType() {
        return getClass().getSimpleName();
    }
}
