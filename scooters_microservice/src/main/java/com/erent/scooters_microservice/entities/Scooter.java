package com.erent.scooters_microservice.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Document(collection = "scooters")
public class Scooter {

    @Id
    private String scooterId;

    private String code;

    private Double lat;
    private Double lon;

    private ScooterStatus scooterStatus;
}
