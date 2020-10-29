package com.climbassist.common.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.google.common.testing.NullPointerTester;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class S3ProxyTest {

    private static final String BUCKET = "bucket";
    private static final String KEY = "key";
    private static final String OBJECT_CONTENT = "this is an object";
    private static final InputStream INPUT_STREAM = IOUtils.toInputStream(OBJECT_CONTENT);
    private static final AmazonS3URI EXPECTED_OBJECT_URI = AmazonS3UriBuilder.buildAmazonS3Uri(BUCKET, KEY);

    @Mock
    private AmazonS3 mockAmazonS3;

    private S3Proxy s3Proxy;

    @BeforeEach
    void setUp() {
        s3Proxy = S3Proxy.builder()
                .amazonS3(mockAmazonS3)
                .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testInstanceMethods(s3Proxy, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void putPublicObject_putsObjectWithMetadata() throws IOException {
        assertThat(s3Proxy.putPublicObject(BUCKET, KEY, INPUT_STREAM, OBJECT_CONTENT.length()),
                is(equalTo(EXPECTED_OBJECT_URI)));
        ArgumentCaptor<PutObjectRequest> putObjectRequestArgumentCaptor =
                ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(mockAmazonS3).putObject(putObjectRequestArgumentCaptor.capture());
        PutObjectRequest putObjectRequest = putObjectRequestArgumentCaptor.getValue();
        assertThat(putObjectRequest.getBucketName(), equalTo(BUCKET));
        assertThat(putObjectRequest.getKey(), equalTo(KEY));
        assertThat(IOUtils.toString(putObjectRequest.getInputStream()), is(equalTo(OBJECT_CONTENT)));
        assertThat((int) putObjectRequest.getMetadata()
                .getContentLength(), is(equalTo(OBJECT_CONTENT.length())));
        assertThat(putObjectRequest.getCannedAcl(), is(equalTo(CannedAccessControlList.PublicRead)));
    }

    @Test
    void deleteObject_deletesObject() {
        s3Proxy.deleteObject(BUCKET, KEY);
        ArgumentCaptor<DeleteObjectRequest> deleteObjectRequestArgumentCaptor =
                ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(mockAmazonS3).deleteObject(deleteObjectRequestArgumentCaptor.capture());
        DeleteObjectRequest actualDeleteObjectRequest = deleteObjectRequestArgumentCaptor.getValue();
        assertThat(actualDeleteObjectRequest.getBucketName(), is(equalTo(BUCKET)));
        assertThat(actualDeleteObjectRequest.getKey(), is(equalTo(KEY)));
    }
}
