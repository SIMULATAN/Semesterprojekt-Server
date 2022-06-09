package com.github.simulatan.semesterprojekt_server.cart.objects;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;

import javax.persistence.Entity;
import java.util.UUID;

@Entity
public class Cart extends PanacheEntity {
	public UUID userId;
}