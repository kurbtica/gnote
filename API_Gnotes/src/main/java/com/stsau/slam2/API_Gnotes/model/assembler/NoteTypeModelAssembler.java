package com.stsau.slam2.API_Gnotes.model.assembler;

import com.stsau.slam2.API_Gnotes.controller.NoteTypeController;
import com.stsau.slam2.API_Gnotes.model.NoteType;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class NoteTypeModelAssembler implements RepresentationModelAssembler<NoteType, EntityModel<NoteType>> {
	@Override
	public EntityModel<NoteType> toModel(NoteType notetype) {

		return EntityModel.of(notetype, //
				linkTo(methodOn(NoteTypeController.class).one(notetype.getId())).withSelfRel(),
				linkTo(methodOn(NoteTypeController.class).all()).withRel("user"));
	}
}
