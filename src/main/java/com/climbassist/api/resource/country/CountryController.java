package com.climbassist.api.resource.country;

import com.climbassist.api.resource.common.CreateResourceResult;
import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.ResourceControllerDelegate;
import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceWithChildrenControllerDelegate;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.common.ValidDepth;
import com.climbassist.api.user.SessionUtils;
import com.climbassist.api.user.UserData;
import com.climbassist.api.user.authorization.AdministratorAuthorizationHandler;
import com.climbassist.api.user.authorization.Authorization;
import com.climbassist.metrics.Metrics;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.annotation.Nullable;
import javax.validation.Valid;
import java.util.Optional;
import java.util.Set;

@Builder
@RestController
@Validated
public class CountryController {

    @NonNull
    private final ResourceControllerDelegate<Country, NewCountry> resourceControllerDelegate;
    @NonNull
    private final ResourceWithChildrenControllerDelegate<Country, NewCountry> resourceWithChildrenControllerDelegate;
    @NonNull
    private final CountriesDao countriesDao;

    @Metrics(api = "GetCountry")
    @RequestMapping(path = "/v1/countries/{countryId}", method = RequestMethod.GET)
    public Country getResource(@ValidCountryId @NonNull @PathVariable String countryId,
                               @ValidDepth @RequestParam(required = false, defaultValue = "0") int depth,
                               @SuppressWarnings("OptionalUsedAsFieldOrParameterType") @SessionAttribute(value = SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME)
                                           @NonNull Optional<UserData> maybeUserData) throws ResourceNotFoundException {
        return resourceWithChildrenControllerDelegate.getResource(countryId, depth, maybeUserData);
    }

    @Metrics(api = "ListCountries")
    @RequestMapping(path = "/v1/countries", method = RequestMethod.GET)
    public Set<Country> getResources() {
        return countriesDao.getResources();
    }

    @Metrics(api = "CreateCountry")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/countries", method = RequestMethod.PUT)
    public CreateResourceResult<Country> createResource(@NonNull @Valid @RequestBody NewCountry newCountry) {
        return resourceControllerDelegate.createResource(newCountry);
    }

    @Metrics(api = "UpdateCountry")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/countries", method = RequestMethod.POST)
    public UpdateResourceResult updateResource(@NonNull @Valid @RequestBody Country country,
                                               @SuppressWarnings("OptionalUsedAsFieldOrParameterType") @SessionAttribute(value = SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME)
                                           @NonNull Optional<UserData> maybeUserData)
            throws ResourceNotFoundException {
        return resourceControllerDelegate.updateResource(country, maybeUserData);
    }

    @Metrics(api = "DeleteCountry")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/countries/{countryId}", method = RequestMethod.DELETE)
    public DeleteResourceResult deleteResource(@NonNull @ValidCountryId @PathVariable String countryId,
                                               @SuppressWarnings("OptionalUsedAsFieldOrParameterType") @SessionAttribute(value = SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME)
                                           @NonNull Optional<UserData> maybeUserData)
            throws ResourceNotFoundException, ResourceNotEmptyException {
        return resourceWithChildrenControllerDelegate.deleteResource(countryId, maybeUserData);
    }
}
