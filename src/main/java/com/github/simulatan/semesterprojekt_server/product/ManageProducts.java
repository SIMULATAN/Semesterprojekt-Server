package com.github.simulatan.semesterprojekt_server.product;

import com.github.simulatan.semesterprojekt_server.product.objects.Manufacturer;
import com.github.simulatan.semesterprojekt_server.product.objects.Product;
import com.github.simulatan.semesterprojekt_server.product.objects.components.CPU;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

@Path("/api/products/manage")
//@Authenticated
@Consumes("application/json")
@Produces("application/json")
public class ManageProducts {
	@GET
	@Path("/man")
	public Uni<List<Manufacturer>> getManufafturesrs() {
		return Manufacturer.listAll();
	}

	@POST
	@Path("/yourmom")
	@Transactional
	public Uni<Response> create(Product product) {
		return createBackend(product);
	}

	@POST
	@Path("/manufacturer")
	@Transactional
	public Uni<Response> create(Manufacturer manufacturer) {
		return createBackend(manufacturer);
	}

	@POST
	@Path("/cpu")
	@Transactional
	public Uni<Response> create(CPU cpu) {
		return createBackend(cpu);
	}

	private <T extends PanacheEntity> Uni<Response> createBackend(T item) {
		if (item == null) return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST).build());
		return Panache.<T>withTransaction(item::persist)
			.onItem().transform(inserted -> Response.created(URI.create("/api/products/" + inserted.id)).build());
	}

	@ServerExceptionMapper
	public Response mapExceptionToResponse(Exception exception) {
		if (exception instanceof WebApplicationException ex) {
			return ex.getResponse();
		}
		if (exception instanceof IllegalArgumentException ex) {
			return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
		}

		return Response.serverError().entity(exception.getMessage()).build();
	}
}