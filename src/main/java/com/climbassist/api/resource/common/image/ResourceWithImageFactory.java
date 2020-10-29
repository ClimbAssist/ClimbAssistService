package com.climbassist.api.resource.common.image;

public interface ResourceWithImageFactory<Resource extends ResourceWithImage> {

    Resource create(Resource resource, String imageLocation, String jpgImageLocation);
}
