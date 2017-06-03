package com.sjsu.airline.Reservations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.sjsu.airline.Flight.Flight;
import com.sjsu.airline.Passengers.Passenger;

@Entity
@Table(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue
    @Column(name="order_number")
    private int orderNumber;

    private int price;

    @ManyToOne
    @JoinColumn(name="passenger_id")
    @JsonBackReference
    private Passenger passenger;

	/*@ManyToMany(mappedBy="reservation", fetch= FetchType.EAGER, cascade=CascadeType.ALL)
	@JsonManagedReference*/
	@ManyToMany() //Many to Many mapping between Reservations and flights
	@JoinTable(name="reservation_flight", joinColumns= {@JoinColumn(name="order_number")},
			inverseJoinColumns = {@JoinColumn(name="flight_number")})
	@JsonManagedReference
	private Set<Flight> flights=new HashSet<>();

    public int getOrderNumber() {
        return orderNumber;
    }
    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }
    public int getPrice() {
        return price;
    }
    public void setPrice(int price) {
        this.price = price;
    }
    public Passenger getPassenger() {
        return passenger;
    }
    public void setPassenger(Passenger passenger) {
        this.passenger = passenger;
    }

    public Set<Flight> getFlights(){return flights;}


    public void addFlight(Flight flight) {
        if(this.flights==null)
            System.out.println("Null found! \n\n\n");
        this.flights.add(flight);
    }
}