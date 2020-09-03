package com.climbassist.test.integration.api.recaptcha;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import lombok.Builder;
import lombok.NonNull;

@Builder
public class RecaptchaBackDoorResponseRetriever {

    @NonNull
    private final AWSSecretsManager awsSecretsManager;
    @NonNull
    private final String recaptchaBackDoorResponseSecretId;

    public String retrieveRecaptchaBackDoorResponse() {
        return awsSecretsManager.getSecretValue(
                new GetSecretValueRequest().withSecretId(recaptchaBackDoorResponseSecretId))
                .getSecretString();
    }
}
