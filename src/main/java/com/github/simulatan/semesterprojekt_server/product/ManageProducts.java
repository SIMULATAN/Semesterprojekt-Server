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
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.json.JSONObject;

import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

//@Authenticated
@Path("/api/products/manage")
@Consumes("application/json")
@Produces("application/json")
public class ManageProducts {

	@GET
	@Path("/all")
	public Uni<JSONObject> listAll() {
		return Panache.withTransaction(() -> {
			var man = Manufacturer.listAll();
			var cpu = CPU.listAll();
			var ram = RAM.listAll();
			var disk = Disk.listAll();
			return Uni.combine().all()
				.unis(man, cpu, ram, disk)
				.asTuple()
				.map(tuple -> new JSONObject()
					.put("manufacturer", tuple.getItem1())
					.put("cpu", tuple.getItem2())
					.put("ram", tuple.getItem3())
					.put("disk", tuple.getItem4()));
		});
	}

	@POST
	@Path("/add")
	@Transactional
	public Uni<Response> create(ProductDTO productDto) {
		return createBackend(Product.createProduct(productDto));
	}

	@POST
	@Path("/add/complete")
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