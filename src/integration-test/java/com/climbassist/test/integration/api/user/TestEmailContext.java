package com.climbassist.test.integration.api.user;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class TestEmailContext {

    private String email;
    private String topicArn;
    private String queueUrl;
    private String subscriptionArn;
    private String ruleSetName;
}
