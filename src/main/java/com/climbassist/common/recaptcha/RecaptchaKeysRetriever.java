package com.climbassist.common.recaptcha;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.NonNull;

@Builder
public class RecaptchaKeysRetriever {

    @NonNull
    private final String recaptchaKeysSecretId;
    @NonNull
    private final String recaptchaBackDoorResponseSecretId;
    @NonNull
    private final AWSSecretsManager awsSecretsManager;
    @NonNull
    private final ObjectMapper objectMapper;

    public RecaptchaKeys retrieveRecaptchaKeys() throws JsonProcessingException {
        String secretString = awsSecretsManager.getSecretValue(
                new GetSecretValueRequest().withSecretId(recaptchaKeysSecretId))
                .getSecretString();
        return objectMapper.readValue(secretString, RecaptchaKeys.class);
    }

    public String retrieveRecaptchaBackDoorResponse() {
        return awsSecretsManager.getSecretValue(
                new GetSecretValueRequest().withSecretId(recaptchaBackDoorResponseSecretId))
                .getSecretString();
    }
}
