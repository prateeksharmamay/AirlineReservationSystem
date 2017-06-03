package com.sjsu.airline.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sjsu.airline.Reservations.Reservation;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

import javax.transaction.Transactional;

@Transactional
public interface ReservationRepository extends JpaRepository<Reservation, Integer>{

    List<Reservation> findByPassengerId(int passenger_id);

//    @Query(value = "select reservation.* from reservation, flight_passengers, flight where flight_passengers.passenger_id=ifnull(?1,flight_passengers.passenger_id) and flight.from_source=ifnull(?2,flight.from_source) and flight.to_destination=ifnull(?3,flight.to_destination) and flight.flight_number=ifnull(?4,flight.flight_number) and reservation.passenger_passenger_id=flight_passengers.passenger_id group by passenger_id", nativeQuery = true)
//    List<Reservation> findByPassengerIdOrFlightNumberOrFromSourceOrToDestination(Integer passenger_id,String from, String to,String flightNumber);


    @Query(value = "select distinct r.* \n" +
            "from reservation r \n" +
            "inner join reservation_flight r2 on r.order_number=r2.order_number \n" +
            "inner join flight f1 on f1.flight_number=r2.flight_number\n" +
            "\n" +
            "where r.passenger_id=ifnull(?1,r.passenger_id)\n" +
            "and\n" +
            "r2.flight_number=ifnull(?2,r2.flight_number)\n" +
            "and\n" +
            "f1.from_source=ifnull(?3,f1.from_source)\n" +
            "and\n" +
            "f1.to_destination=ifnull(?4,f1.to_destination)",nativeQuery = true)

    List<Reservation> findByPassengerIdOrFlightNumberOrFromSourceOrToDestination(Integer passenger_id,String flightNumber,String from, String to);

    @Query(value="select * from reservation where order_number=?1",nativeQuery = true)
    Reservation getReservation(int reservationId);
}
