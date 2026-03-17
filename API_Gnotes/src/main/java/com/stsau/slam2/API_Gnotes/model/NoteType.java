package com.stsau.slam2.API_Gnotes.model;

import jakarta.persistence.*;

@Entity
public class NoteType {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mon_seq_gen")
	@SequenceGenerator(name = "mon_seq_gen", sequenceName = "note_type_seq", allocationSize = 1)
	private Long id;
	private String libelle;

	// Obligatoire si tu utilises des bibliothèques comme Jackson pour le mapping
	// JSON
	public NoteType() {
	}

	public NoteType(String libelle) {
		this.libelle = libelle;
	}

	public Long getId() {
		return id;
	}

	public String getLibelle() {
		return libelle;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	@Override
	public String toString() {
		return "NoteType{" + "id=" + id + ", libelle='" + libelle + '\'' + '}';
	}
}
