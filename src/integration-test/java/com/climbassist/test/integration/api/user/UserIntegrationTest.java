package com.climbassist.test.integration.api.user;

import com.amazonaws.services.sqs.model.Message;
import com.climbassist.api.user.UpdateUserRequest;
import com.climbassist.api.user.UserData;
import com.climbassist.api.user.authentication.AliasRequest;
import com.climbassist.api.user.authentication.ChangePasswordRequest;
import com.climbassist.api.user.authentication.DeleteUserResult;
import com.climbassist.api.user.authentication.RegisterUserRequest;
import com.climbassist.api.user.authentication.RegisterUserResult;
import com.climbassist.api.user.authentication.ResendInitialVerificationEmailResult;
import com.climbassist.api.user.authentication.ResetPasswordRequest;
import com.climbassist.api.user.authentication.ResetPasswordResult;
import com.climbassist.api.user.authentication.SendPasswordResetEmailResult;
import com.climbassist.api.user.authentication.SignInUserRequest;
import com.climbassist.api.user.authentication.SignInUserResult;
import com.climbassist.api.user.authentication.SignOutUserResult;
import com.climbassist.api.user.authentication.VerifyEmailRequest;
import com.climbassist.test.integration.TestIdGenerator;
import com.climbassist.test.integration.api.ApiResponse;
import com.climbassist.test.integration.api.ExceptionUtils;
import com.climbassist.test.integration.client.ClimbAssistClient;
import com.climbassist.test.integration.client.ClimbAssistClientConfiguration;
import com.google.common.collect.ImmutableSet;
import org.apache.http.cookie.Cookie;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

//@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ClimbAssistClientConfiguration.class, TestUserManagerConfiguration.class})
public class UserIntegrationTest extends AbstractTestNGSpringContextTests {

    private static final String VERIFICATION_CODE_GROUP_NAME = "verificationCode";
    private static final Pattern VERIFICATION_CODE_PATTERN = Pattern.compile(
            String.format(".*Your verification code is (?<%s>\\d+).*", VERIFICATION_CODE_GROUP_NAME));
    private static final String PASSWORD_RESET_CODE_GROUP_NAME = "passwordResetCode";
    private static final Pattern PASSWORD_RESET_CODE_PATTERN = Pattern.compile(
            String.format(".*Your password reset code is (?<%s>\\d+).*", PASSWORD_RESET_CODE_GROUP_NAME));
    private static final String PASSWORD = "integ-password";
    private static final String SQS_EMAIL_MESSAGE_SUBJECT_FIELD_NAME = "Subject";
    private static final String VERIFICATION_EMAIL_SUBJECT = "Amazon SES Email Receipt Notification";
    @Autowired
    private ClimbAssistClient climbAssistClient;
    @Autowired
    private TestUserManager testUserManager;

    private String testId;
    private TestEmailContext testEmailContext;

    @BeforeMethod
    public void setUp() {
        testId = TestIdGenerator.generateTestId();
        testEmailContext = testUserManager.setUpTestEmail(testId);
    }

    @AfterMethod
    public void tearDown() {
        testUserManager.cleanUp();
    }

    @Test
    public void registerUser_createsUserAndAllowsSignInAfterVerification() throws IOException {
        TestUserContext testUserContext = testUserManager.registerUser(testId, testEmailContext);
        testUserManager.verifyNewAccountEmail(testUserContext);
        testUserManager.signIn(testUserContext);
    }

    @Test
    public void registerUser_returnsUserNameExistsException_whenUsernameAlreadyExists() {
        testUserManager.registerUser(testId, testEmailContext);
        ApiResponse<RegisterUserResult> registerUserResponse = climbAssistClient.registerUser(
                RegisterUserRequest.builder()
                        .username(testId)
                        .email(testId + "-other-user@test.climbassist.com")
                        .password(PASSWORD)
                        .build());
        ExceptionUtils.assertSpecificException(registerUserResponse, 409, "UsernameExistsException");
    }

    @Test
    public void registerUser_allowsDuplicateEmails_whenEmailAlreadyExistsButHasNotBeenVerified() {
        testUserManager.registerUser(testId, testEmailContext);
        String newUsername = testId + "-other-user";
        testUserManager.registerUser(newUsername, testEmailContext);
    }

