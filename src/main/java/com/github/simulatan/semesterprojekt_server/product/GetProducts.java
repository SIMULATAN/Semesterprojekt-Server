package com.github.simulatan.semesterprojekt_server.product;

import com.github.simulatan.semesterprojekt_server.product.objects.Product;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.enterprise.event.Observes;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.security.Security;
import java.util.List;

@Path("/api/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GetProducts {

	@GET
	public Uni<List<PanacheEntityBase>> list() {
		return Product.listAll();
	}

	@GET
	@Path("{id}")
	public Uni<PanacheEntityBase> getSingle(Long id) {
		return Product.findById(id);
	}

	void config(@Observes StartupEvent ev) {
		Security.addProvider(new BouncyCastleProvider());
	}
}