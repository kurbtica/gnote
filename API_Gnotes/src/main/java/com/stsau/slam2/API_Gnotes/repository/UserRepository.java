package com.stsau.slam2.API_Gnotes.repository;


import com.stsau.slam2.API_Gnotes.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}

