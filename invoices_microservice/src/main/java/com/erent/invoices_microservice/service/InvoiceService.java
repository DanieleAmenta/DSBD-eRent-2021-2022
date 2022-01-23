package com.erent.invoices_microservice.service;

import com.erent.invoices_microservice.data.InvoiceRepository;
import com.erent.invoices_microservice.entities.Invoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InvoiceService {

    @Autowired
    InvoiceRepository repository;

    public Optional<Invoice> GetInvoiceById(String id) {
        return repository.findById(id);
    }

    public List<Invoice> getAllInvoices() {
        return repository.findAll();
    }

    public Invoice CreateInvoice(Invoice i) {

        Optional<Invoice> invoice = repository.findByIdAndUserIdAndScooterId(i.getRentalId(), i.getUserId(), i.getScooterId());
        if (invoice.isPresent()) {
            System.out.println("Invoice already exists: " + invoice.get().getId().toString());
            return null;
        }

        repository.save(i);

        System.out.println("New invoice created: " + i.getId().toString());
        return i;
    }


    public Optional<Invoice> GetInvoice(String rental_id, String user_id, Double total) {
        return repository.findByRentalIdAndUserIdAndTotal(rental_id, user_id, total);
    }

}
