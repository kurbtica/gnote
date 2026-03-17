package com.stsau.slam2.API_Gnotes.model.assembler;

import com.stsau.slam2.API_Gnotes.controller.EvaluationController;
import com.stsau.slam2.API_Gnotes.model.Evaluation;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class EvaluationModelAssembler implements RepresentationModelAssembler<Evaluation, EntityModel<Evaluation>> {

	@Override
	public EntityModel<Evaluation> toModel(Evaluation evaluation) {

		return EntityModel.of(evaluation, //
				linkTo(methodOn(EvaluationController.class).one(evaluation.getId())).withSelfRel(),
				linkTo(methodOn(EvaluationController.class).all()).withRel("evaluation"));
	}
}
