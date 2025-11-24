package com.stsau.slam2.API_Gnotes.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Role {

    @Id
    @GeneratedValue Long id;
    private String libelle;


    Role() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Role(String libelle) {

        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", libelle='" + libelle + '\'' +
                 '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(id, role.id)
                && Objects.equals(libelle, role.libelle)
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, libelle);
    }


}