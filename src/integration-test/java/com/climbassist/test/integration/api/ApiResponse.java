package com.climbassist.test.integration.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.http.cookie.Cookie;

import java.util.Set;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class ApiResponse<ResponseType> {

    private com.climbassist.api.ApiResponse.Error error;
    private ResponseType data;
    private Set<Cookie> cookies;
}
