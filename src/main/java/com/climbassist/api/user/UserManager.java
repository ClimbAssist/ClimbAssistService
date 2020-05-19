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
import com.climbassist.api.user.authentication.IncorrectPasswordException;
import com.climbassist.api.user.authentication.IncorrectVerificationCodeException;
import com.climbassist.api.user.authentication.UserNotVerifiedException;
import com.climbassist.api.user.authentication.UserSessionData;
import com.climbassist.api.user.authentication.UsernameExistsException;
import com.climbassist.api.user.authorization.SessionExpiredException;
import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Builder
@Slf4j
public class UserManager {

    private static final String USERNAME_KEY = "USERNAME";
    private static final String PASSWORD_KEY = "PASSWORD";
    private static final String REFRESH_TOKEN_KEY = "REFRESH_TOKEN";
    private static final String EMAIL_ATTRIBUTE_NAME = "email";
    private static final String USER_ID_ATTRIBUTE_NAME = "sub";
    private static final String EMAIL_VERIFIED_ATTRIBUTE_NAME = "email_verified";

    @NonNull
    private final AWSCognitoIdentityProvider awsCognitoIdentityProvider;

    @NonNull
    private final String userPoolId;

    @NonNull
    private final String userPoolClientId;

    public void register(@NonNull String username, @NonNull String email, @NonNull String password)
            throws UsernameExistsException, EmailExistsException {
        if (emailExists(email)) {
            throw new EmailExistsException(email);
        }

        try {
            awsCognitoIdentityProvider.signUp(new SignUpRequest().withClientId(userPoolClientId)
                    .withUsername(username)
                    .withPassword(password)
                    .withUserAttributes(new AttributeType().withName(EMAIL_ATTRIBUTE_NAME)
                            .withValue(email)));
        } catch (com.amazonaws.services.cognitoidp.model.UsernameExistsException e) {
            log.warn("Caught exception when registering user.", e);
            throw new UsernameExistsException(username);
        }
    }

    public void resendInitialVerificationEmail(@NonNull Alias alias)
            throws com.climbassist.api.user.authentication.UserNotFoundException {
        try {
            awsCognitoIdentityProvider.resendConfirmationCode(
                    new ResendConfirmationCodeRequest().withClientId(userPoolClientId)
                            .withUsername(alias.getValue()));
        } catch (UserNotFoundException e) {
            throw new com.climbassist.api.user.authentication.UserNotFoundException(alias);
        }
    }

    public UserSessionData signIn(@NonNull Alias alias, @NonNull String password)
            throws com.climbassist.api.user.authentication.UserNotFoundException, UserNotVerifiedException,
            EmailNotVerifiedException, IncorrectPasswordException {
        verifyUserIsInUsableState(alias);

        try {
            InitiateAuthResult initiateAuthResult = awsCognitoIdentityProvider.initiateAuth(
                    new InitiateAuthRequest().withClientId(userPoolClientId)
                            .withAuthFlow(AuthFlowType.USER_PASSWORD_AUTH)
                            .withAuthParameters(
                                    ImmutableMap.of(USERNAME_KEY, alias.getValue(), PASSWORD_KEY, password)));
            AuthenticationResultType authenticationResultType = initiateAuthResult.getAuthenticationResult();
            return UserSessionData.builder()
                    .accessToken(authenticationResultType.getAccessToken())
                    .refreshToken(authenticationResultType.getRefreshToken())
                    .build();
        } catch (NotAuthorizedException e) {
            log.warn("Caught exception when signing-in user", e);
            throw new IncorrectPasswordException();
        }
    }

    public void signOut(@NonNull String accessToken) {
        awsCognitoIdentityProvider.globalSignOut(new GlobalSignOutRequest().withAccessToken(accessToken));
    }

    public boolean isSignedIn(@NonNull String accessToken) {
        try {
            awsCognitoIdentityProvider.getUser(new GetUserRequest().withAccessToken(accessToken));
        } catch (NotAuthorizedException e) {
            if (e.getMessage()
                    .contains("Access Token has expired")) {
                throw new AccessTokenExpiredException(e);
            }
            log.warn("Caught exception when checking if user is signed in.", e);
            return false;
        }
        return true;
    }

