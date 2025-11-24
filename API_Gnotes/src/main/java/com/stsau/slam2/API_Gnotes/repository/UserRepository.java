package com.stsau.slam2.API_Gnotes.repository;


import com.stsau.slam2.API_Gnotes.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}

