package com.erent.invoices_microservice.data;

import com.erent.invoices_microservice.entities.Invoice;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.List;

public interface InvoiceRepository extends MongoRepository<Invoice, String> {
    List<Invoice> findByUserId(String user_id);

    Optional<Invoice> findOneByRentalId(String rentalId);

    Optional<Invoice> findByIdAndUserIdAndScooterId(String rental_id, String user_id, String scooter_id);

    Optional<Invoice> findByRentalIdAndUserIdAndTotal(String rental_id, String user_id, Double total);
}
