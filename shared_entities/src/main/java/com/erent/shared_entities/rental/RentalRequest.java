package com.erent.shared_entities.rental;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class RentalRequest implements Serializable {

    private String scooterId;
    private String user_id;
    private String rental_id;

}
