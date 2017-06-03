package com.sjsu.airline.Flight;

import com.sjsu.airline.Exception.SpecialException;
import com.sjsu.airline.Passengers.Passenger;
import com.sjsu.airline.Reservations.Reservation;

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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Vivek Agarwal on 4/25/2017.
 */
@RestController
@RequestMapping("/flight")
public class FlightController {

    @Autowired
    private FlightService flightService;

    @GetMapping(value = "/allFlights") // CGet request that returns all the flights in the system
    public List<Flight> getAllFlights(@RequestParam(value="xml") String xml) throws SpecialException {

        List<Flight> results= flightService.getAllFlights();
        if(results==null){
            SpecialException e = new SpecialException();
            e.setCode(404);
            e.setMessage("Could not find flights.");
            throw e;
        }
        return results;
    }

    @GetMapping(value = "/{id}") // returns a particular flight with the required Flight ID
    public ResponseEntity getFlight(@PathVariable String id, @RequestParam(value="json", required=false) String json, @RequestParam(value="xml", required=false) boolean xml) throws SpecialException, JSONException {
        Flight flightResult= flightService.getFlight(id);
        if(flightResult==null){
            SpecialException e = new SpecialException();
            if(xml == true){
				e.setXml(true);
			}
            e.setCode(404);
            e.setMessage("Sorry the requested flight with "+id+" does not exist!");
            throw e;
        }
        JSONObject res_Object = formatFlight(flightResult);
        if(xml == true){
			System.out.println("Inside xml response");
			return new ResponseEntity(XML.toString(res_Object), HttpStatus.OK);
		}
        return new ResponseEntity(res_Object.toString(), HttpStatus.OK);
    }

    @PostMapping(value="/{id}") // Posts a flight detail object to the database
    public ResponseEntity saveOrUpdateFlight(@PathVariable String id,
                                      @RequestParam(value="price") int price,
                                      @RequestParam(value="from") String from,
                                      @RequestParam(value="to") String to,
                                      @RequestParam(value="departureTime") String departureTime,
                                      @RequestParam(value="arrivalTime") String arrivalTime,
                                      @RequestParam(value="description") String description,
                                      @RequestParam(value="capacity") int capacity,
                                      @RequestParam(value="model") String model,
                                      @RequestParam(value="manufacturer") String manufacturer,
                                      @RequestParam(value="yearOfManufacture") int yearOfManufacture
                                      ) throws SpecialException, JSONException {

        Flight flight=new Flight();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH");
        Date dep = null, arr = null;
        try{
            dep = formatter.parse(departureTime);
            arr = formatter.parse(arrivalTime);
        }
        catch (Exception e) {
            System.out.println("Error Date Format:"+e);
        }

        flight.setNumber(id);
        flight.setPrice(price);
        flight.setFromSource(from);
        flight.setToDestination(to);
        flight.setDepartureTime(dep);
        flight.setArrivalTime(arr);
        flight.setDescription(description);
        flight.setCapacity(capacity);
        flight.setSeatsLeft(capacity);
        flight.setModel(model);
        flight.setManufacturer(manufacturer);
        flight.setYearOfManufacture(yearOfManufacture);

        System.out.println(flight.toString());

        Flight flightResult= flightService.saveOrUpdateFlight(flight);

        if(flightResult==null){
            SpecialException e = new SpecialException();
            e.setCode(404);
            e.setMessage("Could not update flight. Please ensure flight exists and check for overlap.");
            throw e;
        }
        JSONObject res_Object = formatFlight(flightResult);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_XML);
        return new ResponseEntity(XML.toString(res_Object), HttpStatus.OK);
    }

    @DeleteMapping("/{id}") // Deletes a flight with the given id
    public ResponseEntity deleteFlight(@PathVariable String id) throws SpecialException, JSONException {

        if(!flightService.deleteFlight(id)){
            SpecialException e = new SpecialException();
            e.setCode(404);
            e.setMessage("Could not delete flight. Please ensure flight id is correct and no passengers have booked this flight.");
            throw e;
        }
        JSONObject error = new JSONObject();
		JSONObject res_Object = new JSONObject();
		error.put("code", "200");
		error.put("msg", "Flight with id "+id+" deleted successfully!");
		res_Object.put("Response", error);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_XML);
		return new ResponseEntity(XML.toString(res_Object), HttpStatus.OK);
    }
    
    
    public JSONObject formatFlight(Flight flight){ // Formats the flight in the right aspect
    	JSONObject resut_json = new JSONObject();
    	try{
    		JSONObject json_new = new JSONObject();
            Field map = json_new.getClass().getDeclaredField("map");
            map.setAccessible(true);//because the field is private final...
            map.set(json_new, new LinkedHashMap<>());
            map.setAccessible(false);//return flag
            json_new.put("number", flight.getNumber());
            json_new.put("price", flight.getPrice());
            json_new.put("fromSource", flight.getFromSource());
            json_new.put("toDestination", flight.getToDestination());

            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH");

            json_new.put("departureTime", formatter.format(flight.getDepartureTime()));
            json_new.put("arrivalTime", formatter.format(flight.getArrivalTime()));
            json_new.put("seatsLeft", Integer.toString(flight.getSeatsLeft()));
            json_new.put("description", flight.getDescription());
            System.out.println("JSON0-before:"+json_new.toString());
            JSONObject plane = new JSONObject();
            Field pMap = plane.getClass().getDeclaredField("map");
            pMap.setAccessible(true);//because the field is private final...
            pMap.set(plane, new LinkedHashMap<>());
            pMap.setAccessible(false);//return flag
            plane.put("capacity",Integer.toString(flight.getPlane().getCapacity()));
            plane.put("model",flight.getPlane().getModel());
            plane.put("manufacturer",flight.getPlane().getManufacturer());
            plane.put("yearOfManufacture",Integer.toString(flight.getPlane().getYearOfManufacture()));
            json_new.put("plane", plane);
            List<Reservation> reservationList = flight.getReservation();
            List<JSONObject> passengerList = new ArrayList<JSONObject>();
            if(reservationList!=null){
            	for(Reservation reservation: reservationList){
            		 Passenger passenger = reservation.getPassenger();
            		 JSONObject passengerObject = new JSONObject();
                     Field iMap = passengerObject.getClass().getDeclaredField("map");
                     iMap.setAccessible(true);//because the field is private final...
                     iMap.set(passengerObject, new LinkedHashMap<>());
                     iMap.setAccessible(false);//return flag
                     passengerObject.put("id", Integer.toString(passenger.getPassengerId()));
                     passengerObject.put("firstname",passenger.getFirstname());
                     passengerObject.put("lastname",passenger.getLastname());
                     passengerObject.put("age", Integer.toString(passenger.getAge()));
                     passengerObject.put("gender",passenger.getGender());
                     passengerObject.put("phone",passenger.getPhone());  
                     JSONObject pass_List = new JSONObject();
                     pass_List.put("passenger", passengerObject);
                     passengerList.add(pass_List);
            	}
            }
            JSONArray resJsArray = new JSONArray(passengerList);
            System.out.println("resJsArray:"+resJsArray.toString());
            json_new.put("passengers", resJsArray);
            System.out.println("JSON0:"+json_new.toString());
            resut_json.put("flight", json_new);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	return resut_json;
    }
}