    @Test
    public void registerUser_returnsEmailExistsException_whenEmailAlreadyExistsAndHasBeenVerified() throws IOException {
        TestUserContext testUserContext = testUserManager.registerUser(testId, testEmailContext);
        testUserManager.verifyNewAccountEmail(testUserContext);
        ApiResponse<RegisterUserResult> registerUserResponse = climbAssistClient.registerUser(
                RegisterUserRequest.builder()
                        .username(testId + "-does-not-exist")
                        .email(testEmailContext.getEmail())
                        .password(PASSWORD)
                        .build());
        ExceptionUtils.assertSpecificException(registerUserResponse, 409, "EmailExistsException");
    }

    @Test
    public void signIn_returnsUserNotVerifiedException_whenUserHasNotBeenVerified() {
        testUserManager.registerUser(testId, testEmailContext);
        ApiResponse<SignInUserResult> signInUserResponse = climbAssistClient.signIn(SignInUserRequest.builder()
                .username(testId)
                .password(PASSWORD)
                .build());
        ExceptionUtils.assertUserNotVerifiedException(signInUserResponse);
    }

    @Test
    public void signIn_signsInUser_whenUserSignsInWithUsername() throws IOException {
        TestUserContext testUserContext = testUserManager.registerUser(testId, testEmailContext);
        testUserManager.verifyNewAccountEmail(testUserContext);
        testUserManager.signInWithUsername(testId, PASSWORD, testEmailContext.getEmail());
    }

    @Test
    public void signIn_signsInUser_whenUserSignsInWithEmail() throws IOException {
        TestUserContext testUserContext = testUserManager.registerUser(testId, testEmailContext);
        testUserManager.verifyNewAccountEmail(testUserContext);
        testUserManager.signInWithEmail(testEmailContext.getEmail(), PASSWORD, testId);
    }

    @Test
    public void signIn_returnsIncorrectPasswordException_whenPasswordIsWrong() throws IOException {
        TestUserContext testUserContext = testUserManager.registerUser(testId, testEmailContext);
        testUserManager.verifyNewAccountEmail(testUserContext);
        ApiResponse<SignInUserResult> apiResponse = climbAssistClient.signIn(SignInUserRequest.builder()
                .username(testId)
                .password("totally-wrong")
                .build());
        ExceptionUtils.assertIncorrectPasswordException(apiResponse);
    }

    @Test
    public void signIn_returnsEmailNotVerifiedException_whenEmailWasChangedAndUserSignsInUsingEmail()
            throws IOException {
        TestUserContext testUserContext = testUserManager.registerUser(testId, testEmailContext);
        testUserManager.verifyNewAccountEmail(testUserContext);
        Set<Cookie> cookies = testUserManager.signInWithUsername(testId, PASSWORD, testEmailContext.getEmail());
        String newEmail = testId + "-updated@test.climbassist.com";
        updateEmail(newEmail, cookies);
        ApiResponse<SignInUserResult> signInUserResponse = climbAssistClient.signIn(SignInUserRequest.builder()
                .email(newEmail)
                .password(PASSWORD)
                .build());
        ExceptionUtils.assertEmailNotVerifiedException(signInUserResponse);
    }

    @Test
    public void signIn_signsUserIn_whenEmailWasChangedAndUserSignsInUsingUsername() throws IOException {
        TestUserContext testUserContext = testUserManager.registerUser(testId, testEmailContext);
        testUserManager.verifyNewAccountEmail(testUserContext);
        Set<Cookie> cookies = testUserManager.signInWithEmail(testEmailContext.getEmail(), PASSWORD, testId);
        String newEmail = testId + "-updated@test.climbassist.com";
        updateEmail(newEmail, cookies);
        ApiResponse<SignInUserResult> apiResponse = climbAssistClient.signIn(SignInUserRequest.builder()
                .username(testId)
                .password(PASSWORD)
                .build());
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
    }

