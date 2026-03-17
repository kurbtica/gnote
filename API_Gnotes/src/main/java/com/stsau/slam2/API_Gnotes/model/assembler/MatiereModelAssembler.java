package com.stsau.slam2.API_Gnotes.model.assembler;

import com.stsau.slam2.API_Gnotes.controller.MatiereController;
import com.stsau.slam2.API_Gnotes.model.Matiere;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class MatiereModelAssembler implements RepresentationModelAssembler<Matiere, EntityModel<Matiere>> {

	@Override
	public EntityModel<Matiere> toModel(Matiere matiere) {

		return EntityModel.of(matiere, //
				linkTo(methodOn(MatiereController.class).one(matiere.getId())).withSelfRel(),
				linkTo(methodOn(MatiereController.class).all()).withRel("matiere"));
	}
}