package com.climbassist.common.s3;

import com.amazonaws.services.s3.AmazonS3URI;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AmazonS3UriBuilder {

    private static final String OBJECT_URL_TEMPLATE = "https://%s.s3.amazonaws.com/%s";

    public static AmazonS3URI buildAmazonS3Uri(@NonNull final String bucket, @NonNull final String key) {
        return new AmazonS3URI(String.format(OBJECT_URL_TEMPLATE, bucket, key));
    }
}