    @Test
    public void resendInitialVerificationEmail_resendsVerificationEmail() {
        testUserManager.registerUser(testId, testEmailContext);
        climbAssistClient.resendInitialVerificationEmail(AliasRequest.builder()
                .username(testId)
                .build());
        Set<Message> messages = new HashSet<>();
        await().atMost(Duration.ofSeconds(300))
                .pollInterval(Duration.ofSeconds(30))
                .until(() -> {
                    messages.addAll(testUserManager.getAllMessagesFromSqs(testEmailContext.getQueueUrl()));
                    if (messages.stream()
                            .filter(message -> new JSONObject(message.getBody()).getString(
                                    SQS_EMAIL_MESSAGE_SUBJECT_FIELD_NAME)
                                    .equals(VERIFICATION_EMAIL_SUBJECT))
                            .count() > 1) {
                        return true;
                    }
                    else {
                        climbAssistClient.resendInitialVerificationEmail(AliasRequest.builder()
                                .username(testId)
                                .build());
                        return false;
                    }
                });
    }

    @Test
    public void resendInitialVerificationEmail_returnsUserNotFoundException_whenUsernameDoesNotExist() {
        runResendInitialVerificationEmailUserNotFoundTest(AliasRequest.builder()
                .username(testId)
                .build());
    }

    @Test
    public void resendInitialVerificationEmail_returnsUserNotFoundException_whenEmailDoesNotExist() {
        runResendInitialVerificationEmailUserNotFoundTest(AliasRequest.builder()
                .email(testEmailContext.getEmail())
                .build());
    }

    @Test
    public void signOut_doesNothing_whenUserIsNotSignedIn() {
        signOut();
    }

    @Test
    public void signOut_signsOutUser_whenUserIsSignedIn() throws IOException {
        TestUserContext testUserContext = testUserManager.registerUser(testId, testEmailContext);
        testUserManager.verifyNewAccountEmail(testUserContext);
        testUserManager.signInWithUsername(testId, PASSWORD, testEmailContext.getEmail());
        signOut();
    }

