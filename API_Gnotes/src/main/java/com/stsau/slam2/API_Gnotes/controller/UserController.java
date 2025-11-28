package com.stsau.slam2.API_Gnotes.controller;


import com.stsau.slam2.API_Gnotes.model.User;
import com.stsau.slam2.API_Gnotes.model.assembler.UserModelAssembler;
import com.stsau.slam2.API_Gnotes.exception.UserNotFoundException;
import com.stsau.slam2.API_Gnotes.repository.UserRepository;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class UserController { // 1. Changement de nom : EmployeeController -> UserController

    private final UserRepository repository;
    private final UserModelAssembler assembler;

    // 2. Changement du constructeur
    UserController(UserRepository repository, UserModelAssembler assembler) {
        this.repository = repository;
        this.assembler = assembler;
    }

    // Aggregate root
    @GetMapping("/api/users")
    public CollectionModel<EntityModel<User>> all() {
        // 3. Utilisation de l'assembler ici pour nettoyer le code
        // Cela remplace votre logique complexe où 'employee' et 'user' se mélangeaient
        List<EntityModel<User>> users = repository.findAll().stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(users,
                linkTo(methodOn(UserController.class).all()).withSelfRel());
    }

    @PostMapping("/api/users")
    ResponseEntity<?> newUser(@RequestBody User newUser) {
        EntityModel<User> entityModel = assembler.toModel(repository.save(newUser));

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    // Single item
    @GetMapping("/api/users/{id}")
    public EntityModel<User> one(@PathVariable Long id) {

        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return assembler.toModel(user);
    }

    @PutMapping("/api/users/{id}")
    ResponseEntity<?> replaceUser(@RequestBody User newUser, @PathVariable Long id) {

        Optional<User> existing = repository.findById(id);

        User updatedUser = existing
                .map(user -> {
                    // Attention: assurez-vous que setName gère bien nom/prénom dans User.java
                    user.setName(newUser.getName());
                    user.setRole(newUser.getRole());
                    user.setEmail(newUser.getEmail()); // Ajouté pour cohérence
                    user.setAdresse(newUser.getAdresse()); // Ajouté pour cohérence
                    user.setTelephone(newUser.getTelephone()); // Ajouté pour cohérence
                    return repository.save(user);
                })
                .orElseGet(() -> {
                    // Si l'ID n'existe pas, on le définit pour le nouvel utilisateur
                    newUser.setId(id);
                    return repository.save(newUser);
                });

        EntityModel<User> entityModel = assembler.toModel(updatedUser);

        if (existing.isPresent()) {
            // Cas mise à jour → 200 OK
            return ResponseEntity.ok(entityModel);
        } else {
            // Cas création → 201 Created
            return ResponseEntity
                    .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                    .body(entityModel);
        }
    }

    @DeleteMapping("/api/users/{id}")
    void deleteUser(@PathVariable Long id) {
        repository.deleteById(id);
    }
}