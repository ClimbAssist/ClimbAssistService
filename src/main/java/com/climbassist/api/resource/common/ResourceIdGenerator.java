package com.climbassist.api.resource.common;

import com.github.slugify.Slugify;
import lombok.NonNull;
import org.apache.commons.lang3.RandomStringUtils;

public class ResourceIdGenerator {

    @NonNull
    private static final Slugify SLUGIFY = new Slugify().withUnderscoreSeparator(false)
            .withCustomReplacement("_", "-");

    public String generateResourceId(@NonNull String prefix) {
        String slug = SLUGIFY.slugify(prefix);
        return slug + (slug.isEmpty() ? "" : "-") + RandomStringUtils.randomAlphanumeric(10)
                .toLowerCase();
    }
}
