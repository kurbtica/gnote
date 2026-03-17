package com.stsau.slam2.API_Gnotes.exception;

public class NoteNotFoundException extends RuntimeException {

	public NoteNotFoundException(Long id) {
		super("Could not find this Note " + id);
	}
}
