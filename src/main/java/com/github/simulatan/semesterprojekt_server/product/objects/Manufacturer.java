package com.github.simulatan.semesterprojekt_server.product.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;

import javax.persistence.Entity;

@Entity
public class Manufacturer extends PanacheEntity {

	public String name;
	public String img;

	public Manufacturer() {}

	@JsonCreator
	public Manufacturer(String name) {
		this.name = name;
	}
}