package com.github.simulatan.semesterprojekt_server.cart;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public class CartItemDTO {
	@NotNull
	@Positive
	@JsonProperty("product")
	public int productId;
	@NotNull
	@Positive
	public int quantity = 1;
}