    @Test
    public void deleteUser_deletesAndSignsOutUser_whenUserIsSignedIn() throws IOException {
        TestUserContext testUserContext = testUserManager.registerUser(testId, testEmailContext);
        testUserManager.verifyNewAccountEmail(testUserContext);
        Set<Cookie> cookies = testUserManager.signIn(testUserContext);
        ApiResponse<DeleteUserResult> apiResponse = climbAssistClient.deleteUser(cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        verifyUserIsSignedOut(apiResponse.getCookies());
    }

    @Test
    public void deleteUser_returnsAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<DeleteUserResult> apiResponse = climbAssistClient.deleteUser(ImmutableSet.of());
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void verifyEmail_returnsAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<UserData> apiResponse = climbAssistClient.verifyEmail(VerifyEmailRequest.builder()
                .verificationCode("324B21")
                .build(), ImmutableSet.of());
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void verifyEmail_returnsEmailAlreadyVerifiedException_whenEmailIsAlreadyVerified() throws IOException {
        TestUserContext testUserContext = testUserManager.registerUser(testId, testEmailContext);
        testUserManager.verifyNewAccountEmail(testUserContext);
        Set<Cookie> cookies = testUserManager.signIn(testUserContext);
        ApiResponse<UserData> apiResponse = climbAssistClient.verifyEmail(VerifyEmailRequest.builder()
                .verificationCode("324B21")
                .build(), cookies);
        ExceptionUtils.assertEmailAlreadyVerifiedException(apiResponse);
    }

    @Test
    public void verifyEmail_returnsIncorrectVerificationCodeException_whenVerificationCodeIsWrong() throws IOException {
        TestUserContext testUserContext = testUserManager.registerUser(testId, testEmailContext);
        testUserManager.verifyNewAccountEmail(testUserContext);
        Set<Cookie> cookies = testUserManager.signIn(testUserContext);
        String newEmail = testId + "-updated@test.climbassist.com";
        updateEmail(newEmail, cookies);
        ApiResponse<UserData> apiResponse = climbAssistClient.verifyEmail(VerifyEmailRequest.builder()
                .verificationCode("324B21")
                .build(), cookies);
        ExceptionUtils.assertIncorrectVerificationCodeException(apiResponse);
    }

    @Test
    public void verifyEmail_verifiesEmail_whenVerificationCodeIsCorrect() throws IOException {
        TestUserContext testUserContext = testUserManager.registerUser(testId, testEmailContext);
        testUserManager.verifyNewAccountEmail(testUserContext);
        Set<Cookie> cookies = testUserManager.signIn(testUserContext);
        TestEmailContext newTestEmailContext = testUserManager.setUpTestEmail(testId + "-updated");
        cookies = updateEmail(newTestEmailContext.getEmail(), cookies);
        verifyUpdatedAccountEmail(newTestEmailContext, cookies);
    }

    @Test
    public void sendVerificationEmail_returnsAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<UserData> apiResponse = climbAssistClient.sendVerificationEmail(ImmutableSet.of());
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void sendVerificationEmail_returnsEmailAlreadyVerifiedException_whenEmailIsAlreadyVerified()
            throws IOException {
        TestUserContext testUserContext = testUserManager.registerUser(testId, testEmailContext);
        testUserManager.verifyNewAccountEmail(testUserContext);
        Set<Cookie> cookies = testUserManager.signIn(testUserContext);
        ApiResponse<UserData> apiResponse = climbAssistClient.sendVerificationEmail(cookies);
        ExceptionUtils.assertEmailAlreadyVerifiedException(apiResponse);
    }

    @Test
    public void sendVerificationEmail_sendsVerificationEmail_whenEmailIsNotVerified() throws IOException {
        TestUserContext testUserContext = testUserManager.registerUser(testId, testEmailContext);
        testUserManager.verifyNewAccountEmail(testUserContext);
        Set<Cookie> cookies = testUserManager.signIn(testUserContext);
        TestEmailContext newTestEmailContext = testUserManager.setUpTestEmail(testId + "-updated");
        cookies = updateEmail(newTestEmailContext.getEmail(), cookies);
        ApiResponse<UserData> apiResponse = climbAssistClient.sendVerificationEmail(cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .getUsername(), is(equalTo(testId)));
        assertThat(apiResponse.getData()
                .getEmail(), is(equalTo(newTestEmailContext.getEmail())));
        assertThat(apiResponse.getData()
                .isEmailVerified(), is(false));
        assertThat(apiResponse.getData()
                .isAdministrator(), is(false));
        Set<Message> messages = new HashSet<>();
        await().atMost(Duration.ofSeconds(300))
                .pollInterval(Duration.ofSeconds(60))
                .until(() -> {
                    messages.addAll(testUserManager.getAllMessagesFromSqs(newTestEmailContext.getQueueUrl()));
                    return messages.stream()
                            .filter(message -> new JSONObject(message.getBody()).getString(
                                    SQS_EMAIL_MESSAGE_SUBJECT_FIELD_NAME)
                                    .equals(VERIFICATION_EMAIL_SUBJECT))
                            .count() > 1;
                });
    }

    @Test
    public void changePassword_returnsAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<UserData> apiResponse = climbAssistClient.changePassword(ChangePasswordRequest.builder()
                .currentPassword("current-password")
                .newPassword("new-password")
                .build(), ImmutableSet.of());
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void changePassword_returnsIncorrectPasswordException_whenCurrentPasswordIsIncorrect() throws IOException {
        TestUserContext testUserContext = testUserManager.registerUser(testId, testEmailContext);
        testUserManager.verifyNewAccountEmail(testUserContext);
        Set<Cookie> cookies = testUserManager.signIn(testUserContext);
        ApiResponse<UserData> apiResponse = climbAssistClient.changePassword(ChangePasswordRequest.builder()
                .currentPassword("current-password")
                .newPassword("new-password")
                .build(), cookies);
        ExceptionUtils.assertIncorrectPasswordException(apiResponse);
    }

    @Test
    public void changePassword_changesPasswordAndDoesNotSignOutUser() throws IOException {
        TestUserContext testUserContext = testUserManager.registerUser(testId, testEmailContext);
        testUserManager.verifyNewAccountEmail(testUserContext);
        Set<Cookie> cookies = testUserManager.signIn(testUserContext);
        String newPassword = "integ-new-password";
        ApiResponse<UserData> apiResponse = climbAssistClient.changePassword(ChangePasswordRequest.builder()
                .currentPassword(PASSWORD)
                .newPassword(newPassword)
                .build(), cookies);
        assertUserDataIsCorrectForNonAdminVerifiedEmail(apiResponse);
        ApiResponse<UserData> getUserResponse = climbAssistClient.getUser(apiResponse.getCookies());
        assertUserDataIsCorrectForNonAdminVerifiedEmail(getUserResponse);
        testUserManager.signIn(SignInUserRequest.builder()
                .username(testId)
                .password(newPassword)
                .build(), testId, testEmailContext.getEmail());
    }

    @Test
    public void sendPasswordResetEmail_returnsUserNotFoundException_whenUsernameDoesNotExist() {
        ApiResponse<SendPasswordResetEmailResult> apiResponse = climbAssistClient.sendPasswordResetEmail(
                AliasRequest.builder()
                        .username(testId + "-does-not-exist")
                        .build());
        ExceptionUtils.assertUserNotFoundException(apiResponse);
    }

    @Test
    public void sendPasswordResetEmail_returnsUserNotFoundException_whenEmailDoesNotExist() {
        ApiResponse<SendPasswordResetEmailResult> apiResponse = climbAssistClient.sendPasswordResetEmail(
                AliasRequest.builder()
                        .email(testId + "-does-not-exist@climbassist.com")
                        .build());
        ExceptionUtils.assertUserNotFoundException(apiResponse);
    }

    @Test
    public void sendPasswordResetEmail_returnsUserNotVerifiedException_whenUserIsNotVerified() {
        testUserManager.registerUser(testId, testEmailContext);
        ApiResponse<SendPasswordResetEmailResult> apiResponse = climbAssistClient.sendPasswordResetEmail(
                AliasRequest.builder()
                        .username(testId)
                        .build());
        ExceptionUtils.assertUserNotVerifiedException(apiResponse);
    }

    @Test
    public void sendPasswordResetEmail_returnsEmailNotVerifiedException_whenEmailIsNotVerifiedAndSignInIsWithUsername()
            throws IOException {
        TestUserContext testUserContext = testUserManager.registerUser(testId, testEmailContext);
        testUserManager.verifyNewAccountEmail(testUserContext);
        Set<Cookie> cookies = testUserManager.signIn(testUserContext);
        String newEmail = testUserManager.setUpTestEmail(testId + "-updated")
                .getEmail();
        updateEmail(newEmail, cookies);
        ApiResponse<SendPasswordResetEmailResult> apiResponse = climbAssistClient.sendPasswordResetEmail(
                AliasRequest.builder()
                        .username(testId)
                        .build());
        ExceptionUtils.assertEmailNotVerifiedException(apiResponse);
    }

    @Test
    public void sendPasswordResetEmail_returnsEmailNotVerifiedException_whenEmailIsNotVerifiedAndSignInIsWithEmail()
            throws IOException {
        TestUserContext testUserContext = testUserManager.registerUser(testId, testEmailContext);
        testUserManager.verifyNewAccountEmail(testUserContext);
        Set<Cookie> cookies = testUserManager.signIn(testUserContext);
        String newEmail = testUserManager.setUpTestEmail(testId + "-updated")
                .getEmail();
        updateEmail(newEmail, cookies);
        ApiResponse<SendPasswordResetEmailResult> apiResponse = climbAssistClient.sendPasswordResetEmail(
                AliasRequest.builder()
                        .email(newEmail)
                        .build());
        ExceptionUtils.assertEmailNotVerifiedException(apiResponse);
    }

    @Test
    public void sendPasswordResetEmail_sendsPasswordResetEmail() throws IOException {
        TestUserContext testUserContext = testUserManager.registerUser(testId, testEmailContext);
        testUserManager.verifyNewAccountEmail(testUserContext);
        getPasswordResetCode();
    }

    @Test
    public void resetPassword_returnsUserNotFoundException_whenUsernameDoesNotExist() {
        ApiResponse<ResetPasswordResult> apiResponse = climbAssistClient.resetPassword(ResetPasswordRequest.builder()
                .newPassword("new-password")
                .verificationCode("123456")
                .username(testId + "-does-not-exist")
                .build());
        ExceptionUtils.assertUserNotFoundException(apiResponse);
    }

    @Test
    public void resetPassword_returnsUserNotVerifiedException_whenUserIsNotVerified() {
        testUserManager.registerUser(testId, testEmailContext);
        ApiResponse<ResetPasswordResult> apiResponse = climbAssistClient.resetPassword(ResetPasswordRequest.builder()
                .newPassword("new-password")
                .verificationCode("123456")
                .username(testId)
                .build());
        ExceptionUtils.assertUserNotVerifiedException(apiResponse);
    }

    @Test
    public void resetPassword_returnsEmailNotVerifiedException_whenEmailIsNotVerifiedAndRequestIsWithEmail()
            throws IOException {
        TestUserContext testUserContext = testUserManager.registerUser(testId, testEmailContext);
        testUserManager.verifyNewAccountEmail(testUserContext);
        Set<Cookie> cookies = testUserManager.signIn(testUserContext);
        String newEmail = testId + "-updated@test.climbassist.com";
        updateEmail(newEmail, cookies);
        ApiResponse<ResetPasswordResult> apiResponse = climbAssistClient.resetPassword(ResetPasswordRequest.builder()
                .newPassword("new-password")
                .verificationCode("123456")
                .email(newEmail)
                .build());
        ExceptionUtils.assertEmailNotVerifiedException(apiResponse);
    }

    @Test
    public void resetPassword_returnsIncorrectVerificationCodeException_whenVerificationCodeIsWrong() throws IOException {
        TestUserContext testUserContext = testUserManager.registerUser(testId, testEmailContext);
        testUserManager.verifyNewAccountEmail(testUserContext);
        ApiResponse<ResetPasswordResult> apiResponse = climbAssistClient.resetPassword(ResetPasswordRequest.builder()
                .newPassword("new-password")
                .verificationCode("123456")
                .username(testId)
                .build());
        ExceptionUtils.assertIncorrectVerificationCodeException(apiResponse);
    }

    @Test
    public void resetPassword_resetsPassword_whenEmailIsNotVerifiedAndRequestIsWithUsername() throws IOException {
        TestUserContext testUserContext = testUserManager.registerUser(testId, testEmailContext);
        testUserManager.verifyNewAccountEmail(testUserContext);
        Set<Cookie> cookies = testUserManager.signIn(testUserContext);

        String passwordResetCode = getPasswordResetCode();

        String newEmail = testId + "-updated@test.climbassist.com";
        updateEmail(newEmail, cookies);

        String newPassword = "new-password";
        ApiResponse<ResetPasswordResult> resetPasswordResult = climbAssistClient.resetPassword(
                ResetPasswordRequest.builder()
                        .newPassword(newPassword)
                        .verificationCode(passwordResetCode)
                        .username(testId)
                        .build());
        ExceptionUtils.assertNoException(resetPasswordResult);
        assertThat(resetPasswordResult.getData()
                .isSuccessful(), is(true));

        ApiResponse<SignInUserResult> signInUserResponse = climbAssistClient.signIn(SignInUserRequest.builder()
                .username(testId)
                .password(newPassword)
                .build());
        ExceptionUtils.assertNoException(signInUserResponse);
        assertThat(signInUserResponse.getData()
                .isSuccessful(), is(true));

        ApiResponse<UserData> getUserResponse = climbAssistClient.getUser(signInUserResponse.getCookies());
        ExceptionUtils.assertNoException(getUserResponse);
        assertThat(getUserResponse.getData()
                .getUsername(), is(equalTo(testId)));
        assertThat(getUserResponse.getData()
                .getEmail(), is(equalTo(newEmail)));
        assertThat(getUserResponse.getData()
                .isAdministrator(), is(false));
        assertThat(getUserResponse.getData()
                .isEmailVerified(), is(false));
    }

    @Test
    public void resetPassword_resetsPassword() throws IOException {
        TestUserContext testUserContext = testUserManager.registerUser(testId, testEmailContext);
        testUserManager.verifyNewAccountEmail(testUserContext);
        String passwordResetCode = getPasswordResetCode();
        String newPassword = "new-password";
        ApiResponse<ResetPasswordResult> apiResponse = climbAssistClient.resetPassword(ResetPasswordRequest.builder()
                .newPassword(newPassword)
                .verificationCode(passwordResetCode)
                .username(testId)
                .build());
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));

