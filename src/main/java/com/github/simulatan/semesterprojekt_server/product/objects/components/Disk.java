package com.github.simulatan.semesterprojekt_server.product.objects.components;

import com.github.simulatan.semesterprojekt_server.product.objects.Manufacturer;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Disk extends PanacheEntity {

	public String name;
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "manufacturer_id", referencedColumnName = "id")
	public Manufacturer manufacturer;
	public int capacity;
	public String img;
}