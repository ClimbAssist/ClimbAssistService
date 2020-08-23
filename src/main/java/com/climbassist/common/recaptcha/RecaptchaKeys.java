package com.climbassist.common.recaptcha;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class RecaptchaKeys {

    private String siteKey;
    private String secretKey;
}
