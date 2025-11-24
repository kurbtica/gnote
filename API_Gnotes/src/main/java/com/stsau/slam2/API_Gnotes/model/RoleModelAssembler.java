package com.stsau.slam2.API_Gnotes.model;

import com.stsau.slam2.API_Gnotes.controller.UserController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class RoleModelAssembler implements RepresentationModelAssembler<Role, EntityModel<Role>> {

    @Override
    public EntityModel<Role> toModel(Role role) {

        return EntityModel.of(role, //
                linkTo(methodOn(UserController.class).one(role.getId())).withSelfRel(),
                linkTo(methodOn(UserController.class).all()).withRel("role"));
    }
}