package com.erent.rentals_microservice.service;

import com.erent.rentals_microservice.data.RentalRepository;
import com.erent.rentals_microservice.entities.Rental;
import com.erent.shared_entities.rental.RentalRequest;
import com.erent.shared_entities.rental.RentalResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RentalService {

    @Autowired
    private RentalRepository repository;

    public RentalResponse MarkInvoiceGenerated(RentalRequest request) {

        RentalResponse response = new RentalResponse();
        response.setRental_id(request.getRental_id());
        response.setSuccess(false);

        Optional<Rental> rental = repository.findByIdAndUserIdAndScooterId(request.getRental_id(),
                                                                            request.getUser_id(),
                                                                            request.getScooterId());

        if (!rental.isPresent()) {
            response.setMessage("Rental not found");
            return response;
        }

        if (rental.get().getStopTimestamp() == null) {
            response.setMessage("Rental not stopped yet");
            return response;
        }

        if (rental.get().getInvoiceGenerated()) {
            response.setMessage("Invoice already generated");
            return response;
        }

        rental.get().setInvoiceGenerated(true);
        repository.save(rental.get());

        response.setMessage("Set InvoiceGenerate to true for Rental " + request.getRental_id() + " done successfully");
        response.setSuccess(true);
        return response;
    }

    public RentalResponse MarkInvoiceOpened(RentalRequest request) {

        RentalResponse response = new RentalResponse();
        response.setRental_id(request.getRental_id());
        response.setSuccess(false);

        Optional<Rental> rental = repository.findByIdAndUserIdAndScooterId(request.getRental_id(),
                                                                            request.getUser_id(),
                                                                            request.getScooterId());

        if (!rental.isPresent()) {
            response.setMessage("Rental not found");
            return response;
        }

        if (!rental.get().getInvoiceGenerated()) {
            response.setMessage("Invoice not yet generated");
            return response;
        }

        if (rental.get().getInvoiceOpened()) {
            response.setMessage("Invoice already opened");
            return response;
        } else {
            rental.get().setInvoiceOpened(true);
        }

        repository.save(rental.get());

        response.setMessage("Set InvoiceOpened to true for Rental " + request.getRental_id() + " done successfully");
        response.setSuccess(true);
        return response;
    }

}
