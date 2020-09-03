package com.climbassist.api.recaptcha;

import com.climbassist.api.contact.GetRecaptchaSiteKeyResult;
import com.climbassist.metrics.Metrics;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Builder
@RestController
@Validated
public class RecaptchaController {

    @NonNull
    private final RecaptchaKeysRetriever recaptchaKeysRetriever;

    @Metrics(api = "GetRecaptchaSiteKey")
    @RequestMapping(path = "/v1/recaptcha-site-key", method = RequestMethod.GET)
    public GetRecaptchaSiteKeyResult getRecaptchaSiteKey() throws JsonProcessingException {
        RecaptchaKeys recaptchaKeys = recaptchaKeysRetriever.retrieveRecaptchaKeys();
        return GetRecaptchaSiteKeyResult.builder()
                .siteKey(recaptchaKeys.getSiteKey())
                .build();
    }
}
