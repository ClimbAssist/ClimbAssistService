package com.climbassist.api.v2;

import com.climbassist.api.ApiConfiguration;
import com.climbassist.api.resource.common.ResourceIdGenerator;
import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.state.State;
import com.climbassist.api.resource.country.CountryNotFoundException;
import com.climbassist.api.resource.country.NewCountry;
import com.climbassist.api.resource.country.ValidCountryId;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.validation.Valid;
import java.util.Optional;
import java.util.Set;

@Builder
@RestController
@Validated
public class CountryController {

    @NonNull
    private final ResourcesDao resourcesDao;
    @NonNull
    private final ResourceIdGenerator resourceIdGenerator;

    @Metrics(api = "GetCountry")
    @RequestMapping(path = "/" + ApiConfiguration.V2_VERSION + "/countries/{id}", method = RequestMethod.GET)
    public Country getCountry(@ValidCountryId @NonNull @PathVariable final String id,
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            @SessionAttribute(value = SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME)
            @NonNull Optional<UserData> maybeUserData) throws ResourceNotFoundException {
        return (maybeUserData.isPresent() ? resourcesDao.getResource(id, Country.class, maybeUserData.get()) :
                resourcesDao.getResource(id, Country.class)).orElseThrow(() -> new CountryNotFoundException(id));
    }

    @Metrics(api = "ListCountries")
    @RequestMapping(path = "/" + ApiConfiguration.V2_VERSION + "/countries", method = RequestMethod.GET)
    public Set<Country> listCountries(@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            @SessionAttribute(value = SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME)
            @NonNull Optional<UserData> maybeUserData) {
        return maybeUserData.isPresent() ? resourcesDao.listResources(Country.class, maybeUserData.get()) :
                resourcesDao.listResources(Country.class);
    }

    @Metrics(api = "CreateCountry")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/" + ApiConfiguration.V2_VERSION + "/countries", method = RequestMethod.PUT)
    public Country createCountry(@NonNull @Valid @RequestBody final NewCountry newCountry) {
        Country country = Country.builder()
                .id(resourceIdGenerator.generateResourceId(newCountry.getName()))
                .name(newCountry.getName())
                .state(State.IN_REVIEW)
                .build();
        resourcesDao.saveResource(country);
        return country;
    }

    @Metrics(api = "UpdateCountry")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/" + ApiConfiguration.V2_VERSION + "/countries", method = RequestMethod.POST)
    public Country updateCountry(@NonNull @Valid @RequestBody final Country country,
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            @SessionAttribute(value = SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME)
            @NonNull Optional<UserData> maybeUserData) throws ResourceNotFoundException {
        (maybeUserData.isPresent() ? resourcesDao.getResource(country.getId(), Country.class, maybeUserData.get()) :
                resourcesDao.getResource(country.getId(), Country.class)).orElseThrow(
                () -> new CountryNotFoundException(country.getId()));
        resourcesDao.saveResource(country);
        return country;
    }

    @Metrics(api = "DeleteCountry")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/" + ApiConfiguration.V2_VERSION + "/countries/{id}", method = RequestMethod.DELETE)
    public Country deleteCountry(@NonNull @ValidCountryId @PathVariable final String id,
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            @SessionAttribute(value = SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME)
            @NonNull Optional<UserData> maybeUserData) throws ResourceNotFoundException, ResourceNotEmptyException {
        Country country =
                (maybeUserData.isPresent() ? resourcesDao.getResource(id, Country.class, maybeUserData.get()) :
                        resourcesDao.getResource(id, Country.class)).orElseThrow(
                        () -> new CountryNotFoundException(id));
        resourcesDao.deleteResource(id, Country.class);
        return country;
    }
}
