package com.climbassist.api.user.authentication;

import com.climbassist.api.user.Alias;
import com.climbassist.api.user.SessionUtils;
import com.climbassist.api.user.UserData;
import com.climbassist.api.user.UserManager;
import com.climbassist.api.user.authorization.AuthenticatedAuthorizationHandler;
import com.climbassist.api.user.authorization.Authorization;
import com.climbassist.metrics.Metrics;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Optional;

@Builder
@RestController
@Slf4j
public class UserAuthenticationController {

    private static final String COOKIE_PATH = "/";

    @NonNull
    private final UserManager userManager;

    @Metrics(api = "RegisterUser")
    @RequestMapping(path = "/v1/user/register", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public RegisterUserResult register(@NonNull @Valid @RequestBody RegisterUserRequest registerUserRequest)
            throws UsernameExistsException, EmailExistsException {
        userManager.register(registerUserRequest.getUsername(), registerUserRequest.getEmail(),
                registerUserRequest.getPassword());
        return RegisterUserResult.builder()
                .username(registerUserRequest.getUsername())
                .email(registerUserRequest.getEmail())
                .build();
    }

    @Metrics(api = "ResendInitialVerificationEmail")
    @RequestMapping(path = "v1/user/resend-initial-verification-email", method = RequestMethod.POST)
    public ResendInitialVerificationEmailResult resendInitialVerificationEmail(
            @NonNull @Valid @RequestBody AliasRequest aliasRequest) throws UserNotFoundException {
        userManager.resendInitialVerificationEmail(new Alias(Optional.ofNullable(aliasRequest.getUsername()),
                Optional.ofNullable(aliasRequest.getEmail())));
        return ResendInitialVerificationEmailResult.builder()
                .successful(true)
                .build();
    }

    @Metrics(api = "SignIn")
    @RequestMapping(path = "/v1/user/sign-in", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public SignInUserResult signIn(@NonNull @Valid @RequestBody SignInUserRequest signInUserRequest,
                                   @NonNull HttpServletResponse httpServletResponse)
            throws UserNotFoundException, UserNotVerifiedException, EmailNotVerifiedException,
            IncorrectPasswordException {
        Alias alias = new Alias(Optional.ofNullable(signInUserRequest.getUsername()),
                Optional.ofNullable(signInUserRequest.getEmail()));
        UserSessionData userSessionData = userManager.signIn(alias, signInUserRequest.getPassword());
        SessionUtils.setSessionCookies(httpServletResponse, userSessionData);
        return SignInUserResult.builder()
                .successful(true)
                .build();
    }

    @Metrics(api = "SignOut")
    @RequestMapping(path = "/v1/user/sign-out", method = RequestMethod.POST)
    public SignOutUserResult signOut(
            @SessionAttribute(value = SessionUtils.ACCESS_TOKEN_SESSION_ATTRIBUTE_NAME, required = false) @Nullable
                    String accessToken, @NonNull HttpServletResponse httpServletResponse) {
        if (accessToken != null && userManager.isSignedIn(accessToken)) {
            userManager.signOut(accessToken);
        }
        SessionUtils.removeSessionCookies(httpServletResponse);
        return SignOutUserResult.builder()
                .successful(true)
                .build();
    }

    @Metrics(api = "DeleteUser")
    @Authorization(AuthenticatedAuthorizationHandler.class)
    @RequestMapping(path = "/v1/user", method = RequestMethod.DELETE)
    public DeleteUserResult deleteUser(
            @SessionAttribute(SessionUtils.ACCESS_TOKEN_SESSION_ATTRIBUTE_NAME) @NonNull String accessToken,
            @NonNull HttpServletResponse httpServletResponse) {
        userManager.deleteUser(accessToken);
        SessionUtils.removeSessionCookies(httpServletResponse);
        return DeleteUserResult.builder()
                .successful(true)
                .build();
    }

    @Metrics(api = "VerifyEmail")
    @Authorization(AuthenticatedAuthorizationHandler.class)
    @RequestMapping(path = "v1/user/verify-email", method = RequestMethod.POST)
    public UserData verifyEmail(
            @SessionAttribute(SessionUtils.ACCESS_TOKEN_SESSION_ATTRIBUTE_NAME) @NonNull String accessToken,
            @NonNull @Valid @RequestBody VerifyEmailRequest verifyEmailRequest)
            throws IncorrectVerificationCodeException, EmailAlreadyVerifiedException {
        userManager.verifyEmail(accessToken, verifyEmailRequest.getVerificationCode());
        return userManager.getUserData(accessToken);
    }

    @Metrics(api = "SendVerificationEmail")
    @Authorization(AuthenticatedAuthorizationHandler.class)
    @RequestMapping(path = "v1/user/send-verification-email", method = RequestMethod.POST)
    public UserData sendVerificationEmail(
            @SessionAttribute(SessionUtils.ACCESS_TOKEN_SESSION_ATTRIBUTE_NAME) @NonNull String accessToken)
            throws EmailAlreadyVerifiedException {
        userManager.sendVerificationEmail(accessToken);
        return userManager.getUserData(accessToken);
    }

    @Metrics(api = "ChangePassword")
    @Authorization(AuthenticatedAuthorizationHandler.class)
    @RequestMapping(path = "/v1/user/change-password", method = RequestMethod.POST)
    public UserData changePassword(
            @SessionAttribute(SessionUtils.ACCESS_TOKEN_SESSION_ATTRIBUTE_NAME) @NonNull String accessToken,
            @NonNull @Valid @RequestBody ChangePasswordRequest changePasswordRequest)
            throws IncorrectPasswordException {
        userManager.changePassword(accessToken, changePasswordRequest.getCurrentPassword(),
                changePasswordRequest.getNewPassword());
        return userManager.getUserData(accessToken);
    }

    @Metrics(api = "SendPasswordResetEmail")
    @RequestMapping(path = "/v1/user/send-password-reset-email", method = RequestMethod.POST)
    public SendPasswordResetEmailResult sendPasswordResetEmail(@NonNull @Valid @RequestBody AliasRequest aliasRequest)
            throws UserNotFoundException, EmailNotVerifiedException, UserNotVerifiedException {
        Alias alias = new Alias(Optional.ofNullable(aliasRequest.getUsername()),
                Optional.ofNullable(aliasRequest.getEmail()));
        userManager.sendPasswordResetEmail(alias);
        return SendPasswordResetEmailResult.builder()
                .successful(true)
                .build();
    }

    @Metrics(api = "ResetPassword")
    @RequestMapping(path = "/v1/user/reset-password", method = RequestMethod.POST)
    public ResetPasswordResult resetPassword(@NonNull @Valid @RequestBody ResetPasswordRequest resetPasswordRequest)
            throws UserNotFoundException, UserNotVerifiedException, IncorrectVerificationCodeException,
            EmailNotVerifiedException {
        Alias alias = new Alias(Optional.ofNullable(resetPasswordRequest.getUsername()),
                Optional.ofNullable(resetPasswordRequest.getEmail()));
        userManager.resetPassword(alias, resetPasswordRequest.getVerificationCode(),
                resetPasswordRequest.getNewPassword());
        return ResetPasswordResult.builder()
                .successful(true)
                .build();
    }
}
