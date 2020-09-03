package com.climbassist.api.recaptcha;

import com.climbassist.api.contact.GetRecaptchaSiteKeyResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecaptchaControllerTest {

    private static final RecaptchaKeys RECAPTCHA_KEYS = RecaptchaKeys.builder()
            .siteKey("site-key")
            .secretKey("secret-key")
            .build();
    private static final GetRecaptchaSiteKeyResult GET_RECAPTCHA_SITE_KEY_RESULT = GetRecaptchaSiteKeyResult.builder()
            .siteKey(RECAPTCHA_KEYS.getSiteKey())
            .build();

    @Mock
    private RecaptchaKeysRetriever mockRecaptchaKeysRetriever;

    private RecaptchaController recaptchaController;

    @BeforeEach
    void setUp() {
        recaptchaController = RecaptchaController.builder()
                .recaptchaKeysRetriever(mockRecaptchaKeysRetriever)
                .build();
    }

    @Test
    void getRecaptchaSiteKey_returnsRecaptchaSiteKey() throws JsonProcessingException {
        when(mockRecaptchaKeysRetriever.retrieveRecaptchaKeys()).thenReturn(RECAPTCHA_KEYS);
        assertThat(recaptchaController.getRecaptchaSiteKey(), is(equalTo(GET_RECAPTCHA_SITE_KEY_RESULT)));
        verify(mockRecaptchaKeysRetriever).retrieveRecaptchaKeys();
    }

}
