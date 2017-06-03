package com.sjsu.airline.repositories;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sjsu.airline.Flight.Flight;

@Transactional
public interface FlightRepository extends JpaRepository<Flight, String>{

}
