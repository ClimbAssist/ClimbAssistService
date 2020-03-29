package com.climbassist.api;

class PathNotFoundException extends Exception {

    PathNotFoundException(String path) {
        super(String.format("No API exists at path %s.", path));
    }
}
