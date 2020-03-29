package com.climbassist.api.user.authentication;

import com.climbassist.api.user.ValidPassword;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor // required for @Builder, because of a bug
@Builder
@Data
@NoArgsConstructor
public class ChangePasswordRequest {

    @ValidPassword
    private String currentPassword;

    @ValidPassword
    private String newPassword;
}
