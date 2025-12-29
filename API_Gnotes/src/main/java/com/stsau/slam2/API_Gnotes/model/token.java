package com.stsau.slam2.API_Gnotes.model;

import jakarta.persistence.JoinColumn;

public class token {

    @JoinColumn(name = "user_id")
    private User user;

    private String token;
}
