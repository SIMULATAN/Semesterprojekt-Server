package com.github.simulatan.semesterprojekt_server.cart;

import com.github.simulatan.semesterprojekt_server.product.objects.Product;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Entity
public class CartItem extends PanacheEntity {
	@ManyToOne(optional = false)
	@JoinColumn(name = "cart_id", referencedColumnName = "id")
	public Cart cart;
	@ManyToOne(optional = false)
	@JoinColumn(name = "product_id", referencedColumnName = "id")
	public Product product;
	@NotNull
	@Positive
	public int quantity;

	public CartItem() {}

	public CartItem(Cart cart, Product product, int quantity) {
		this.cart = cart;
		this.product = product;
		this.quantity = quantity;
	}
}