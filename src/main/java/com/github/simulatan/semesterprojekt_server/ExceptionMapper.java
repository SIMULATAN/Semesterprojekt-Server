package com.github.simulatan.semesterprojekt_server;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import javax.persistence.PersistenceException;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Produces("text/plain")
public class ExceptionMapper {
	@ServerExceptionMapper
	@Produces("text/plain")
	public Response mapExceptionToResponse(Exception exception) {
		if (exception instanceof WebApplicationException ex) {
			return ex.getResponse();
		}
		if (exception instanceof IllegalArgumentException ex) {
			return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).type(MediaType.TEXT_PLAIN_TYPE).build();
		}

		if (exception instanceof PersistenceException ex) {
			return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).type(MediaType.TEXT_PLAIN_TYPE).build();
		}

		return Response.serverError().entity(exception.getMessage()).build();
	}
}