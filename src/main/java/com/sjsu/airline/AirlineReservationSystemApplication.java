package com.sjsu.airline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@EnableJpaRepositories(basePackages = "com.sjsu.airline.repositories")
@EnableAutoConfiguration
@EntityScan(basePackages = {"com.sjsu.airline.Passengers", "com.sjsu.airline.Plane", "com.sjsu.airline.Reservations", "com.sjsu.airline.Flight"})
@SpringBootApplication
	public class AirlineReservationSystemApplication extends WebMvcConfigurerAdapter{
	public static void main(String[] args) {
		SpringApplication.run(AirlineReservationSystemApplication.class, args);
	}
	
	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		configurer.favorParameter(true).parameterName("xml").ignoreAcceptHeader(true).
		defaultContentType(MediaType.APPLICATION_JSON)
		.mediaType("true", MediaType.APPLICATION_XML);
	}
}