        testUserManager.signIn(SignInUserRequest.builder()
                .username(testId)
                .password(newPassword)
                .build(), testId, testEmailContext.getEmail());
    }

    @Test
    public void getUser_returnsUserDataWithEmailVerified_whenEmailIsVerified() throws IOException {
        TestUserContext testUserContext = testUserManager.registerUser(testId, testEmailContext);
        testUserManager.verifyNewAccountEmail(testUserContext);
        Set<Cookie> cookies = testUserManager.signIn(testUserContext);
        ApiResponse<UserData> apiResponse = climbAssistClient.getUser(cookies);
        assertUserDataIsCorrectForNonAdminVerifiedEmail(apiResponse);
    }

    @Test
    public void getUser_returnsUserDataWithNewEmailAndEmailNotVerified_whenNewEmailIsNotVerified() throws IOException {
        TestUserContext testUserContext = testUserManager.registerUser(testId, testEmailContext);
        testUserManager.verifyNewAccountEmail(testUserContext);
        Set<Cookie> cookies = testUserManager.signIn(testUserContext);
        String newEmail = testId + "-updated@test.climbassist.com";
        cookies = updateEmail(newEmail, cookies);
        ApiResponse<UserData> apiResponse = climbAssistClient.getUser(cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .getUsername(), is(equalTo(testId)));
        assertThat(apiResponse.getData()
                .getEmail(), is(equalTo(newEmail)));
        assertThat(apiResponse.getData()
                .isEmailVerified(), is(false));
        assertThat(apiResponse.getData()
                .isAdministrator(), is(false));
    }

    @Test
    public void getUser_returnsUserDataWithNewEmailAndEmailVerified_whenNewEmailIsVerified() throws IOException {
        TestUserContext testUserContext = testUserManager.registerUser(testId, testEmailContext);
        testUserManager.verifyNewAccountEmail(testUserContext);
        Set<Cookie> cookies = testUserManager.signIn(testUserContext);
        TestEmailContext newTestEmailContext = testUserManager.setUpTestEmail(testId + "updated");
        cookies = updateEmail(newTestEmailContext.getEmail(), cookies);
        verifyUpdatedAccountEmail(newTestEmailContext, cookies);
        ApiResponse<UserData> apiResponse = climbAssistClient.getUser(cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .getUsername(), is(equalTo(testId)));
        assertThat(apiResponse.getData()
                .getEmail(), is(equalTo(newTestEmailContext.getEmail())));
        assertThat(apiResponse.getData()
                .isEmailVerified(), is(true));
        assertThat(apiResponse.getData()
                .isAdministrator(), is(false));
    }

    @Test
    public void getUser_returnsUserDataWithAdministratorTrue_whenUserIsAdministrator() throws IOException {
        TestUserContext testUserContext = testUserManager.registerUser(testId, testEmailContext);
        testUserManager.verifyNewAccountEmail(testUserContext);
        Set<Cookie> cookies = testUserManager.signIn(testUserContext);
        testUserManager.makeUserAdministrator(testId);
        ApiResponse<UserData> apiResponse = climbAssistClient.getUser(cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .getUsername(), is(equalTo(testId)));
        assertThat(apiResponse.getData()
                .getEmail(), is(equalTo(testEmailContext.getEmail())));
        assertThat(apiResponse.getData()
                .isEmailVerified(), is(true));
        assertThat(apiResponse.getData()
                .isAdministrator(), is(true));
    }

    @Test
    public void updateUser_updatesEmail() throws IOException {
        TestUserContext testUserContext = testUserManager.registerUser(testId, testEmailContext);
        testUserManager.verifyNewAccountEmail(testUserContext);
        Set<Cookie> cookies = testUserManager.signIn(testUserContext);
        updateEmail(testId + "-updated@test.climbassist.com", cookies);
    }

    private void verifyUpdatedAccountEmail(TestEmailContext testEmailContext, Set<Cookie> cookies) {
        Set<Message> messages = new HashSet<>();
        await().atMost(Duration.ofSeconds(300))
                .pollInterval(Duration.ofSeconds(60))
                .until(() -> {
                    messages.addAll(testUserManager.getAllMessagesFromSqs(testEmailContext.getQueueUrl()));
                    return testUserManager.findEmailReceiptMessage(messages)
                            .isPresent();
                });
        String emailBody = new JSONObject(testUserManager.findEmailReceiptMessage(messages)
                .get()
                .getBody()).getString("Message")
                .trim();
        Matcher matcher = VERIFICATION_CODE_PATTERN.matcher(emailBody);
        if (!matcher.matches()) {
            throw new IllegalStateException("Verification email does not contain verification code.");
        }
        String verificationCode = matcher.group(VERIFICATION_CODE_GROUP_NAME);
        ApiResponse<UserData> apiResponse = climbAssistClient.verifyEmail(VerifyEmailRequest.builder()
                .verificationCode(verificationCode)
                .build(), cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .getUsername(), is(equalTo(testId)));
        assertThat(apiResponse.getData()
                .getEmail(), is(equalTo(testEmailContext.getEmail())));
        assertThat(apiResponse.getData()
                .isAdministrator(), is(false));
        assertThat(apiResponse.getData()
                .isEmailVerified(), is(true));
    }

    private void runResendInitialVerificationEmailUserNotFoundTest(AliasRequest aliasRequest) {
        ApiResponse<ResendInitialVerificationEmailResult> apiResponse =
                climbAssistClient.resendInitialVerificationEmail(aliasRequest);
        ExceptionUtils.assertUserNotFoundException(apiResponse);
    }

    private void signOut() {
        ApiResponse<SignOutUserResult> apiResponse = climbAssistClient.signOut(ImmutableSet.of());
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        verifyUserIsSignedOut(apiResponse.getCookies());
    }

    private void verifyUserIsSignedOut(Set<Cookie> cookies) {
        ApiResponse<UserData> apiResponse = climbAssistClient.getUser(cookies);
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    private void assertUserDataIsCorrectForNonAdminVerifiedEmail(ApiResponse<UserData> apiResponse) {
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .getUsername(), is(equalTo(testId)));
        assertThat(apiResponse.getData()
                .getEmail(), is(equalTo(testEmailContext.getEmail())));
        assertThat(apiResponse.getData()
                .isAdministrator(), is(false));
        assertThat(apiResponse.getData()
                .isEmailVerified(), is(true));
    }

    private String getPasswordResetCode() {
        ApiResponse<SendPasswordResetEmailResult> apiResponse = climbAssistClient.sendPasswordResetEmail(
                AliasRequest.builder()
                        .username(testId)
                        .build());
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        Set<Message> messages = new HashSet<>();
        await().atMost(Duration.ofSeconds(300))
                .pollInterval(Duration.ofSeconds(60))
                .until(() -> {
                    messages.addAll(testUserManager.getAllMessagesFromSqs(testEmailContext.getQueueUrl()));
                    return testUserManager.findEmailReceiptMessage(messages)
                            .isPresent();
                });
        String emailBody = new JSONObject(testUserManager.findEmailReceiptMessage(messages)
                .get()
                .getBody()).getString("Message")
                .trim();
        Matcher matcher = PASSWORD_RESET_CODE_PATTERN.matcher(emailBody);
        if (!matcher.matches()) {
            throw new IllegalStateException("Password reset email does not contain password reset code.");
        }
        return matcher.group(PASSWORD_RESET_CODE_GROUP_NAME);
    }

    private Set<Cookie> updateEmail(String newEmail, Set<Cookie> cookies) {
        ApiResponse<UserData> apiResponse = climbAssistClient.updateUser(UpdateUserRequest.builder()
                .email(newEmail)
                .build(), cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .getUsername(), is(equalTo(testId)));
        assertThat(apiResponse.getData()
                .getEmail(), is(equalTo(newEmail)));
        assertThat(apiResponse.getData()
                .isEmailVerified(), is(false));
        assertThat(apiResponse.getData()
                .isAdministrator(), is(false));
        return apiResponse.getCookies();
    }
}
