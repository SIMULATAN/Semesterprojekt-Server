package com.github.simulatan.semesterprojekt_server.product.objects;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;

import javax.persistence.*;

@Entity
@Table(name = "products")
public class Product extends PanacheEntity {
	@Column(nullable = false)
	public String name;
	@PrimaryKeyJoinColumn
	@ManyToOne(optional = false)
	@JoinColumn(name = "manufacturer_id", referencedColumnName = "id", insertable = false)
	public Manufacturer manufacturer;
	@Column(nullable = true)
	public String img;
}