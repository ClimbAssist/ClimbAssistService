package com.climbassist.api.contact.recaptcha;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.NullPointerTester;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.BasicStatusLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecaptchaVerifierTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final RecaptchaKeys RECAPTCHA_KEYS = RecaptchaKeys.builder()
            .siteKey("site-key")
            .secretKey("secret-key")
            .build();
    private static final String RECAPTCHA_RESPONSE = "recaptcha-response";
    private static final String REMOTE_IP = "0.0.0.0";

    @Mock
    private RecaptchaKeysRetriever mockRecaptchaKeysRetriever;
    @Mock
    private ObjectMapper mockObjectMapper;
    @Mock
    private HttpClient mockHttpClient;

    private RecaptchaVerifier recaptchaVerifier;

    @BeforeEach
    void setUp() {
        recaptchaVerifier = RecaptchaVerifier.builder()
                .recaptchaKeysRetriever(mockRecaptchaKeysRetriever)
                .objectMapper(mockObjectMapper)
                .httpClient(mockHttpClient)
                .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testInstanceMethods(recaptchaVerifier, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void verifyRecaptchaResult_doesNothing_whenRecaptchaIsSuccessful()
            throws IOException, RecaptchaVerificationException {
        RecaptchaVerificationResponse recaptchaVerificationResponse = RecaptchaVerificationResponse.builder()
                .success(true)
                .errorCodes(ImmutableSet.of())
                .build();
        setUpMocks(recaptchaVerificationResponse);
        recaptchaVerifier.verifyRecaptchaResult(RECAPTCHA_RESPONSE, REMOTE_IP);
        verifyMocks(recaptchaVerificationResponse);
    }

    @Test
    void verifyRecaptchaResult_throwsRecaptchaVerificationException_whenRecaptchaIsNotSuccessful() throws IOException {
        RecaptchaVerificationResponse recaptchaVerificationResponse = RecaptchaVerificationResponse.builder()
                .success(false)
                .errorCodes(ImmutableSet.of("invalid-input-response"))
                .build();
        setUpMocks(recaptchaVerificationResponse);
        assertThrows(RecaptchaVerificationException.class,
                () -> recaptchaVerifier.verifyRecaptchaResult(RECAPTCHA_RESPONSE, REMOTE_IP));
        verifyMocks(recaptchaVerificationResponse);
    }

    private void setUpMocks(RecaptchaVerificationResponse recaptchaVerificationResponse) throws IOException {
        when(mockRecaptchaKeysRetriever.retrieveRecaptchaKeys()).thenReturn(RECAPTCHA_KEYS);
        HttpResponse httpResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, ""));
        httpResponse.setEntity(new StringEntity(OBJECT_MAPPER.writeValueAsString(recaptchaVerificationResponse)));
        when(mockHttpClient.execute(any())).thenReturn(httpResponse);
        when(mockObjectMapper.readValue(any(InputStream.class), eq(RecaptchaVerificationResponse.class))).thenReturn(
                recaptchaVerificationResponse);
    }

    private void verifyMocks(RecaptchaVerificationResponse recaptchaVerificationResponse) throws IOException {
        verify(mockRecaptchaKeysRetriever).retrieveRecaptchaKeys();
        String expectedEntityString = IOUtils.toString(new UrlEncodedFormEntity(
                ImmutableList.of(new BasicNameValuePair("secret", RECAPTCHA_KEYS.getSecretKey()),
                        new BasicNameValuePair("response", RECAPTCHA_RESPONSE),
                        new BasicNameValuePair("remoteip", REMOTE_IP))).getContent());
        ArgumentCaptor<HttpPost> httpPostArgumentCaptor = ArgumentCaptor.forClass(HttpPost.class);
        verify(mockHttpClient).execute(httpPostArgumentCaptor.capture());
        HttpPost httpPost = httpPostArgumentCaptor.getValue();
        assertThat(httpPost.getURI(), is(equalTo(URI.create("https://www.google.com/recaptcha/api/siteverify"))));
        assertThat(IOUtils.toString(httpPost.getEntity()
                .getContent()), is(equalTo(expectedEntityString)));
        ArgumentCaptor<InputStream> inputStreamArgumentCaptor = ArgumentCaptor.forClass(InputStream.class);
        verify(mockObjectMapper).readValue(inputStreamArgumentCaptor.capture(),
                eq(RecaptchaVerificationResponse.class));
        assertThat(IOUtils.toString(inputStreamArgumentCaptor.getValue()),
                is(equalTo(OBJECT_MAPPER.writeValueAsString(recaptchaVerificationResponse))));
    }
}
