package com.sjsu.airline.Flight;

import com.sjsu.airline.Passengers.Passenger;
import com.sjsu.airline.Reservations.Reservation;
import com.sjsu.airline.repositories.FlightRepository;
import com.sjsu.airline.repositories.PassengerRepository;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by Vivek Agarwal on 4/26/2017.
 */
@Service

public class FlightService {

    @Autowired
    private FlightRepository flightRepository;

    public Flight save(Flight flight) { // Saves a flight
        try {
            flightRepository.save(flight);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return flight;
    }
    public Flight saveOrUpdateFlight(Flight flight) { // Saves a new flight or updates an existing flight

        if(!flightRepository.exists(flight.getNumber())) {
            try {
                flightRepository.save(flight);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return flight;
        }

        Flight presentFlight=flightRepository.getOne(flight.getNumber());
        List<Passenger> passengers=presentFlight.getPassengers();
        if(passengers==null) {
            System.out.println(" No passengers found for flight. Updating/Saving it now");
            flightRepository.save(flight);//solved
            return flight;
        }

        List<Date> newTiming=new ArrayList<>();
        newTiming.add(flight.getDepartureTime());
        newTiming.add(flight.getArrivalTime());


        for(Passenger passenger:passengers){ //Checks for overlap with reservations of all passengers
            List<Reservation> reservations=passenger.getReservation();
            for(Reservation reservation:reservations){
                Set<Flight> bookedFlights=reservation.getFlights();
                for(Flight bookedFlight:bookedFlights){
                    if(bookedFlight != presentFlight){
                        List<Date> reservedTiming=new ArrayList<>();
                        reservedTiming.add(bookedFlight.getDepartureTime());
                        reservedTiming.add(bookedFlight.getArrivalTime());
                        if(overlap(newTiming,reservedTiming))
                            return null;
                    }
                }
            }
        }

        try {
            flightRepository.save(flight);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return flight;
    }

    private boolean overlap(List<Date> newTiming, List<Date> reservedTime) {
        if(newTiming.get(1).compareTo(reservedTime.get(0))<0) return false;
        if(newTiming.get(0).compareTo(reservedTime.get(1))>0) return false;
        return true;
    }

    public List<Flight> getAllFlights() {
        List<Flight> results=null;
        try{
            results=flightRepository.findAll();
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return results;
    }

    public Flight getFlight(String id) {
        try {
            Flight flight= flightRepository.findOne(id);
            return flight;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteFlight(String id) {
        try {
            Flight flight=flightRepository.findOne(id);
            if(flight==null){
                System.out.println("Flight with id :"+id+" does not exist.");
                return false;
            }
            System.out.println("Flight passengers :"+flight.getPassengers());
            if(flight.getPassengers()!=null && flight.getPassengers().size()>0)
                return false;
            flightRepository.delete(id);
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public Passenger addPassenger(Flight flight, Passenger passenger) { // Adds a passenger to the flight
        if(flight.getSeatsLeft()<=0)
            return null;
        System.out.println("Flight :"+flight+" passenger :"+passenger);
        flight.addPassenger(passenger);
        flight.setSeatsLeft(flight.getSeatsLeft()-1);
        flightRepository.save(flight);
        return passenger;
    }

    public void setReservation(Flight flight,Reservation reservation) {
        flight.setReservation(reservation);
    }

    public boolean exists(String flightId) { // Checks if a flight with the id exists in db
        Flight flight=flightRepository.findOne(flightId);
        if(flight==null)
            return false;
        return true;
    }
}
