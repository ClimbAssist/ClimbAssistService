package com.climbassist.common.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.Builder;
import lombok.NonNull;

import java.io.InputStream;

@Builder
public class S3Proxy {

    private static final String OBJECT_URL_TEMPLATE = "https://%s.s3.amazonaws.com/%s";

    @NonNull
    private final AmazonS3 amazonS3;

    public String putPublicObject(@NonNull String bucket, @NonNull String key, @NonNull InputStream inputStream,
                                  long contentLength) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(contentLength);
        amazonS3.putObject(new PutObjectRequest(bucket, key, inputStream, objectMetadata).withCannedAcl(
                CannedAccessControlList.PublicRead));
        return String.format(OBJECT_URL_TEMPLATE, bucket, key);
    }

    public void deleteObject(@NonNull String bucket, @NonNull String key) {
        amazonS3.deleteObject(new DeleteObjectRequest(bucket, key));
    }
}