    public String refreshAccessToken(@NonNull String refreshToken) throws SessionExpiredException {
        try {
            InitiateAuthResult initiateAuthResult = awsCognitoIdentityProvider.initiateAuth(
                    new InitiateAuthRequest().withClientId(userPoolClientId)
                            .withAuthFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                            .withAuthParameters(ImmutableMap.of(REFRESH_TOKEN_KEY, refreshToken)));
            return initiateAuthResult.getAuthenticationResult()
                    .getAccessToken();
        } catch (NotAuthorizedException e) {
            throw new SessionExpiredException(e);
        }
    }

    public void deleteUser(@NonNull String accessToken) {
        awsCognitoIdentityProvider.deleteUser(new DeleteUserRequest().withAccessToken(accessToken));
    }

    public void verifyEmail(@NonNull String accessToken, @NonNull String verificationCode)
            throws IncorrectVerificationCodeException, EmailAlreadyVerifiedException {
        UserData userData = getUserData(accessToken);
        if (userData.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException();
        }
        try {
            awsCognitoIdentityProvider.verifyUserAttribute(new VerifyUserAttributeRequest().withAttributeName(
                    EMAIL_ATTRIBUTE_NAME)
                    .withAccessToken(accessToken)
                    .withCode(verificationCode));
        } catch (ExpiredCodeException | CodeMismatchException e) {
            throw new IncorrectVerificationCodeException(e);
        }
    }

    public void sendVerificationEmail(@NonNull String accessToken) throws EmailAlreadyVerifiedException {
        UserData userData = getUserData(accessToken);
        if (userData.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException();
        }
        awsCognitoIdentityProvider.getUserAttributeVerificationCode(
                new GetUserAttributeVerificationCodeRequest().withAccessToken(accessToken)
                        .withAttributeName(EMAIL_ATTRIBUTE_NAME));
    }

    public UserData getUserData(@NonNull String accessToken) {
        GetUserResult getUserResult = awsCognitoIdentityProvider.getUser(
                new GetUserRequest().withAccessToken(accessToken));
        String userId = getUserAttributeValue(getUserResult.getUserAttributes(), USER_ID_ATTRIBUTE_NAME);
        String username = getUserResult.getUsername();
        String email = getUserAttributeValue(getUserResult.getUserAttributes(), EMAIL_ATTRIBUTE_NAME);
        boolean isEmailVerified = Boolean.parseBoolean(
                getUserAttributeValue(getUserResult.getUserAttributes(), EMAIL_VERIFIED_ATTRIBUTE_NAME));
        AdminListGroupsForUserResult adminListGroupsForUserResult = awsCognitoIdentityProvider.adminListGroupsForUser(
                new AdminListGroupsForUserRequest().withUserPoolId(userPoolId)
                        .withUsername(username));
        boolean isAdministrator = adminListGroupsForUserResult.getGroups()
                .stream()
                .anyMatch(groupType -> groupType.getGroupName()
                        .equals("Administrators"));
        return UserData.builder()
                .userId(userId)
                .username(username)
                .email(email)
                .isEmailVerified(isEmailVerified)
                .isAdministrator(isAdministrator)
                .build();
    }

    public void changePassword(@NonNull String accessToken, @NonNull String currentPassword,
                               @NonNull String newPassword) throws IncorrectPasswordException {
        try {
            awsCognitoIdentityProvider.changePassword(new ChangePasswordRequest().withAccessToken(accessToken)
                    .withPreviousPassword(currentPassword)
                    .withProposedPassword(newPassword));
        } catch (NotAuthorizedException e) {
            throw new IncorrectPasswordException();
        }
    }

