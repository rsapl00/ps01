package com.safeway.app.ps01.controller.resource;

import lombok.Data;
import lombok.NonNull;

@Data
public class User {

    public User() {}

    @NonNull
    private String username;

    @NonNull
    private String email;

    @NonNull
    private String division;

    @NonNull
    private RoleType roleType;

}