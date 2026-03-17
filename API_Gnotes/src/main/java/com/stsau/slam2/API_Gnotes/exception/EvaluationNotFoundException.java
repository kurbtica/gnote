package com.stsau.slam2.API_Gnotes.exception;

public class EvaluationNotFoundException extends RuntimeException {

	public EvaluationNotFoundException(Long id) {
		super("Could not find this Evaluation " + id);
	}
}
