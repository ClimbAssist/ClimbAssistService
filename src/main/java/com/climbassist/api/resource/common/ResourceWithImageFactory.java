package com.climbassist.api.resource.common;

public interface ResourceWithImageFactory<Resource extends ResourceWithImage> {

    Resource create(Resource resource, String imageLocation);
}
