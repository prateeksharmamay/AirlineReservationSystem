package com.sjsu.airline.Passengers;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.sjsu.airline.Exception.SpecialException;
import com.sjsu.airline.Flight.Flight;
import com.sjsu.airline.Reservations.Reservation;


@RestController
public class PassangerController {

	@Autowired
	private PassengerService passengerService; 
	
	@GetMapping(value="/allPassengers") // Gets a List of all passengers in the database
	public List<Passenger> getAllPassengers(@RequestParam(value="xml") String xml){
		System.out.println("In get all passengers/..");
		return passengerService.getAllPassengers();
	}
	
	@GetMapping("/passenger/{id}") // Gets a particular passenger with the id
	public ResponseEntity/*Passenger*/ getPassenger(@PathVariable int id, @RequestParam(value="json", required=false) String json, @RequestParam(value="xml", required=false) boolean xml) throws Exception{
		if(passengerService.getPassenger(id) ==  null)
		{
			SpecialException e = new SpecialException();
			if(xml == true){
				e.setXml(true);
			}
			e.setCode(404);
			e.setMessage("Sorry! The requested passenger with id "+id+" does not exist");
			throw e;
		}
		//ModelMap resultMap = new ModelMap();
		//resultMap.addAttribute("passenger", passengerFormat(passengerService.getPassenger(id)));
		//Map<String, Object> m = format(passengerService.getPassenger(id)); 
		JSONObject res_Object = passengerXML(passengerService.getPassenger(id));
		//JSONObject res_Object = new JSONObject(resultMap);
		if(xml == true){
			System.out.println("Inside xml response");
			return new ResponseEntity(XML.toString(res_Object), HttpStatus.OK);
		}
		
		return new ResponseEntity(res_Object.toString(), HttpStatus.OK);
		//return ResponseEntity.ok(res.toString());
	}
		
	@PostMapping("/passenger")// Posts a new passenger with the detail
	public ResponseEntity createPassenger(@RequestParam(value="firstname", required=true) String firstName, @RequestParam(value="lastname", required=true) String lastName, @RequestParam(value="age", required=true) int age, @RequestParam(value="gender", required=true) String gender, @RequestParam(value="phone", required=true) String phone) throws Exception{
		Passenger pa = new Passenger();
		pa.setFirstname(firstName);
		pa.setLastname(lastName);
		pa.setGender(gender);
		pa.setAge(age);
		pa.setPhone(phone);
		if(passengerService.createPassenger(pa) ==  null)
		{
			System.out.println("Inside create if");
			SpecialException e = new SpecialException();
			e.setCode(404);
			e.setMessage("Another person with same number already exists");
			throw e;
		}
		ModelMap resultMap = new ModelMap();
		resultMap.addAttribute("passenger", passengerFormat(pa));
		return new ResponseEntity(resultMap, HttpStatus.OK);
	}
	
	@PutMapping("/passenger/{id}") // Updates an existing passenger
	public ResponseEntity updatePassenger(@PathVariable int id, @RequestParam(value="firstname", required=true) String firstName, @RequestParam(value="lastname", required=true) String lastName, @RequestParam(value="age", required=true) int age, @RequestParam(value="gender", required=true) String gender, @RequestParam(value="phone", required=true) String phone) throws Exception{
		if(passengerService.updatePassenger(id, firstName, lastName, age, gender, phone) ==  null)
		{
			System.out.println("Inside update if");
			SpecialException e = new SpecialException();
			e.setCode(404);
			e.setMessage("Sorry the passenger could not be updated");
			throw e;
		}
		ModelMap resultMap = new ModelMap();
		resultMap.addAttribute("passenger", passengerFormat(passengerService.updatePassenger(id, firstName, lastName, age, gender, phone)));
		return new ResponseEntity(resultMap, HttpStatus.OK);
	}
	
	@DeleteMapping("/passenger/{id}") //Deletes a given passenger
	public ResponseEntity deletePassenger(@PathVariable int id) throws SpecialException, JSONException {
		if(!passengerService.deletePassenger(id))
		{
			SpecialException e = new SpecialException();
			e.setCode(404);
			e.setMessage("Passenger with id "+ id+ " does not exist");
			throw e;
		}
		JSONObject error = new JSONObject();
		JSONObject res_Object = new JSONObject();
		error.put("code", "200");
		error.put("msg", "Passenger with id "+id+" deleted successfully!");
		res_Object.put("Response", error);
		return new ResponseEntity(res_Object.toString(), HttpStatus.OK);
	}
	
