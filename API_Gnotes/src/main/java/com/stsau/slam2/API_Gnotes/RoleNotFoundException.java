package com.stsau.slam2.API_Gnotes;

public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException(Long id) {
        super("Could not find this role " + id);
    }
}
