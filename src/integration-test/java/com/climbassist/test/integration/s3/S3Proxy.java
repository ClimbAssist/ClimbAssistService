package com.climbassist.test.integration.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.GetObjectRequest;
import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Builder
public class S3Proxy {

    @NonNull
    private final AmazonS3 amazonS3;

    public void assertS3ObjectEquals(String s3Url, InputStream expectedContents) throws IOException {
        AmazonS3URI amazonS3Uri = new AmazonS3URI(URI.create(s3Url));
        InputStream actualContents =
                amazonS3.getObject(new GetObjectRequest(amazonS3Uri.getBucket(), amazonS3Uri.getKey()))
                        .getObjectContent();
        assertThat(IOUtils.contentEquals(actualContents, expectedContents), is(true));
    }

    public void assertS3ObjectDoesNotExist(String s3Url) {
        AmazonS3URI amazonS3Uri = new AmazonS3URI(URI.create(s3Url));
        assertThat(amazonS3.doesObjectExist(amazonS3Uri.getBucket(), amazonS3Uri.getKey()), is(false));
    }
}
