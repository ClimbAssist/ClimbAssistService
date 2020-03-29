package com.climbassist.api.resource.path;

import com.climbassist.api.resource.common.CreateResourceResult;
import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.ResourceControllerDelegate;
import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceWithChildrenControllerDelegate;
import com.climbassist.api.resource.common.ResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.common.ValidDepth;
import com.climbassist.api.resource.common.ordering.InvalidOrderingException;
import com.climbassist.api.resource.crag.Crag;
import com.climbassist.api.resource.crag.ValidCragId;
import com.climbassist.api.user.authorization.AdministratorAuthorizationHandler;
import com.climbassist.api.user.authorization.Authorization;
import com.climbassist.metrics.Metrics;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Set;

@Builder
@RestController
@Slf4j
@Validated
public class PathController {

    @NonNull
    private final ResourceControllerDelegate<Path, NewPath> resourceControllerDelegate;
    @NonNull
    private final ResourceWithParentControllerDelegate<Path, NewPath, Crag> resourceWithParentControllerDelegate;
    @NonNull
    private final ResourceWithChildrenControllerDelegate<Path, NewPath> resourceWithChildrenControllerDelegate;

    @Metrics(api = "GetPath")
    @RequestMapping(path = "/v1/paths/{pathId}", method = RequestMethod.GET)
    public Path getResource(@ValidPathId @NonNull @PathVariable String pathId,
                            @ValidDepth @RequestParam(required = false, defaultValue = "0") int depth)
            throws ResourceNotFoundException {
        return resourceWithChildrenControllerDelegate.getResource(pathId, depth);
    }

    @Metrics(api = "ListPaths")
    @RequestMapping(path = "/v1/crags/{cragId}/paths", method = RequestMethod.GET)
    public Set<Path> getResourcesForParent(@ValidCragId @NonNull @PathVariable String cragId)
            throws InvalidOrderingException, ResourceNotFoundException {
        return resourceWithParentControllerDelegate.getResourcesForParent(cragId);
    }

    @Metrics(api = "CreatePath")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/paths", method = RequestMethod.PUT)
    public CreateResourceResult<Path> createResource(@NonNull @Valid @RequestBody NewPath newPath)
            throws ResourceNotFoundException {
        return resourceWithParentControllerDelegate.createResource(newPath);
    }

    @Metrics(api = "UpdatePath")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/paths", method = RequestMethod.POST)
    public UpdateResourceResult updateResource(@NonNull @Valid @RequestBody Path path)
            throws ResourceNotFoundException {
        return resourceWithParentControllerDelegate.updateResource(path);
    }

    @Metrics(api = "DeletePath")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/paths/{pathId}", method = RequestMethod.DELETE)
    public DeleteResourceResult deleteResource(@NonNull @ValidPathId @PathVariable String pathId)
            throws ResourceNotFoundException, ResourceNotEmptyException {
        return resourceWithChildrenControllerDelegate.deleteResource(pathId);
    }
}
