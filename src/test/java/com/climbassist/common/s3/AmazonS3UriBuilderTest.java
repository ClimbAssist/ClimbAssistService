package com.climbassist.common.s3;

import com.amazonaws.services.s3.AmazonS3URI;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class AmazonS3UriBuilderTest {

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testStaticMethods(AmazonS3UriBuilder.class, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    public void buildAmazonS3Uri_returnsAmazonS3UriForBucketAndKey() {
        String bucket = "bucket";
        String key = "key";
        AmazonS3URI amazonS3Uri = AmazonS3UriBuilder.buildAmazonS3Uri(bucket, key);
        assertThat(amazonS3Uri.getBucket(), is(equalTo(bucket)));
        assertThat(amazonS3Uri.getKey(), is(equalTo(key)));
        assertThat(amazonS3Uri.getURI()
                .toString(), is(equalTo(String.format("https://%s.s3.amazonaws.com/%s", bucket, key))));
    }

}
