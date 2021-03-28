# API Documentation

<details>
  <summary>Table of Contents</summary>

- [Request/Response Shape](#requestresponse-shape)
- [Failures](#failures)
    * [Common Failures](#common-failures)
- [Resource Shapes](#resource-shapes)
    * [Country](#country)
    * [Region](#region)
    * [Area](#area)
    * [Sub-Area](#sub-area)
    * [Crag](#crag)
    * [Wall](#wall)
    * [Route](#route)
    * [Pitch](#pitch)
    * [Point](#point)
    * [Path](#path)
    * [Path Point](#path-point)
    * [User](#user)
- [Depth](#depth)
- [Ordering](#ordering)
- [Optional Update Parameters](#optional-update-parameters)
- [Authorization](#authorization)
- [V2 APIs](#v2-apis)
    * [Country](#country-1)
        + [ListCountries](#listcountries)
        + [GetCountry](#getcountry)
        + [CreateCountry](#createcountry)
        + [UpdateCountry](#updatecountry)
        + [DeleteCountry](#deletecountry)
- [V1 APIs](#v1-apis)
    * [Country](#country-2)
        + [ListCountries](#listcountries-1)
        + [GetCountry](#getcountry-1)
        + [CreateCountry](#createcountry-1)
        + [UpdateCountry](#updatecountry-1)
        + [DeleteCountry](#deletecountry-1)
    * [Region](#region-1)
        + [ListRegions](#listregions)
        + [GetRegion](#getregion)
        + [CreateRegion](#createregion)
        + [UpdateRegion](#updateregion)
        + [DeleteRegion](#deleteregion)
    * [Area](#area-1)
        + [ListAreas](#listareas)
        + [GetArea](#getarea)
        + [CreateArea](#createarea)
        + [UpdateArea](#updatearea)
        + [DeleteArea](#deletearea)
    * [Sub-Area](#sub-area-1)
        + [ListSubAreas](#listsubareas)
        + [GetSubArea](#getsubarea)
        + [CreateSubArea](#createsubarea)
        + [UpdateSubArea](#updatesubarea)
        + [DeleteSubArea](#deletesubarea)
    * [Crag](#crag-1)
        + [ListCrags](#listcrags)
        + [GetCrag](#getcrag)
        + [CreateCrag](#createcrag)
        + [UpdateCrag](#updatecrag)
        + [UploadCragModels](#uploadcragmodels)
        + [UploadCragPhoto](#uploadcragphoto)
        + [DeleteCrag](#deletecrag)
    * [Wall](#wall-1)
        + [ListWalls](#listwalls)
        + [GetWall](#getwall)
        + [CreateWall](#createwall)
        + [UpdateWall](#updatewall)
        + [DeleteWall](#deletewall)
    * [Route](#route-1)
        + [ListRoutes](#listroutes)
        + [GetRoute](#getroute)
        + [CreateRoute](#createroute)
        + [UpdateRoute](#updateroute)
        + [UploadRoutePhoto](#uploadroutephoto)
        + [DeleteRoute](#deleteroute)
    * [Pitch](#pitch-1)
        + [ListPitches](#listpitches)
        + [GetPitch](#getpitch)
        + [CreatePitch](#createpitch)
        + [UpdatePitch](#updatepitch)
        + [DeletePitch](#deletepitch)
    * [Point](#point-1)
        + [ListPoints](#listpoints)
        + [GetPoint](#getpoint)
        + [CreatePoint](#createpoint)
        + [BatchCreatePoints](#batchcreatepoints)
        + [UpdatePoint](#updatepoint)
        + [DeletePoint](#deletepoint)
        + [BatchDeletePoints](#batchdeletepoints)
    * [Path](#path-1)
        + [ListPaths](#listpaths)
        + [GetPath](#getpath)
        + [CreatePath](#createpath)
        + [UpdatePath](#updatepath)
        + [DeletePath](#deletepath)
    * [Path Point](#path-point-1)
        + [ListPathPoints](#listpathpoints)
        + [GetPathPoint](#getpathpoint)
        + [CreatePathPoint](#createpathpoint)
        + [BatchCreatePathPoints](#batchcreatepathpoints)
        + [UpdatePathPoint](#updatepathpoint)
        + [DeletePathPoint](#deletepathpoint)
        + [BatchDeletePathPoints](#batchdeletepathpoints)
    * [User](#user-1)
        + [RegisterUser](#registeruser)
        + [SignIn](#signin)
        + [SignOut](#signout)
        + [GetUser](#getuser)
        + [DeleteUser](#deleteuser)
        + [UpdateUser](#updateuser)
        + [VerifyEmail](#verifyemail)
        + [SendVerificationEmail](#sendverificationemail)
        + [ChangePassword](#changepassword)
        + [SendPasswordResetEmail](#sendpasswordresetemail)
        + [ResetPassword](#resetpassword)
    * [Contact](#contact)
        + [SendContactEmail](#sendcontactemail)

</details>

## Request/Response Shape

The APIs in ClimbAssistService follow a standard input and output format, which is always JSON. For all successful API
calls, the response will contain the payload in the `data` field, whether it is an object or an array.

```json
{
  "data": {
    ...
  }
}
```

or

```json
{
  "data": [
    ...
  ]
}
```

## Failures

If an API encounters a failure while processing, it will return an error. In this case, the response object will not
have the `data` field, and instead will have an `error` field containing an error type and a relevant error message.
Additionally, the status code of the response will reflect the error.

```json
{
  "error": {
    "type": string,
    "message": string
  }
}
```

### Common Failures

These failures are common to many, if not all, APIs, so they will not be documented specifically for each API.

|Error Type|Status Code|Reason|
|---|---|---|
|`InternalFailure`|500|Thrown when an unexpected error occurs. This indicates a bug in the back-end.|
|`ApiNotFoundException`|404|Thrown when an API does not exist at the specified path and method.|
|`InvalidRequestException`|400|Thrown when the request is invalid. This can happen if a required body is missing, if the body is not valid JSON, or if the body is missing required fields, among other reasons.|
|`AuthorizationException`|401|Thrown when the caller does not have sufficient authorization to call the API. See [Authorization](#Authorization)|
|`ResourceNotFoundException`|404|Thrown when a requested resource (i.e. `Country`, `Area`, `Crag`, `Route`) does not exist or the current user does not have access to it.|
|`UserNotFoundException`|404|Thrown when a requested user does not exist.|
|`InvalidOrderingException`|409|Thrown when the caller requests an ordered list of resources but the ordering is invalid. See [Ordering](#Ordering).|

## Resource Shapes

Each resource has a specific shape which can be nested into other resources. Child resources are ignored in create and
update APIs.

### Country

#### V1

```json
{
  "countryId": string,
  "name": string,
  "regions": [ // only present if the resource has children
    Region,
    ...
  ]
}
```

l

#### V2

```json
{
  "id": string,
  "name": string,
  "state": string "IN_REVIEW" | "PUBLIC"
}
```

### Region

```json
{
  "regionId": string,
  "countryId": string,
  "name": string,
  "areas": [ // only present if the resource has children
    Area,
    ...
  ]
}
```

### Area

```json
{
  "areaId": string,
  "regionId": string,
  "name"
  string,
  "description": string,
  "sub-areas": [ // only present if the resource has children
    Sub-Area,
    ...
  ]
}
```

### Sub-Area

```json
{
  "subAreaId": string,
  "areaId": string,
  "name": string,
  "description": string,
  "crags": [ // only present if the resource has children
    Crag,
    ...
  ]
}
```

### Crag

```json
{
  "cragId": string,
  "subAreaId": string,
  "name": string,
  "description": string,
  "imageLocation": string, // optional
  "location": {
    "longitude": double,
    "latitude": double,
    "zoom": double
  },
  "model": { // optional
    "modelLocation": string,
    "lowResModelLocation": string,
    "azimuth": {
      "minimum": double,
      "maximum": double
    },
    "light": double,
    "scale": double,
    "modelAngle": double
  },
  "parking": [ // optional
    {
      "latitude": double,
      "longitude": double
    },
    ...
  ],
  "walls": [ // only present if the resource has children
    Wall,
    ...
  ],
  "state": string, "IN_REVIEW" | "PUBLIC"
}
```

### Wall

```json
{
  "wallId": string,
  "cragId": string,
  "name": string,
  "first": boolean, // optional
  "next": string, // optional
  "routes": [ // only present if the resource has children
    Route,
    ...
  ]
}
```

### Route

```json
{
  "routeId": string,
  "wallId": string,
  "name": string
  "description": string,
  "grade": int,
  "gradeModifier": string, // optional
  "danger": string, // optional
  "center": {
    "x": double,
    "y": double,
    "z": double
  },
  "mainImageLocation": string, // optional
  "protection": string, // optional
  "style": string,
  "first": boolean, // optional
  "next": string, // optional
  "pitches": [ // only present if the resource has children
    Pitch,
    ...
  ]
}
```

### Pitch

```json
{
  "pitchId": string,
  "routeId": string,
  "description": string,
  "grade": int,
  "gradeModifier": string, // optional
  "danger": string, // optional
  "anchors": { // optional
    "fixed": boolean
    "x": double,
    "y": double,
    "z": double
  },
  "distance": double, // optional
  "first": boolean, // optional
  "next": string, // optional
  "points": [ // only present if the resource has children
    Point,
    ...
  ]
}
```

### Point

```json
{
  "pointId": string,
  "pitchId": string,
  "x": double,
  "y": double,
  "z": double,
  "first": boolean, // optional
  "next": string // optional
}
```

### Path

```json
{
  "pathId": string,
  "cragId": string,
  "pathPoints": [ // only present if the resource has children
    PathPoint,
    ...
  ]
}
```

### Path Point

```json
{
  "pathPointId": string,
  "pathId": string,
  "latitude": double,
  "longitude": double,
  "first": boolean, // optional
  "next": string // optional
}
```

### User

```json
{
  "username": string,
  "email": string,
  "isEmailVerified": boolean,
  "isAdministrator": boolean
}
```

## Depth

In most of the get APIs, there is an optional `depth` query parameter which specifies how deep you want to retrieve
children. If `depth` is 0, only the specifically requested resource is returned. If `depth` is 1, the resource and its
children are returned. If depth is 2, the resource, its children, and its children's children are returned, and so on.
If not specified, `depth` defaults to 0.

## Ordering

In several of the list APIs, there is an optional `ordered` parameter which specifies if the results should be returned
in order. Resource order is determined by the `first` and `next` fields of each resource. For an ordering to be valid,
there must only be one resource with `first` set to true, and each `next` field must be an existing resource and must
not create a loop. If `ordered` is true, the server will attempt to order the results before returning, but may throw
an `InvalidOrderingException` if the ordering is invalid. If not specified, `ordered` defaults to true.

For create and update APIs on orderable resources, the `first` and `next` fields of neighboring resources will not be
automatically updated, so any changes to the ordering will require multiple calls to update each of the affected
resources. The server will only validate that all `next` fields are existing resources, and will not attempt to validate
other requirements for a valid ordering.

## State

Some resources types have a `state` field. This is used to hide resources from non-administrator users until they have
been approved by an administrator. If a resource has state `IN_REVIEW` and the caller is not an administrator, the APIs
will behave exactly as if that resource does not exist. It will not appear in queries using the `depth` parameter
either. The state can be updated by calling the appropriate update API. All newly created resources with state will
always start with state `IN_REVIEW`.

## Optional Update Parameters

In all of the update APIs, if parameters are optional, their values will be deleted if they are not specified in the
request. This applies to all parameters except children parameters, which are ignored in create and update requests.

## Authorization

Some APIs require specific authorization to be called. If the caller is not authorized, the API will return a
`AuthorizationException`. An authorization strategy of "None" means that anyone can call that API. "User"
requires the caller to be a signed-in user. "Administrator" requires the caller to be a signed-in user and also be an
administrator, which is a privileged access level that can only be obtained by manually adding that user to the
"Administrators" Cognito group.

## V2 APIs

The V2 APIs work similarly to V1 with a few key differences. The first is that all resource types can be `PUBLIC` or
`IN_REVIEW`, and thus, the corresponding APIs will only return resources to which the user has access. All resources are
created with state `IN_REVIEW`. Second is that the `depth` parameter has been removed completely is it is not necessary
for the new website experience. The input and output shapes have also been tweaked to be more RESTful.

### Country

#### GetCountry

|Method|Path|Description|Authorization|
|---|---|---|---|
|`GET`|`/v2/countries/{id}`|Returns a single country.|None|

##### Output

`Country`

#### ListCountries

|Method|Path|Description|Authorization|
|---|---|---|---|
|`GET`|`/v2/countries`|Returns all countries to which the user has access.|None|

##### Output

`Country[]`

#### CreateCountry

|Method|Path|Description|Authorization|
|---|---|---|---|
|`PUT`|`/v2/countries`|Creates a new country with state `IN_REVIEW`. Returns the newly created country.|Administrator|

##### Input

```json
{
  "name": string
}
```

##### Output

`Country`

#### UpdateCountry

|Method|Path|Description|Authorization|
|---|---|---|---|
|`POST`|`/v2/countries`|Updates the specified country. Returns the newly updated Country.|Administrator|

##### Input

`Country`

##### Output

`Country`

#### DeleteCountry

|Method|Path|Description|Authorization|
|---|---|---|---|
|`DELETE`|`/v2/countries/{id}`|Deletes the specified country. Returns the deleted country.|Administrator|

##### Output

`Country`

## V1 APIs

### Country

#### ListCountries

|Method|Path|Description|Authorization|
|---|---|---|---|
|`GET`|`/v1/countries`|Returns all countries.|None|

##### Output

`Country[]`

#### GetCountry

|Method|Path|Description|Authorization|
|---|---|---|---|
|`GET`|`/v1/countries/{countryId}`|Returns a single country and its children, if specified.|None|

##### Query Parameters

`depth`: `int`

##### Output

`Country`

#### CreateCountry

|Method|Path|Description|Authorization|
|---|---|---|---|
|`PUT`|`/v1/countries`|Creates a new country.|Administrator|

##### Input

```json
{
  "name": string
}
```

##### Output

```json
{
  "countryId": string
}
```

#### UpdateCountry

|Method|Path|Description|Authorization|
|---|---|---|---|
|`POST`|`/v1/countries`|Updates the specified country.|Administrator|

##### Input

`Country`

##### Output

```json
{
  "successful": true
}
```

#### DeleteCountry

|Method|Path|Description|Authorization|
|---|---|---|---|
|`DELETE`|`/v1/countries/{countryId}`|Deletes the specified country.|Administrator|

##### Output

```json
{
  "successful": true
}
```

### Region

#### ListRegions

|Method|Path|Description|Authorization|
|---|---|---|---|
|`GET`|`/v1/countries/{countryId}/regions`|Returns all regions within the specified country.|None|

##### Output

`Region[]`

#### GetRegion

|Method|Path|Description|Authorization|
|---|---|---|---|
|`GET`|`/v1/regions/{regionId}`|Returns a single region and its children, if specified.|None|

##### Query Parameters

`depth`: `int`

##### Output

`Region`

#### CreateRegion

|Method|Path|Description|Authorization|
|---|---|---|---|
|`PUT`|`/v1/regions`|Creates a new region.|Administrator|

##### Input:

```json
{
  "countryId": string,
  "name": string
}
```

##### Output:

```json
{
  "regionId": string
}
```

#### UpdateRegion

|Method|Path|Description|Authorization|
|---|---|---|---|
|`POST`|`/v1/regions`|Updates the specified region.|Administrator|

##### Input

`Region`

##### Output

```json
{
  "successful": true
}
```

#### DeleteRegion

|Method|Path|Description|Authorization|
|---|---|---|---|
|`DELETE`|`/v1/regions/{regionId}`|Deletes the specified region.|Administrator|

##### Output

```json
{
  "successful": true
}
```

### Area

#### ListAreas

|Method|Path|Description|Authorization|
|---|---|---|---|
|`GET`|`/v1/regions/{regionId}/areas`|Returns all areas within the specified region.|None|

##### Output

`Area[]`

#### GetArea

|Method|Path|Description|Authorization|
|---|---|---|---|
|`GET`|`/v1/areas/{areaId}`|Returns a single area and its children, if specified.|None|

##### Query Parameters

`depth`: `int`

##### Output

`Area`

#### CreateArea

|Method|Path|Description|Authorization|
|---|---|---|---|
|`PUT`|`/v1/areas`|Creates a new area.|Administrator|

##### Input

```json
{
  "regionId": string,
  "name": string,
  "description": string, // optional
  "location": {
    "longitude": double,
    "latitude": double,
    "zoom": double
  }
}
```

##### Output

```json
{
  "areaId": string
}
```

#### UpdateArea

|Method|Path|Description|Authorization|
|---|---|---|---|
|`POST`|`/v1/areas`|Updates the specified area.|Administrator|

##### Input

`Area`

##### Output

```json
{
  "successful": true
}
```

#### DeleteArea

|Method|Path|Description|Authorization|
|---|---|---|---|
|`DELETE`|`/v1/areas/{areaId}`|Deletes the specified area.|Administrator|

##### Output

```json
{
  "successful": true
}
```

### Sub-Area

#### ListSubAreas

|Method|Path|Description|Authorization|
|---|---|---|---|
|`GET`|`/v1/areas/{areaId}/sub-areas`|Returns all sub-areas within the specified area.|None|

##### Output

`SubArea[]`

#### GetSubArea

|Method|Path|Description|Authorization|
|---|---|---|---|
|`GET`|`/v1/sub-areas/{subAreaId}`|Returns a single sub-area and its children, if specified.|None|

##### Query Parameters

`depth`: `int`

##### Output

`SubArea`

#### CreateSubArea

|Method|Path|Description|Authorization|
|---|---|---|---|
|`PUT`|`/v1/sub-areas`|Creates a new sub-area.|Administrator|

##### Input

```json
{
  "areaId": string,
  "name": string,
  "description": string
}
```

##### Output

```json
{
  "subAreaId": string
}
```

#### UpdateSubArea

|Method|Path|Description|Authorization|
|---|---|---|---|
|`POST`|`/v1/sub-areas`|Updates the specified sub-area.|Administrator|

##### Input

`SubArea`

##### Output

```json
{
  "successful": true
}
```

#### DeleteSubArea

|Method|Path|Description|Authorization|
|---|---|---|---|
|`DELETE`|`/v1/sub-areas/{subAreaId}`|Deletes the specified sub-area.|Administrator|

##### Output

```json
{
  "successful": true
}
```

### Crag

#### ListCrags

|Method|Path|Description|Authorization|
|---|---|---|---|
|`GET`|`/v1/sub-areas/{subAreaId}/crags`|Returns all crags within the specified sub-area.|None|

##### Output

`Crag[]`

#### GetCrag

|Method|Path|Description|Authorization|
|---|---|---|---|
|`GET`|`/v1/crags/{cragId}`|Returns a single crag and its children, if specified.|None|

##### Query Parameters

`depth`: `int`

##### Output

`Crag`

#### CreateCrag

|Method|Path|Description|Authorization|
|---|---|---|---|
|`PUT`|`/v1/crags`|Creates a new crag.|Administrator|

##### Input

```json
{
  "subAreaId": string,
  "name": string,
  "description": string,
  "location": {
    "longitude": double,
    "latitude": double,
    "zoom": double
  }
}
```

##### Output

```json
{
  "cragId": string
}
```

#### UpdateCrag

|Method|Path|Description|Authorization|
|---|---|---|---|
|`POST`|`/v1/crags`|Updates the specified crag.|Administrator|

##### Input

`Crag`

##### Output

```json
{
  "successful": true
}
```

#### UploadCragModels

|Method|Path|Description|Authorization|
|---|---|---|---|
|`POST`|`/v1/crags/{cragId}/models`|Uploads new 3D models for the specified crag.|Administrator|

##### Input

Form-data request with two files included. The first file is the high-resolution model, which must have the key
`high-resolution-model.glb`. The second file is the low-resolution model, which must have the key
`low-resolution-model.glb`.

##### Output

```json
{
  "successful": true
}
```

#### UploadCragPhoto

|Method|Path|Description|Authorization|
|---|---|---|---|
|`POST`|`/v1/crags/{cragId}/photo`|Converts to .webp format and uploads both formats of the photo for the specified crag. Replaces the existing photo, if there is one.|Administrator|

##### Input

Form-data request with one file included. The file is the image, in .jpg format, which needs to have the key
`photo.jpg`.

##### Output

```json
{
  "successful": true
}
```

##### Throws

|Error Type|Status Code|Reason|
|---|---|---|
|`WebpConverterException`|400|Thrown when the .jpg file cannot be converted to .webp format. May be caused by an invalid or corrupt .jpg file.|

#### DeleteCrag

|Method|Path|Description|Authorization|
|---|---|---|---|
|`DELETE`|`/v1/crags/{cragId}`|Deletes the specified crag.|Administrator|

##### Output

```json
{
  "successful": true
}
```

### Wall

#### ListWalls

|Method|Path|Description|Authorization|
|---|---|---|---|
|`GET`|`/v1/crags/{cragId}/walls`|Returns all walls within the specified crag.|None|

##### Query Parameters

`ordered`: `boolean`

##### Output

`Wall[]`

#### GetWall

|Method|Path|Description|Authorization|
|---|---|---|---|
|`GET`|`/v1/walls/{wallId}`|Returns a single wall and its children, if specified.|None|

##### Query Parameters

`depth`: `int`

##### Output

`Wall`

#### CreateWall

|Method|Path|Description|Authorization|
|---|---|---|---|
|`PUT`|`/v1/walls`|Creates a new wall.|Administrator|

##### Input

```json
{
  "cragId": string,
  "name": string,
  "first": boolean, // optional
  "next": string // optional
}
```

##### Output

```json
{
  "wallId": string
}
```

#### UpdateWall

|Method|Path|Description|Authorization|
|---|---|---|---|
|`POST`|`/v1/walls`|Updates the specified wall.|Administrator|

##### Input

`Wall`

##### Output

```json
{
  "successful": true
}
```

#### DeleteWall

|Method|Path|Description|Authorization|
|---|---|---|---|
|`DELETE`|`/v1/walls/{wallId}`|Deletes the specified wall.|Administrator|

##### Output

```json
{
  "successful": true
}
```

### Route

#### ListRoutes

|Method|Path|Description|Authorization|
|---|---|---|---|
|`GET`|`/v1/walls/{wallId}/routes`|Returns all routes within the specified wall.|None|

##### Query Parameters

`ordered`: `boolean`

##### Output

`Route[]`

#### GetRoute

|Method|Path|Description|Authorization|
|---|---|---|---|
|`GET`|`/v1/routes/{routeId}`|Returns a single route and its children, if specified.|None|

##### Query Parameters

`depth`: `int`

##### Output

`Route`

#### CreateRoute

|Method|Path|Description|Authorization|
|---|---|---|---|
|`PUT`|`/v1/routes`|Creates a new route.|Administrator|

##### Input

```json
{
  "wallId": string,
  "name": string
  "description": string, // optional
  "center": { // optional
    "x": double,
    "y": double,
    "z": double
  },
  "protection": string, // optional
  "style": string,
  "first": boolean, // optional
  "next": string // optional
}
```

##### Output

```json
{
  "routeId": string
}
```

#### UpdateRoute

|Method|Path|Description|Authorization|
|---|---|---|---|
|`POST`|`/v1/routes`|Updates the specified route.|Administrator|

##### Input

`Route`

##### Output

```json
{
  "successful": true
}
```

#### UploadRoutePhoto

|Method|Path|Description|Authorization|
|---|---|---|---|
|`POST`|`/v1/routes/{routeId}/photo`|Converts to .webp format and uploads both formats of the photo for the specified route. Replaces the existing photo, if there is one.|Administrator|

##### Input

Form-data request with one file included. The file is the image, in .jpg format, which needs to have the key
`photo.jpg`.

##### Output

```json
{
  "successful": true
}
```

##### Throws

|Error Type|Status Code|Reason|
|---|---|---|
|`WebpConverterException`|400|Thrown when the .jpg file cannot be converted to .webp format. May be caused by an invalid or corrupt .jpg file.|

#### DeleteRoute

|Method|Path|Description|Authorization|
|---|---|---|---|
|`DELETE`|`/v1/routes/{routeId}`|Deletes the specified route.|Administrator|

##### Output

```json
{
  "successful": true
}
```

### Pitch

#### ListPitches

|Method|Path|Description|Authorization|
|---|---|---|---|
|`GET`|`/v1/routes/{routeId}/pitches`|Returns all pitches within the specified route.|None|

##### Query Parameters

`ordered`: `boolean`

##### Output

`Pitch[]`

#### GetPitch

|Method|Path|Description|Authorization|
|---|---|---|---|
|`GET`|`/v1/pitches/{pitchId}`|Returns a single pitch and its children, if specified.|

##### Query Parameters

`depth`: `int`

##### Output

`Pitch[]`

#### CreatePitch

|Method|Path|Description|Authorization|
|---|---|---|---|
|`PUT`|`/v1/pitches`|Creates a new pitch.|Administrator|

##### Input

```json
{
  "routeId": string,
  "description": string,
  "grade": int,
  "gradeModifier": string, // optional
  "danger": string, // optional
  "anchors": {// optional
    "fixed": boolean
    "x": double,
    "y": double,
    "z": double
  },
  "distance": double, // optional
  "first": boolean, // optional
  "next": string // optional
}
```

##### Output

```json
{
  "pitchId": string
}
```

##### Throws

|Error Type|Status Code|Reason|
|---|---|---|
|`PitchConsistencyException`|409|Thrown when the newly created pitch does not propagate through the data store before the request times out. In this case, the pitch was created but the parent route(s) may or may not have been updated and a follow-up request to update the parent route(s) may be required.|

##### Notes

This API will attempt to automatically update the grade, grade modifier, and danger of the parent route(s).

#### UpdatePitch

|Method|Path|Description|Authorization|
|---|---|---|---|
|`POST`|`/v1/pitches`|Updates the specified pitch.|Administrator|

##### Input

`Pitch`

##### Output

```json
{
  "successful": true
}
```

##### Throws

|Error Type|Status Code|Reason|
|---|---|---|
|`PitchConsistencyException`|409|Thrown when the newly created pitch does not propagate through the data store before the request times out. In this case, the pitch was created but the parent route(s) may or may not have been updated and a follow-up request to update the parent route(s) may be required.|

##### Notes

This method will attempt to automatically update the grade, grade modifier, and danger of the parent route(s).

#### DeletePitch

|Method|Path|Description|Authorization|
|---|---|---|---|
|`DELETE`|`/v1/pitches/{pitchId}`|Deletes the specified pitch.|Administrator|

##### Output

```json
{
  "successful": true
}
```

##### Throws

|Error Type|Status Code|Reason|
|---|---|---|
|`PitchConsistencyException`|409|Thrown when the newly created pitch does not propagate through the data store before the request times out. In this case, the pitch was created but the parent route(s) may or may not have been updated and a follow-up request to update the parent route(s) may be required.|

##### Notes

This method will attempt to automatically update the grade, grade modifier, and danger of the parent route(s).

### Point

#### ListPoints

|Method|Path|Description|Authorization|
|---|---|---|---|
|`GET`|`/v1/pitches/{pitchId}/points`|Returns all points within the specified pitch.|None|

##### Query Parameters

`ordered`: `boolean`

##### Output

`Point[]`

#### GetPoint

|Method|Path|Description|Authorization|
|---|---|---|---|
|`GET`|`/v1/points/{pointId}`|Returns a single point.|None|

##### Output

`Point`

#### CreatePoint

|Method|Path|Description|Authorization|
|---|---|---|---|
|`PUT`|`/v1/points`|Creates a new point.|Administrator|

##### Input

```json
{
  "pitchId": string,
  "x": double,
  "y": double,
  "z": double,
  "first": boolean, // optional
  "next": string // optional
}
```

##### Output

```json
{
  "pointId": string
}
```

#### BatchCreatePoints

|Method|Path|Description|Authorization|
|---|---|---|---|
|`PUT`|`/v1/pitches/{pitchId}/points`|Creates one or more points within the specified pitch in the order specified. The point IDs will also be returned in the same order.|Administrator|

##### Input

```json
{
  "newPoints": [
    {
      "x": double,
      "y": double,
      "z": double
    },
    ...
  ]
}
```

##### Output

```json
{
  "pointIds": [
    string,
    ...
  ]
}
```

#### UpdatePoint

|Method|Path|Description|Authorization|
|---|---|---|---|
|`POST`|`/v1/points`|Updates the specified point.|Administrator|

##### Input

`Point`

##### Output

```json
{
  "successful": true
}
```

#### DeletePoint

|Method|Path|Description|Authorization|
|---|---|---|---|
|`DELETE`|`/v1/points/point-1`|Deletes the specified point.|Administrator|

##### Output

```json
{
  "successful": true
}
```

##### Output

```json
{
  "successful": true
}
```

#### BatchDeletePoints

|Method|Path|Description|Authorization|
|---|---|---|---|
|`DELETE`|`/v1/pitches/{pitchId}/points`|Deletes all of the points within the specified pitch.|Administrator|

##### Output

```json
{
  "successful": true
}
```

### Path

#### ListPaths

|Method|Path|Description|Authorization|
|---|---|---|---|
|`GET`|`/v1/crags/{cragId}/paths`|Returns all paths within the specified crag.|None|

##### Query Parameters

`ordered`: `boolean`

##### Output

`Path[]`

#### GetPath

|Method|Path|Description|Authorization|
|---|---|---|---|
|`GET`|`/v1/paths/{pathId}`|Returns a single path and its children, if specified.|None|

##### Query Parameters

`depth`: `int`

##### Output

`Path`

#### CreatePath

|Method|Path|Description|Authorization|
|---|---|---|---|
|`PUT`|`/v1/paths`|Creates a new path.|Administrator|

##### Input

```json
{
  "cragId": string
}
```

##### Output

```json
{
  "pathId": string
}
```

#### UpdatePath

|Method|Path|Description|Authorization|
|---|---|---|---|
|`POST`|`/v1/paths`|Updates the specified pitch.|Administrator|

##### Input

`Path`

##### Output

```json
{
  "successful": true
}
```

#### DeletePath

|Method|Path|Description|Authorization|
|---|---|---|---|
|`DELETE`|`/v1/paths/{pathId}`|Deletes the specified path.|Administrator|

##### Output

```json
{
  "successful": true
}
```

### Path Point

#### ListPathPoints

|Method|Path|Description|Authorization|
|---|---|---|---|
|`GET`|`/v1/paths/{pathId}/path-points`|Returns all path points within the specified path.|None|

##### Query Parameters

`ordered`: `boolean`

##### Output

`PathPoint[]`

#### GetPathPoint

|Method|Path|Description|Authorization|
|---|---|---|---|
|`GET`|`/v1/path-points/{pathPointId}`|Returns a single path point.|None|

##### Output

`PathPoint`

#### CreatePathPoint

|Method|Path|Description|Authorization|
|---|---|---|---|
|`PUT`|`/v1/path-points`|Creates a new path point.|Administrator|

##### Input

```json
{
  "pathId": string,
  "latitude": double,
  "longitude": double,
  "first": boolean, // optional
  "next": string // optional
}
```

##### Output

```json
{
  "pathId": string
}
```

#### BatchCreatePathPoints

|Method|Path|Description|Authorization|
|---|---|---|---|
|`PUT`|`/v1/paths/{pathId}/path-points`|Creates one or more path points within the specified path in the order specified. The path point IDs will also be returned in the same order.|Administrator|

##### Input

```json
{
  "newPathPoints": [
    {
      "latitude": double,
      "longitude": double
    },
    ...
  ]
}
```

##### Output

```json
{
  "pathPointIds": [
    string,
    ...
  ]
}
```

#### UpdatePathPoint

|Method|Path|Description|Authorization|
|---|---|---|---|
|`POST`|`/v1/path-points`|Updates the specified path point.|Administrator|

##### Input

`PathPoint`

##### Output

```json
{
  "successful": true
}
```

#### DeletePathPoint

|Method|Path|Description|Authorization|
|---|---|---|---|
|`DELETE`|`/v1/path-points/{pathPointId}`|Deletes the specified path point.|Administrator|

##### Output

```json
{
  "successful": true
}
```

#### BatchDeletePathPoints

|Method|Path|Description|Authorization|
|---|---|---|---|
|`DELETE`|`/v1/paths/{pathId}/path-points`|Deletes all of the path points within the specified path.|Administrator|

##### Output

```json
{
  "successful": true
}
```

### User

#### RegisterUser

|Method|Path|Description|Authorization|
|---|---|---|---|
|`POST`|`/v1/user/register`|Creates a new user which is initially unverified. Additionally sends a verification email to the email specified.|None|

##### Input

```json
{
  "username": string,
  "email": string,
  "password": string
}
```

##### Output

```json
{
  "username": string,
  "email": string
}
```

##### Throws

|Error Type|Status Code|Reason|
|---|---|---|
|`UsernameExistsException`|409|Thrown when the requested username already exists.|
|`EmailExistsException`|409|Thrown when the requested email already exists.|

#### SignIn

|Method|Path|Description|Authorization|
|---|---|---|---|
|`POST`|`/v1/user/sign-in`|Signs in the specified user.|None|

##### Input

```json
{
  "username": string,
  "password": string
}
```

or

```json
{
  "email": string,
  "password": string
}
```

##### Output

```json
{
  "successful": true
}
```

##### Throws

|Error Type|Status Code|Reason|
|---|---|---|
|`UserNotVerifiedException`|401|Thrown when the user has not clicked the initial verification link in their email received after signing up.|
|`EmailNotVerifiedException`|401|Thrown when the user has verified their account but has since changed their email and has not used `VerifyEmail` to verify it. In this case, the user can still sign in with their username, but not with their email.|
|`IncorrectPasswordException`|401|Thrown when the password supplied is incorrect.|

##### Notes

Only `username` or `email` may be specified, and not both.

#### SignOut

|Method|Path|Description|Authorization|
|---|---|---|---|
|`POST`|`/v1/user/sign-out`|Signs out the currently signed-in user.|User|

##### Output

```json
{
  "successful": true
}
```

#### GetUser

|Method|Path|Description|Authorization|
|---|---|---|---|
|`GET`|`/v1/user`|Returns the currently signed-in user.|User|

##### Output

`User`

#### DeleteUser

|Method|Path|Description|Authorization|
|---|---|---|---|
|`DELETE`|`/v1/user`|Deletes the currently signed-in user.|User|

##### Output

```json
{
  "successful": boolean
}
```

#### UpdateUser

|Method|Path|Description|Authorization|
|---|---|---|---|
|`POST`|`/v1/user`|Updates the currently signed-in user.|User|

##### Input

```json
{
  "email": string
}
```

##### Notes

If the user's email is changed, the user will need to verify that new email address. This API will automatically send
the verification email if the email is changed.

##### Output

`User`

#### VerifyEmail

|Method|Path|Description|Authorization|
|---|---|---|---|
|`POST`|`/v1/user/verify-email`|Verifies the email for the currently signed-in user using a verification code from `SendVerificationEmail`.|User|

##### Input

```json
{
  "verificationCode": string
}
```

##### Output

`User`

##### Throws

|Error Type|Status Code|Reason|
|---|---|---|
|`IncorrectVerificationCodeException`|401|Thrown when the verification code supplied is not correct.|
|`EmailAlreadyVerifiedException`|409|Thrown when the user's email is already verified.|

#### SendVerificationEmail

|Method|Path|Description|Authorization|
|---|---|---|---|
|`POST`|`/v1/user/send-verification-email`|Sends a verification email to the currently signed-in user's email address. This email contains a code that can be used to verify the email with `VerifyEmail`.|User|

##### Output

`User`

##### Throws

|Error Type|Status Code|Reason|
|---|---|---|
|`EmailAlreadyVerifiedException`|409|Thrown when the user's email is already verified.|

#### ChangePassword

|Method|Path|Description|Authorization|
|---|---|---|---|
|`POST`|`/v1/user/change-password`|Changes the password of the currently signed-in user.|User|

##### Input

```json
{
  "currentPassword": string
  "newPassword": string
}
```

##### Output

`User`

##### Throws

|Error Type|Status Code|Reason|
|---|---|---|
|`IncorrectPasswordException`|401|Thrown when current password supplied is not correct.|

#### SendPasswordResetEmail

|Method|Path|Description|Authorization|
|---|---|---|---|
|`POST`|`/v1/user/send-password-reset-email`|Sends an email to the currently signed-in user's email address with a verification code which can be used to reset their password with `ResetPassword`.|None|

##### Input

```json
{
  "username": string
}
```

or

```json
{
  "email": string
}
```

##### Output

```json
{
  "successful": true
}
```

##### Throws

|Error Type|Status Code|Reason|
|---|---|---|
|`UserNotVerifiedException`|401|Thrown when the user has not clicked the initial verification link in their email received after signing up.|
|`EmailNotVerifiedException`|401|Thrown when the user has verified their account but has since changed their email and has not used `VerifyEmail` to verify it.|

##### Notes

Only one of username or email may be present, not both.

#### ResetPassword

|Method|Path|Description|Authorization|
|---|---|---|---|
|`POST`|`/v1/user/reset-password`|Resets the specified user's password using a verification code from `SendPasswordResetEmail`.|None|

##### Input

```json
{
  "username": string,
  "verificationCode": string,
  "newPassword": string
}
```

or

```json
{
  "email": string,
  "verificationCode": string,
  "newPassword": string
}
```

##### Output

```json
{
  "successful": true
}
```

##### Throws

|Error Type|Status Code|Reason|
|---|---|---|
|`UserNotVerifiedException`|401|Thrown when the user has not clicked the initial verification link in their email received after signing up.|
|`EmailNotVerifiedException`|401|Thrown when the user has verified their account but has since changed their email and has not used `VerifyEmail` to verify it.|
|`IncorrectVerificationCodeException`|401|Thrown when the verification code supplied is not correct.|

##### Notes

Only one of username or email may be present, not both.

### Contact

#### SendContactEmail

|Method|Path|Description|Authorization|
|---|---|---|---|
|`POST`|`/v1/contact`|Sends an email to info@climbassist.com.|None|

##### Input

```json
{
  "subject": string,
  "body": string,
  "replyToEmail": string
}
```

##### Output

```json
{
  "successful": true
}
```

#### GetRecaptchaSiteKey

|Method|Path|Description|Authorization|
|---|---|---|---|
|`GET`|`/v1/recaptcha-site-key`|Returns the reCAPTCHA site key for this stage.|None|

##### Output

```json
{
  "siteKey": string
}
```
