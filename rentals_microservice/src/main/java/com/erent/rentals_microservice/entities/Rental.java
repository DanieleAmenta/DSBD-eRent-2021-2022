package com.erent.rentals_microservice.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Document(collection = "rentals")
public class Rental {
    @Id
    private String id;

    private String scooterId;
    private String userId;

    private Long startTimestamp;
    private Long stopTimestamp;

    private Double price_per_minute;
    private Double price_per_start;
    private Double amount_to_pay;

    private RentalStatus status;

    private Boolean invoiceGenerated;
    private Boolean invoiceOpened;
}