    public void sendPasswordResetEmail(@NonNull Alias alias) throws EmailNotVerifiedException, UserNotVerifiedException,
            com.climbassist.api.user.authentication.UserNotFoundException {
        UserType userType = getUserType(alias);
        if (!userType.getUserStatus()
                .equals(UserStatusType.CONFIRMED.toString())) {
            throw new UserNotVerifiedException();
        }
        if (!getUserAttributeValue(userType.getAttributes(), EMAIL_VERIFIED_ATTRIBUTE_NAME).equals("true")) {
            throw new EmailNotVerifiedException();
        }
        awsCognitoIdentityProvider.forgotPassword(new ForgotPasswordRequest().withClientId(userPoolClientId)
                .withUsername(alias.getValue()));
    }

    public void resetPassword(@NonNull Alias alias, @NonNull String verificationCode, @NonNull String newPassword)
            throws IncorrectVerificationCodeException, com.climbassist.api.user.authentication.UserNotFoundException,
            UserNotVerifiedException, EmailNotVerifiedException {
        verifyUserIsInUsableState(alias);
        try {
            awsCognitoIdentityProvider.confirmForgotPassword(new ConfirmForgotPasswordRequest().withClientId(
                    userPoolClientId)
                    .withUsername(alias.getValue())
                    .withConfirmationCode(verificationCode)
                    .withPassword(newPassword));
        } catch (CodeMismatchException | ExpiredCodeException e) {
            throw new IncorrectVerificationCodeException(e);
        }
    }

    void updateUser(@NonNull String accessToken, @NonNull String newEmail) {
        awsCognitoIdentityProvider.updateUserAttributes(new UpdateUserAttributesRequest().withAccessToken(accessToken)
                .withUserAttributes(new AttributeType().withName(EMAIL_ATTRIBUTE_NAME)
                        .withValue(newEmail)));
    }

    // this checks if the email address exists or not.
    // because Cognito doesn't have an API to check this, and has no way to disallow duplicate emails, we have to
    // try to sign in with an incorrect password and, if we get a NotAuthorizedException, that means the email
    // already exists.
    // A UserNotFoundException means that the email does not yet exist.
    private boolean emailExists(String email) {
        try {
            awsCognitoIdentityProvider.initiateAuth(new InitiateAuthRequest().withClientId(userPoolClientId)
                    .withAuthFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .withAuthParameters(ImmutableMap.of(USERNAME_KEY, email, PASSWORD_KEY, " ")));
        } catch (NotAuthorizedException e) {
            return true;
        } catch (UserNotFoundException e) {
            return false;
        }
        return true;
    }

    private String getUserAttributeValue(List<AttributeType> attributes, String attributeName) {
        return attributes.stream()
                .filter(attributeType -> attributeType.getName()
                        .equals(attributeName))
                .findAny()
                .orElseThrow(InvalidUserDataException::new)
                .getValue();
    }

    // this confirms that the user exists, the account is confirmed, and the email is confirmed (if using the alias
    // is an email)
    private void verifyUserIsInUsableState(Alias alias)
            throws com.climbassist.api.user.authentication.UserNotFoundException, UserNotVerifiedException,
            EmailNotVerifiedException {
        UserType userType = getUserType(alias);

        if (!userType.getUserStatus()
                .equals(UserStatusType.CONFIRMED.toString())) {
            throw new UserNotVerifiedException();
        }
        if (alias.getType() == Alias.AliasType.EMAIL && !getUserAttributeValue(userType.getAttributes(),
                EMAIL_VERIFIED_ATTRIBUTE_NAME).equals("true")) {
            throw new EmailNotVerifiedException();
        }
    }

    private UserType getUserType(Alias alias) throws com.climbassist.api.user.authentication.UserNotFoundException {
        ListUsersResult listUsersResult = awsCognitoIdentityProvider.listUsers(
                new ListUsersRequest().withUserPoolId(userPoolId)
                        .withFilter(String.format("%s=\"%s\"", alias.getType()
                                .getName(), alias.getValue())));
        if (listUsersResult.getUsers()
                .size() == 0) {
            throw new com.climbassist.api.user.authentication.UserNotFoundException(alias);
        }
        return listUsersResult.getUsers()
                .get(0);
    }
}
