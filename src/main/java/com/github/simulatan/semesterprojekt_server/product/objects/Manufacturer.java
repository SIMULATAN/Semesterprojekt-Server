package com.github.simulatan.semesterprojekt_server.product.objects;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotEmpty;

@Entity
public class Manufacturer extends PanacheEntity {

	@Column(nullable = false, unique = true)
	@NotEmpty(message = "Name must not be empty")
	public String name;
	public String img;
}