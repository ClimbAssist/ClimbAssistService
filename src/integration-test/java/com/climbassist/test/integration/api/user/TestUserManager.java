package com.climbassist.test.integration.api.user;

import com.amazonaws.auth.policy.Condition;
import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Principal;
import com.amazonaws.auth.policy.Resource;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.actions.SQSActions;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminAddUserToGroupRequest;
import com.amazonaws.services.cognitoidp.model.AdminDeleteUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminRemoveUserFromGroupRequest;
import com.amazonaws.services.cognitoidp.model.UserNotFoundException;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.AlreadyExistsException;
import com.amazonaws.services.simpleemail.model.CreateReceiptRuleRequest;
import com.amazonaws.services.simpleemail.model.CreateReceiptRuleSetRequest;
import com.amazonaws.services.simpleemail.model.DeleteReceiptRuleRequest;
import com.amazonaws.services.simpleemail.model.DescribeActiveReceiptRuleSetRequest;
import com.amazonaws.services.simpleemail.model.DescribeActiveReceiptRuleSetResult;
import com.amazonaws.services.simpleemail.model.ReceiptAction;
import com.amazonaws.services.simpleemail.model.ReceiptRule;
import com.amazonaws.services.simpleemail.model.SNSAction;
import com.amazonaws.services.simpleemail.model.SetActiveReceiptRuleSetRequest;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.DeleteTopicRequest;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.UnsubscribeRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SetQueueAttributesRequest;
import com.climbassist.api.user.UserData;
import com.climbassist.api.user.authentication.AliasRequest;
import com.climbassist.api.user.authentication.RegisterUserRequest;
import com.climbassist.api.user.authentication.RegisterUserResult;
import com.climbassist.api.user.authentication.SignInUserRequest;
import com.climbassist.api.user.authentication.SignInUserResult;
import com.climbassist.test.integration.api.ApiResponse;
import com.climbassist.test.integration.api.ExceptionUtils;
import com.climbassist.test.integration.client.ClimbAssistClient;
import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@Builder
public class TestUserManager {

    private static final String QUEUE_ARN_ATTRIBUTE_NAME = "QueueArn";
    private static final String TEST_PASSWORD = "integ-password";
    private static final String VERIFICATION_LINK_GROUP_NAME = "verificationLink";
    private static final Pattern VERIFICATION_LINK_PATTERN = Pattern.compile(
            String.format(".*<a href=(?<%s>.*)>Verify Email.*", VERIFICATION_LINK_GROUP_NAME));
    private static final String VERIFICATION_CODE_GROUP_NAME = "verificationCode";
    private static final Pattern VERIFICATION_CODE_PATTERN = Pattern.compile(
            String.format(".*Your verification code is (?<%s>\\d+).*", VERIFICATION_CODE_GROUP_NAME));
    private static final String PASSWORD_RESET_CODE_GROUP_NAME = "passwordResetCode";
    private static final Pattern PASSWORD_RESET_CODE_PATTERN = Pattern.compile(
            String.format(".*Your password reset code is (?<%s>\\d+).*", PASSWORD_RESET_CODE_GROUP_NAME));
    private static final String PASSWORD = "integ-password";
    private static final String SQS_EMAIL_MESSAGE_SUBJECT_FIELD_NAME = "Subject";
    private static final String VERIFICATION_EMAIL_SUBJECT = "Amazon SES Email Receipt Notification";
    private static final String SES_RULE_SET_NAME = "integ";
    private static final String ADMINISTRATORS_GROUP_NAME = "Administrators";

    private final Set<String> testUsernames = new HashSet<>();
    private final Set<TestEmailContext> testEmailContexts = new HashSet<>();

    @NonNull
    private final AmazonSNS amazonSNS;
    @NonNull
    private final AmazonSQS amazonSQS;
    @NonNull
    private final AmazonSimpleEmailService amazonSimpleEmailService;
    @NonNull
    private final ClimbAssistClient climbAssistClient;
    @NonNull
    private final AWSCognitoIdentityProvider awsCognitoIdentityProvider;
    @NonNull
    private final String userPoolId;
    @NonNull
    private final HttpClient httpClient;
    @NonNull
    private final AWSSecretsManager awsSecretsManager;
    @NonNull
    private final String recaptchaBackDoorResponseSecretId;

    public Set<Cookie> createVerifyAndSignInTestUser(@NonNull String testId) throws IOException {
        TestEmailContext testEmailContext = setUpTestEmail(testId);
        TestUserContext testUserContext = registerUser(testId, testEmailContext);
        verifyNewAccountEmail(testUserContext);
        return signIn(testUserContext);
    }

