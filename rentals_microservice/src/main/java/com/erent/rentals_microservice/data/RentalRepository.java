package com.erent.rentals_microservice.data;

import com.erent.rentals_microservice.entities.Rental;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RentalRepository extends MongoRepository<Rental, String> {
    List<Rental> findByUserIdOrderByStartTimestamp(String userId);

    Optional<Rental> findByIdAndUserIdAndScooterId(String id, String userId, String scooterId);
}
