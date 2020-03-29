package com.climbassist.api.resource.common.batch;

import java.util.Set;

public interface BatchDeleteResourcesRequest {

    Set<String> getResourceIds();
}
