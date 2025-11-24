package com.stsau.slam2.API_Gnotes.controller;


import com.stsau.slam2.API_Gnotes.*;
import com.stsau.slam2.API_Gnotes.Model.RoleModelAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class RoleController { // 1. Changement de nom : EmployeeController -> UserController

    private final RoleRepository repository;
    private final RoleModelAssembler assembler;

    // 2. Changement du constructeur
    RoleController(RoleRepository repository, RoleModelAssembler assembler) {
        this.repository = repository;
        this.assembler = assembler;
    }

    // Aggregate root
    @GetMapping("/roles")
    public CollectionModel<EntityModel<Role>> all() {
        // 3. Utilisation de l'assembler ici pour nettoyer le code
        // Cela remplace votre logique complexe où 'employee' et 'user' se mélangeaient
        List<EntityModel<Role>> roles = repository.findAll().stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(roles,
                linkTo(methodOn(RoleController.class).all()).withSelfRel());
    }



    // Single item
    @GetMapping("/roles/{id}")
    public EntityModel<Role> one(@PathVariable Long id) {

        Role role = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return assembler.toModel(role);
    }



}