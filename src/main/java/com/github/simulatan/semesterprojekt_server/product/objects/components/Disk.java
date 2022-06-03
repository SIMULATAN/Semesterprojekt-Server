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
public class Disk extends PanacheEntity {
	@Column(unique = true)
	@NotEmpty(message = "Name must not be empty")
	public String name;
	public String img;
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "manufacturer_id", referencedColumnName = "id")
	public Manufacturer manufacturer;
	@NotNull
	@Positive
	public int capacity;

	public Disk() {}

	public Disk(String name, String img, Manufacturer manufacturer, int capacity) {
		this.name = name;
		this.img = img;
		this.manufacturer = manufacturer;
		this.capacity = capacity;
	}
	public static Uni<Disk> createDisk(DiskDTO diskDto) {
		return Manufacturer.findById(diskDto.manufacturerId)
			.onItem()
			.ifNull()
			.failWith(new WebApplicationException("Manufacturer not found", Response.Status.NOT_FOUND))
			.onItem()
			.transform(manufacturer -> new Disk(diskDto.name, diskDto.img, (Manufacturer) manufacturer, diskDto.capacity));
	}
}