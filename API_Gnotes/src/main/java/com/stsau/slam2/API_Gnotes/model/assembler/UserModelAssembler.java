package com.stsau.slam2.API_Gnotes.model.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import com.stsau.slam2.API_Gnotes.controller.UserController;
import com.stsau.slam2.API_Gnotes.model.User;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class UserModelAssembler implements RepresentationModelAssembler<User, EntityModel<User>> {

    @Override
    public EntityModel<User> toModel(User user) {

        return EntityModel.of(user, //
                linkTo(methodOn(UserController.class).one(user.getId())).withSelfRel(),
                linkTo(methodOn(UserController.class).all()).withRel("user"));
    }
}