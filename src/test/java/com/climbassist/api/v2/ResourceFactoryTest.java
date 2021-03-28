package com.climbassist.api.v2;

import com.climbassist.api.resource.common.state.State;
import com.google.common.testing.NullPointerTester;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResourceFactoryTest {

    @SuperBuilder
    public static class TestResource extends Resource {
        public TestResource() {
        }
    }

    public static class TestResourcePrivateConstructor extends Resource {
        private TestResourcePrivateConstructor() {
        }
    }

    public static class TestResourceThrowsException extends Resource {
        public TestResourceThrowsException() throws Exception {
            throw new Exception();
        }
    }

    public static class TestResourceNoDefaultConstructor extends Resource {
        public TestResourceNoDefaultConstructor(String unused) {

        }
    }

    private ResourceFactory resourceFactory;

    @BeforeEach
    void setUp() {
        resourceFactory = new ResourceFactory();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testConstructors(ResourceFactory.class, NullPointerTester.Visibility.PACKAGE);
        nullPointerTester.testInstanceMethods(resourceFactory, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void buildResource_returnsResourceWithNoFieldsSet_whenNoIdIsPassed() {
        assertThat(resourceFactory.buildResource(TestResource.class), is(equalTo(new TestResource())));
    }

    @Test
    void buildResource_throwsResourceInstantiationException_whenResourceClassIsAbstract() {
        assertThrows(ResourceInstantiationException.class, () -> resourceFactory.buildResource(Resource.class));
    }

    @Test
    void buildResource_throwsResourceInstantiationException_whenDefaultConstructorIsPrivate() {
        assertThrows(ResourceInstantiationException.class,
                () -> resourceFactory.buildResource(TestResourcePrivateConstructor.class));
    }

    @Test
    void buildResource_throwsResourceInstantiationException_whenDefaultConstructorThrowsException() {
        assertThrows(ResourceInstantiationException.class,
                () -> resourceFactory.buildResource(TestResourceThrowsException.class));
    }

    @Test
    void buildResource_throwsResourceInstantiationException_whenResourceClassDoesNotHaveDefaultConstructor() {
        assertThrows(ResourceInstantiationException.class,
                () -> resourceFactory.buildResource(TestResourceNoDefaultConstructor.class));
    }

    @Test
    void buildResource_returnsResourceWithId_whenIdIsPassed() {
        String id = "id";
        assertThat(resourceFactory.buildResource(TestResource.class, id), is(equalTo(TestResource.builder()
                .id(id)
                .build())));
    }
}
