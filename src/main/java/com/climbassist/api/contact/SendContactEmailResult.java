package com.climbassist.api.contact;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SendContactEmailResult {

    private boolean successful;
}
