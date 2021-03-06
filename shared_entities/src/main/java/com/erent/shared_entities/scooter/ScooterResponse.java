package com.erent.shared_entities.scooter;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class ScooterResponse implements Serializable {
    private String scooter_id;
    private Boolean success;
    private String message;
}
