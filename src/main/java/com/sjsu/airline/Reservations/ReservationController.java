package com.sjsu.airline.Reservations;

import com.sjsu.airline.Exception.SpecialException;
import com.sjsu.airline.Flight.Flight;
import com.sjsu.airline.Flight.FlightService;
import com.sjsu.airline.Passengers.Passenger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@RestController
@RequestMapping("/reservation")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private FlightService flightService;

    @GetMapping(value="/{id}")
    public ResponseEntity getReservation(@PathVariable int id) throws SpecialException {
        Reservation reservation = reservationService.getReservation(id);
    	JSONObject res_Object = formatReservation(reservation);
        if(reservation==null){
            SpecialException e = new SpecialException();
            e.setCode(404);
            e.setMessage("Sorry! The requested reservation with id "+id+" does not exist");
            throw e;
        }
        return new ResponseEntity(res_Object.toString(), HttpStatus.OK);
    }
    
    @PostMapping
    public ResponseEntity reserve(@RequestParam(value = "passengerId") int passengerID,@RequestParam(value="flightLists") List<String> flightLists) throws SpecialException, JSONException {
        Reservation reservation=reservationService.makeReservation(passengerID,flightLists);
        JSONObject res_Object = formatReservation(reservation);
        if(reservation==null){
            SpecialException e = new SpecialException();
            e.setCode(404);
            e.setMessage("Sorry! The requested reservarion for passenger with id "+passengerID+" could not be performed. Either passenger or flight not present or conflict detected or seats remaining is zero");
            throw e;
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_XML);
        return new ResponseEntity(XML.toString(res_Object), httpHeaders,HttpStatus.OK);
    }

    @GetMapping // Returns a response entity for the search reservation
    public ResponseEntity searchReservation(@RequestParam(value = "passengerId", required = false) Integer passengerID,
                                               @RequestParam(value="from",required = false) String from,
                                               @RequestParam(value="to", required = false) String to,
                                               @RequestParam(value="flightNumber",required = false) String flightNumber) throws SpecialException, JSONException {

        List<Reservation> reservations= reservationService.searchReservation(passengerID,from,to,flightNumber);
        if(reservations==null || reservations.size()==0){
            SpecialException e = new SpecialException();
            e.setCode(404);
            e.setMessage("Sorry your search returned no results.");
            throw e;
        }
        JSONObject jsonO = new JSONObject();
        List<JSONObject> reservationList = new ArrayList<JSONObject>();
        if(reservations != null){
        	for(Reservation reservation: reservations){
        		reservationList.add(formatReservation(reservation));
        	}
        }
        JSONArray flJsArray = new JSONArray(reservationList);
        jsonO.put("reservations", flJsArray);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_XML);
        return new ResponseEntity(XML.toString(jsonO), httpHeaders,HttpStatus.OK);
    }

    @DeleteMapping(value = "/{id}") // Deletes a reservation with the given id
    public ResponseEntity deleteReservation(@PathVariable Integer id) throws SpecialException, JSONException {
    	//reservationService.deleteReservation(id);
        if(!reservationService.deleteReservation(id)){
            SpecialException e = new SpecialException();
            e.setCode(404);
            e.setMessage("Reservation with number "+id+" does not exist ");
            throw e;
        }
        JSONObject res_Object = new JSONObject();
        try{
        	JSONObject jsonO = new JSONObject();
	    	Field map = jsonO.getClass().getDeclaredField("map");
			map.setAccessible(true);//because the field is private final...
			map.set(jsonO, new LinkedHashMap<>());
			map.setAccessible(false);//return flag
			jsonO.put("code", "200");
			jsonO.put("msg", "Reservation with number "+id+" is cancelled successfully!");
			res_Object.put("Resposne", jsonO);
		}
        catch(Exception e){
			SpecialException p = new SpecialException();
            p.setCode(404);
            p.setMessage("Reservation with number "+id+" does not exist ");
            throw p;
		}
        return new ResponseEntity(res_Object.toString(), HttpStatus.OK);
    }

    @PostMapping(value = "/{id}") // Updates the reservation
    public ResponseEntity updateReservation(@PathVariable Integer id,
                                         @RequestParam(value = "flightsAdded", required = false) List<String> flightsAdded,
                                         @RequestParam(value = "flightsRemoved", required = false) List<String> flightsRemoved) throws SpecialException {
        if(flightsAdded!=null && flightsAdded.size()==0) {
            SpecialException e = new SpecialException();
            e.setCode(404);
            e.setMessage("Could not update reservation. Please add flights in the flightsAdded parameter.");
            throw e;

        }
        if(flightsRemoved!=null && flightsRemoved.size()==0){
            SpecialException e = new SpecialException();
            e.setCode(404);
            e.setMessage("Could not update reservation. Please add flights in the flightsRemoved parameter.");
            throw e;
        }

        boolean removal=false;
        if(flightsRemoved!=null){
            for(String flightId:flightsRemoved){
                if(!flightService.exists(flightId)){
                    SpecialException e = new SpecialException();
                    e.setCode(404);
                    e.setMessage("Could not remove flight from reservation. The flight id "+flightId+" does not exist.");
                    throw e;
                }
                if(!reservationService.bookedFight(id,flightId)){
                    SpecialException e = new SpecialException();
                    e.setCode(404);
                    e.setMessage("Could not remove flight from reservation. The flight id "+flightId+" is not reserved.");
                    throw e;
                }
            }
        }


        if (flightsAdded != null) {
            if (!reservationService.addFlights(id, flightsAdded)){
                SpecialException e = new SpecialException();
                e.setCode(404);
                e.setMessage("Could not add flights to reservation. Please check for conflicts before retrying or seats left is zero.");
                throw e;
            }
            if (flightsRemoved != null) {
                if (!reservationService.removeFlights(id, flightsRemoved)){
                    SpecialException e = new SpecialException();
                    e.setCode(404);
                    e.setMessage("Could not remove flights from reservation.");
                    throw e;
                }
            }
        }
        else{
            if (flightsRemoved != null) {
                if (!reservationService.removeFlights(id, flightsRemoved)){
                    SpecialException e = new SpecialException();
                    e.setCode(404);
                    e.setMessage("Could not remove flights from reservation.");
                    throw e;
                }
            }
        }


        JSONObject res_Object = formatReservation(reservationService.getReservation(id));
        return new ResponseEntity(res_Object.toString(), HttpStatus.OK);
    }
    
    public JSONObject formatReservation(Reservation reservation){
    	JSONObject resut_json = new JSONObject();
    	try{
    		JSONObject jsonO = new JSONObject();
	    	Field map = jsonO.getClass().getDeclaredField("map");
			map.setAccessible(true);//because the field is private final...
			map.set(jsonO, new LinkedHashMap<>());
			map.setAccessible(false);//return flag
			jsonO.put("orderNumber", Integer.toString(reservation.getOrderNumber()));
			jsonO.put("price", Integer.toString(reservation.getPrice()));
			Passenger passenger = reservation.getPassenger();
			JSONObject passJSON = new JSONObject();
			Field passMap = passJSON.getClass().getDeclaredField("map");
			passMap.setAccessible(true);//because the field is private final...
			passMap.set(passJSON, new LinkedHashMap<>());
			passMap.setAccessible(false);//return flag
			passJSON.put("id", Integer.toString(passenger.getPassengerId()));
			passJSON.put("firstname", passenger.getFirstname());
			passJSON.put("lastname", passenger.getLastname());
			passJSON.put("age", Integer.toString(passenger.getAge()));
			passJSON.put("gender", passenger.getGender());
			passJSON.put("phone", passenger.getPhone());
			jsonO.put("passenger", passJSON);
			List<JSONObject> flightList = new ArrayList<JSONObject>();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH");
			Set<Flight> listF = reservation.getFlights();
			if(listF != null){
				for(Flight flight: listF){
					JSONObject flightObj = new JSONObject();
					Field fMap = flightObj.getClass().getDeclaredField("map");
					fMap.setAccessible(true);//because the field is private final...
					fMap.set(flightObj, new LinkedHashMap<>());
					fMap.setAccessible(false);//return flag
					flightObj.put("number", flight.getNumber());
					flightObj.put("price", flight.getPrice());
					flightObj.put("from", flight.getFromSource());
					flightObj.put("to", flight.getToDestination());
					flightObj.put("departureTime", dateFormat.format(flight.getDepartureTime()));
					flightObj.put("arrivalTime", dateFormat.format(flight.getArrivalTime()));
                    flightObj.put("seatsLeft", Integer.toString(flight.getSeatsLeft()));
					flightObj.put("description", flight.getDescription());
					JSONObject plane = new JSONObject();
					Field pMap = plane.getClass().getDeclaredField("map");
					pMap.setAccessible(true);//because the field is private final...
					pMap.set(plane, new LinkedHashMap<>());
					pMap.setAccessible(false);//return flag
					plane.put("capacity", flight.getPlane().getCapacity());
					plane.put("model", flight.getPlane().getModel());
					plane.put("manufacturer", flight.getPlane().getManufacturer());
					plane.put("yearOfManufacture", flight.getPlane().getYearOfManufacture());
					flightObj.put("plane", plane);
					JSONObject f =new JSONObject();
					f.put("flight", flightObj);
					flightList.add(f);
				}
			}
			JSONArray flJsArray = new JSONArray(flightList);
			jsonO.put("flights", flJsArray);
			resut_json.put("reservation", jsonO);
		}catch(Exception e){
			System.out.println(e);
		}
    	return resut_json;
    }


}
