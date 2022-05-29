package com.github.simulatan.semesterprojekt_server.product.objects.components;

import com.github.simulatan.semesterprojekt_server.product.objects.Manufacturer;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class RAM extends PanacheEntity {
	public int speed;
	/**
	 * In kilobytes
	 */
	public long size;
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "manufacturer_id", referencedColumnName = "id")
	public Manufacturer manufacturer;

	public RAM() {

	}

	public RAM(long yourmom) {

	}
}