package com.github.simulatan.semesterprojekt_server.product.objects;

import com.github.simulatan.semesterprojekt_server.product.objects.components.CPU;
import com.github.simulatan.semesterprojekt_server.product.objects.components.Disk;
import com.github.simulatan.semesterprojekt_server.product.objects.components.RAM;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;

import javax.persistence.*;

@Entity
@Table(name = "products")
public class Product extends PanacheEntity {
	@Column(nullable = false)
	public String name;
	@ManyToOne(optional = false)
	@JoinColumn(name = "manufacturer_id", referencedColumnName = "id")
	public Manufacturer manufacturer;

	@ManyToOne(optional = false)
	@JoinColumn(name = "cpu_id", referencedColumnName = "id")
	public CPU cpu;

	@ManyToOne(optional = false)
	@JoinColumn(name = "disk_id", referencedColumnName = "id")
	public Disk disk;

	@ManyToOne(optional = false)
	@JoinColumn(name = "ram_id", referencedColumnName = "id")
	public RAM ram;

	@Column(nullable = true)
	public String img;

	public Product() {}

	public Product(String name, Manufacturer manufacturer, CPU cpu, Disk disk, RAM ram, String img) {
		this.name = name;
		this.manufacturer = manufacturer;
		this.cpu = cpu;
		this.disk = disk;
		this.ram = ram;
		this.img = img;
	}

	public static Product createProduct(ProductDTO productDto) {
		// this should be done asynchronously but my deadline is too tight to do that
		return new Product(
			productDto.name,
			(Manufacturer) Manufacturer.findById(productDto.manufacturerId).await().indefinitely(),
			(CPU) CPU.findById(productDto.cpuId).await().indefinitely(),
			(Disk) Manufacturer.findById(productDto.diskId).await().indefinitely(),
			(RAM) RAM.findById(productDto.ramId).await().indefinitely(),
			productDto.img
		);
	}
}