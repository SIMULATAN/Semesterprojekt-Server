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
public class RAM extends PanacheEntity {
	@Column(unique = true)
	@NotEmpty(message = "Name must not be empty")
	public String name;
	public String img;
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "manufacturer_id", referencedColumnName = "id")
	public Manufacturer manufacturer;
	/**
	 * in MHz
	 */
	@NotNull
	@Positive
	public int speed;
	/**
	 * In kilobytes
	 */
	@NotNull
	@Positive
	public long size;

	public RAM() {}

	public RAM(String name, String img, Manufacturer manufacturer, int speed, long size) {
		this.name = name;
		this.img = img;
		this.manufacturer = manufacturer;
		this.speed = speed;
		this.size = size;
	}

	public static Uni<RAM> createRAM(RAMDTO ramDto) {
		return Manufacturer.findById(ramDto.manufacturerId)
			.onItem()
			.ifNull()
			.failWith(new WebApplicationException("Manufacturer not found", Response.Status.NOT_FOUND))
			.onItem()
			.transform(manufacturer -> new RAM(ramDto.name, ramDto.img, (Manufacturer) manufacturer, ramDto.speed, ramDto.size));
	}
}