package com.erent.rentals_microservice.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ScooterRequest implements Serializable {

    private String scooter_id;
    private String user_id;
    private String rental_id;
    private ScooterOperation operation;
    private Double lat;
    private Double lon;
    private String scooterCode;

}
