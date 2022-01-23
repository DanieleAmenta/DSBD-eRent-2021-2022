package com.erent.rentals_microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class RentalsMicroserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RentalsMicroserviceApplication.class, args);
    }

}
