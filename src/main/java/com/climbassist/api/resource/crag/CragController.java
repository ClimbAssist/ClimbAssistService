package com.climbassist.api.resource.crag;

import com.amazonaws.services.s3.AmazonS3URI;
import com.climbassist.api.resource.common.CreateResourceResult;
import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceWithChildrenControllerDelegate;
import com.climbassist.api.resource.common.ResourceWithImageControllerDelegate;
import com.climbassist.api.resource.common.ResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.common.UploadImageResult;
import com.climbassist.api.resource.common.ValidDepth;
import com.climbassist.api.resource.path.PathsDao;
import com.climbassist.api.resource.subarea.SubArea;
import com.climbassist.api.resource.subarea.ValidSubAreaId;
import com.climbassist.api.resource.wall.WallsDao;
import com.climbassist.api.user.authorization.AdministratorAuthorizationHandler;
import com.climbassist.api.user.authorization.Authorization;
import com.climbassist.common.s3.S3Proxy;
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
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Set;

@Builder
@RestController
@Validated
@Slf4j
public class CragController {

    private static final String HIGH_RESOLUTION_MODEL_NAME = "high-resolution-model.glb";
    private static final String LOW_RESOLUTION_MODEL_NAME = "low-resolution-model.glb";
    private static final String HIGH_RESOLUTION_MODEL_KEY_TEMPLATE = "%s/%s.glb";
    private static final String LOW_RESOLUTION_MODEL_KEY_TEMPLATE = "%s/%s-low-resolution.glb";
    private static final String IMAGE_NAME = "photo.webp";
    private static final String IMAGE_KEY_TEMPLATE = "%s/%s.webp";

    @NonNull
    private final ResourceWithParentControllerDelegate<Crag, NewCrag, SubArea> resourceWithParentControllerDelegate;
    @NonNull
    private final ResourceWithChildrenControllerDelegate<Crag, NewCrag> resourceWithChildrenControllerDelegate;
    @NonNull
    private final ResourceWithImageControllerDelegate<Crag> resourceWithImageControllerDelegate;
    @NonNull
    private final CragsDao cragsDao;
    @NonNull
    private final S3Proxy s3Proxy;
    @NonNull
    private final String modelsBucketName;
    @NonNull
    private final CragNotFoundExceptionFactory cragNotFoundExceptionFactory;
    @NonNull
    private final WallsDao wallsDao;
    @NonNull
    private final PathsDao pathsDao;
    @NonNull
    private final CragNotEmptyExceptionFactory cragNotEmptyExceptionFactory;

    @Metrics(api = "GetCrag")
    @RequestMapping(path = "/v1/crags/{cragId}", method = RequestMethod.GET)
    public Crag getResource(@ValidCragId @NonNull @PathVariable String cragId,
                            @ValidDepth @RequestParam(required = false, defaultValue = "0") int depth)
            throws ResourceNotFoundException {
        return resourceWithChildrenControllerDelegate.getResource(cragId, depth);
    }

    @Metrics(api = "ListCrags")
    @RequestMapping(path = "/v1/sub-areas/{subAreaId}/crags", method = RequestMethod.GET)
    public Set<Crag> getResourcesForParent(@ValidSubAreaId @NonNull @PathVariable String subAreaId)
            throws ResourceNotFoundException {
        return resourceWithParentControllerDelegate.getResourcesForParent(subAreaId);
    }

    @Metrics(api = "CreateCrag")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/crags", method = RequestMethod.PUT)
    public CreateResourceResult<Crag> createResource(@NonNull @Valid @RequestBody NewCrag newCrag)
            throws ResourceNotFoundException {
        return resourceWithParentControllerDelegate.createResource(newCrag);
    }

    @Metrics(api = "UpdateCrag")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/crags", method = RequestMethod.POST)
    public UpdateResourceResult updateResource(@NonNull @Valid @RequestBody Crag crag)
            throws ResourceNotFoundException {
        return resourceWithParentControllerDelegate.updateResource(crag);
    }

