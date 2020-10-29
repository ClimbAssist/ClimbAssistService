package com.climbassist.api.resource.common.image.webpconverter;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.s3.AmazonS3URI;
import com.climbassist.common.s3.AmazonS3UriBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebpConverterTest {

    private static final String WEBP_CONVERTER_LAMBDA_FUNCTION_NAME = "ClimbAssistWebpConverter";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final AmazonS3URI SOURCE_AMAZON_S3_URI =
            AmazonS3UriBuilder.buildAmazonS3Uri("source-bucket", "source-key");
    private static final AmazonS3URI DESTINATION_AMAZON_S3_URI =
            AmazonS3UriBuilder.buildAmazonS3Uri("destination-bucket", "destination-key");
    private static final WebpConverterRequest WEBP_CONVERTER_REQUEST = WebpConverterRequest.builder()
            .sourceLocation(WebpConverterRequest.S3Location.builder()
                    .bucket(SOURCE_AMAZON_S3_URI.getBucket())
                    .key(SOURCE_AMAZON_S3_URI.getKey())
                    .build())
            .destinationLocation(WebpConverterRequest.S3Location.builder()
                    .bucket(DESTINATION_AMAZON_S3_URI.getBucket())
                    .key(DESTINATION_AMAZON_S3_URI.getKey())
                    .build())
            .build();
    private static final WebpConverterResponse WEBP_CONVERTER_RESPONSE_200 = WebpConverterResponse.builder()
            .statusCode(200)
            .body(WebpConverterResponse.Body.builder()
                    .message("It's done but there's blood everywhere")
                    .build())
            .build();
    private static final WebpConverterResponse WEBP_CONVERTER_RESPONSE_400 = WebpConverterResponse.builder()
            .statusCode(400)
            .body(WebpConverterResponse.Body.builder()
                    .message("Big fucking error")
                    .build())
            .build();

    @Mock
    private ObjectMapper mockObjectMapper;
    @Mock
    private AWSLambda mockAwsLambda;

    private WebpConverter webpConverter;

    @BeforeEach
    public void setUp() {
        webpConverter = WebpConverter.builder()
                .objectMapper(mockObjectMapper)
                .awsLambda(mockAwsLambda)
                .webpConverterLambdaFunctionName(WEBP_CONVERTER_LAMBDA_FUNCTION_NAME)
                .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.setDefault(AmazonS3URI.class, SOURCE_AMAZON_S3_URI);
        nullPointerTester.testInstanceMethods(webpConverter, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    public void convertToWebp_invokesWebpConverterLambda_whenStatusCodeIs200()
            throws IOException, WebpConverterException {
        setUpMocks(WEBP_CONVERTER_RESPONSE_200);
        webpConverter.convertToWebp(SOURCE_AMAZON_S3_URI, DESTINATION_AMAZON_S3_URI);
        doVerifications(WEBP_CONVERTER_RESPONSE_200);
    }

    @Test
    public void convertToWebp_throwsWebpConverterException_whenStatusCodeIsNot200() throws IOException {
        setUpMocks(WEBP_CONVERTER_RESPONSE_400);
        WebpConverterException webpConverterException = assertThrows(WebpConverterException.class,
                () -> webpConverter.convertToWebp(SOURCE_AMAZON_S3_URI, DESTINATION_AMAZON_S3_URI));
        assertThat(webpConverterException.getHttpStatus(),
                is(equalTo(HttpStatus.valueOf(WEBP_CONVERTER_RESPONSE_400.getStatusCode()))));
        doVerifications(WEBP_CONVERTER_RESPONSE_400);
    }

    private void setUpMocks(WebpConverterResponse webpConverterResponse) throws IOException {
        when(mockObjectMapper.writeValueAsString(any())).thenReturn(
                OBJECT_MAPPER.writeValueAsString(WEBP_CONVERTER_REQUEST));
        when(mockAwsLambda.invoke(any())).thenReturn(new InvokeResult().withPayload(
                ByteBuffer.wrap(OBJECT_MAPPER.writeValueAsBytes(webpConverterResponse))));
        when(mockObjectMapper.readValue(any(byte[].class), eq(WebpConverterResponse.class))).thenReturn(
                webpConverterResponse);
    }

    private void doVerifications(WebpConverterResponse webpConverterResponse) throws IOException {
        verify(mockObjectMapper).writeValueAsString(WEBP_CONVERTER_REQUEST);
        verify(mockAwsLambda).invoke(new InvokeRequest().withFunctionName(WEBP_CONVERTER_LAMBDA_FUNCTION_NAME)
                .withPayload(OBJECT_MAPPER.writeValueAsString(WEBP_CONVERTER_REQUEST)));
        verify(mockObjectMapper).readValue(OBJECT_MAPPER.writeValueAsBytes(webpConverterResponse),
                WebpConverterResponse.class);
    }
}
