package com.github.simulatan.semesterprojekt_server.product.objects;

import com.fasterxml.jackson.annotation.JsonProperty;


public class ProductDTO {
	@JsonProperty
	public String name;
	@JsonProperty("manufacturer_id")
	public long manufacturerId;
	@JsonProperty("cpu_id")
	public long cpuId;
	@JsonProperty("disk_id")
	public long diskId;
	@JsonProperty("ram_id")
	public long ramId;
	@JsonProperty
	public String img;
}