    @Metrics(api = "DeleteCrag")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/crags/{cragId}", method = RequestMethod.DELETE)
    public DeleteResourceResult deleteResource(@NonNull @ValidCragId @PathVariable String cragId)
            throws ResourceNotFoundException, ResourceNotEmptyException {
        Crag crag = cragsDao.getResource(cragId)
                .orElseThrow(() -> cragNotFoundExceptionFactory.create(cragId));
        if (!wallsDao.getResources(cragId)
                .isEmpty() || !pathsDao.getResources(cragId)
                .isEmpty()) {
            throw cragNotEmptyExceptionFactory.create(cragId);
        }
        cragsDao.deleteResource(cragId);
        if (crag.getImageLocation() != null) {
            log.info(String.format("Deleting image for crag %s at %s", cragId, crag.getImageLocation()));
            AmazonS3URI amazonS3URI = new AmazonS3URI(crag.getImageLocation());
            s3Proxy.deleteObject(amazonS3URI.getBucket(), amazonS3URI.getKey());
        }
        if (crag.getModel() != null) {
            log.info(String.format("Deleting model for crag %s at %s", cragId, crag.getModel()
                    .getModelLocation()));
            AmazonS3URI modelAmazonS3Uri = new AmazonS3URI(crag.getModel()
                    .getModelLocation());
            s3Proxy.deleteObject(modelAmazonS3Uri.getBucket(), modelAmazonS3Uri.getKey());

            log.info(String.format("Deleting model for crag %s at %s", cragId, crag.getModel()
                    .getLowResModelLocation()));
            AmazonS3URI lowResModelAmazonS3Uri = new AmazonS3URI(crag.getModel()
                    .getLowResModelLocation());
            s3Proxy.deleteObject(lowResModelAmazonS3Uri.getBucket(), lowResModelAmazonS3Uri.getKey());
        }
        return DeleteResourceResult.builder()
                .successful(true)
                .build();
    }

    @Metrics(api = "UploadCragModel")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/crags/{cragId}/models", method = RequestMethod.POST)
    public UploadModelsResult uploadModels(@ValidCragId @NonNull @PathVariable String cragId,
                                           @NonNull @RequestParam(HIGH_RESOLUTION_MODEL_NAME)
                                                   MultipartFile highResolutionModel,
                                           @NonNull @RequestParam(LOW_RESOLUTION_MODEL_NAME)
                                                   MultipartFile lowResolutionModel)
            throws IOException, CragNotFoundException {

        Crag crag = cragsDao.getResource(cragId)
                .orElseThrow(() -> new CragNotFoundException(cragId));

        String highResolutionModelLocation = s3Proxy.putPublicObject(modelsBucketName,
                String.format(HIGH_RESOLUTION_MODEL_KEY_TEMPLATE, cragId, cragId), highResolutionModel.getInputStream(),
                highResolutionModel.getSize());
        String lowResolutionModelLocation = s3Proxy.putPublicObject(modelsBucketName,
                String.format(LOW_RESOLUTION_MODEL_KEY_TEMPLATE, cragId, cragId), lowResolutionModel.getInputStream(),
                lowResolutionModel.getSize());

        Crag newCrag = crag.toBuilder()
                .model(crag.getModel() == null ? Model.builder()
                        .modelLocation(highResolutionModelLocation)
                        .lowResModelLocation(lowResolutionModelLocation)
                        .build() : crag.getModel()
                        .toBuilder()
                        .modelLocation(highResolutionModelLocation)
                        .lowResModelLocation(lowResolutionModelLocation)
                        .build())
                .build();
        cragsDao.saveResource(newCrag);

        return UploadModelsResult.builder()
                .successful(true)
                .build();
    }

    @Metrics(api = "UploadCragPhoto")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/crags/{cragId}/photo", method = RequestMethod.POST)
    public UploadImageResult uploadImage(@ValidCragId @NonNull @PathVariable String cragId,
                                         @NonNull @RequestParam(IMAGE_NAME) MultipartFile image)
            throws IOException, ResourceNotFoundException {
        return resourceWithImageControllerDelegate.uploadImage(cragId, image);
    }
}
