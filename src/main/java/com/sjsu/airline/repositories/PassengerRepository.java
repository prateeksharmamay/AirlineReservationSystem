package com.sjsu.airline.repositories;

import com.sjsu.airline.Passengers.Passenger;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;


@Transactional
public interface PassengerRepository extends JpaRepository<Passenger, Integer>{

		
}
