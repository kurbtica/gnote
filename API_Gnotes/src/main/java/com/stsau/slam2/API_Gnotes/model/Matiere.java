package com.stsau.slam2.API_Gnotes.model;

import jakarta.persistence.*;

@Entity
public class Matiere {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mon_seq_gen")
	@SequenceGenerator(name = "mon_seq_gen", sequenceName = "matiere_seq", allocationSize = 1)
	private Long id;
	private String libelle;

	public Matiere() {
	}

	public Matiere(String libelle) {
		this.libelle = libelle;
	}

	// Getters et setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	@Override
	public String toString() {
		return "Matiere{" + "id=" + id + ", libelle='" + libelle + '\'' + '}';
	}
}
