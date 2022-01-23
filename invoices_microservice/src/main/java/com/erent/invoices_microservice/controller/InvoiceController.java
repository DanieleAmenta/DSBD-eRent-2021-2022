package com.erent.invoices_microservice.controller;


import com.erent.invoices_microservice.data.InvoiceRepository;
import com.erent.invoices_microservice.entities.Invoice;
import com.erent.invoices_microservice.service.InvoiceService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(path = "/${api_base}")
public class InvoiceController {

    @Autowired
    InvoiceService service;

    @Autowired
    InvoiceRepository repository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka_invoice_topic}")
    private String invoice_topic;

    @Value("${admin_user_id}")
    private String admin_user_id;

    @Value("${kafka_invoice_opened_key}")
    private String invoice_opened_key;

    @GetMapping(path = "/ping")
    public @ResponseBody
    String getPong() {
        return "Pong";
    }

    @GetMapping(path = "/{id}")
    public @ResponseBody
    String getInvoice(@PathVariable String id,
                      @RequestHeader("X-User-ID") String user_id,
                      HttpServletRequest servletRequest) {

        Optional<Invoice> i = repository.findOneByRentalId(id);

        if (!i.isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Specified object can't be found."
            );
        }

        if (!user_id.equals(i.get().getUserId()) && !user_id.equals(admin_user_id)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, ""
            );
        }

        if (!user_id.equals(admin_user_id)) {
            // It's a user, allow the updating of rental data
            kafkaTemplate.send(invoice_topic, invoice_opened_key, new Gson().toJson(i.get()));
        }

        return new Gson().toJson(i.get());
    }

    @GetMapping(path = "/invoices")
    public @ResponseBody
    String getAllInvoices(@RequestHeader("X-User-ID") String user_id,
                                 HttpServletRequest servletRequest) {

        List<Invoice> invoiceList;

        if (user_id.equals(admin_user_id)) {
            invoiceList = repository.findAll();
        } else {
            invoiceList = repository.findByUserId(user_id);
        }

        return new Gson().toJson(invoiceList);

    }
}
