package com.climbassist.api.contact;

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
