package com.github.simulatan.semesterprojekt_server.product;

import com.github.simulatan.semesterprojekt_server.product.objects.Manufacturer;
import com.github.simulatan.semesterprojekt_server.product.objects.Product;
import com.github.simulatan.semesterprojekt_server.product.objects.ProductDTO;
import com.github.simulatan.semesterprojekt_server.product.objects.components.CPU;
import com.github.simulatan.semesterprojekt_server.product.objects.components.Disk;
import com.github.simulatan.semesterprojekt_server.product.objects.components.RAM;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.smallrye.mutiny.Uni;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.EnumSet;
import java.util.List;

//@Authenticated
@Path("/api/products/manage")
@Consumes("application/json")
@Produces("application/json")
public class ManageProducts {
	@POST
	@Path("/product")
	@Transactional
	public Uni<Response> create(ProductDTO productDto) {
		return createBackend(Product.createProduct(productDto));
	}

	@POST
	@Path("/product/complete")
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

	@GET
	@Path("/manufacturer")
	public Uni<List<Manufacturer>> getManufacturers() {
		return Manufacturer.listAll();
	}

	@POST
	@Path("/cpu")
	@Transactional
	public Uni<Response> create(CPU cpu) {
		return createBackend(cpu);
	}

	@GET
	@Path("/cpu")
	public Uni<List<CPU>> getCPUs() {
		return CPU.listAll();
	}

	@POST
	@Path("/disk")
	@Transactional
	public Uni<Response> create(Disk product) {
		return createBackend(product);
	}

	@GET
	@Path("/disk")
	public Uni<List<Disk>> getDisks() {
		return Disk.listAll();
	}

	@POST
	@Path("/ram")
	@Transactional
	public Uni<Response> create(RAM ram) {
		return createBackend(ram);
	}

	@GET
	@Path("/ram")
	public Uni<List<RAM>> getRAMs() {
		return RAM.listAll();
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