package com.github.simulatan.semesterprojekt_server.product.objects.components;

import com.github.simulatan.semesterprojekt_server.product.objects.Manufacturer;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.smallrye.mutiny.Uni;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@Entity
public class CPU extends PanacheEntity {
	@Column(unique = true)
	@NotEmpty(message = "Name must not be empty")
	public String name;
	public String img;
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "manufacturer_id", referencedColumnName = "id")
	public Manufacturer manufacturer;
	@NotNull
	@Positive
	public int cores;
	@NotNull
	@Positive
	public int threads;
	@NotNull
	@Positive
	public double clock;

	public CPU() {}

	public CPU(String name, String img, Manufacturer manufacturer, int cores, int threads, double clock) {
		this.name = name;
		this.img = img;
		this.manufacturer = manufacturer;
		this.cores = cores;
		this.threads = threads;
		this.clock = clock;
	}

	public static Uni<CPU> createCPU(CPUDTO cpuDto) {
		return Manufacturer.findById(cpuDto.manufacturerId)
			.onItem()
			.ifNull()
			.failWith(new WebApplicationException("Manufacturer not found", Response.Status.NOT_FOUND))
			.onItem()
			.transform(manufacturer -> new CPU(cpuDto.name, cpuDto.img, (Manufacturer) manufacturer, cpuDto.cores, cpuDto.threads, cpuDto.clock));
	}
}