    public void cleanUp() {
        testUsernames.forEach(testUsername -> {
            try {
                awsCognitoIdentityProvider.adminDeleteUser(new AdminDeleteUserRequest().withUserPoolId(userPoolId)
                        .withUsername(testUsername));
            } catch (UserNotFoundException ignored) {
                // do nothing - this is the expected end state anyway
            }
        });
        testUsernames.clear();

        testEmailContexts.forEach(testEmailContext -> {
            amazonSimpleEmailService.deleteReceiptRule(new DeleteReceiptRuleRequest().withRuleSetName(SES_RULE_SET_NAME)
                    .withRuleName(testEmailContext.getRuleSetName()));
            amazonSNS.unsubscribe(new UnsubscribeRequest(testEmailContext.getSubscriptionArn()));
            amazonSQS.deleteQueue(new DeleteQueueRequest(testEmailContext.getQueueUrl()));
            amazonSNS.deleteTopic(new DeleteTopicRequest(testEmailContext.getTopicArn()));
        });
        testEmailContexts.clear();
    }

    public void makeUserAdministrator(@NonNull String username) {
        awsCognitoIdentityProvider.adminAddUserToGroup(new AdminAddUserToGroupRequest().withUserPoolId(userPoolId)
                .withUsername(username)
                .withGroupName(ADMINISTRATORS_GROUP_NAME));
    }

    public void makeUserNotAdministrator(@NonNull String username) {
        awsCognitoIdentityProvider.adminRemoveUserFromGroup(new AdminRemoveUserFromGroupRequest().withUserPoolId(
                userPoolId)
                .withUsername(username)
                .withGroupName(ADMINISTRATORS_GROUP_NAME));
    }

    public TestEmailContext setUpTestEmail(@NonNull String testId) {
        String email = testId + "@test.climbassist.com";
        String topicArn = amazonSNS.createTopic(new CreateTopicRequest(testId))
                .getTopicArn();
        String queueUrl = amazonSQS.createQueue(new CreateQueueRequest(testId))
                .getQueueUrl();
        String queueArn = amazonSQS.getQueueAttributes(
                new GetQueueAttributesRequest(queueUrl).withAttributeNames(QUEUE_ARN_ATTRIBUTE_NAME))
                .getAttributes()
                .get(QUEUE_ARN_ATTRIBUTE_NAME);
        amazonSQS.setQueueAttributes(new SetQueueAttributesRequest(queueUrl,
                ImmutableMap.of("Policy", new Policy().withStatements(new Statement(Statement.Effect.Allow).withActions(
                        SQSActions.SendMessage)
                        .withPrincipals(Principal.All)
                        .withResources(new Resource(queueArn))
                        .withConditions(new Condition().withType("ArnEquals")
                                .withConditionKey("aws:SourceArn")
                                .withValues(topicArn)))
                        .toJson())));
        String subscriptionArn = amazonSNS.subscribe(new SubscribeRequest(topicArn, "sqs", queueArn))
                .getSubscriptionArn();
        DescribeActiveReceiptRuleSetResult describeActiveReceiptRuleSetResult =
                amazonSimpleEmailService.describeActiveReceiptRuleSet(new DescribeActiveReceiptRuleSetRequest());
        if (describeActiveReceiptRuleSetResult.getMetadata() == null ||
                !describeActiveReceiptRuleSetResult.getMetadata()
                        .getName()
                        .equals(SES_RULE_SET_NAME)) {
            try {
                amazonSimpleEmailService.createReceiptRuleSet(
                        new CreateReceiptRuleSetRequest().withRuleSetName(SES_RULE_SET_NAME));
            } catch (AlreadyExistsException ignored) {
                // this is the end state we want anyway
            }

            amazonSimpleEmailService.setActiveReceiptRuleSet(
                    new SetActiveReceiptRuleSetRequest().withRuleSetName(SES_RULE_SET_NAME));
        }
        amazonSimpleEmailService.createReceiptRule(new CreateReceiptRuleRequest().withRuleSetName(SES_RULE_SET_NAME)
                .withRule(new ReceiptRule().withName(testId)
                        .withEnabled(true)
                        .withRecipients(email)
                        .withActions(new ReceiptAction().withSNSAction(new SNSAction().withTopicArn(topicArn)))));

        TestEmailContext testEmailContext = TestEmailContext.builder()
                .email(email)
                .queueUrl(queueUrl)
                .topicArn(topicArn)
                .subscriptionArn(subscriptionArn)
                .ruleSetName(testId)
                .build();
        testEmailContexts.add(testEmailContext);
        return testEmailContext;
    }

    public TestUserContext registerUser(@NonNull String username, @NonNull TestEmailContext testEmailContext) {
        ApiResponse<RegisterUserResult> registerUserResponse = climbAssistClient.registerUser(
                RegisterUserRequest.builder()
                        .username(username)
                        .email(testEmailContext.getEmail())
                        .password(TEST_PASSWORD)
                        .recaptchaResponse(retrieveRecaptchaBackDoorResponse())
                        .build());
        ExceptionUtils.assertNoException(registerUserResponse);
        testUsernames.add(username);
        RegisterUserResult registerUserResult = registerUserResponse.getData();
        assertThat(registerUserResult.getEmail(), is(equalTo(testEmailContext.getEmail())));
        assertThat(registerUserResult.getUsername(), is(equalTo(username)));

        return TestUserContext.builder()
                .username(username)
                .password(TEST_PASSWORD)
                .testEmailContext(testEmailContext)
                .build();
    }

