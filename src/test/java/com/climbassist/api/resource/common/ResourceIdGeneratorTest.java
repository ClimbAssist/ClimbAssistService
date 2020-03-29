package com.climbassist.api.resource.common;

import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;

class ResourceIdGeneratorTest {

    private ResourceIdGenerator resourceIdGenerator;

    @BeforeEach
    void setUp() {
        resourceIdGenerator = new ResourceIdGenerator();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testInstanceMethods(resourceIdGenerator, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void generateResourceId_returnsSlugifiedNameWithRandomId_whenNameIsLowerCaseAlphabetic() {
        String resourceId = resourceIdGenerator.generateResourceId("test");
        assertThat(resourceId, matchesPattern("test-[0-9a-z-]{10}"));
    }

    @Test
    void generateResourceId_returnsSlugifiedNameWithRandomId_whenNameIsUpperCaseAlphabetic() {
        String resourceId = resourceIdGenerator.generateResourceId("TEST");
        assertThat(resourceId, matchesPattern("test-[0-9a-z-]{10}"));
    }

    @Test
    void generateResourceId_returnsSlugifiedNameWithRandomId_whenNameIsNumeric() {
        String resourceId = resourceIdGenerator.generateResourceId("12345");
        assertThat(resourceId, matchesPattern("12345-[0-9a-z-]{10}"));
    }

    @Test
    void generateResourceId_returnsOnlyRandomId_whenNameIsNonAlphanumeric() {
        String resourceId = resourceIdGenerator.generateResourceId(" !@#$%^&*()-=_+[]{};':\",./<>?");
        assertThat(resourceId, matchesPattern("[0-9a-z-]{10}"));
    }

    @Test
    void generateResourceId_returnsOnlyRandomId_whenNameIsEmpty() {
        String resourceId = resourceIdGenerator.generateResourceId("");
        assertThat(resourceId, matchesPattern("[0-9a-z-]{10}"));
    }

    @Test
    void generateResourceId_returnsSlugifiedNameWithRandomId_whenNameIsMixedCaseAlphanumeric() {
        String resourceId = resourceIdGenerator.generateResourceId("test1234TESTtest1234");
        assertThat(resourceId, matchesPattern("test1234testtest1234-[0-9a-z-]{10}"));
    }

    @Test
    void generateResourceId_returnsSlugifiedNameWithRandomId_whenNameIsAllCharactersMixed() {
        String resourceId = resourceIdGenerator.generateResourceId("test1234!@#$%^!@#$%^TEST_test");
        assertThat(resourceId, matchesPattern("test1234-test-test-[0-9a-z-]{10}"));
    }

    @Test
    void generateResourceId_returnsSlugifiedNameWithRandomId_whenNameHasSpaces() {
        String resourceId = resourceIdGenerator.generateResourceId("This is a name");
        assertThat(resourceId, matchesPattern("this-is-a-name-[0-9a-z-]{10}"));
    }

    @Test
    void generateResourceId_returnsSlugifiedNameWithRandomId_whenNameHasLeadingAndTrailingNonAlphanumericCharacters() {
        String resourceId = resourceIdGenerator.generateResourceId("^&*()This is a name  ");
        assertThat(resourceId, matchesPattern("this-is-a-name-[0-9a-z-]{10}"));
    }
}