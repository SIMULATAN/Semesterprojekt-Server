package com.github.simulatan.semesterprojekt_server.product.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;

import javax.persistence.Entity;

@Entity
public class Manufacturer extends PanacheEntity {

	public String name;
	public String img;

	public Manufacturer(long id) {
		super.id = id;
	}

	@JsonCreator
	public static Manufacturer factory(int id) {
		return (Manufacturer) findById(id).await().indefinitely();
	}
}