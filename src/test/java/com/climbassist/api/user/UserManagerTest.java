package com.climbassist.api.user;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminListGroupsForUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminListGroupsForUserResult;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import com.amazonaws.services.cognitoidp.model.ChangePasswordRequest;
import com.amazonaws.services.cognitoidp.model.CodeMismatchException;
import com.amazonaws.services.cognitoidp.model.ConfirmForgotPasswordRequest;
import com.amazonaws.services.cognitoidp.model.DeleteUserRequest;
import com.amazonaws.services.cognitoidp.model.ExpiredCodeException;
import com.amazonaws.services.cognitoidp.model.ForgotPasswordRequest;
import com.amazonaws.services.cognitoidp.model.GetUserAttributeVerificationCodeRequest;
import com.amazonaws.services.cognitoidp.model.GetUserRequest;
import com.amazonaws.services.cognitoidp.model.GetUserResult;
import com.amazonaws.services.cognitoidp.model.GlobalSignOutRequest;
import com.amazonaws.services.cognitoidp.model.GroupType;
import com.amazonaws.services.cognitoidp.model.InitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.InitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.ListUsersRequest;
import com.amazonaws.services.cognitoidp.model.ListUsersResult;
import com.amazonaws.services.cognitoidp.model.NotAuthorizedException;
import com.amazonaws.services.cognitoidp.model.ResendConfirmationCodeRequest;
import com.amazonaws.services.cognitoidp.model.SignUpRequest;
import com.amazonaws.services.cognitoidp.model.UpdateUserAttributesRequest;
import com.amazonaws.services.cognitoidp.model.UserNotFoundException;
import com.amazonaws.services.cognitoidp.model.UserStatusType;
import com.amazonaws.services.cognitoidp.model.UserType;
import com.amazonaws.services.cognitoidp.model.VerifyUserAttributeRequest;
import com.climbassist.api.user.authentication.AccessTokenExpiredException;
import com.climbassist.api.user.authentication.EmailAlreadyVerifiedException;
import com.climbassist.api.user.authentication.EmailExistsException;
import com.climbassist.api.user.authentication.EmailNotVerifiedException;
import com.climbassist.api.user.authentication.InvalidPasswordException;
import com.climbassist.api.user.authentication.InvalidVerificationCodeException;
import com.climbassist.api.user.authentication.UserAuthenticationException;
import com.climbassist.api.user.authentication.UserNotVerifiedException;
import com.climbassist.api.user.authentication.UserSessionData;
import com.climbassist.api.user.authentication.UsernameExistsException;
import com.climbassist.api.user.authorization.SessionExpiredException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserManagerTest {

    private static final String USERNAME = "spock";
    private static final String EMAIL = "spock@enterprise.com";
    private static final String PASSWORD = "fascinating";
    private static final String NEW_PASSWORD = "illogical";
    private static final String USER_POOL_ID = "userPoolId";
    private static final String USER_POOL_CLIENT_ID = "clientId";
    private static final String ACCESS_TOKEN = "access-token";
    private static final String REFRESH_TOKEN = "refresh-token";
    private static final String VERIFICATION_CODE = "123456";
    private static final String EMAIL_ATTRIBUTE_NAME = "email";
    private static final String EMAIL_VERIFIED_ATTRIBUTE_NAME = "email_verified";
    private static final Alias USERNAME_ALIAS = new Alias(USERNAME, Alias.AliasType.USERNAME);
    private static final Alias EMAIL_ALIAS = new Alias(EMAIL, Alias.AliasType.EMAIL);
    private static final InitiateAuthRequest EXPECTED_INITIATE_AUTH_REQUEST_FOR_SIGN_UP =
            new InitiateAuthRequest().withClientId(USER_POOL_CLIENT_ID)
                    .withAuthFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .withAuthParameters(ImmutableMap.of("USERNAME", EMAIL, "PASSWORD", " "));
    private static final SignUpRequest EXPECTED_SIGN_UP_REQUEST = new SignUpRequest().withClientId(USER_POOL_CLIENT_ID)
            .withUsername(USERNAME)
            .withPassword(PASSWORD)
            .withUserAttributes(new AttributeType().withName(EMAIL_ATTRIBUTE_NAME)
                    .withValue(EMAIL));
    private static final ListUsersRequest EXPECTED_LIST_USERS_REQUEST_USERNAME = new ListUsersRequest().withUserPoolId(
            USER_POOL_ID)
            .withFilter(String.format("username=\"%s\"", USERNAME));
    private static final ListUsersRequest EXPECTED_LIST_USERS_REQUEST_EMAIL = new ListUsersRequest().withUserPoolId(
            USER_POOL_ID)
            .withFilter(String.format("email=\"%s\"", EMAIL));
    private static final InitiateAuthRequest EXPECTED_INITIATE_AUTH_REQUEST_FOR_SIGN_IN_USERNAME =
            new InitiateAuthRequest().withClientId(USER_POOL_CLIENT_ID)
                    .withAuthFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .withAuthParameters(ImmutableMap.of("USERNAME", USERNAME, "PASSWORD", PASSWORD));
    private static final InitiateAuthRequest EXPECTED_INITIATE_AUTH_REQUEST_FOR_SIGN_IN_EMAIL =
            new InitiateAuthRequest().withClientId(USER_POOL_CLIENT_ID)
                    .withAuthFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .withAuthParameters(ImmutableMap.of("USERNAME", EMAIL, "PASSWORD", PASSWORD));
    private static final GetUserRequest EXPECTED_GET_USER_REQUEST = new GetUserRequest().withAccessToken(ACCESS_TOKEN);
    private static final GetUserResult GET_USER_RESULT_EMAIL_VERIFIED = new GetUserResult().withUsername(USERNAME)
            .withUserAttributes(new AttributeType().withName(EMAIL_ATTRIBUTE_NAME)
                    .withValue(EMAIL), new AttributeType().withName("email_verified")
                    .withValue("true"));
    private static final GetUserResult GET_USER_RESULT_EMAIL_NOT_VERIFIED = new GetUserResult().withUsername(USERNAME)
            .withUserAttributes(new AttributeType().withName(EMAIL_ATTRIBUTE_NAME)
                    .withValue(EMAIL), new AttributeType().withName(EMAIL_VERIFIED_ATTRIBUTE_NAME)
                    .withValue("false"));
    private static final AdminListGroupsForUserRequest EXPECTED_ADMIN_LIST_GROUPS_FOR_USER_REQUEST =
            new AdminListGroupsForUserRequest().withUserPoolId(USER_POOL_ID)
                    .withUsername(USERNAME);
    private static final VerifyUserAttributeRequest EXPECTED_VERIFY_USER_ATTRIBUTE_REQUEST =
            new VerifyUserAttributeRequest().withAccessToken(ACCESS_TOKEN)
                    .withAttributeName(EMAIL_ATTRIBUTE_NAME)
                    .withCode(VERIFICATION_CODE);
    private static final UserData EXPECTED_USER_DATA_EMAIL_VERIFIED = UserData.builder()
            .username(USERNAME)
            .email(EMAIL)
            .isAdministrator(false)
            .isEmailVerified(true)
            .build();
    private static final ListUsersResult LIST_USERS_RESULT = new ListUsersResult().withUsers(ImmutableList.of(
            new UserType().withUserStatus(UserStatusType.CONFIRMED)
                    .withAttributes(new AttributeType().withName(EMAIL_VERIFIED_ATTRIBUTE_NAME)
                            .withValue("true"))));

    @Mock
    private AWSCognitoIdentityProvider mockAwsCognitoIdentityProvider;

    private UserManager userManager;

    @BeforeEach
    void setUp() {
        userManager = UserManager.builder()
                .userPoolId(USER_POOL_ID)
                .userPoolClientId(USER_POOL_CLIENT_ID)
                .awsCognitoIdentityProvider(mockAwsCognitoIdentityProvider)
                .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.setDefault(Alias.class, USERNAME_ALIAS);
        nullPointerTester.testInstanceMethods(userManager, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void register_throwsEmailExistsException_whenEmailHasAlreadyBeenRegistered() {
        when(mockAwsCognitoIdentityProvider.initiateAuth(any())).thenThrow(
                new NotAuthorizedException("not authorized"));
        EmailExistsException emailExistsException = assertThrows(EmailExistsException.class,
                () -> userManager.register(USERNAME, EMAIL, PASSWORD));
        assertThat(emailExistsException.getMessage(),
                is(equalTo(String.format("User with email %s already exists.", EMAIL))));
        verify(mockAwsCognitoIdentityProvider).initiateAuth(EXPECTED_INITIATE_AUTH_REQUEST_FOR_SIGN_UP);
    }

    @Test
    void register_throwsEmailExistsException_whenInitiateAuthDoesNotThrowException() {
        EmailExistsException emailExistsException = assertThrows(EmailExistsException.class,
                () -> userManager.register(USERNAME, EMAIL, PASSWORD));
        assertThat(emailExistsException.getMessage(),
                is(equalTo(String.format("User with email %s already exists.", EMAIL))));
        verify(mockAwsCognitoIdentityProvider).initiateAuth(EXPECTED_INITIATE_AUTH_REQUEST_FOR_SIGN_UP);
    }

    @Test
    void register_throwsUsernameExistsException_whenSignUpThrowsUsernameExistsException() {
        when(mockAwsCognitoIdentityProvider.initiateAuth(any())).thenThrow(new UserNotFoundException(""));
        when(mockAwsCognitoIdentityProvider.signUp(any())).thenThrow(
                new com.amazonaws.services.cognitoidp.model.UsernameExistsException(""));
        UsernameExistsException usernameExistsException = assertThrows(UsernameExistsException.class,
                () -> userManager.register(USERNAME, EMAIL, PASSWORD));
        assertThat(usernameExistsException.getMessage(),
                is(equalTo(String.format("User with username %s already exists.", USERNAME))));
        verify(mockAwsCognitoIdentityProvider).initiateAuth(EXPECTED_INITIATE_AUTH_REQUEST_FOR_SIGN_UP);
        verify(mockAwsCognitoIdentityProvider).signUp(EXPECTED_SIGN_UP_REQUEST);
    }

    @Test
    void register_signsUpUser() throws EmailExistsException, UsernameExistsException {
        when(mockAwsCognitoIdentityProvider.initiateAuth(any())).thenThrow(new UserNotFoundException(""));
        userManager.register(USERNAME, EMAIL, PASSWORD);
        verify(mockAwsCognitoIdentityProvider).initiateAuth(EXPECTED_INITIATE_AUTH_REQUEST_FOR_SIGN_UP);
        verify(mockAwsCognitoIdentityProvider).signUp(EXPECTED_SIGN_UP_REQUEST);
    }

    @Test
    void resendInitialVerificationEmail_resendsVerificationEmail()
            throws com.climbassist.api.user.authentication.UserNotFoundException {
        userManager.resendInitialVerificationEmail(USERNAME_ALIAS);
        verify(mockAwsCognitoIdentityProvider).resendConfirmationCode(
                new ResendConfirmationCodeRequest().withClientId(USER_POOL_CLIENT_ID)
                        .withUsername(USERNAME_ALIAS.getValue()));
    }

    @Test
    void resendInitialVerificationEmail_throwsUserNotFoundException_whenCognitoThrowsUserNotFoundException() {
        when(mockAwsCognitoIdentityProvider.resendConfirmationCode(any())).thenThrow(new UserNotFoundException(""));
        assertThrows(com.climbassist.api.user.authentication.UserNotFoundException.class,
                () -> userManager.resendInitialVerificationEmail(USERNAME_ALIAS));
        verify(mockAwsCognitoIdentityProvider).resendConfirmationCode(
                new ResendConfirmationCodeRequest().withClientId(USER_POOL_CLIENT_ID)
                        .withUsername(USERNAME_ALIAS.getValue()));
    }

    @Test
    void signIn_throwsUserNotFoundException_whenNoUsersExistMatchingAlias() {
        runUserNotFoundTest(() -> userManager.signIn(USERNAME_ALIAS, PASSWORD));
    }

    @Test
    void signIn_throwsUserNotVerifiedException_whenUserIsNotVerified() {
        runUserNotVerifiedTest(() -> userManager.signIn(USERNAME_ALIAS, PASSWORD));
    }

    @Test
    void signIn_throwsEmailNotVerifiedException_whenEmailIsNotVerified() {
        runEmailNotVerifiedTest(() -> userManager.signIn(EMAIL_ALIAS, PASSWORD));
    }

    @Test
    void signIn_throwsInvalidPasswordException_whenPasswordIsIncorrect() {
        when(mockAwsCognitoIdentityProvider.listUsers(any())).thenReturn(LIST_USERS_RESULT);
        when(mockAwsCognitoIdentityProvider.initiateAuth(any())).thenThrow(new NotAuthorizedException(""));
        assertThrows(InvalidPasswordException.class, () -> userManager.signIn(USERNAME_ALIAS, PASSWORD));
        verify(mockAwsCognitoIdentityProvider).listUsers(EXPECTED_LIST_USERS_REQUEST_USERNAME);
        verify(mockAwsCognitoIdentityProvider).initiateAuth(EXPECTED_INITIATE_AUTH_REQUEST_FOR_SIGN_IN_USERNAME);
    }

    @Test
    void signIn_returnsUserSessionData_whenSigningInWithUsername()
            throws UserAuthenticationException, com.climbassist.api.user.authentication.UserNotFoundException {
        when(mockAwsCognitoIdentityProvider.listUsers(any())).thenReturn(LIST_USERS_RESULT);
        when(mockAwsCognitoIdentityProvider.initiateAuth(any())).thenReturn(
                new InitiateAuthResult().withAuthenticationResult(
                        new AuthenticationResultType().withAccessToken(ACCESS_TOKEN)
                                .withRefreshToken(REFRESH_TOKEN)));
        assertThat(userManager.signIn(USERNAME_ALIAS, PASSWORD), is(equalTo(UserSessionData.builder()
                .accessToken(ACCESS_TOKEN)
                .refreshToken(REFRESH_TOKEN)
                .build())));
        verify(mockAwsCognitoIdentityProvider).listUsers(EXPECTED_LIST_USERS_REQUEST_USERNAME);
        verify(mockAwsCognitoIdentityProvider).initiateAuth(EXPECTED_INITIATE_AUTH_REQUEST_FOR_SIGN_IN_USERNAME);
    }

    @Test
    void signIn_returnsUserSessionData_whenSigningInWithEmail()
            throws UserAuthenticationException, com.climbassist.api.user.authentication.UserNotFoundException {
        when(mockAwsCognitoIdentityProvider.listUsers(any())).thenReturn(LIST_USERS_RESULT);
        when(mockAwsCognitoIdentityProvider.initiateAuth(any())).thenReturn(
                new InitiateAuthResult().withAuthenticationResult(
                        new AuthenticationResultType().withAccessToken(ACCESS_TOKEN)
                                .withRefreshToken(REFRESH_TOKEN)));
        assertThat(userManager.signIn(EMAIL_ALIAS, PASSWORD), is(equalTo(UserSessionData.builder()
                .accessToken(ACCESS_TOKEN)
                .refreshToken(REFRESH_TOKEN)
                .build())));
        verify(mockAwsCognitoIdentityProvider).listUsers(EXPECTED_LIST_USERS_REQUEST_EMAIL);
        verify(mockAwsCognitoIdentityProvider).initiateAuth(EXPECTED_INITIATE_AUTH_REQUEST_FOR_SIGN_IN_EMAIL);
    }

    @Test
    void signOut_signsOutUser() {
        userManager.signOut(ACCESS_TOKEN);
        verify(mockAwsCognitoIdentityProvider).globalSignOut(new GlobalSignOutRequest().withAccessToken(ACCESS_TOKEN));
    }

    @Test
    void refreshAccessToken_returnsNewAccessToken() throws SessionExpiredException {
        when(mockAwsCognitoIdentityProvider.initiateAuth(any())).thenReturn(
                new InitiateAuthResult().withAuthenticationResult(
                        new AuthenticationResultType().withAccessToken(ACCESS_TOKEN)));
        assertThat(userManager.refreshAccessToken(REFRESH_TOKEN), is(equalTo(ACCESS_TOKEN)));
        verify(mockAwsCognitoIdentityProvider).initiateAuth(new InitiateAuthRequest().withClientId(USER_POOL_CLIENT_ID)
                .withAuthFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                .withAuthParameters(ImmutableMap.of("REFRESH_TOKEN", REFRESH_TOKEN)));
    }

    @Test
    void refreshAccessToken_throwsSessionExpiredException_whenNotAuthorizedExceptionIsThrown() {
        when(mockAwsCognitoIdentityProvider.initiateAuth(any())).thenThrow(new NotAuthorizedException(""));
        assertThrows(SessionExpiredException.class, () -> userManager.refreshAccessToken(REFRESH_TOKEN));
        verify(mockAwsCognitoIdentityProvider).initiateAuth(new InitiateAuthRequest().withClientId(USER_POOL_CLIENT_ID)
                .withAuthFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                .withAuthParameters(ImmutableMap.of("REFRESH_TOKEN", REFRESH_TOKEN)));
    }

    @Test
    void isSignedIn_returnsTrue_whenUserIsSignedIn() {
        when(mockAwsCognitoIdentityProvider.getUser(any())).thenReturn(new GetUserResult());
        assertThat(userManager.isSignedIn(ACCESS_TOKEN), is(equalTo(true)));
        verify(mockAwsCognitoIdentityProvider).getUser(EXPECTED_GET_USER_REQUEST);
    }

    @Test
    void isSignedIn_throwsAccessTokenExpiredException_whenAccessTokenIsExpired() {
        when(mockAwsCognitoIdentityProvider.getUser(any())).thenThrow(
                new NotAuthorizedException("Access Token has expired"));
        assertThrows(AccessTokenExpiredException.class, () -> userManager.isSignedIn(ACCESS_TOKEN));
        verify(mockAwsCognitoIdentityProvider).getUser(EXPECTED_GET_USER_REQUEST);
    }

    @Test
    void isSignedIn_returnsFalse_whenAccessTokenIsNotValid() {
        when(mockAwsCognitoIdentityProvider.getUser(any())).thenThrow(new NotAuthorizedException(""));
        assertThat(userManager.isSignedIn(ACCESS_TOKEN), is(equalTo(false)));
        verify(mockAwsCognitoIdentityProvider).getUser(EXPECTED_GET_USER_REQUEST);
    }

    @Test
    void deleteUser_deletesUser() {
        userManager.deleteUser(ACCESS_TOKEN);
        verify(mockAwsCognitoIdentityProvider).deleteUser(new DeleteUserRequest().withAccessToken(ACCESS_TOKEN));
    }

    @Test
    void verifyEmail_throwsEmailAlreadyVerifiedException_whenEmailIsAlreadyVerified() {
        when(mockAwsCognitoIdentityProvider.getUser(any())).thenReturn(GET_USER_RESULT_EMAIL_VERIFIED);
        when(mockAwsCognitoIdentityProvider.adminListGroupsForUser(any())).thenReturn(
                new AdminListGroupsForUserResult().withGroups());
        assertThrows(EmailAlreadyVerifiedException.class,
                () -> userManager.verifyEmail(ACCESS_TOKEN, VERIFICATION_CODE));
        verify(mockAwsCognitoIdentityProvider).getUser(EXPECTED_GET_USER_REQUEST);
        verify(mockAwsCognitoIdentityProvider).adminListGroupsForUser(EXPECTED_ADMIN_LIST_GROUPS_FOR_USER_REQUEST);
        verify(mockAwsCognitoIdentityProvider, never()).verifyUserAttribute(any());
    }

    @Test
    void verifyEmail_throwsInvalidVerificationCodeException_whenCodeIsInvalid() {
        when(mockAwsCognitoIdentityProvider.getUser(any())).thenReturn(GET_USER_RESULT_EMAIL_NOT_VERIFIED);
        when(mockAwsCognitoIdentityProvider.adminListGroupsForUser(any())).thenReturn(
                new AdminListGroupsForUserResult().withGroups());
        when(mockAwsCognitoIdentityProvider.verifyUserAttribute(any())).thenThrow(new CodeMismatchException(""));
        assertThrows(InvalidVerificationCodeException.class,
                () -> userManager.verifyEmail(ACCESS_TOKEN, VERIFICATION_CODE));
        verify(mockAwsCognitoIdentityProvider).getUser(EXPECTED_GET_USER_REQUEST);
        verify(mockAwsCognitoIdentityProvider).adminListGroupsForUser(EXPECTED_ADMIN_LIST_GROUPS_FOR_USER_REQUEST);
        verify(mockAwsCognitoIdentityProvider).verifyUserAttribute(EXPECTED_VERIFY_USER_ATTRIBUTE_REQUEST);
    }

    @Test
    void verifyEmail_throwsInvalidVerificationCodeException_whenCodeIsExpired() {
        when(mockAwsCognitoIdentityProvider.getUser(any())).thenReturn(GET_USER_RESULT_EMAIL_NOT_VERIFIED);
        when(mockAwsCognitoIdentityProvider.adminListGroupsForUser(any())).thenReturn(
                new AdminListGroupsForUserResult().withGroups());
        when(mockAwsCognitoIdentityProvider.verifyUserAttribute(any())).thenThrow(new ExpiredCodeException(""));
        assertThrows(InvalidVerificationCodeException.class,
                () -> userManager.verifyEmail(ACCESS_TOKEN, VERIFICATION_CODE));
        verify(mockAwsCognitoIdentityProvider).getUser(EXPECTED_GET_USER_REQUEST);
        verify(mockAwsCognitoIdentityProvider).adminListGroupsForUser(EXPECTED_ADMIN_LIST_GROUPS_FOR_USER_REQUEST);
        verify(mockAwsCognitoIdentityProvider).verifyUserAttribute(EXPECTED_VERIFY_USER_ATTRIBUTE_REQUEST);
    }

    @Test
    void verifyEmail_verifiesEmail_whenCodeIsValid()
            throws InvalidVerificationCodeException, EmailAlreadyVerifiedException {
        when(mockAwsCognitoIdentityProvider.getUser(any())).thenReturn(GET_USER_RESULT_EMAIL_NOT_VERIFIED);
        when(mockAwsCognitoIdentityProvider.adminListGroupsForUser(any())).thenReturn(
                new AdminListGroupsForUserResult().withGroups());
        userManager.verifyEmail(ACCESS_TOKEN, VERIFICATION_CODE);
        verify(mockAwsCognitoIdentityProvider).getUser(EXPECTED_GET_USER_REQUEST);
        verify(mockAwsCognitoIdentityProvider).adminListGroupsForUser(EXPECTED_ADMIN_LIST_GROUPS_FOR_USER_REQUEST);
        verify(mockAwsCognitoIdentityProvider).verifyUserAttribute(EXPECTED_VERIFY_USER_ATTRIBUTE_REQUEST);
    }

    @Test
    void sendVerificationEmail_throwsEmailAlreadyVerifiedException_whenEmailIsAlreadyVerified() {
        when(mockAwsCognitoIdentityProvider.getUser(any())).thenReturn(GET_USER_RESULT_EMAIL_VERIFIED);
        when(mockAwsCognitoIdentityProvider.adminListGroupsForUser(any())).thenReturn(
                new AdminListGroupsForUserResult().withGroups());
        assertThrows(EmailAlreadyVerifiedException.class, () -> userManager.sendVerificationEmail(ACCESS_TOKEN));
        verify(mockAwsCognitoIdentityProvider).getUser(EXPECTED_GET_USER_REQUEST);
        verify(mockAwsCognitoIdentityProvider).adminListGroupsForUser(EXPECTED_ADMIN_LIST_GROUPS_FOR_USER_REQUEST);
    }

    @Test
    void sendVerificationEmail_sendsVerificationEmail() throws EmailAlreadyVerifiedException {
        when(mockAwsCognitoIdentityProvider.getUser(any())).thenReturn(GET_USER_RESULT_EMAIL_NOT_VERIFIED);
        when(mockAwsCognitoIdentityProvider.adminListGroupsForUser(any())).thenReturn(
                new AdminListGroupsForUserResult().withGroups());
        userManager.sendVerificationEmail(ACCESS_TOKEN);
        verify(mockAwsCognitoIdentityProvider).getUser(EXPECTED_GET_USER_REQUEST);
        verify(mockAwsCognitoIdentityProvider).adminListGroupsForUser(EXPECTED_ADMIN_LIST_GROUPS_FOR_USER_REQUEST);
        verify(mockAwsCognitoIdentityProvider).getUserAttributeVerificationCode(
                new GetUserAttributeVerificationCodeRequest().withAccessToken(ACCESS_TOKEN)
                        .withAttributeName(EMAIL_ATTRIBUTE_NAME));
    }

    @Test
    void getUserData_returnsUserDataWithoutAdministrator_whenUserIsNotInAnyGroups() {
        when(mockAwsCognitoIdentityProvider.getUser(any())).thenReturn(GET_USER_RESULT_EMAIL_VERIFIED);
        when(mockAwsCognitoIdentityProvider.adminListGroupsForUser(any())).thenReturn(
                new AdminListGroupsForUserResult().withGroups());
        assertThat(userManager.getUserData(ACCESS_TOKEN), is(equalTo(EXPECTED_USER_DATA_EMAIL_VERIFIED)));
        verify(mockAwsCognitoIdentityProvider).getUser(EXPECTED_GET_USER_REQUEST);
        verify(mockAwsCognitoIdentityProvider).adminListGroupsForUser(EXPECTED_ADMIN_LIST_GROUPS_FOR_USER_REQUEST);
    }

    @Test
    void getUserData_returnsUserDataWithAdministrator_whenUserIsAdministrator() {
        when(mockAwsCognitoIdentityProvider.getUser(any())).thenReturn(GET_USER_RESULT_EMAIL_VERIFIED);
        when(mockAwsCognitoIdentityProvider.adminListGroupsForUser(any())).thenReturn(
                new AdminListGroupsForUserResult().withGroups(new GroupType().withGroupName("Administrators")));
        assertThat(userManager.getUserData(ACCESS_TOKEN), is(equalTo(UserData.builder()
                .username(USERNAME)
                .email(EMAIL)
                .isAdministrator(true)
                .isEmailVerified(true)
                .build())));
        verify(mockAwsCognitoIdentityProvider).getUser(EXPECTED_GET_USER_REQUEST);
        verify(mockAwsCognitoIdentityProvider).adminListGroupsForUser(EXPECTED_ADMIN_LIST_GROUPS_FOR_USER_REQUEST);
    }

    @Test
    void getUserData_throwsInvalidUserDataException_whenUserDoesNotHaveEmail() {
        when(mockAwsCognitoIdentityProvider.getUser(any())).thenReturn(new GetUserResult().withUsername(USERNAME)
                .withUserAttributes());
        assertThrows(InvalidUserDataException.class, () -> userManager.getUserData(ACCESS_TOKEN));
        verify(mockAwsCognitoIdentityProvider).getUser(EXPECTED_GET_USER_REQUEST);
        verify(mockAwsCognitoIdentityProvider, never()).adminListGroupsForUser(any());
    }

    @Test
    void changePassword_throwsInvalidPasswordException_whenCurrentPasswordIsIncorrect() {
        String wrongPassword = "dammit-jim";
        when(mockAwsCognitoIdentityProvider.changePassword(any())).thenThrow(new NotAuthorizedException(""));
        assertThrows(InvalidPasswordException.class,
                () -> userManager.changePassword(ACCESS_TOKEN, wrongPassword, NEW_PASSWORD));
        verify(mockAwsCognitoIdentityProvider).changePassword(new ChangePasswordRequest().withAccessToken(ACCESS_TOKEN)
                .withPreviousPassword(wrongPassword)
                .withProposedPassword(NEW_PASSWORD));
    }

    @Test
    void changePassword_changesPassword() throws InvalidPasswordException {
        userManager.changePassword(ACCESS_TOKEN, PASSWORD, NEW_PASSWORD);
        verify(mockAwsCognitoIdentityProvider).changePassword(new ChangePasswordRequest().withAccessToken(ACCESS_TOKEN)
                .withPreviousPassword(PASSWORD)
                .withProposedPassword(NEW_PASSWORD));
    }

    @Test
    void sendPasswordResetEmail_throwsUserNotFoundException_whenUserDoesNotExist() {
        runUserNotFoundTest(() -> userManager.sendPasswordResetEmail(USERNAME_ALIAS));
    }

    @Test
    void sendPasswordResetEmail_throwsUserNotVerifiedException_whenUserIsNotVerified() {
        runUserNotVerifiedTest(() -> userManager.sendPasswordResetEmail(USERNAME_ALIAS));
    }

    @Test
    void sendPasswordResetEmail_throwsEmailNotVerifiedException_whenEmailIsNotVerifiedAndSignInIsWithUsername() {
        when(mockAwsCognitoIdentityProvider.listUsers(any())).thenReturn(new ListUsersResult().withUsers(
                ImmutableList.of(new UserType().withUserStatus(UserStatusType.CONFIRMED)
                        .withAttributes(new AttributeType().withName(EMAIL_VERIFIED_ATTRIBUTE_NAME)
                                .withValue("false")))));
        assertThrows(EmailNotVerifiedException.class, () -> userManager.sendPasswordResetEmail(USERNAME_ALIAS));
        verify(mockAwsCognitoIdentityProvider).listUsers(EXPECTED_LIST_USERS_REQUEST_USERNAME);
    }

    @Test
    void sendPasswordResetEmail_throwsEmailNotVerifiedException_whenEmailIsNotVerifiedAndSignInIsWithEmail() {
        runEmailNotVerifiedTest(() -> userManager.sendPasswordResetEmail(EMAIL_ALIAS));
    }

    @Test
    void sendPasswordResetEmail_callsForgotPasswordWithUsername_whenUsernameIsPassedIn()
            throws com.climbassist.api.user.authentication.UserNotFoundException, EmailNotVerifiedException,
            UserNotVerifiedException {
        when(mockAwsCognitoIdentityProvider.listUsers(any())).thenReturn(LIST_USERS_RESULT);
        userManager.sendPasswordResetEmail(USERNAME_ALIAS);
        verify(mockAwsCognitoIdentityProvider).listUsers(EXPECTED_LIST_USERS_REQUEST_USERNAME);
        verify(mockAwsCognitoIdentityProvider).forgotPassword(
                new ForgotPasswordRequest().withClientId(USER_POOL_CLIENT_ID)
                        .withUsername(USERNAME));
    }

    @Test
    void sendPasswordResetEmail_callsForgotPasswordWithEmail_whenEmailIsPassedIn()
            throws com.climbassist.api.user.authentication.UserNotFoundException, EmailNotVerifiedException,
            UserNotVerifiedException {
        when(mockAwsCognitoIdentityProvider.listUsers(any())).thenReturn(LIST_USERS_RESULT);
        userManager.sendPasswordResetEmail(EMAIL_ALIAS);
        verify(mockAwsCognitoIdentityProvider).listUsers(EXPECTED_LIST_USERS_REQUEST_EMAIL);
        verify(mockAwsCognitoIdentityProvider).forgotPassword(
                new ForgotPasswordRequest().withClientId(USER_POOL_CLIENT_ID)
                        .withUsername(EMAIL));
    }

    @Test
    void resetPassword_throwsUserNotFoundException_whenUserDoesNotExist() {
        runUserNotFoundTest(() -> userManager.resetPassword(USERNAME_ALIAS, VERIFICATION_CODE, NEW_PASSWORD));
    }

    @Test
    void resetPassword_throwsUserNotVerifiedException_whenUserIsNotVerified() {
        runUserNotVerifiedTest(() -> userManager.resetPassword(USERNAME_ALIAS, VERIFICATION_CODE, NEW_PASSWORD));
    }

    @Test
    void resetPassword_throwsEmailNotVerifiedException_whenEmailIsNotVerified() {
        runEmailNotVerifiedTest(() -> userManager.resetPassword(EMAIL_ALIAS, VERIFICATION_CODE, NEW_PASSWORD));
    }

    @Test
    void resetPassword_throwsInvalidVerificationCodeException_whenCodeIsIncorrect() {
        when(mockAwsCognitoIdentityProvider.listUsers(any())).thenReturn(LIST_USERS_RESULT);
        when(mockAwsCognitoIdentityProvider.confirmForgotPassword(any())).thenThrow(new CodeMismatchException(""));
        assertThrows(InvalidVerificationCodeException.class,
                () -> userManager.resetPassword(USERNAME_ALIAS, VERIFICATION_CODE, NEW_PASSWORD));
        verify(mockAwsCognitoIdentityProvider).listUsers(EXPECTED_LIST_USERS_REQUEST_USERNAME);
        verify(mockAwsCognitoIdentityProvider).confirmForgotPassword(new ConfirmForgotPasswordRequest().withClientId(
                USER_POOL_CLIENT_ID)
                .withUsername(USERNAME)
                .withConfirmationCode(VERIFICATION_CODE)
                .withPassword(NEW_PASSWORD));
    }

    @Test
    void resetPassword_throwsInvalidVerificationCodeException_whenCodeIsExpired() {
        when(mockAwsCognitoIdentityProvider.listUsers(any())).thenReturn(LIST_USERS_RESULT);
        when(mockAwsCognitoIdentityProvider.confirmForgotPassword(any())).thenThrow(new ExpiredCodeException(""));
        assertThrows(InvalidVerificationCodeException.class,
                () -> userManager.resetPassword(USERNAME_ALIAS, VERIFICATION_CODE, NEW_PASSWORD));
        verify(mockAwsCognitoIdentityProvider).listUsers(EXPECTED_LIST_USERS_REQUEST_USERNAME);
        verify(mockAwsCognitoIdentityProvider).confirmForgotPassword(new ConfirmForgotPasswordRequest().withClientId(
                USER_POOL_CLIENT_ID)
                .withUsername(USERNAME)
                .withConfirmationCode(VERIFICATION_CODE)
                .withPassword(NEW_PASSWORD));
    }

    @Test
    void resetPassword_callsConfirmForgotPasswordWithUsername_whenUsernameIsPassedIn()
            throws com.climbassist.api.user.authentication.UserNotFoundException, EmailNotVerifiedException,
            UserNotVerifiedException, InvalidVerificationCodeException {
        when(mockAwsCognitoIdentityProvider.listUsers(any())).thenReturn(LIST_USERS_RESULT);
        userManager.resetPassword(USERNAME_ALIAS, VERIFICATION_CODE, NEW_PASSWORD);
        verify(mockAwsCognitoIdentityProvider).listUsers(EXPECTED_LIST_USERS_REQUEST_USERNAME);
        verify(mockAwsCognitoIdentityProvider).confirmForgotPassword(new ConfirmForgotPasswordRequest().withClientId(
                USER_POOL_CLIENT_ID)
                .withUsername(USERNAME)
                .withConfirmationCode(VERIFICATION_CODE)
                .withPassword(NEW_PASSWORD));
    }

    @Test
    void resetPassword_callsForgotPasswordWithEmail_whenEmailIsPassedIn()
            throws com.climbassist.api.user.authentication.UserNotFoundException, EmailNotVerifiedException,
            UserNotVerifiedException, InvalidVerificationCodeException {
        when(mockAwsCognitoIdentityProvider.listUsers(any())).thenReturn(LIST_USERS_RESULT);
        userManager.resetPassword(EMAIL_ALIAS, VERIFICATION_CODE, NEW_PASSWORD);
        verify(mockAwsCognitoIdentityProvider).listUsers(EXPECTED_LIST_USERS_REQUEST_EMAIL);
        verify(mockAwsCognitoIdentityProvider).confirmForgotPassword(new ConfirmForgotPasswordRequest().withClientId(
                USER_POOL_CLIENT_ID)
                .withUsername(EMAIL)
                .withConfirmationCode(VERIFICATION_CODE)
                .withPassword(NEW_PASSWORD));
    }

    @Test
    void updateUser_updatesUserEmail() {
        userManager.updateUser(ACCESS_TOKEN, EMAIL);
        verify(mockAwsCognitoIdentityProvider).updateUserAttributes(
                new UpdateUserAttributesRequest().withAccessToken(ACCESS_TOKEN)
                        .withUserAttributes(new AttributeType().withName(EMAIL_ATTRIBUTE_NAME)
                                .withValue(EMAIL)));
    }

    private void runUserNotFoundTest(Executable executable) {
        when(mockAwsCognitoIdentityProvider.listUsers(any())).thenReturn(
                new ListUsersResult().withUsers(ImmutableList.of()));
        assertThrows(com.climbassist.api.user.authentication.UserNotFoundException.class, executable);
        verify(mockAwsCognitoIdentityProvider).listUsers(EXPECTED_LIST_USERS_REQUEST_USERNAME);
    }

    private void runUserNotVerifiedTest(Executable executable) {
        when(mockAwsCognitoIdentityProvider.listUsers(any())).thenReturn(new ListUsersResult().withUsers(
                ImmutableList.of(new UserType().withUserStatus(UserStatusType.UNCONFIRMED))));
        assertThrows(UserNotVerifiedException.class, executable);
        verify(mockAwsCognitoIdentityProvider).listUsers(EXPECTED_LIST_USERS_REQUEST_USERNAME);
    }

    private void runEmailNotVerifiedTest(Executable executable) {
        when(mockAwsCognitoIdentityProvider.listUsers(any())).thenReturn(new ListUsersResult().withUsers(
                ImmutableList.of(new UserType().withUserStatus(UserStatusType.CONFIRMED)
                        .withAttributes(new AttributeType().withName(EMAIL_VERIFIED_ATTRIBUTE_NAME)
                                .withValue("false")))));
        assertThrows(EmailNotVerifiedException.class, executable);
        verify(mockAwsCognitoIdentityProvider).listUsers(EXPECTED_LIST_USERS_REQUEST_EMAIL);
    }
}
