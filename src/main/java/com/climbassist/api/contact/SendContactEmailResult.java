package com.climbassist.api.contact;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
class SendContactEmailResult {

    private boolean successful;
}
