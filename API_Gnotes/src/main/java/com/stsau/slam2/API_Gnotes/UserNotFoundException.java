package com.stsau.slam2.API_Gnotes;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("Could not find user " + id);
    }
}
