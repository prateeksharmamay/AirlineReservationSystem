package com.sjsu.airline.Passengers;

import java.util.List;
import java.util.UUID;

import com.sjsu.airline.Flight.Flight;
import com.sjsu.airline.Reservations.Reservation;
import com.sjsu.airline.Reservations.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sjsu.airline.repositories.PassengerRepository;

@Service
public class PassengerService {

	@Autowired
	private PassengerRepository passengerRepository;
	@Autowired
	private ReservationService reservationService;

	public Passenger getPassenger(int id){
		try{
			return passengerRepository.findOne(id);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Passenger> getAllPassengers(){
		return passengerRepository.findAll();
	}
	
	public Passenger createPassenger(Passenger pass){ // Creates a new passenger with the given passenger object
		try{
			passengerRepository.save(pass);
		}catch(Exception e){
			pass = null;
		}
		return pass;
	}
	
	public boolean deletePassenger(int id){ // Deletes the given passenger
		try{
			Passenger passenger=passengerRepository.findOne(id);
		
			if(passenger==null)
				return false;
	
			List<Reservation> reservations=passenger.getReservation();
	
			Reservation reserves[]=new Reservation[reservations.size()];
			int i=0;
	
			for(Reservation res:reservations)
				reserves[i++]=res;
	
			try {
				for (int j=0;j<reserves.length;j++) {
					reservationService.deleteReservation(reserves[j].getOrderNumber());
				}
			}
			catch (Exception e){
				e.printStackTrace();
			}
			try {
				passengerRepository.delete(id);
			}
			catch (Exception e){
				e.printStackTrace();
				return false;
			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public Passenger updatePassenger(int id, String firstName, String lastName, int age, String gender, String phone){
		try{
			System.out.println("before update");
			Passenger p=passengerRepository.findOne(id);
			System.out.println("Processed update");
			if(p !=null){
				p.setFirstname(firstName);
				p.setLastname(lastName);
				p.setGender(gender);
				p.setAge(age);
				p.setPhone(phone);
				passengerRepository.save(p);
			}
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
		return passengerRepository.findOne(id);
	}

	public void addReservation(Passenger passenger,Reservation reservation) {
		passenger.addReservation(reservation);
		//passengerRepository.save(passenger);
	}

}
