package com.stsau.slam2.API_Gnotes.model;

public enum Role {

	// Définition des constantes
	ADMIN(1, "Administrateur"), ENSEIGNANT(2, "Enseignant"), ETUDIANT(3, "Étudiant");

	private final int id;
	private final String libelle;

	// Constructeur privé (obligatoire pour les enums)
	Role(int id, String libelle) {
		this.id = id;
		this.libelle = libelle;
	}

	public int getId() {
		return id;
	}

	public String getLibelle() {
		return libelle;
	}
}