    public void verifyNewAccountEmail(@NonNull TestUserContext testUserContext) throws IOException {
        Set<Message> messages = new HashSet<>();
        await().atMost(Duration.ofSeconds(300))
                .pollInterval(Duration.ofSeconds(60))
                .until(() -> {
                    messages.addAll(getAllMessagesFromSqs(testUserContext.getTestEmailContext()
                            .getQueueUrl()));
                    if (findEmailReceiptMessage(messages).isPresent()) {
                        return true;
                    }
                    else {
                        climbAssistClient.resendInitialVerificationEmail(AliasRequest.builder()
                                .username(testUserContext.getUsername())
                                .build());
                        return false;
                    }
                });
        String emailBody = new JSONObject(findEmailReceiptMessage(messages).get()
                .getBody()).getString("Message")
                .trim();
        Matcher matcher = VERIFICATION_LINK_PATTERN.matcher(emailBody);
        if (!matcher.matches()) {
            throw new IllegalStateException("Verification email does not contain verification link.");
        }
        String verificationLink = matcher.group(VERIFICATION_LINK_GROUP_NAME);
        String confirmationPage = IOUtils.toString(httpClient.execute(new HttpGet(verificationLink))
                .getEntity()
                .getContent());
        assertThat(confirmationPage, containsString("Your registration has been confirmed!"));
    }

    public Set<Cookie> signInWithUsername(@NonNull String username, @NonNull String password,
                                          @NonNull String expectedEmail) {
        return signIn(SignInUserRequest.builder()
                .username(username)
                .password(password)
                .build(), username, expectedEmail);
    }

    public Set<Cookie> signInWithEmail(@NonNull String email, @NonNull String password,
                                       @NonNull String expectedUsername) {
        return signIn(SignInUserRequest.builder()
                .email(email)
                .password(password)
                .build(), expectedUsername, email);
    }

    public Set<Cookie> signIn(@NonNull TestUserContext testUserContext) {
        return signIn(SignInUserRequest.builder()
                .username(testUserContext.getUsername())
                .password(testUserContext.getPassword())
                .build(), testUserContext.getUsername(), testUserContext.getTestEmailContext()
                .getEmail());
    }

    public Set<Cookie> signIn(@NonNull SignInUserRequest signInUserRequest, @NonNull String expectedUsername,
                              @NonNull String expectedEmail) {
        ApiResponse<SignInUserResult> signInUserResponse = climbAssistClient.signIn(signInUserRequest);
        ExceptionUtils.assertNoException(signInUserResponse);
        assertThat(signInUserResponse.getData()
                .isSuccessful(), is(true));

        ApiResponse<UserData> getUserResponse = climbAssistClient.getUser(signInUserResponse.getCookies());
        ExceptionUtils.assertNoException(getUserResponse);
        assertThat(getUserResponse.getData()
                .getUsername(), is(equalTo(expectedUsername)));
        assertThat(getUserResponse.getData()
                .getEmail(), is(equalTo(expectedEmail)));
        assertThat(getUserResponse.getData()
                .isAdministrator(), is(false));
        assertThat(getUserResponse.getData()
                .isEmailVerified(), is(true));

        return getUserResponse.getCookies();
    }

    public Set<Message> getAllMessagesFromSqs(@NonNull String queueUrl) {
        Set<Message> messages = new HashSet<>();
        List<Message> newMessages;
        do {
            newMessages = amazonSQS.receiveMessage(new ReceiveMessageRequest(queueUrl).withMaxNumberOfMessages(10))
                    .getMessages();
            messages.addAll(newMessages);
            if (!newMessages.isEmpty()) {
                amazonSQS.deleteMessageBatch(new DeleteMessageBatchRequest(queueUrl, newMessages.stream()
                        .map(message -> new DeleteMessageBatchRequestEntry(message.getMessageId(),
                                message.getReceiptHandle()))
                        .collect(Collectors.toList())));
            }
        } while (!newMessages.isEmpty());
        return messages;
    }

    public Optional<Message> findEmailReceiptMessage(Set<Message> messages) {
        return messages.stream()
                .filter(message -> new JSONObject(message.getBody()).getString(SQS_EMAIL_MESSAGE_SUBJECT_FIELD_NAME)
                        .equals(VERIFICATION_EMAIL_SUBJECT))
                .findAny();
    }

    public String retrieveRecaptchaBackDoorResponse() {
        return awsSecretsManager.getSecretValue(
                new GetSecretValueRequest().withSecretId(recaptchaBackDoorResponseSecretId))
                .getSecretString();
    }

}
