package com.climbassist.api.resource.common.image.webpconverter;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.s3.AmazonS3URI;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Builder
@Slf4j
public class WebpConverter {

    @NonNull
    private final AWSLambda awsLambda;
    @NonNull
    private final String webpConverterLambdaFunctionName;
    @NonNull
    private final ObjectMapper objectMapper;

    public void convertToWebp(@NonNull AmazonS3URI source, @NonNull AmazonS3URI destination)
            throws IOException, WebpConverterException {
        WebpConverterRequest webpConverterRequest = WebpConverterRequest.builder()
                .sourceLocation(WebpConverterRequest.S3Location.builder()
                        .bucket(source.getBucket())
                        .key(source.getKey())
                        .build())
                .destinationLocation(WebpConverterRequest.S3Location.builder()
                        .bucket(destination.getBucket())
                        .key(destination.getKey())
                        .build())
                .build();
        WebpConverterResponse webpConverterResponse = objectMapper.readValue(awsLambda.invoke(
                new InvokeRequest().withFunctionName(webpConverterLambdaFunctionName)
                        .withPayload(objectMapper.writeValueAsString(webpConverterRequest)))
                .getPayload()
                .array(), WebpConverterResponse.class);
        if (webpConverterResponse.getStatusCode() != 200) {
            log.error(String.format("Unable to convert image to WEBP format. Received the following response: %s",
                    webpConverterResponse));
            throw new WebpConverterException(webpConverterResponse.getBody()
                    .getMessage());
        }
    }
}