	public JSONObject passengerXML(Passenger passenger){ // Formats the passenger object
		JSONObject resut_json = new JSONObject();
		try{
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH");
			JSONObject jsonO = new JSONObject(passengerFormat(passenger));
			Field map = jsonO.getClass().getDeclaredField("map");
			map.setAccessible(true);//because the field is private final...
			map.set(jsonO, new LinkedHashMap<>());
			map.setAccessible(false);//return flag
			jsonO.put("id", Integer.toString(passenger.getPassengerId()));
			jsonO.put("firstname", passenger.getFirstname());
			jsonO.put("lastname", passenger.getLastname());
			jsonO.put("age", Integer.toString(passenger.getAge()));
			jsonO.put("gender", passenger.getGender());
			jsonO.put("phone", passenger.getPhone());
			List<Reservation> reservations = passenger.getReservation();
			System.out.println("Reservations in passeneger " + reservations.size());
			List<JSONObject> reservationList = new ArrayList<JSONObject>();
			if(reservations != null){
				for(Reservation reservation:reservations){
					JSONObject reservationObject = new JSONObject();
					Field iMap = reservationObject.getClass().getDeclaredField("map");
					iMap.setAccessible(true);//because the field is private final...
					iMap.set(reservationObject, new LinkedHashMap<>());
					iMap.setAccessible(false);//return flag
					reservationObject.put("orderNumber", reservation.getOrderNumber());
					reservationObject.put("price", reservation.getPrice());
					List<JSONObject> flightList = new ArrayList<JSONObject>();
					for(Flight flight: reservation.getFlights()){
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
						flightObj.put("description", flight.getDescription());
						JSONObject plane = new JSONObject();
						Field pMap = plane.getClass().getDeclaredField("map");
						fMap.setAccessible(true);//because the field is private final...
						fMap.set(plane, new LinkedHashMap<>());
						fMap.setAccessible(false);//return flag
						plane.put("capacity", flight.getPlane().getCapacity());
						plane.put("model", flight.getPlane().getModel());
						plane.put("manufacturer", flight.getPlane().getManufacturer());
						plane.put("yearOfManufacture", flight.getPlane().getYearOfManufacture());
						flightObj.put("plane", plane);
						JSONObject f =new JSONObject();
						f.put("flight", flightObj);
						flightList.add(f);
					}
					JSONObject resList = new JSONObject();
					JSONArray flJsArray = new JSONArray(flightList);
					reservationObject.put("flights", flJsArray);
					resList.put("reservation", reservationObject);
					reservationList.add(resList);
				}
			}
			JSONArray resJsArray = new JSONArray(reservationList);
			jsonO.put("reservations", resJsArray);
			resut_json.put("passenger", jsonO);
		}catch(Exception e){
			System.out.println(e);
		}
		return resut_json;
	}

	
	public ModelMap passengerFormat(Passenger passenger){
		ModelMap resultMap = new ModelMap();
		resultMap.addAttribute("id", passenger.getPassengerId());
		resultMap.addAttribute("firstname", passenger.getFirstname());
		resultMap.addAttribute("lastname", passenger.getLastname());
		resultMap.addAttribute("age", passenger.getAge());
		resultMap.addAttribute("gender", passenger.getGender());
		resultMap.addAttribute("phone", passenger.getPhone());
		List<Reservation> reservations = new ArrayList<Reservation>();
		reservations=passenger.getReservation();
		List<ModelMap> reservationList = new ArrayList<ModelMap>();
		if(reservations != null){
			for(Reservation reservation:reservations){
				ModelMap reservationMap = new ModelMap();
				reservationMap.addAttribute("orderNumber", reservation.getOrderNumber());
				reservationMap.addAttribute("price", reservation.getPrice());
				List<ModelMap> flightList = new ArrayList<ModelMap>();
				for(Flight flight: reservation.getFlights()){
					ModelMap flightMap = new ModelMap();
					flightMap.addAttribute("flight", flight);
					flightList.add(flightMap);
				}
				ModelMap resList = new ModelMap();
				reservationMap.addAttribute("flights", flightList);
				resList.addAttribute("reservation", reservationMap);
				reservationList.add(resList);
			}
		}
		resultMap.addAttribute("reservations", reservationList);
		return resultMap;
	}
	
}