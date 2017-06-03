package com.sjsu.airline.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sjsu.airline.Plane.Plane;


public interface PlaneRepository extends JpaRepository<Plane, Integer>{

}
