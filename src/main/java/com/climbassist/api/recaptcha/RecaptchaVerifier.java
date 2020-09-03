package com.climbassist.api.recaptcha;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;

/**
 * Verifies a reCAPTCHA response against Google's servers.
 * https://developers.google.com/recaptcha/docs/verify
 */
@Builder
@Slf4j
public class RecaptchaVerifier {

    @NonNull
    private final RecaptchaKeysRetriever recaptchaKeysRetriever;
    @NonNull
    private final HttpClient httpClient;
    @NonNull
    private final ObjectMapper objectMapper;

    public void verifyRecaptchaResult(@NonNull String recaptchaResponse, @NonNull String remoteIp)
            throws IOException, RecaptchaVerificationException {
        String recaptchaBackDoorResponse = recaptchaKeysRetriever.retrieveRecaptchaBackDoorResponse();
        if (recaptchaBackDoorResponse.equals(recaptchaResponse)) {
            log.info(String.format("Received recaptcha response matching the back door response for remoteIp %s.",
                    remoteIp));
            return;
        }
        RecaptchaKeys recaptchaKeys = recaptchaKeysRetriever.retrieveRecaptchaKeys();
        HttpPost httpPost = new HttpPost("https://www.google.com/recaptcha/api/siteverify");
        httpPost.setEntity(new UrlEncodedFormEntity(
                ImmutableList.of(new BasicNameValuePair("secret", recaptchaKeys.getSecretKey()),
                        new BasicNameValuePair("response", recaptchaResponse),
                        new BasicNameValuePair("remoteip", remoteIp))));
        HttpResponse httpResponse = httpClient.execute(httpPost);
        RecaptchaVerificationResponse recaptchaVerificationResponse = objectMapper.readValue(httpResponse.getEntity()
                .getContent(), RecaptchaVerificationResponse.class);
        if (!recaptchaVerificationResponse.isSuccess()) {
            throw new RecaptchaVerificationException(recaptchaVerificationResponse.getErrorCodes());
        }
    }
}
