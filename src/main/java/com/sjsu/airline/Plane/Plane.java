package com.sjsu.airline.Plane;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import javax.persistence.*;

@Embeddable
@Entity
public class Plane {

	@Id
	@GeneratedValue
	@Column(name="plane_id")
	private int plane_id;

	@Column(name="capacity")
	private int capacity;
	
	@Column(name="model")
	private String model;
	
	@Column(name="manufacturer")
	private String manufacturer;

	@Column(name="year_of_manufacture")
	private int yearOfManufacture;

    @Override
    public String toString() {
        return "Plane{" +
                "plane_id=" + plane_id +
                ", capacity=" + capacity +
                ", model='" + model + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", yearOfManufacture=" + yearOfManufacture +
                '}';
    }

    public Plane(){

	}
	
	public int getPlane_id() {
		return plane_id;
	}
	public void setPlane_id(int plane_id) {
		this.plane_id = plane_id;
	}
	public int getCapacity() {
		return capacity;
	}
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public String getManufacturer() {
		return manufacturer;
	}
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}
	public int getYearOfManufacture() {
		return yearOfManufacture;
	}
	public void setYearOfManufacture(int yearOfManufacture) {
		this.yearOfManufacture = yearOfManufacture;
	}
	
	
}
