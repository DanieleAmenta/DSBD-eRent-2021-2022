package com.erent.rentals_microservice.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class RentalResponse implements Serializable {

    private String rental_id;
    private Boolean success;
    private String message;

}