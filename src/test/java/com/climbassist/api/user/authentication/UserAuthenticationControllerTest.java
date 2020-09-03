package com.climbassist.api.user.authentication;

import com.climbassist.api.user.Alias;
import com.climbassist.api.user.CookieTestUtils;
import com.climbassist.api.user.UserData;
import com.climbassist.api.user.UserManager;
import com.climbassist.api.recaptcha.RecaptchaVerificationException;
import com.climbassist.api.recaptcha.RecaptchaVerifier;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAuthenticationControllerTest {

    private static final String USERNAME = "spock";
    private static final String EMAIL = "spock@enterprise.com";
    private static final String USER_ID = "S179-276SP";
    private static final String PASSWORD = "fascinating";
    private static final String NEW_PASSWORD = "illogical";
    private static final UserSessionData USER_SESSION_DATA = UserSessionData.builder()
            .accessToken("access-token")
            .refreshToken("refresh-token")
            .build();
    private static final RegisterUserRequest REGISTER_USER_REQUEST = RegisterUserRequest.builder()
            .username(USERNAME)
            .email(EMAIL)
            .password(PASSWORD)
            .build();
    private static final RegisterUserResult EXPECTED_REGISTER_USER_RESULT = RegisterUserResult.builder()
            .username(USERNAME)
            .email(EMAIL)
            .build();
    private static final SignInUserRequest SIGN_IN_USER_REQUEST_USERNAME = SignInUserRequest.builder()
            .username(USERNAME)
            .password(PASSWORD)
            .build();
    private static final SignInUserRequest SIGN_IN_USER_REQUEST_EMAIL = SignInUserRequest.builder()
            .email(EMAIL)
            .password(PASSWORD)
            .build();
    private static final SignInUserResult EXPECTED_SIGN_IN_USER_RESULT = SignInUserResult.builder()
            .successful(true)
            .build();
    private static final String ACCESS_TOKEN = "access token";
    private static final ZonedDateTime CURRENT_ZONED_DATE_TIME = ZonedDateTime.now();
    private static final long USER_DATA_RETENTION_TIME_MINUTES = 42;
    private static final UserData USER_DATA = UserData.builder()
            .userId(USER_ID)
            .username(USERNAME)
            .email(EMAIL)
            .isEmailVerified(false)
            .isAdministrator(false)
            .build();
    private static final UserData USER_DATA_WITH_EXPIRATION_TIME = UserData.builder()
            .userId(USER_DATA.getUserId())
            .username(USER_DATA.getUsername())
            .email(USER_DATA.getEmail())
            .isEmailVerified(USER_DATA.isEmailVerified())
            .isAdministrator(USER_DATA.isAdministrator())
            .expirationTime(CURRENT_ZONED_DATE_TIME.plusMinutes(USER_DATA_RETENTION_TIME_MINUTES)
                    .toEpochSecond())
            .build();
    private static final String VERIFICATION_CODE = "123456";
    private static final VerifyEmailRequest VERIFY_EMAIL_REQUEST = VerifyEmailRequest.builder()
            .verificationCode(VERIFICATION_CODE)
            .build();
    private static final ChangePasswordRequest CHANGE_PASSWORD_REQUEST = ChangePasswordRequest.builder()
            .currentPassword(PASSWORD)
            .newPassword(NEW_PASSWORD)
            .build();
    private static final SendPasswordResetEmailResult EXPECTED_SEND_PASSWORD_RESET_EMAIL_RESULT =
            SendPasswordResetEmailResult.builder()
                    .successful(true)
                    .build();
    private static final ResetPasswordResult EXPECTED_RESET_PASSWORD_RESULT = ResetPasswordResult.builder()
            .successful(true)
            .build();
    private static final Alias USERNAME_ALIAS = new Alias(USERNAME, Alias.AliasType.USERNAME);
    private static final Alias EMAIL_ALIAS = new Alias(EMAIL, Alias.AliasType.EMAIL);

    @Mock
    private UserManager mockUserManager;
    @Mock
    private DeletedUsersDao mockDeletedUsersDao;
    @Mock
    private Supplier<ZonedDateTime> mockCurrentZonedDateTimeSupplier;
    @Mock
    private RecaptchaVerifier mockRecaptchaVerifier;

    private MockHttpServletRequest mockHttpServletRequest;

    private MockHttpServletResponse mockHttpServletResponse;

    private UserAuthenticationController userAuthenticationController;

    @BeforeEach
    void setUp() {
        userAuthenticationController = UserAuthenticationController.builder()
                .userManager(mockUserManager)
                .deletedUsersDao(mockDeletedUsersDao)
                .userDataRetentionTimeMinutes(USER_DATA_RETENTION_TIME_MINUTES)
                .currentZonedDateTimeSupplier(mockCurrentZonedDateTimeSupplier)
                .recaptchaVerifier(mockRecaptchaVerifier)
                .build();
        mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.setRemoteAddr("0.0.0.0");
        mockHttpServletResponse = new MockHttpServletResponse();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.setDefault(RegisterUserRequest.class, REGISTER_USER_REQUEST);
        nullPointerTester.setDefault(VerifyEmailRequest.class, VERIFY_EMAIL_REQUEST);
        nullPointerTester.setDefault(ChangePasswordRequest.class, CHANGE_PASSWORD_REQUEST);
        nullPointerTester.setDefault(SignInUserRequest.class, SIGN_IN_USER_REQUEST_USERNAME);
        nullPointerTester.testInstanceMethods(userAuthenticationController, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void register_registersUserAndReturnsUsernameAndEmail()
            throws EmailExistsException, UsernameExistsException, IOException, RecaptchaVerificationException {
        assertThat(userAuthenticationController.register(REGISTER_USER_REQUEST, mockHttpServletRequest),
                is(equalTo(EXPECTED_REGISTER_USER_RESULT)));
        verify(mockRecaptchaVerifier).verifyRecaptchaResult(REGISTER_USER_REQUEST.getRecaptchaResponse(),
                mockHttpServletRequest.getRemoteAddr());
        verify(mockUserManager).register(REGISTER_USER_REQUEST.getUsername(), REGISTER_USER_REQUEST.getEmail(),
                REGISTER_USER_REQUEST.getPassword());
    }

    @Test
    void register_throwsRecaptchaVerificationException_whenRecaptchaIsUnsuccessful()
            throws EmailExistsException, UsernameExistsException, IOException, RecaptchaVerificationException {
        doThrow(new RecaptchaVerificationException(ImmutableSet.of())).when(mockRecaptchaVerifier)
                .verifyRecaptchaResult(any(), any());
        assertThrows(RecaptchaVerificationException.class,
                () -> userAuthenticationController.register(REGISTER_USER_REQUEST, mockHttpServletRequest));
        verify(mockRecaptchaVerifier).verifyRecaptchaResult(REGISTER_USER_REQUEST.getRecaptchaResponse(),
                mockHttpServletRequest.getRemoteAddr());
        verify(mockUserManager, never()).register(any(), any(), any());
    }

    @Test
    void resendInitialVerificationEmail_resendsVerificationEmail() throws UserNotFoundException {
        assertThat(userAuthenticationController.resendInitialVerificationEmail(AliasRequest.builder()
                .username(USERNAME)
                .build()), is(equalTo(ResendInitialVerificationEmailResult.builder()
                .successful(true)
                .build())));
        verify(mockUserManager).resendInitialVerificationEmail(USERNAME_ALIAS);
    }

    @Test
    void signIn_setsCookiesAndReturnsTrue_whenAliasIsEmail() throws AuthenticationException, UserNotFoundException {
        runSignInTest(Alias.AliasType.EMAIL);
    }

    @Test
    void signIn_setsCookiesAndReturnsTrue_whenAliasIsUsername() throws AuthenticationException, UserNotFoundException {
        runSignInTest(Alias.AliasType.USERNAME);
    }

    @Test
    void signOut_removesCookies_whenAccessTokenIsNull() {
        assertThat(userAuthenticationController.signOut(null, mockHttpServletResponse), is(equalTo(
                SignOutUserResult.builder()
                        .successful(true)
                        .build())));
        verify(mockUserManager, never()).isSignedIn(any());
        CookieTestUtils.verifySessionCookiesAreRemoved(mockHttpServletResponse);
    }

    @Test
    void signOut_removesCookies_whenUserIsNotSignedIn() {
        when(mockUserManager.isSignedIn(any())).thenReturn(false);
        assertThat(userAuthenticationController.signOut(ACCESS_TOKEN, mockHttpServletResponse), is(equalTo(
                SignOutUserResult.builder()
                        .successful(true)
                        .build())));
        verify(mockUserManager).isSignedIn(ACCESS_TOKEN);
        CookieTestUtils.verifySessionCookiesAreRemoved(mockHttpServletResponse);
    }

    @Test
    void signOut_signsOutUserAndRemovesCookies_whenUserIsSignedIn() {
        when(mockUserManager.isSignedIn(any())).thenReturn(true);
        assertThat(userAuthenticationController.signOut(ACCESS_TOKEN, mockHttpServletResponse), is(equalTo(
                SignOutUserResult.builder()
                        .successful(true)
                        .build())));
        verify(mockUserManager).isSignedIn(ACCESS_TOKEN);
        verify(mockUserManager).signOut(ACCESS_TOKEN);
        CookieTestUtils.verifySessionCookiesAreRemoved(mockHttpServletResponse);
    }

    @Test
    void deleteUser_deletesUserAndRemovesCookies() {
        when(mockUserManager.getUserData(any())).thenReturn(USER_DATA);
        when(mockCurrentZonedDateTimeSupplier.get()).thenReturn(CURRENT_ZONED_DATE_TIME);
        assertThat(userAuthenticationController.deleteUser(ACCESS_TOKEN, mockHttpServletResponse), is(equalTo(
                DeleteUserResult.builder()
                        .successful(true)
                        .build())));
        verify(mockUserManager).getUserData(ACCESS_TOKEN);
        verify(mockCurrentZonedDateTimeSupplier).get();
        verify(mockDeletedUsersDao).saveResource(USER_DATA_WITH_EXPIRATION_TIME);
        verify(mockUserManager).deleteUser(ACCESS_TOKEN);
        CookieTestUtils.verifySessionCookiesAreRemoved(mockHttpServletResponse);
    }

    @Test
    void verifyEmail_verifiesEmailAndReturnsUserData()
            throws IncorrectVerificationCodeException, EmailAlreadyVerifiedException {
        when(mockUserManager.getUserData(any())).thenReturn(USER_DATA);
        assertThat(userAuthenticationController.verifyEmail(ACCESS_TOKEN, VERIFY_EMAIL_REQUEST),
                is(equalTo(USER_DATA)));
        verify(mockUserManager).verifyEmail(ACCESS_TOKEN, VERIFICATION_CODE);
        verify(mockUserManager).getUserData(ACCESS_TOKEN);
    }

    @Test
    void sendVerificationEmail_sendsVerificationEmailAndReturnsUserData() throws EmailAlreadyVerifiedException {
        when(mockUserManager.getUserData(any())).thenReturn(USER_DATA);
        assertThat(userAuthenticationController.sendVerificationEmail(ACCESS_TOKEN), is(equalTo(USER_DATA)));
        verify(mockUserManager).sendVerificationEmail(ACCESS_TOKEN);
        verify(mockUserManager).getUserData(ACCESS_TOKEN);
    }

    @Test
    void changePassword_changesPasswordAndReturnsUserData() throws IncorrectPasswordException {
        when(mockUserManager.getUserData(any())).thenReturn(USER_DATA);
        assertThat(userAuthenticationController.changePassword(ACCESS_TOKEN, CHANGE_PASSWORD_REQUEST),
                is(equalTo(USER_DATA)));
        verify(mockUserManager).changePassword(ACCESS_TOKEN, PASSWORD, NEW_PASSWORD);
        verify(mockUserManager).getUserData(ACCESS_TOKEN);
    }

    @Test
    void sendPasswordResetEmail_callsSendPasswordResetEmailAndReturnsTrue_whenUsingUsername()
            throws UserNotFoundException, EmailNotVerifiedException, UserNotVerifiedException {
        assertThat(userAuthenticationController.sendPasswordResetEmail(AliasRequest.builder()
                .username(USERNAME)
                .build()), is(equalTo(EXPECTED_SEND_PASSWORD_RESET_EMAIL_RESULT)));
        verify(mockUserManager).sendPasswordResetEmail(USERNAME_ALIAS);
    }

    @Test
    void sendPasswordResetEmail_callsSendPasswordResetEmailAndReturnsTrue_whenUsingEmail()
            throws UserNotFoundException, EmailNotVerifiedException, UserNotVerifiedException {
        assertThat(userAuthenticationController.sendPasswordResetEmail(AliasRequest.builder()
                .email(EMAIL)
                .build()), is(equalTo(EXPECTED_SEND_PASSWORD_RESET_EMAIL_RESULT)));
        verify(mockUserManager).sendPasswordResetEmail(EMAIL_ALIAS);
    }

    @Test
    void resetPassword_callsResetPasswordAndReturnsTrue_whenUsingUsername()
            throws UserNotFoundException, EmailNotVerifiedException, UserNotVerifiedException,
            IncorrectVerificationCodeException {
        assertThat(userAuthenticationController.resetPassword(ResetPasswordRequest.builder()
                .username(USERNAME)
                .verificationCode(VERIFICATION_CODE)
                .newPassword(NEW_PASSWORD)
                .build()), is(equalTo(EXPECTED_RESET_PASSWORD_RESULT)));
        verify(mockUserManager).resetPassword(USERNAME_ALIAS, VERIFICATION_CODE, NEW_PASSWORD);
    }

    @Test
    void resetPassword_callsResetPasswordAndReturnsTrue_whenUsingEmail()
            throws UserNotFoundException, EmailNotVerifiedException, UserNotVerifiedException,
            IncorrectVerificationCodeException {
        assertThat(userAuthenticationController.resetPassword(ResetPasswordRequest.builder()
                .email(EMAIL)
                .verificationCode(VERIFICATION_CODE)
                .newPassword(NEW_PASSWORD)
                .build()), is(equalTo(EXPECTED_RESET_PASSWORD_RESULT)));
        verify(mockUserManager).resetPassword(EMAIL_ALIAS, VERIFICATION_CODE, NEW_PASSWORD);
    }

    private void runSignInTest(Alias.AliasType aliasType) throws AuthenticationException, UserNotFoundException {
        SignInUserRequest signInUserRequest;
        Alias alias;
        switch (aliasType) {
            case EMAIL:
                alias = new Alias(EMAIL, aliasType);
                signInUserRequest = SIGN_IN_USER_REQUEST_EMAIL;
                break;
            case USERNAME:
                alias = new Alias(USERNAME, aliasType);
                signInUserRequest = SIGN_IN_USER_REQUEST_USERNAME;
                break;
            default:
                throw new IllegalArgumentException(String.format("Test run with unsupported AliasType %s", aliasType));
        }

        when(mockUserManager.signIn(any(), any())).thenReturn(USER_SESSION_DATA);

        assertThat(userAuthenticationController.signIn(signInUserRequest, mockHttpServletResponse),
                equalTo(EXPECTED_SIGN_IN_USER_RESULT));

        verify(mockUserManager).signIn(alias, PASSWORD);
        CookieTestUtils.verifySessionCookiesAreCorrect(mockHttpServletResponse, USER_SESSION_DATA);
    }
}
