package com.github.simulatan.semesterprojekt_server.product.objects.components;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public class CPUDTO {
	@NotEmpty(message = "Name must not be empty")
	public String name;
	public String img;
	@NotNull
	@Positive
	// required because the frontend is stupid
	@JsonProperty("manufacturer")
	public long manufacturerId;
	@NotNull
	@Positive
	public int cores;
	@NotNull
	@Positive
	public int threads;
	@NotNull
	@Positive
	public double clock;
}