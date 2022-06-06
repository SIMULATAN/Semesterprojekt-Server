package com.github.simulatan.semesterprojekt_server.product.objects;

import com.github.simulatan.semesterprojekt_server.product.objects.components.CPU;
import com.github.simulatan.semesterprojekt_server.product.objects.components.Disk;
import com.github.simulatan.semesterprojekt_server.product.objects.components.RAM;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.smallrye.mutiny.Uni;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;

@Entity
@Table(name = "products")
public class Product extends PanacheEntity {
	@Column(nullable = false)
	public String name;
	public String img;
	@ManyToOne(optional = false)
	@JoinColumn(name = "manufacturer_id", referencedColumnName = "id")
	@Cascade(CascadeType.PERSIST)
	public Manufacturer manufacturer;

	@ManyToOne(optional = false)
	@JoinColumn(name = "cpu_id", referencedColumnName = "id")
	@Cascade(CascadeType.PERSIST)
	public CPU cpu;

	@ManyToOne(optional = false)
	@JoinColumn(name = "disk_id", referencedColumnName = "id")
	@Cascade(CascadeType.PERSIST)
	public Disk disk;

	@ManyToOne(optional = false)
	@JoinColumn(name = "ram_id", referencedColumnName = "id")
	@Cascade(CascadeType.PERSIST)
	public RAM ram;

	public Product() {}

	public Product(String name, String img, Manufacturer manufacturer, CPU cpu, Disk disk, RAM ram) {
		this.name = name;
		this.img = img;
		this.manufacturer = manufacturer;
		this.cpu = cpu;
		this.disk = disk;
		this.ram = ram;
	}

	public static Uni<Product> createProduct(ProductDTO productDto) {
		// this should be done asynchronously but my deadline is too tight to do that
		Uni<Manufacturer> man = Manufacturer.findById(productDto.manufacturerId);
		Uni<CPU> cpu = CPU.findById(productDto.cpuId);
		Uni<RAM> ram = RAM.findById(productDto.ramId);
		Uni<Disk> disk = Disk.findById(productDto.diskId);
		return Uni.combine().all()
			.unis(man, cpu, ram, disk)
			.asTuple()
			.map(tuple -> new Product(
				productDto.name,
				productDto.img,
				tuple.getItem1(),
				tuple.getItem2(),
				tuple.getItem4(),
				tuple.getItem3()
			));
	}
}