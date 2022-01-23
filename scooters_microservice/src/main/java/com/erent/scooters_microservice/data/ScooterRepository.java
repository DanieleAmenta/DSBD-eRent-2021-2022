package com.erent.scooters_microservice.data;

import com.erent.scooters_microservice.entities.Scooter;
import com.erent.scooters_microservice.entities.ScooterStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ScooterRepository extends MongoRepository<Scooter, String> {
    List<Scooter> findByScooterStatus(ScooterStatus status);

    Optional<Scooter> findByCode(String code);

    Optional<Scooter> findByScooterId(String id);
}
