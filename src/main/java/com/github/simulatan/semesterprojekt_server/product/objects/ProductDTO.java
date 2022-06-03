package com.github.simulatan.semesterprojekt_server.product.objects;

import com.fasterxml.jackson.annotation.JsonProperty;


public class ProductDTO {
	@JsonProperty
	public String name;
	@JsonProperty("manufacturer")
	public long manufacturerId;
	@JsonProperty("cpu")
	public long cpuId;
	@JsonProperty("disk")
	public long diskId;
	@JsonProperty("ram")
	public long ramId;
	@JsonProperty
	public String img;
}