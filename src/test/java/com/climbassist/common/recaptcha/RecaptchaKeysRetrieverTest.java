package com.climbassist.common.recaptcha;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecaptchaKeysRetrieverTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final RecaptchaKeys RECAPTCHA_KEYS = RecaptchaKeys.builder()
            .siteKey("site-key")
            .secretKey("secret-key")
            .build();
    private static final String RECAPTCHA_BACK_DOOR_RESPONSE = "letmein";
    private static final String RECAPTCHA_KEYS_SECRET_ID = "ClimbAssistRecaptchaKeys";
    private static final String RECAPTCHA_BACK_DOOR_RESPONSE_SECRET_ID = "RecaptchaBackDoorResponse";

    @Mock
    private AWSSecretsManager mockAwsSecretsManager;
    @Mock
    private ObjectMapper mockObjectMapper;

    private RecaptchaKeysRetriever recaptchaKeysRetriever;

    @BeforeEach
    void setUp() {
        recaptchaKeysRetriever = RecaptchaKeysRetriever.builder()
                .recaptchaKeysSecretId(RECAPTCHA_KEYS_SECRET_ID)
                .awsSecretsManager(mockAwsSecretsManager)
                .objectMapper(mockObjectMapper)
                .recaptchaBackDoorResponseSecretId(RECAPTCHA_BACK_DOOR_RESPONSE_SECRET_ID)
                .build();
    }

    @Test
    void retrieveRecaptchaKeys_returnsRecaptchaKeysFromSecretsManager() throws JsonProcessingException {
        String recaptchaKeysAsString = OBJECT_MAPPER.writeValueAsString(RECAPTCHA_KEYS);
        when(mockAwsSecretsManager.getSecretValue(any())).thenReturn(
                new GetSecretValueResult().withSecretString(recaptchaKeysAsString));
        when(mockObjectMapper.readValue(anyString(), eq(RecaptchaKeys.class))).thenReturn(RECAPTCHA_KEYS);
        assertThat(recaptchaKeysRetriever.retrieveRecaptchaKeys(), is(equalTo(RECAPTCHA_KEYS)));
        verify(mockAwsSecretsManager).getSecretValue(
                new GetSecretValueRequest().withSecretId(RECAPTCHA_KEYS_SECRET_ID));
        verify(mockObjectMapper).readValue(recaptchaKeysAsString, RecaptchaKeys.class);
    }

    @Test
    void retrieveRecaptchaBackDoorResponse_returnsRecaptchaBackDoorResponseFromSecretsManager() {
        when(mockAwsSecretsManager.getSecretValue(any())).thenReturn(
                new GetSecretValueResult().withSecretString(RECAPTCHA_BACK_DOOR_RESPONSE));
        assertThat(recaptchaKeysRetriever.retrieveRecaptchaBackDoorResponse(),
                is(equalTo(RECAPTCHA_BACK_DOOR_RESPONSE)));
        verify(mockAwsSecretsManager).getSecretValue(
                new GetSecretValueRequest().withSecretId(RECAPTCHA_BACK_DOOR_RESPONSE_SECRET_ID));
    }
}
