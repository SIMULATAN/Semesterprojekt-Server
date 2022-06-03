package com.github.simulatan.semesterprojekt_server.product.objects.components;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public class RAMDTO {
	@NotEmpty(message = "Name must not be empty")
	public String name;
	public String img;
	@NotNull
	@Positive
	@JsonProperty("manufacturer")
	public long manufacturerId;
	/**
	 * in MHz
	 */
	@NotNull
	@Positive
	public int speed;
	/**
	 * In kilobytes
	 */
	@NotNull
	@Positive
	public long size;
}